# Regression-set operator gate

**Status:** Documented 2026-05-20
**Locks:** `docs/RUBRIC_AUDIT.md` § Decisions locked (full regression re-run before merge); `docs/PHASE_2_GATE.md` (per-criterion agreement ≥ 80%).
**Enforcement:** Operator-local, not CI.

## Why this exists

Every rubric change must run end-to-end against the live grader before merging. PR #146 (Unit 10 c2 split) demonstrated the gap: it merged without the live re-run because the gate wasn't documented anywhere actionable. This doc fixes that.

## Why not CI

GitHub repository secrets storage adds an exposure surface the project does not want to take on. Self-hosted runners would trade one surface for another. The locked decision says the re-run must happen — it does not say CI must be the enforcer. The operator can be, as long as the check actually happens and the result is recorded in the PR.

## Procedure (operator)

For any PR that edits `content/units/**` (rubric text) or `content/regression-sets/**`:

```bash
git checkout <pr-branch>
pip install -r backend/requirements.txt
python -m backend.scripts.migrate_db
python -m backend.scripts.ingest_units content/units/
python -m backend.scripts.run_regression_set \
  content/regression-sets/<unit-id>.yml --threshold 80
```

The `--threshold 80` flag (added in the same PR as this doc) gates the run on the PHASE_2_GATE.md bar: exit code 0 = ≥ 80% per-criterion agreement; exit code 1 = below.

`.env` provides `DATABASE_URL`, `AI_PROVIDER_API_KEY`, and `AI_MODEL` (defaults to `claude-sonnet-4-6`); no shell exports needed.

## PR-side requirement

Before merge, the rubric-change PR description (or a PR comment) carries at minimum:

- The `Per-criterion agreement:    X/Y (Z%)` line from the run output.
- Any `[FAIL]` per-pair rows.
- Any `[ERROR]` per-pair rows (payload-level failures, distinct from grader disagreement).

If agreement is below 80%, there are open ERRORs, or any FAIL row deserves inspection, triage before merge:
- Re-run with `--show-criteria` to surface which specific criterion the grader disagreed on for each FAIL pair. The flag adds a per-criterion `expected=… actual=… [ok|MISS]` block under each FAIL row; PASS pairs are unaffected.
- Realign per-pair `expected` values where the grader's strict reading is correct, then re-run.
- Document any preserved disagreements in the matching `docs/UNIT_*_GATE.md` (one new entry per realigned pair), per the precedent set by Units 2, 9, 10.
- Investigate ERROR rows against the known payload-bug patterns (markdown headers, emoji density, parenthetical option-lists per `docs/UNIT_*_GATE.md` lessons).

## What CI does cover

The default workflow (`.github/workflows/ci.yml`) still runs the fast no-secret guardrails on every PR:

- `lint_unit_markdown` — schema validity of authored unit markdown.
- `ingest_units --check` — every unit parses, prereq pointers resolve, sources well-formed.
- `run_regression_set --check` — every regression-set YAML schema-validates against `parse_regression_set`.

These catch ~all structural breakage. The operator gate above is the live-grader complement.

## Cost reference

Per `docs/PHASE_2_GATE.md`: ≈ $0.009 / grade call with prompt-cache hit. A 21-pair set ≈ $0.20 / run. The c2-split SPLIT rollout (Units 10–13 HIGH + Units 2–9 MEDIUM) is bounded by ~$2.50 total even with re-runs.
