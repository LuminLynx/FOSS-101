# Unit 4 — c2-split gate

**Status:** Passed 2026-05-21. Split kept (3 realignments, 2 preserved disagreements).
**Unit:** `evals-bundle-0` (position 4)
**Change under gate:** the criterion-2 SPLIT from `docs/RUBRIC_AUDIT.md` (PR #159), third of the MEDIUM batch — old bundled c2 ("names a concrete failure mode AND explains the mechanism") split into name-only c2 + a new c3 (explain the mechanism); old regime criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **kept.** Gate run against the live grader:

| Metric | Value |
|--------|-------|
| Per-criterion agreement | 79/84 (94%) raw — **82/84 (≈98%) after realignment** |
| Fully passed | 17/21 (raw) |
| Errors | 0 |

Above the 80% bar. (No grader-payload ERRORs this run — Unit 4's emoji pair p018 caps at 4 emojis per the Unit 3 lesson and graded clean.)

## Headline: the differential pair confirmed

**p007 PASSED 4/4** — judged `c2=true, c3=false` (gestures at LLM-judge bias without naming the self-enhancement mechanism), and the grader agreed. The predictive c2=T/c3=F differential held on the pair authored for exactly that purpose.

## Realignments (grader's read correct/reasonable)

- **p017 c3 → false.** The pseudocode pair. The grader held its comments ("Blind to unknown regressions by construction", "self-enhancement bias") to the strict standard — labels, not explanations. Same pattern as Unit 3 p017. Now a clean c2=T/c3=F differential, joining p007.
- **p021 c3 → false.** Concise pair; the grader holds c3 strict on its compact mechanism phrasing. Realigned toward the strict direction the audit intends.
- **p021 c4 → true.** The grader reads p021 as mapping methods to regimes ("LLM-judge on escalations + human eval on hard cases") — defensible. The inherited c4=false (from the pre-split gate) was stale.

## Preserved disagreements (grader *lenient* against an authored-strict value)

- **p011 c2** (`expected=false, actual=true`). The pair's own label is "names all four methods + regime distinction *without naming failure-mode mechanism*" — c2 (failure mode) is authored-absent. The grader read it as present. Grader-lenient on c2; the strict authored value is kept.
- **p014 c1** (`expected=false, actual=true`). Grader reads "use multiple eval approaches" as naming the multi-method choice. The grader-lenient-on-c1 pattern (Unit 2 p008, Unit 3 p011/p022). c1 is unsplit — deferred c1/c3/c4 follow-up.

Both are grader-*lenient* drifts on the unsplit/authored-strict criteria. They feed the deferred follow-up rather than being realigned (the audit tightens *toward* strict, so the lenient grader read isn't the calibration target).

## What this unlocks

- Unit 4's c2 split is gated and documented; the merged rubric (PR #159) stands.
- **Unit-4 confirmed the nuance predicted in the split PR:** the mechanism half (c3) split cleanly and mostly tracked the old c2's credited content, while the historical strictness lived in c4 (regime). Three c2=T/c3=F differential pairs now exist (p007, p017, p021).
- The deferred **c1/c3/c4 follow-up** gains data points: Unit 4 p011 c2 (grader-lenient on c2) and p014 c1 (grader-lenient on c1), alongside Unit 2 p008 c1, Unit 3 p011+p022 c1, Unit 10 p008 c4, Unit 11 p014 c1. The pattern is now broad: **the grader reads the unsplit AND-clauses (c1, c4) leniently** — the clear next audit target.
