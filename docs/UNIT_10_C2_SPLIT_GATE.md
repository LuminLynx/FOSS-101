# Unit 10 — c2-split gate

**Status:** Passed 2026-05-21. Split kept.
**Unit:** `vector-search-rag-bundle-0` (position 10)
**Change under gate:** the criterion-2 SPLIT from `docs/RUBRIC_AUDIT.md` (PR #146) — old bundled c2 ("mechanism AND the three failure modes need different fixes") split into mechanism-only c2 + a new c3 (different-fixes meta-claim); old signals criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **well-calibrated and kept.** Two end-to-end runs against the live grader (Claude Sonnet 4.6):

| Run | Per-criterion agreement | Fully passed | Flagged-correct |
|-----|------------------------|--------------|-----------------|
| 1 (`--threshold 80`) | 81/84 (96.4%) | 18/21 | 21/21 |
| 2 (`--show-criteria`) | 79/84 (94%) | 17/21 | 21/21 |

Both clear the 80% PHASE_2_GATE.md bar comfortably. The run-to-run delta is grader stochasticity on the borderline pairs (p011 passed run 1, failed run 2).

The split's own criteria (c2 mechanism, c3 different-fixes) **agree with the grader on every pair except two c3 realignments below, both of which are author error in the expected values — not the split.** The split achieved its goal: the different-fixes meta-claim is now graded as its own criterion, and the "all-four-met" bar is meaningfully harder than the old bundled "all-three-met."

## Triage of disagreements

`--show-criteria` (run 2) isolated which criterion each FAIL disagreed on.

### Realigned — grader's strict reading is correct (author error)

- **p009, c3 → false.** "Each dimension has its own measurement discipline and its own set of fixes" is a generic assertion, not a substantive identification that the three failure modes need *different* fixes. The new c3 requires the latter.
- **p011, c3 → false.** p011 names only *two* dimensions (c1=false — it collapses citation faithfulness into groundedness) and says "the fixes are different" for that 2-way split. It cannot identify different fixes for *three* failure modes when it only recognizes two. The original expected (c3=true) was internally inconsistent with c1=false.
- **p021, c2 → false.** p021's "mechanism" is the single-upstream-cause theory — "if we get retrieval right, groundedness and citation faithfulness follow" — which is precisely the misconception the unit teaches against (the three dimensions are independent). It is not a valid per-dimension failure mechanism. The original expected (c2=true) credited mechanistic-sounding language without noticing it was the wrong mechanism.

All three were over-lenient paper judgments from the split authoring; the grader caught them. Realigning makes the regression set's ground truth correct.

### Preserved — grader is over-strict (no realignment)

- **p008, c4.** The grader marks c4 (the unsplit signals criterion) not-met. But p008 names a clean signal per dimension (recall@K/MRR, RAGAS faithfulness, span-level source-quote validation) **and** recognizes the single-signal trap ("each layer can fail independently — high recall doesn't imply good generation"). It genuinely meets both conjuncts of c4. Expected stays `c4=true`; this remains a live disagreement on runs where the grader reads c4 strictly.

  **Why it matters:** c4 is itself an AND-clause ("names a signal per dimension **AND** recognizes the single-signal-as-proxy PM error") that this audit has *not* split. p008's disagreement is the same lenient/strict bundling fragility the audit is about, now visible on c4. It is a data point for the deferred **c1/c3/c4 follow-up audit**, not a c2-split issue.

## Net expected-value changes (vs. the split-authoring state)

| Pair | Was | Now | Reason |
|------|-----|-----|--------|
| p009 | (T,F,T,F) | (T,F,F,F) | c3 realigned |
| p011 | (F,T,T,F) | (F,T,F,F) | c3 realigned |
| p021 | (T,T,F,T) | (T,F,F,T) | c2 realigned |
| p008 | (T,F,F,T) | (T,F,F,T) | unchanged — c4 preserved as a documented disagreement |

## What this unlocks

- Unit 10's c2 split is fully gated and documented. The merged rubric (PR #146) stands.
- The realigned regression set is the new ground truth; a re-run should agree on p009/p011/p021 (modulo grader stochasticity) and continue to disagree on p008 c4 until the c4 follow-up.
- **Cross-unit signal:** the c4 (signals + PM-error) AND-clause shows the same bundling fragility on Unit 10 that the c2 audit targeted. The deferred c1/c3/c4 follow-up should treat c4 on the same framework.
- Rollout continues to Units 11 (streaming-ux) and 12 (tool-use) — HIGH-severity, separable "decisions constrain each other" meta-claim, gate-confirmed per unit (Unit 13 is the reverted exception, see `RUBRIC_AUDIT.md` § Rollout findings).
