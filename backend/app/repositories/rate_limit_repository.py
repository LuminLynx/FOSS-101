"""Per-user rate limiting for the paid grader endpoint (OWASP LLM10).

Sliding-window limiter over the `grade_attempts` log (migration 023).
`check_and_record_grade_attempt` is called once per grade request,
*before* the Claude call, so an attempt is counted whether or not the
grade ultimately succeeds — the cost signal is the call itself.

Concurrency: a single user looping the endpoint is exactly the abuse
vector, so the count-and-insert runs under a per-user transactional
advisory lock keyed on the user id. Two concurrent requests from the
same user serialize on that key, so they cannot both observe an
under-limit count and both insert past the cap. The lock is per-user, so
different users never block each other.
"""
from __future__ import annotations

from typing import Any

from ..config import GRADE_RATE_LIMIT_MAX, GRADE_RATE_LIMIT_WINDOW_SECONDS
from ..db import get_connection


class RateLimitExceededError(Exception):
    """Raised when a user exceeds the grade rate limit.

    `retry_after_seconds` is the whole-second wait until the oldest
    attempt in the current window ages out, suitable for a Retry-After
    header.
    """

    def __init__(self, retry_after_seconds: int, limit: int, window_seconds: int) -> None:
        super().__init__(
            f"Grade rate limit exceeded: {limit} attempts per {window_seconds}s."
        )
        self.retry_after_seconds = retry_after_seconds
        self.limit = limit
        self.window_seconds = window_seconds


def check_and_record_grade_attempt(
    user_id: str,
    *,
    max_attempts: int = GRADE_RATE_LIMIT_MAX,
    window_seconds: int = GRADE_RATE_LIMIT_WINDOW_SECONDS,
    _connection_factory: Any = None,
) -> None:
    """Record a grade attempt for `user_id`, or raise if over the limit.

    Within one transaction, under a per-user advisory lock:
      1. drop this user's attempts older than the window (keeps the
         table bounded),
      2. count remaining attempts in the window,
      3. if at/over `max_attempts`, raise RateLimitExceededError without
         inserting,
      4. otherwise insert this attempt and commit.
    """
    connect = _connection_factory or get_connection
    with connect() as connection:
        connection.execute("BEGIN")
        try:
            connection.execute(
                "SELECT pg_advisory_xact_lock(hashtext(%s))",
                (f"grade-rate:{user_id}",),
            )
            connection.execute(
                """
                DELETE FROM grade_attempts
                WHERE user_id = %s
                  AND attempted_at < NOW() - make_interval(secs => %s)
                """,
                (user_id, window_seconds),
            )
            row = connection.execute(
                """
                SELECT
                    COUNT(*) AS attempts,
                    MIN(attempted_at) AS oldest
                FROM grade_attempts
                WHERE user_id = %s
                """,
                (user_id,),
            ).fetchone()

            attempts = row["attempts"]
            if attempts >= max_attempts:
                oldest = row["oldest"]
                age_row = connection.execute(
                    "SELECT EXTRACT(EPOCH FROM (NOW() - %s)) AS age",
                    (oldest,),
                ).fetchone()
                retry_after = max(1, int(window_seconds - float(age_row["age"])))
                connection.execute("ROLLBACK")
                raise RateLimitExceededError(
                    retry_after_seconds=retry_after,
                    limit=max_attempts,
                    window_seconds=window_seconds,
                )

            connection.execute(
                "INSERT INTO grade_attempts (user_id) VALUES (%s)",
                (user_id,),
            )
            connection.execute("COMMIT")
        except RateLimitExceededError:
            raise
        except Exception:
            connection.execute("ROLLBACK")
            raise
