"""Integration tests for PathRepository (Postgres-gated on TEST_DATABASE_URL)."""
from __future__ import annotations

from .conftest import seed_path_with_units


def test_get_path_returns_path_with_unit_manifest(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import path_repository

    path = path_repository.get_path(seed["path_id"])

    assert path is not None
    assert path["id"] == "path-llm-pms"
    assert path["slug"] == "llm-systems-for-pms"
    assert path["title"] == "LLM Systems for PMs"
    units = path["units"]
    assert [u["id"] for u in units] == ["unit-a", "unit-b"]
    assert [u["position"] for u in units] == [1, 2]
    # Manifest only — no bite/depth/sources.
    assert "biteMd" not in units[0]
    assert set(units[0].keys()) == {
        "id",
        "slug",
        "title",
        "position",
        "status",
        "prereqUnitIds",
    }
    # Prereqs ride along on the manifest so the client can gate units.
    assert units[0]["prereqUnitIds"] == []
    assert units[1]["prereqUnitIds"] == ["unit-a"]


def test_get_path_returns_none_for_unknown_id(gated_db) -> None:
    from app.repositories import path_repository

    assert path_repository.get_path("does-not-exist") is None


def test_list_paths_returns_every_path(gated_db) -> None:
    seed_path_with_units(gated_db)

    from app.repositories import path_repository

    paths = path_repository.list_paths()
    assert len(paths) == 1
    assert paths[0]["id"] == "path-llm-pms"


def test_next_unit_for_user_returns_first_uncompleted(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository, path_repository

    # No completions yet -> next is unit-a (position 1).
    nxt = path_repository.next_unit_for_user(seed["user_id"], seed["path_id"])
    assert nxt is not None
    assert nxt["id"] == "unit-a"

    # After completing unit-a, next is unit-b.
    completion_repository.record_completion(seed["user_id"], "unit-a")
    nxt = path_repository.next_unit_for_user(seed["user_id"], seed["path_id"])
    assert nxt is not None
    assert nxt["id"] == "unit-b"

    # After completing both, next is None.
    completion_repository.record_completion(seed["user_id"], "unit-b")
    assert path_repository.next_unit_for_user(seed["user_id"], seed["path_id"]) is None


def test_draft_units_are_not_served(gated_db) -> None:
    """Draft units are ingested (gradable) but must not reach learners.

    A draft unit bound to the live path must be excluded from both the
    path manifest (get_path) and the "Continue" target (next_unit_for_user),
    so unverified content cannot ship just because it was ingested.
    """
    seed = seed_path_with_units(gated_db)

    # A draft unit at position 3 — ingested for the gate, but not published.
    with gated_db() as conn:
        conn.execute(
            """
            INSERT INTO units (
                id, path_id, slug, position, title, definition,
                trade_off_framing, bite_md, depth_md, prereq_unit_ids, status
            ) VALUES
                ('unit-c', 'path-llm-pms', 'safety', 3,
                 'Safety', 'Bounds the abuse surface.',
                 'when / when / cost', 'bite C', 'depth C', '{unit-b}', 'draft')
            """,
        )
        conn.commit()

    from app.repositories import completion_repository, path_repository

    # get_path manifest excludes the draft unit.
    path = path_repository.get_path(seed["path_id"])
    assert [u["id"] for u in path["units"]] == ["unit-a", "unit-b"]

    # next_unit never points at a draft: after completing every published
    # unit, "next" is None — not the draft unit-c.
    completion_repository.record_completion(seed["user_id"], "unit-a")
    completion_repository.record_completion(seed["user_id"], "unit-b")
    assert path_repository.next_unit_for_user(seed["user_id"], seed["path_id"]) is None


def test_next_unit_for_user_returns_none_for_path_with_no_units(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    with gated_db() as conn:
        conn.execute(
            "INSERT INTO paths (id, slug, title) VALUES ('empty', 'empty-path', 'Empty')"
        )
        conn.commit()

    from app.repositories import path_repository

    assert path_repository.next_unit_for_user(seed["user_id"], "empty") is None
