# Unit 2 — c2-split gate

**Status:** Passed 2026-05-21. Split kept (3 realignments).
**Unit:** `context-window-bundle-0` (position 2)
**Change under gate:** the criterion-2 SPLIT from `docs/RUBRIC_AUDIT.md` (PR #155), first of the MEDIUM-severity batch — old bundled c2 ("names a concrete failure mode AND identifies the mechanism behind it") split into name-only c2 + a new c3 (explain the mechanism); old regime-distinction criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **kept.** Gate run against the live grader (Claude Sonnet 4.6):

| Metric | Value |
|--------|-------|
| Per-criterion agreement | 76/88 (86%) raw — **~79/80 (≈99%) on scoreable pairs after triage** |
| Fully passed | 15/22 (raw) |
| Flagged-correct | 19/22 |
| Errors | 2 (infra) |

Above the 80% bar even raw. The raw number understates calibration because it includes 2 infra ERRORs (8 criteria) and 3 author-error c3 judgments (realigned below).

## The key finding: the split is differentially exercised

The split PR's coverage note claimed no pair lands in the "names-but-doesn't-explain" middle (c2=true, c3=false), so the split wasn't differentially tested. **The gate disproved this.** Three pairs failed on exactly that distinction, all `c3 expected=true, actual=false`:

- **p007** — "they cost more linearly... the model may not use the long context well": states the failures with adjectives, never explains *why*.
- **p017** — pseudocode comments "Linear cost scales with corpus size" / "multi-needle → recall degrades": labels, not mechanisms.
- **p021** — "linear input cost per call, recall degradation mid-document": telegraphic naming, no WHY.

The grader read c3 strictly and correctly: pairs that **explain** the mechanism (p002 "pays for 150k input tokens at the per-token rate"; p010 in depth) pass c3; pairs that only **name** the failure do not. The initial paper judgment over-credited c3 on these three.

**Realigned p007/p017/p021 c3 → false.** This both matches the grader and converts them into the c2=true / c3=false differential pairs the set was missing. The coverage gap is closed, not by adding a pair, but by correcting the judgment the gate exposed.

## Other disagreements (not split issues)

- **p008 c1** — the known preserved disagreement (grader lenient on the budget framing, on the unsplit c1). Carries forward unchanged.
- **p010** — was a c2 preserved disagreement on the old bundled criterion (grader wanted multiple mechanisms). The singular split (c2 = name *a* failure mode; c3 = explain *the* mechanism) **resolved it** — p010 (F,T,T,F) passed and agreed with the grader.
- **p014 flag** — grader flagged the 3-word "Use RAG" (expected flagged=false). A stochastic flag flip (the flag's known unreliability across units). Preserved; not a criterion or split issue.
- **p016, p022 ERRORs** — grader-payload bug (p016 Portuguese edge-case, p022 hedge-heavy). Infra; clear on isolation re-run per the cross-unit pattern.

## Realignments

Three: p007 c3 → false, p017 c3 → false, p021 c3 → false. All author-error (paper judgment too lenient on "explain the mechanism"); the grader's strict reading is correct.

## What this unlocks

- Unit 2's c2 split is gated and documented; the merged rubric (PR #155) stands.
- **MEDIUM-batch lesson:** unlike the HIGH units (where the meta-claim was the separable half), here the *mechanism* half (c3) is what the grader holds strictly — answers readily name a failure mode but often only *state* it. Expect the same c3-strict pattern on Units 3–9; author/judge the c3 (explain-the-mechanism) expected values against a strict "why, not just that" reading from the start.
- The deferred **c1/c3/c4 follow-up** gains another c1 data point (p008).
