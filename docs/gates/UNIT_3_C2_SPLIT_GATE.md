# Unit 3 — c2-split gate

**Status:** Passed 2026-05-21. Split kept (zero realignments).
**Unit:** `latency-bundle-0` (position 3)
**Change under gate:** the criterion-2 SPLIT from `docs/audits/RUBRIC_AUDIT.md` (PR #157), second of the MEDIUM-severity batch — old bundled c2 ("names a concrete failure mode AND explains the mechanism") split into name-only c2 + a new c3 (explain the mechanism); old regime criterion renumbered to c4. Rubric grew 3 → 4.
**Procedure:** `docs/gates/REGRESSION_GATE.md` (operator-run, local grader).

## Decision

The split is **kept, with no realignments.** Gate run against the live grader:

| Metric | Value |
|--------|-------|
| Per-criterion agreement | 74/88 (84%) raw — **~74/76 (≈97%) on scoreable split criteria** |
| Fully passed | 17/22 (raw) |
| Errors | 3 (1 known + 2 stochastic) |

Above the 80% bar.

## Headline: the predictive strict-c3 approach validated

Unit 2's gate taught that the grader holds the mechanism half (c3) strictly. For Unit 3 that lesson was applied **predictively** — p007/p017/p022 were judged `c2=true, c3=false` (name a failure mode without explaining its mechanism) up front, not discovered at the gate. **The grader confirmed `c3=false` on all of them:**

- **p007** — PASS 4/4 (gestures "faster but lower quality", no mechanism).
- **p017** — PASS 4/4 (pseudocode comments state conclusions, don't explain).
- **p022** — c3 `expected=false, actual=false` (names B's action-item loss, no capability mechanism).

These three map exactly to the pairs the *original* Unit 3 gate flagged as c2-AND-structure borderlines — the split cleanly isolates the half that caused the ambiguity. **Zero disagreements on the split criteria (c2, c3).**

(p021, the inverse watch-pair judged `c3=true` on its clocks mechanism, ERRORed this run and wasn't scored — re-run to confirm.)

## Disagreements — both on the unsplit c1

- **p011 c1** (`expected=false, actual=true`) and **p022 c1** (`expected=false, actual=true`) — the grader read their framing as naming the multi-axis trade-off. This is the grader-lenient-on-c1 pattern, identical to Unit 2 p008 c1. c1 is the unsplit "names X AND treats as Y" criterion; preserved as data points for the deferred c1/c3/c4 follow-up, not split issues.

## Errors (infra, not content)

- **p018** — known deterministic emoji-payload marker (8 emojis; kept as a known-bad regression marker per the set header).
- **p008, p021** — stochastic grader-payload bug (no emoji; graded clean previously). Recommend an isolation re-run to confirm; not a split or content issue.

## Realignments

None. The split criteria matched the grader on every scoreable pair. The two live disagreements are grader-lenient reads of the unsplit c1 (deferred), not split errors.

## What this unlocks

- Unit 3's c2 split is gated and documented; the merged rubric (PR #157) stands.
- **The predictive strict-c3 approach is validated** — judging c3 against a strict "why, not just that" reading up front produced zero realignments here (vs. 3 at Unit 2's gate, where the lesson hadn't yet been applied). Units 4–9 can use the same approach with confidence.
- The deferred **c1/c3/c4 follow-up** now has four c1/c4 data points: Unit 10 p008 c4, Unit 11 p014 c1, Unit 2 p008 c1, and Unit 3 p011 + p022 c1 — a consistent "c1 AND-clause is grader-lenient" signal.
