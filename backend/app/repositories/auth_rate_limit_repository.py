"""Per-account rate limiting for the auth endpoints (login / signup).

Keyed by an opaque string (e.g. "login:<email>"), not a user id — the
referenced account may not exist. Backed by the `auth_attempts` log
(migration 024). Mirrors `rate_limit_repository`: a per-key transactional
advisory lock guards prune+count(+insert), so concurrent requests for the
same key can't race past the cap. Reuses `RateLimitExceededError` so the
endpoint layer handles 429s uniformly.

Usage pattern:
  * login  — call `check_auth_rate_limit(key)` before verifying the
    password; on a bad credential call `record_auth_attempt(key)`; on
    success call `clear_auth_attempts(key)` so an honest user is never
    locked out by their own successful logins.
  * signup — `check_auth_rate_limit(key)` then `record_auth_attempt(key)`
    before the expensive bcrypt hash, throttling repeated probing of one
    email.
"""
from __future__ import annotations

from typing import Any

from ..config import AUTH_RATE_LIMIT_MAX, AUTH_RATE_LIMIT_WINDOW_SECONDS
from ..db import get_connection
from .rate_limit_repository import RateLimitExceededError


def _prune(connection: Any, key: str, window_seconds: int) -> None:
    connection.execute(
        """
        DELETE FROM auth_attempts
        WHERE attempt_key = %s
          AND attempted_at < NOW() - make_interval(secs => %s)
        """,
        (key, window_seconds),
    )


def check_auth_rate_limit(
    key: str,
    *,
    max_attempts: int = AUTH_RATE_LIMIT_MAX,
    window_seconds: int = AUTH_RATE_LIMIT_WINDOW_SECONDS,
) -> None:
    """Raise RateLimitExceededError if `key` has >= max_attempts in the
    window. Records nothing — recording is the caller's decision (e.g.
    login records only on failure)."""
    with get_connection() as connection:
        connection.execute("BEGIN")
        try:
            connection.execute(
                "SELECT pg_advisory_xact_lock(hashtext(%s))", (f"auth-rate:{key}",)
            )
            _prune(connection, key, window_seconds)
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
            connection.execute("COMMIT")
        except RateLimitExceededError:
            raise
        except Exception:
            connection.execute("ROLLBACK")
            raise


def record_auth_attempt(
    key: str, *, window_seconds: int = AUTH_RATE_LIMIT_WINDOW_SECONDS
) -> None:
    """Record one attempt for `key` (and prune stale rows for it)."""
    with get_connection() as connection:
        connection.execute("BEGIN")
        try:
            connection.execute(
                "SELECT pg_advisory_xact_lock(hashtext(%s))", (f"auth-rate:{key}",)
            )
            _prune(connection, key, window_seconds)
            connection.execute(
                "INSERT INTO auth_attempts (attempt_key) VALUES (%s)", (key,)
            )
            connection.execute("COMMIT")
        except Exception:
            connection.execute("ROLLBACK")
            raise


def clear_auth_attempts(key: str) -> None:
    """Drop all recorded attempts for `key` (e.g. after a successful login)."""
    with get_connection() as connection:
        connection.execute("DELETE FROM auth_attempts WHERE attempt_key = %s", (key,))
        connection.commit()
