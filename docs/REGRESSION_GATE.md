# Regression-set operator gate

**Status:** Documented 2026-05-20
**Locks:** `docs/RUBRIC_AUDIT.md` § Decisions locked (full regression re-run before merge); `docs/PHASE_2_GATE.md` (per-criterion agreement ≥ 80%).
**Enforcement:** Operator-local, not CI.

## Why this exists

Every rubric change must run end-to-end against the live grader before merging. PR #146 (Unit 10 c2 split) demonstrated the gap: it merged without the live re-run because the gate wasn't documented anywhere actionable. This doc fixes that.

## Why not CI

GitHub repository secrets storage adds an exposure surface the project does not want to take on. Self-hosted runners would trade one surface for another. The locked decision says the re-run must happen — it does not say CI must be the enforcer. The operator can be, as long as the check actually happens and the result is recorded in the PR.

## The grader vs. the gold standard (read this before realigning anything)

**The grader is Anthropic Claude Sonnet 4.6** (STRATEGY § T2-E; `.env` `AI_MODEL=claude-sonnet-4-6`), with a documented escalation path to Opus 4.7 for the hardest rubrics. It is the model that grades real learners' answers in production, so the regression set exists to keep *it* honest.

**The content and its expected values were authored by the more capable Opus 4.7 and locked.** They are the **gold standard** — the ideal grade a fair human/Opus reading would assign.

This capability asymmetry has a hard consequence for gate triage:

- A disagreement between the Sonnet grader and an Opus-authored expected value is, by default, a **grader calibration gap to document** — *not* evidence the gold standard is wrong. The weaker model disagreeing with the stronger author's judgment is the expected failure direction.
- **Do not edit a locked expected value down to match the grader.** Doing so degrades the gold standard to the weaker model and defeats the regression set's purpose, which is to *surface* Sonnet's miscalibration, not hide it.
- **Realign an expected value only when it is a plain authoring error** that a careful reader (human/Opus) would independently agree with — established by the *fair-human read*, not by what the grader output on a given run. The grader is stochastic; a single run is never sufficient grounds to change locked ground truth.
- Preserve-by-default. When in doubt, keep the Opus value and log the Sonnet disagreement as a calibration gap (and as input to the deferred c1/c3/c4 follow-up audit, which is cataloguing exactly these gaps).

This principle was added 2026-05-21 after the c2-split batch surfaced the asymmetry: several early gate realignments flipped Opus-authored values to match Sonnet's stricter/looser reads, which was backwards.

## When the pre-merge gate is required vs. optional

`RUBRIC_AUDIT.md` § Decisions locked originally required a full pre-merge gate run for *every* rubric change. The grader/gold-standard principle above narrows that:

- **Gate REQUIRED** for any change that *realigns* expected values, or any new authoring — i.e. anything where the grader's output could change a recorded value or where there's no locked value to fall back on.
- **Gate OPTIONAL (informational only)** for a *faithful preserve-by-default split* — a structural c2-split that only redistributes a locked `old-c2=T` into `c2=T` + `c3=T` (and `old-c2=F` into a fair-read name/mechanism judgment), with **zero realignments**. These don't change any locked value's meaning, and since we don't act on grader output, a gate run can't change a single expected value — it would only produce a documented snapshot of Sonnet calibration gaps. Local validators (lint, `ingest_units --check`, `run_regression_set --check`, pytest) plus the faithful decomposition are sufficient to merge.

Rationale: the original "always gate" rule predated this principle; its purpose was to catch grader miscalibration we'd *act on*. When we preserve by default, that purpose doesn't apply. Running the gate remains welcome as an informational snapshot, just not a merge gate. (Recorded 2026-05-21, during the MEDIUM-batch rollout, to avoid silent process drift.)

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
