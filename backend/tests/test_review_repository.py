"""Integration tests for ReviewRepository — F5 spaced review
(Postgres-gated on TEST_DATABASE_URL).

Covers docs/guides/F5_SPACED_REVIEW.md decisions D1/D3/D6 and the
seed-on-completion hook (the only cross-repository call in
completion_repository).
"""
from __future__ import annotations

import pytest

from .conftest import seed_path_with_units


def _force_due(get_connection, user_id: str, unit_id: str) -> None:
    """Backdate due_at into the past so a mark_reviewed call passes
    the D6 due-gate. Seeded/advanced rows are always future-dated;
    tests that exercise the tick must make the row due first."""
    with get_connection() as conn:
        conn.execute(
            "UPDATE review_schedule SET due_at = NOW() - make_interval(secs => 1) "
            "WHERE user_id = %s AND unit_id = %s",
            (user_id, unit_id),
        )
        conn.commit()


def test_seed_review_creates_row_due_next_day(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    row = review_repository.seed_review(seed["user_id"], "unit-a")

    assert row["userId"] == seed["user_id"]
    assert row["unitId"] == "unit-a"
    assert row["intervalDays"] == 1
    assert row["lastReviewedAt"] is None
    # due ~ now + 1 day; assert it lands ~24h out with generous slack.
    with gated_db() as conn:
        delta = conn.execute(
            "SELECT EXTRACT(EPOCH FROM (due_at - NOW())) AS secs "
            "FROM review_schedule WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()["secs"]
    assert 23 * 3600 < delta < 25 * 3600


def test_seed_review_is_first_write_wins(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    first = review_repository.seed_review(seed["user_id"], "unit-a")
    # Advance it, then re-seed: the re-seed must NOT reset interval/due.
    _force_due(gated_db, seed["user_id"], "unit-a")
    review_repository.mark_reviewed(seed["user_id"], "unit-a")
    again = review_repository.seed_review(seed["user_id"], "unit-a")

    assert again["id"] == first["id"]
    assert again["intervalDays"] == 3  # still advanced, not reset to 1
    with gated_db() as conn:
        count = conn.execute(
            "SELECT COUNT(*) AS n FROM review_schedule "
            "WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()["n"]
    assert count == 1  # no duplicate row


def test_mark_reviewed_advances_the_ladder(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    review_repository.seed_review(seed["user_id"], "unit-a")
    # D1/D6: explicit ladder 1 -> 3 -> 7 -> 21 -> 60, then stays 60.
    # Each tick re-futures due_at, so force-due before every step
    # (D6 amendment: only a due review advances).
    expected = [3, 7, 21, 60, 60]
    seen = []
    for _ in range(5):
        _force_due(gated_db, seed["user_id"], "unit-a")
        seen.append(
            review_repository.mark_reviewed(seed["user_id"], "unit-a")["intervalDays"]
        )
    assert seen == expected


def test_mark_reviewed_sets_last_reviewed_and_due(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    review_repository.seed_review(seed["user_id"], "unit-a")
    _force_due(gated_db, seed["user_id"], "unit-a")
    row = review_repository.mark_reviewed(seed["user_id"], "unit-a")

    assert row["lastReviewedAt"] is not None
    assert row["intervalDays"] == 3
    with gated_db() as conn:
        delta = conn.execute(
            "SELECT EXTRACT(EPOCH FROM (due_at - NOW())) AS secs "
            "FROM review_schedule WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()["secs"]
    # due ~ now + 3 days.
    assert 71 * 3600 < delta < 73 * 3600


def test_mark_reviewed_unscheduled_raises(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    with pytest.raises(review_repository.ReviewNotScheduledError):
        review_repository.mark_reviewed(seed["user_id"], "unit-b")


def test_mark_reviewed_not_due_raises(gated_db) -> None:
    # D6 amendment: a scheduled-but-not-yet-due review must be
    # rejected (due in +1 day; no force-due here), and the row
    # must NOT advance.
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    review_repository.seed_review(seed["user_id"], "unit-a")
    with pytest.raises(review_repository.ReviewNotDueError):
        review_repository.mark_reviewed(seed["user_id"], "unit-a")

    with gated_db() as conn:
        row = conn.execute(
            "SELECT interval_days, last_reviewed_at FROM review_schedule "
            "WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()
    assert row["interval_days"] == 1  # unchanged
    assert row["last_reviewed_at"] is None  # never advanced


def test_completion_seeds_a_review(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import completion_repository

    result = completion_repository.record_completion(seed["user_id"], "unit-a")
    assert result["alreadyCompleted"] is False

    with gated_db() as conn:
        row = conn.execute(
            "SELECT interval_days, last_reviewed_at FROM review_schedule "
            "WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()
    assert row is not None
    assert row["interval_days"] == 1
    assert row["last_reviewed_at"] is None


def test_duplicate_completion_does_not_reseed(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import completion_repository, review_repository

    completion_repository.record_completion(seed["user_id"], "unit-a")
    # Advance the review, then re-complete the unit. The duplicate
    # completion (alreadyCompleted=True) must NOT reset the schedule.
    _force_due(gated_db, seed["user_id"], "unit-a")
    review_repository.mark_reviewed(seed["user_id"], "unit-a")
    dup = completion_repository.record_completion(seed["user_id"], "unit-a")
    assert dup["alreadyCompleted"] is True

    with gated_db() as conn:
        row = conn.execute(
            "SELECT interval_days FROM review_schedule "
            "WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchone()
    assert row["interval_days"] == 3  # still advanced, not reset to 1


def test_list_due_returns_due_units_joined_and_ordered(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from datetime import datetime, timedelta, timezone

    from app.repositories import review_repository

    review_repository.seed_review(seed["user_id"], "unit-a")
    review_repository.seed_review(seed["user_id"], "unit-b")
    # Seeded due = now + 1 day, so nothing is due "now".
    assert review_repository.list_due(seed["user_id"]) == []

    # With due_before in 2 days, both are due; ordered by due_at then
    # unit position -> unit-a (position 1) before unit-b (position 2).
    horizon = datetime.now(timezone.utc) + timedelta(days=2)
    due = review_repository.list_due(seed["user_id"], due_before=horizon)
    assert [r["unitId"] for r in due] == ["unit-a", "unit-b"]
    assert due[0]["slug"] == "tokenization"
    assert due[0]["title"] == "Tokenization"
    assert due[0]["intervalDays"] == 1
    assert due[0]["lastReviewedAt"] is None


def test_list_due_excludes_not_yet_due(gated_db) -> None:
    seed = seed_path_with_units(gated_db)
    from app.repositories import review_repository

    review_repository.seed_review(seed["user_id"], "unit-a")
    # Default due_before = NOW(); seeded review is +1 day out.
    assert review_repository.list_due(seed["user_id"]) == []
