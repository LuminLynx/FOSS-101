# Unit 14 Gate — Agents / multi-step reasoning

> **Per-unit gate audit** for `agents-bundle-0` (Phase 3, Unit 14).
> Mirrors the structure of `docs/UNIT_12_GATE.md` and prior
> per-unit gates.

---

## Decision

**Unit 14 PASSED 2026-05-24. Status flipped `draft` → `published`.**

The 21-pair regression set was run live against the deployed
Sonnet 4.6 grader on 2026-05-24 and hit **98.8% per-criterion
agreement (83/84)** on the first run — above the 80% publish
threshold. **Zero realignments, zero rewrites.** Two preserved
disagreements (p010 c3, p011 flag), both held to the gold standard
per `docs/REGRESSION_GATE.md`. Unit 14 was authored against the
post-c2-split 4-criterion rubric from the start, so no retroactive
split was needed.

| Criterion | Required | Run (21 pairs) | Verdict |
|---|---|---|---|
| Per-criterion agreement | ≥ 80% | **98.8% (83/84)** | ✅ |
| Honest flagged behavior | spec-faithful | 20/21 — p011 preserved disagreement | ✅ |
| Cost / call | reasonable | cache reads 93,320 ≈ 7× input; in-band | ✅ |

---

## Run evidence (2026-05-24)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6,
prompt caching on rubric per STRATEGY.md § T2-E).

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  19 (90%)
Per-criterion agreement:    83/84 (98%)
Flagged-correct:            20/21

Token usage (cost-relevant):
  input tokens:        13253
  cache reads:         93320
  output tokens:       14376
```

---

## Headline: the c1/c3 split is clean (no Unit 13-style fusion)

Unit 14 is a "three coupled decisions" unit, structurally identical
to Units 11 (streaming-ux) and 12 (tool-use). The standing risk for
this shape is the Unit 13 failure mode — the split-out meta-claim
criterion (c3, "the decisions constrain each other") collapsing into
c1 ("treats them as coupled"). Three watch-pairs were authored to
probe it: **p009, p011, p021** state generic coupling at the c1 level
without identifying the specific mutual constraint (p011 actively
dismisses termination as a framework detail).

**The grader returned `c3=false` on all three.** The constraint claim
is genuinely separable from naming/coupling on Unit 14, exactly as on
Units 11 and 12. The split is well-calibrated; it was not forced onto
coupled conjuncts.

The three surfaced-c3 pairs (p006, p008, p010 — which explain a
specific constraint) returned `c3=true` where intended (modulo the
p010 borderline below), confirming the criterion fires in both
directions.

---

## Triage of disagreements (two; both preserved, zero realignments)

### p010 c3 — reproducible grader-strict on a borderline (preserve)

Gate run scored p010 `crit=3/4`. Isolated `--pair p010
--show-criteria` re-run (2026-05-24, ×2) pinned the disagreement to
**c3, reproducibly** (`expected=true / actual=false` on both runs):

```
c1: expected=false actual=false [ok]
c2: expected=true  actual=true  [ok]
c3: expected=true  actual=false [MISS]
c4: expected=false actual=false [ok]
```

p010's answer *does* contain the canonical c3 constraint — *"once the
model directs the loop, only a guard outside the model can decide it
is actually done"* maps directly to c3's example (a model-directed
loop forces an external termination guard). But it is woven into a
mechanism-focused answer rather than stated cleanly as "the decisions
constrain each other," which is why the grader strict-reads it as
not-met.

**Disposition: preserve `c3=true`.** This is a reproducible *stricter*
Sonnet read on a *genuine borderline* — `docs/REGRESSION_GATE.md` is
explicit that this is **not** grounds to flip the Opus-authored value
down; reproducibility makes it a *cleaner documented calibration gap*,
not a realign. Same family as Unit 11 p014 (vague-coupling c1) and
Unit 13 p008 (unnamed-hybrid c3). The gold standard stays above the
weaker grader. Logged as a reproducible c3-strict calibration gap for
the deferred c1/c3/c4 follow-up audit.

### p011 flag — known flag-unreliability (preserve)

p011 (the watch-pair that dismisses termination as a framework
detail) returned **all four criteria matching** — including the
target `c3=false` — but the grader **flagged** it (`flagged
want=False got=True`).

**Disposition: preserve `flagged=false`.** This is the documented
flag-unreliability pattern observed across Units 2/4/5/9/10: the flag
does not fire as a reliable property of answer content, and per-
criterion agreement is the gate, not the flag axis. The grader flagged
a confidently-dismissive single-criterion answer as "review needed";
the criterion judgments are all correct. No realignment.

---

## What this unlocks

Unit 14 (Agents / multi-step reasoning) is the fourteenth unit
published on the canonical *"LLM Systems for PMs"* path — **14 of
20 published.** Per `docs/curriculum/v1-path-outline.md`, **Unit 15
(Safety + content moderation)** is already locked (title, trade-off,
prereqs 7/12/14, position rationale) and is the next unit to author,
satisfying the one-unit-ahead lock buffer that Unit 14 itself
required.

The two preserved disagreements (p010 c3 reproducible-strict, p011
flag) are inputs to the deferred c1/c3/c4 follow-up audit
(`docs/RUBRIC_AUDIT_C1_C4.md`), not action items for this gate.

---

## References

- Regression set: `content/regression-sets/agents-bundle-0.yml`
  (authored as PR #191; gate-passed and published in this PR).
- Unit: `content/units/agents-bundle-0.md` (status flipped
  `draft` → `published` in this PR).
- Grader: `backend/app/ai_service.py` (T2-D guardrails).
- Runner: `backend/scripts/run_regression_set.py`.
- Authoring blueprint / template: `docs/UNIT_12_GATE.md` +
  `docs/UNIT_11_GATE.md` (the "three coupled decisions" references).
- Gate procedure: `docs/REGRESSION_GATE.md`.
- Preserve-by-default / gold-standard principle:
  `docs/REGRESSION_GATE.md` § The grader vs. the gold standard.
