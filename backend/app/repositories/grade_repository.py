"""GradeRepository — writes/reads against the `grades` table.

Per docs/STRATEGY.md § T2-A, grades are per-criterion (Met / Not Met
with confidence + rationale + answer-quote), never holistic. The
schema lives in migration 019:

    grades (
        id BIGSERIAL PK,
        completion_id BIGINT FK -> completions.id ON DELETE CASCADE,
        criterion_id BIGINT FK -> rubric_criteria.id ON DELETE CASCADE,
        met BOOLEAN, confidence REAL, rationale TEXT,
        flagged BOOLEAN, created_at TIMESTAMPTZ,
        UNIQUE (completion_id, criterion_id)
    )

`flagged` is repeated on every grade row in this table by design — the
schema doesn't carry a top-level "this submission was flagged" column,
so we write the same flagged value on every row in a single submission.
A higher-level read can decide whether to surface "any criterion
flagged" or "all criteria flagged"; today we set the same value
across, since the grader emits one top-level flagged per call.

Every submission UPSERTs by (completion_id, criterion_id): re-grading
the same completion replaces the prior row in place. Older grades for
that pair are not retained — the unique constraint enforces a single
current grade per criterion. If history is needed later, it can be
added via an `attempts` table without touching this repository.

`answer_quote` is captured by the grader (T2-D guardrail) but not
persisted on the row today — the column doesn't exist in migration
019. Storing it requires a future migration; for the Phase 2 gate we
return it in the API response from the grader output but don't save
it. Tracked as a follow-up.
"""
from __future__ import annotations

from typing import Any

from ..db import get_connection


class CompletionNotOwnedError(Exception):
    """Raised when grades are written/read for a completion that doesn't
    belong to the acting user — a tenant-isolation guard. It can't happen
    via the current endpoint (the completion id is server-derived from the
    authenticated user), but it's enforced at the repository so a future
    caller that passes a client-supplied completion_id can't cross tenants."""


def _map_grade_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "completionId": row["completion_id"],
        "criterionId": row["criterion_id"],
        "met": row["met"],
        "confidence": float(row["confidence"]),
        "rationale": row["rationale"],
        "flagged": row["flagged"],
        "createdAt": row["created_at"],
    }


def upsert_grades(
    completion_id: int,
    grades: list[dict[str, Any]],
    flagged: bool,
    user_id: str,
) -> list[dict[str, Any]]:
    """Replace this completion's grades atomically.

    The grades table is keyed (completion_id, criterion_id), so a plain
    INSERT … ON CONFLICT only refreshes rows whose criterion id appears
    in the incoming submission. If the rubric's criterion set changed
    between grading attempts (chunk 6's append-only versioning gives a
    re-authored rubric a fresh set of criterion ids), prior rows for
    retired criteria would linger and reads would mix old and new.

    To prevent that, we DELETE every existing row for this completion
    whose criterion_id is *not* in the incoming submission, then UPSERT
    each incoming grade — all in a single transaction. After the call,
    `grades` for this completion exactly mirrors what the grader just
    returned.

    Each grade is { criterion_id, met, confidence, rationale, ... }.
    Returns the persisted rows in the order grades were submitted.
    """
    if not grades:
        return []

    incoming_criterion_ids = [int(g["criterion_id"]) for g in grades]

    persisted: list[dict[str, Any]] = []
    with get_connection() as connection:
        # Tenant guard: refuse to write grades for a completion the user
        # doesn't own. Belt-and-suspenders today (the endpoint derives
        # completion_id from the authenticated user), enforced here so a
        # future client-supplied id can't cross tenants.
        owner = connection.execute(
            "SELECT 1 FROM completions WHERE id = %s AND user_id = %s",
            (completion_id, user_id),
        ).fetchone()
        if owner is None:
            raise CompletionNotOwnedError(completion_id)

        # Atomic with the UPSERTs below. Drop any prior rows whose
        # criterion id isn't in the incoming submission — those reflect
        # a rubric the grader no longer evaluated against.
        connection.execute(
            """
            DELETE FROM grades
            WHERE completion_id = %s
              AND criterion_id <> ALL(%s::bigint[])
            """,
            (completion_id, incoming_criterion_ids),
        )

        for grade in grades:
            row = connection.execute(
                """
                INSERT INTO grades (completion_id, criterion_id, met, confidence, rationale, flagged)
                VALUES (%s, %s, %s, %s, %s, %s)
                ON CONFLICT (completion_id, criterion_id) DO UPDATE SET
                    met = EXCLUDED.met,
                    confidence = EXCLUDED.confidence,
                    rationale = EXCLUDED.rationale,
                    flagged = EXCLUDED.flagged
                RETURNING *
                """,
                (
                    completion_id,
                    grade["criterion_id"],
                    grade["met"],
                    grade["confidence"],
                    grade["rationale"],
                    flagged,
                ),
            ).fetchone()
            persisted.append(_map_grade_row(row))
        connection.commit()
    return persisted


def list_grades_for_completion(completion_id: int, user_id: str) -> list[dict[str, Any]]:
    """Return all grades for a completion the user owns, by criterion position.

    Scoped by `user_id` via the completions join so one user can never read
    another's grades, regardless of how `completion_id` was obtained.
    """
    with get_connection() as connection:
        rows = connection.execute(
            """
            SELECT g.*
            FROM grades g
            JOIN rubric_criteria rc ON rc.id = g.criterion_id
            JOIN completions c ON c.id = g.completion_id
            WHERE g.completion_id = %s AND c.user_id = %s
            ORDER BY rc.position ASC
            """,
            (completion_id, user_id),
        ).fetchall()
    return [_map_grade_row(row) for row in rows]
