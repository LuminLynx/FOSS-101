# Unit 11 — c2-split gate

**Status:** Passed 2026-05-21. Split kept.
**Unit:** `streaming-ux-bundle-0` (position 11)
**Change under gate:** the criterion-2 SPLIT from `docs/RUBRIC_AUDIT.md` (PR #152) — old bundled c2 ("mismatched-combination failure mechanism AND the decisions constrain each other") split into mechanism-only c2 + a new c3 (constrain-each-other meta-claim); old map+PM criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **clean and kept.** One gate run against the live grader (Claude Sonnet 4.6):

| Metric | Value |
|--------|-------|
| Per-criterion agreement | 78/84 (92%) — **78/80 (97.5%) excluding the p011 infra ERROR** |
| Fully passed | 18/21 |
| Flagged-correct | 20/21 (the 21st is the p011 ERROR, unscoreable) |

Above the 80% PHASE_2_GATE.md bar either way.

## The headline: no c1/c3 overlap

Unit 11 carried the same risk that sank Unit 13 — that the split's new meta-claim criterion isn't separable from an existing one. Here the worry was c3 ("the decisions constrain each other") collapsing into c1 ("treats them as coupled"). Three pairs were authored to probe it: **p009, p014, p021** state generic coupling at the c1 level without identifying the specific mutual constraint, and were paper-judged `c3=false`.

**The grader agreed `c3=false` on all three.** The "X rules out Y" constraint is genuinely distinct content from naming the three decisions and from asserting they're coupled. Unit 11 splits cleanly like Unit 10, not fused like Unit 13.

## Disagreements (none on the split criteria)

- **p011 — grader-payload ERROR.** The grader returned a malformed payload (missing `grades`/`flagged`). This is the known stochastic payload bug documented in the regression-set header (p011 grades clean in isolation; the T2-D guardrail rejects rather than mis-grades). Infra, not content. Optional: re-run in isolation to confirm clean. No action on the set.
- **p021, c2 — grader lenient (preserved).** The grader marked c2 (mechanism) met; expected is `false`. p021 asserts the chat triple "does not fit this surface" and dismisses recovery, but gives no concrete failure mechanism (no "half-parsed table the user reads as broken"). Expected `c2=false` stands; the grader was lenient on a borderline. Not realigned.
- **p014, c1 — grader strict on the unsplit c1 (preserved).** The grader marked c1 not-met; expected is `true`. p014 names the three decisions but its coupling treatment is deliberately vague ("they are coupled, decide them together") without conveying "the combination produces the feel." This is the **unsplit c1 AND-clause** ("names the three AND treats them as coupled") showing the same bundling fragility the audit targets. Expected `c1=true` stands; logged as a data point for the deferred **c1/c3/c4 follow-up audit** (joins Unit 10's p008 c4).

## Realignments

None. All expected values stand. The two live disagreements (p021 c2, p014 c1) are preserved — one grader-lenient borderline, one deferred-c1 territory — not author errors.

## What this unlocks

- Unit 11's c2 split is gated and documented. The merged rubric (PR #152) stands.
- **Unit 12 (tool-use)** carries the identical "decisions constrain each other" meta-claim; Unit 11's clean result is strong evidence it will split cleanly too. Gate-confirmed per unit regardless.
- The deferred **c1/c3/c4 follow-up** now has two data points where an unsplit AND-clause produced a grader disagreement: Unit 10 p008 (c4: signals + PM-error) and Unit 11 p014 (c1: names + treats-as-coupled). Both are the same lenient/strict bundling fragility the c2 audit addressed, on criteria not yet split.
