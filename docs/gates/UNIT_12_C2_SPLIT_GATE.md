# Unit 12 — c2-split gate

**Status:** Passed 2026-05-21. Split kept.
**Unit:** `tool-use-bundle-0` (position 12)
**Change under gate:** the criterion-2 SPLIT from `docs/RUBRIC_AUDIT.md` (PR #154) — old bundled c2 ("mismatched-combination failure mechanism AND the decisions constrain each other") split into mechanism-only c2 + a new c3 (constrain-each-other meta-claim); old map+PM criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **clean and kept** — on a perfect gate run.

| Metric | Value |
|--------|-------|
| Per-criterion agreement | **84/84 (100%)** |
| Fully passed | **21/21** |
| Flagged-correct | 21/21 |
| Errors | 0 |

The cleanest run in the rollout: every one of the 21 pairs' four criteria matched the grader. Zero realignments, zero preserved disagreements.

## No c1/c3 overlap (confirmed, not just expected)

Unit 12 shares the exact "decisions constrain each other" meta-claim with Unit 11. The same three watch-pairs were authored to probe a c1/c3 overlap — **p009, p011, p021** state generic coupling at the c1 level without identifying the specific mutual constraint (p011 actively *dismisses* recovery), and were paper-judged `c3=false`. **The grader agreed `c3=false` on all three** (each passed 4/4). The constraint claim is genuinely separable from naming/coupling. Splits cleanly like Units 10 and 11.

## Surfaced latent-c3 credit (confirmed)

p006, p008, p010 — their old bundled c2 was `true` because both conjuncts held; the split awards each a true new c3 (they explain the constraint). The grader confirmed `c3=true` on all three (each passed 4/4).

## Realignments

None. The strict-AND paper authoring matched the live grader exactly — the same outcome the original Unit 12 authoring achieved (its pre-split gate was the cleanest in the path at 95% → 100%), now reproduced under the 4-criterion split.

## What this unlocks

- Unit 12's c2 split is gated and documented in the same PR as the split.
- **The HIGH-severity batch is complete:** Units 10 (kept), 11 (kept), 12 (kept), 13 (reverted — coupled conjuncts). Three of four split cleanly; the one exception (13) was caught at its gate and reverted.
- Remaining rollout: the **MEDIUM-severity batch** (Units 2–9, "failure-mode + mechanism") and the deferred **c1/c3/c4 follow-up audit** (data points: Unit 10 p008 c4, Unit 11 p014 c1).
