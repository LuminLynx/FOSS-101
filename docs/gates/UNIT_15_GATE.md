# Unit 15 Gate — Safety + content moderation

> **Per-unit gate audit** for `safety-bundle-0` (Phase 3, Unit 15).
> Mirrors `docs/UNIT_14_GATE.md` and prior per-unit gates. Closes the
> Production block (Units 11–15) and the v1 spine's first 15 units.

---

## Decision

**Unit 15 PASSED 2026-05-25. Status flipped `draft` → `published`.**

The 21-pair regression set hit **97.6% per-criterion agreement (82/84)**
against the live Sonnet 4.6 grader — above the 80% publish threshold —
with **0 errored pairs**. Two preserved per-criterion disagreements
(p011 c3, p021 c3), both held to the gold standard per
`docs/REGRESSION_GATE.md`. Authored against the post-c2-split
4-criterion rubric from the start; no retroactive split needed.

| Criterion | Required | Run (21 pairs) | Verdict |
|---|---|---|---|
| Per-criterion agreement | ≥ 80% | **97.6% (82/84)** | ✅ |
| Honest flagged behavior | spec-faithful | 20/21 — p003 preserved disagreement | ✅ |
| Cost / call | reasonable | cache reads 96,500 ≈ 7× input; in-band | ✅ |

---

## Headline: this gate caught a grader-wide over-flagging regression (now fixed)

Unit 15 was the first unit gated **after** the H1 answer-quote
verification (PR #202) merged. The first gate run exposed a side effect
of H1, then a fix (PR #204) resolved it — both runs are recorded here
because the diagnosis is the substantive finding of this gate.

| | First run (H1 = exact-substring) | Re-run (H1 = token-overlap, #204) |
|---|---|---|
| Per-criterion agreement | 97.6% (82/84) | **97.6% (82/84)** |
| Flagged-correct | 12/21 | **20/21** |
| Spurious flags (`want=False got=True`) | 9 | **1** |
| Errored | 0 | 0 |

The first run flagged **9 of 21** correct answers. Diagnosis: H1 forced
`flagged=true` whenever the grader's `answer_quote` was not a verbatim
substring of the answer — but Sonnet routinely paraphrases, reorders, and
truncates its quotes, so honest quotes failed the exact-substring check.
Because H1 was already live on `main`, this over-flagged grading on
**every** unit, not just this one.

**PR #204** replaced exact-substring with a **token-overlap** grounding
check (a quote is grounded when ≥ `AI_QUOTE_MIN_OVERLAP`, default 0.5, of
its words appear in the answer; multiplicity-counted per Codex P1). The
re-run flipped 8 of the 9 false flags back to correct (flagged-correct
12/21 → 20/21) and left per-criterion agreement **unchanged at 97.6%** —
confirming the fix only ever moved the `flagged` bit, never which
criteria are `met`, so it could not affect the gate metric.

---

## Run evidence (2026-05-25, post-#204)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6, prompt
caching on rubric per STRATEGY.md § T2-E).

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  18 (85%)
Per-criterion agreement:    82/84 (97%)
Flagged-correct:            20/21

Token usage (cost-relevant):
  input tokens:        13868
  cache reads:         96500
  output tokens:       14552
```

---

## Triage of disagreements (three; all preserved, zero realignments)

### p011 c3 + p021 c3 — the c1/c3-overlap watch-pair (preserve)

Both pairs scored `crit=3/4`, with the miss on **c3** (the
"decisions mutually constrain each other" criterion). Both are
deliberate watch-pairs: they name the three coupled decisions (c1) and,
for p021, map the guarded-action combination to the money-transfer
surface (c4), but state the constraint only *generically* ("the pieces
have to fit") without identifying the *specific* mutual constraint — so
the grader strict-reads c3 as not-met.

**Disposition: preserve `c3=true`.** This is the same reproducible
grader-strict-on-borderline pattern as Unit 14 p010 and Unit 11 p014 —
`docs/REGRESSION_GATE.md` is explicit that a stricter Sonnet read on a
genuine borderline is **not** grounds to flip the Opus-authored value
down. The c1/c3 split is otherwise clean (the surfaced-c3 pairs return
`c3=true`), so the criterion fires in both directions. Logged as a
c1/c3-overlap calibration gap for the deferred c1/c3/c4 follow-up audit.

### p003 flag — off-topic answer flagged by the grader (preserve)

p003 (an off-topic, gradable answer that pivots to PII/data-privacy
compliance) returned **all four criteria matching** — the grader
correctly marked every criterion not-met — but **flagged** it (`flagged
want=False got=True`). This is the grader exercising its own
system-prompt instruction to flag off-topic / unsuitable-to-grade
answers; it is **not** an H1 quote-grounding flag (not-met criteria
carry empty quotes, which skip the check).

**Disposition: preserve `flagged=false`.** Same documented
flag-unreliability pattern as Unit 14 p011 and Units 2/4/5/9/10: the flag
is not a reliable property of answer content, and per-criterion agreement
is the gate, not the flag axis. No realignment. One residual flag on 21
pairs is well inside tolerance.

---

## What this unlocks

Unit 15 (Safety + content moderation) is the fifteenth unit published on
the canonical *"LLM Systems for PMs"* path — **15 of 20 published** — and
**closes the Production block (Units 11–15)** and the v1 spine's
front-and-middle arc.

Per `docs/curriculum/v1-path-outline.md`, Units 16–20 (the Operating
block) are 🟧 placeholders, to be **locked from closed-beta signal**
before authoring. No unit is locked ahead of Unit 15 yet; the
one-unit-ahead buffer is the next thing to establish if authoring
continues into the Operating block.

The three preserved disagreements (p011/p021 c3 reproducible-strict, p003
flag) are inputs to the deferred c1/c3/c4 follow-up audit
(`docs/RUBRIC_AUDIT_C1_C4.md`), not action items for this gate.

---

## References

- Regression set: `content/regression-sets/safety-bundle-0.yml`.
- Unit: `content/units/safety-bundle-0.md` (status flipped `draft` →
  `published` in this PR).
- Grader: `backend/app/ai_service.py` (T2-D guardrails; H1 quote
  grounding).
- Quote-grounding fix recorded here: PR #202 (H1, flag-on-unverified) +
  PR #204 (token-overlap loosening that resolved the over-flagging).
- Runner: `backend/scripts/run_regression_set.py`.
- Gate procedure: `docs/REGRESSION_GATE.md`.
- Preserve-by-default / gold-standard principle:
  `docs/REGRESSION_GATE.md` § The grader vs. the gold standard.
