"""Per-account rate limiting for the auth endpoints (login / signup).

Keyed by an opaque string (e.g. "login:<email>"), not a user id — the
referenced account may not exist. Backed by the `auth_attempts` log
(migration 024). Mirrors `rate_limit_repository`: a per-key transactional
advisory lock guards prune+count+insert in ONE transaction, so concurrent
requests for the same key can't read an under-limit count and all insert
past the cap (TOCTOU). Reuses `RateLimitExceededError` so the endpoint
layer handles 429s uniformly.

Usage pattern:
  * login  — call `check_and_record_auth_attempt(key)` before verifying the
    password (it records the attempt atomically); on a *successful* login
    call `clear_auth_attempts(key)` so an honest user is never locked out
    by their own prior typos. A failed login leaves the recorded attempt.
  * signup — `check_and_record_auth_attempt(key)` before the bcrypt hash,
    throttling repeated probing of one email.

Pruning is global (all expired rows), not per-key: the keys are
attacker-controlled (arbitrary emails), so a per-key prune would let a
spray of unique addresses accumulate rows forever.
"""
from __future__ import annotations

from typing import Any

from ..config import AUTH_RATE_LIMIT_MAX, AUTH_RATE_LIMIT_WINDOW_SECONDS
from ..db import get_connection
from .rate_limit_repository import RateLimitExceededError


def _prune_expired(connection: Any, window_seconds: int) -> None:
    # Global prune (not filtered by key): auth keys are attacker-controlled,
    # so only deleting the current key's rows would let unique-email sprays
    # accumulate rows indefinitely.
    connection.execute(
        "DELETE FROM auth_attempts WHERE attempted_at < NOW() - make_interval(secs => %s)",
        (window_seconds,),
    )


def check_and_record_auth_attempt(
    key: str,
    *,
    max_attempts: int = AUTH_RATE_LIMIT_MAX,
    window_seconds: int = AUTH_RATE_LIMIT_WINDOW_SECONDS,
) -> None:
    """Atomically check the limit for `key` and record this attempt.

    Under a per-key advisory lock, in one transaction: prune expired rows,
    count this key's attempts in the window, raise RateLimitExceededError
    if already at/over the cap (without inserting), otherwise insert this
    attempt. The atomic check+insert closes the TOCTOU gap a separate
    check-then-record would open under concurrency.
    """
    with get_connection() as connection:
        connection.execute("BEGIN")
        try:
            connection.execute(
                "SELECT pg_advisory_xact_lock(hashtext(%s))", (f"auth-rate:{key}",)
            )
            _prune_expired(connection, window_seconds)
            row = connection.execute(
                """
                SELECT COUNT(*) AS attempts, MIN(attempted_at) AS oldest
                FROM auth_attempts
                WHERE attempt_key = %s
                """,
                (key,),
            ).fetchone()
            if row["attempts"] >= max_attempts:
                age_row = connection.execute(
                    "SELECT EXTRACT(EPOCH FROM (NOW() - %s)) AS age", (row["oldest"],)
                ).fetchone()
                retry_after = max(1, int(window_seconds - float(age_row["age"])))
                connection.execute("ROLLBACK")
                raise RateLimitExceededError(
                    retry_after_seconds=retry_after,
                    limit=max_attempts,
                    window_seconds=window_seconds,
                )
            connection.execute(
                "INSERT INTO auth_attempts (attempt_key) VALUES (%s)", (key,)
            )
            connection.execute("COMMIT")
        except RateLimitExceededError:
            raise
        except Exception:
            connection.execute("ROLLBACK")
            raise


def clear_auth_attempts(key: str) -> None:
    """Drop all recorded attempts for `key` (e.g. after a successful login)."""
    with get_connection() as connection:
        connection.execute("DELETE FROM auth_attempts WHERE attempt_key = %s", (key,))
        connection.commit()
