"""ReviewRepository — writes/reads against the `review_schedule` table.

Implements F5 (Loop step 6, "Return") per docs/F5_SPACED_REVIEW.md.
Decisions locked in that doc; the load-bearing ones here:

- D1: algorithm is interval-doubling on the explicit ladder
  [1, 3, 7, 21, 60] days, capped at 60. There is NO multiply
  formula — the ladder array is normative (a x3 rule would give
  1->3->9->27->60 and contradict D1).
- D3: a review row is seeded on a NEW completion, first review
  due NOW() + 1 day. First-write-wins, idempotent on
  (user_id, unit_id) — a duplicate completion never resets or
  duplicates an existing schedule row.
- D6: the review tick advances interval_days to the next ladder
  value (monotonic, no quality signal, no reset path), sets
  last_reviewed_at = NOW() and due_at = NOW() + interval_days.

Migration 020 created `review_schedule` as shape-only; this is
the first application code to touch it. No new migration (D1 fits
the existing columns exactly).

list_due is the read endpoint's query (D5), added in F5
implementation step 2 alongside the GET /api/v1/review-schedule
endpoint.
"""
from __future__ import annotations

from datetime import datetime
from typing import Any

from ..db import get_connection

# D1 / D6: the ladder is normative. interval_days advances to the
# next value strictly greater than the current one, and stays at
# the final value (60) once reached. No multiply formula.
_LADDER: tuple[int, ...] = (1, 3, 7, 21, 60)


class ReviewNotScheduledError(Exception):
    """Raised when mark_reviewed is called for a (user_id, unit_id)
    pair that has no review_schedule row (the unit was never
    completed, so it was never seeded)."""


class ReviewNotDueError(Exception):
    """Raised when mark_reviewed is called for a scheduled review
    that is not yet due (due_at > NOW()). D6 amendment: only a due
    review advances the ladder — strict, no grace window."""


def _next_interval(current: int) -> int:
    """Smallest ladder value strictly greater than `current`.

    Returns the final ladder value (60) once at or past it, so a
    unit advances monotonically and never falls back (D6). Defensive
    against off-ladder values: picks the next ladder step above
    whatever `current` is.
    """
    for step in _LADDER:
        if step > current:
            return step
    return _LADDER[-1]


def _map_review_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "userId": row["user_id"],
        "unitId": row["unit_id"],
        "dueAt": row["due_at"],
        "intervalDays": row["interval_days"],
        "lastReviewedAt": row["last_reviewed_at"],
    }


def seed_review(user_id: str, unit_id: str) -> dict[str, Any]:
    """Seed the first spaced review for (user_id, unit_id). (D3)

    First review due NOW() + 1 day, interval_days = 1.
    Idempotent and first-write-wins: ON CONFLICT (user_id, unit_id)
    DO NOTHING means a duplicate completion never resets or
    duplicates an existing schedule. Returns the schedule row
    (the freshly seeded one, or the pre-existing one on conflict).
    """
    with get_connection() as connection:
        row = connection.execute(
            """
            INSERT INTO review_schedule (user_id, unit_id, due_at, interval_days)
            VALUES (%s, %s, NOW() + make_interval(days => 1), 1)
            ON CONFLICT (user_id, unit_id) DO NOTHING
            RETURNING *
            """,
            (user_id, unit_id),
        ).fetchone()

        if row is None:
            # Already scheduled (sequential re-seed or a concurrent
            # insert won the race). Surface the existing row rather
            # than resetting it — first-write-wins per D3.
            row = connection.execute(
                "SELECT * FROM review_schedule WHERE user_id = %s AND unit_id = %s",
                (user_id, unit_id),
            ).fetchone()

        connection.commit()

    return _map_review_row(row)


def mark_reviewed(user_id: str, unit_id: str) -> dict[str, Any]:
    """Advance the review schedule one ladder step. (D6)

    interval_days -> next ladder value, last_reviewed_at = NOW(),
    due_at = NOW() + interval_days. Monotonic: no quality signal,
    no reset path. Raises ReviewNotScheduledError if the pair has
    no schedule row (the unit was never completed/seeded). Raises
    ReviewNotDueError if the row exists but is not yet due
    (due_at > NOW()) — D6 amendment, strict, no grace window.

    Concurrency: the row is SELECT ... FOR UPDATE locked before the
    read-compute-write so two concurrent reviews of the same pair
    serialize instead of both reading the same interval and writing
    the same next step (a lost tick that would under-advance the
    schedule). The lock is held until the transaction commits at the
    end of this block. The due check uses a DB-side clock so it is
    immune to app/DB clock skew, and specifically clock_timestamp()
    (real wall time at evaluation) NOT NOW(): NOW() is the
    transaction-start timestamp, fixed for the whole tx, so if this
    SELECT waits on a FOR UPDATE lock held by a concurrent tick, a
    request that started just before due_at but acquired the lock
    after due_at would wrongly evaluate "not due" against the stale
    tx time and return a false 409. The advance below keeps NOW() —
    the sub-second difference is irrelevant over day-scale intervals
    and matches D6's "now()". Keeping the ladder in Python
    (_next_interval) rather than a SQL CASE keeps _LADDER the single
    normative source (D6) without reintroducing a race.
    """
    with get_connection() as connection:
        current = connection.execute(
            "SELECT interval_days, (due_at <= clock_timestamp()) AS is_due "
            "FROM review_schedule "
            "WHERE user_id = %s AND unit_id = %s "
            "FOR UPDATE",
            (user_id, unit_id),
        ).fetchone()
        if current is None:
            raise ReviewNotScheduledError(f"{user_id}/{unit_id}")
        if not current["is_due"]:
            raise ReviewNotDueError(f"{user_id}/{unit_id}")

        next_interval = _next_interval(current["interval_days"])

        row = connection.execute(
            """
            UPDATE review_schedule
            SET interval_days = %s,
                last_reviewed_at = NOW(),
                due_at = NOW() + make_interval(days => %s),
                updated_at = NOW()
            WHERE user_id = %s AND unit_id = %s
            RETURNING *
            """,
            (next_interval, next_interval, user_id, unit_id),
        ).fetchone()

        connection.commit()

    return _map_review_row(row)


def _map_due_row(row: Any) -> dict[str, Any]:
    return {
        "unitId": row["unit_id"],
        "slug": row["slug"],
        "title": row["title"],
        "dueAt": row["due_at"],
        "intervalDays": row["interval_days"],
        "lastReviewedAt": row["last_reviewed_at"],
    }


def list_due(user_id: str, due_before: datetime | None = None) -> list[dict[str, Any]]:
    """Reviews due for `user_id` at or before `due_before`. (D5)

    `due_before` defaults to NOW() (server-side) when None — the
    common case "what's due right now". Joins `units` for the
    slug/title the client needs to render the review entry. Ordered
    by due_at then unit position (D5), so the oldest-due unit and,
    within a tie, the earliest path position surfaces first.
    """
    with get_connection() as connection:
        rows = connection.execute(
            """
            SELECT rs.unit_id,
                   u.slug,
                   u.title,
                   rs.due_at,
                   rs.interval_days,
                   rs.last_reviewed_at
            FROM review_schedule rs
            JOIN units u ON u.id = rs.unit_id
            WHERE rs.user_id = %s
              AND rs.due_at <= COALESCE(%s, NOW())
            ORDER BY rs.due_at ASC, u.position ASC
            """,
            (user_id, due_before),
        ).fetchall()
    return [_map_due_row(row) for row in rows]
