# Rubric Audit — Criterion-2 AND-Clause Bundling

**Status:** Draft (proposal, no rubric changes executed)
**Date:** 2026-05-20
**Scope:** All 13 published / authored units in `content/units/`
**Owner:** Founder sign-off required before any rubric version bump

---

## Context

Every gate document since `UNIQUE_2_GATE.md` flags the same recurring grader failure: criterion 2 (and to a lesser extent c1, c3) bundles multiple substantive checks under a single "X AND Y" clause. The grader meets X, declares the criterion `met: true`, and reports high confidence — even when the learner missed Y. This produces the worst calibration shape we care about: **high confidence on wrong judgements**.

The `UNIT_12_GATE.md` post-mortem documents the fix that worked for Unit 12 (`tool-use`) and Unit 13 (`multimodal`): **strict-AND pair authoring** — every regression pair authored so `expected.met:true` only when both conjuncts hold. That discipline let Unit 12 hit 100% on its re-run with zero rewrites.

This audit takes the next step: instead of fixing the *pair-authoring side*, we tighten the *rubric side* so future grader runs (and future authors writing regression pairs) face less ambiguity by construction.

**Non-goal:** retroactive grade reinterpretation. The `rubrics` table is version-scoped (`UNIQUE (decision_prompt_id, version)` per `migrations/018`). A rubric edit bumps the version; prior `grades` rows continue to reference their original `rubric_criteria.id` and remain intact.

---

## Method

For each unit's `rubric.text[1]` (criterion 2):
1. Extract verbatim from the YAML frontmatter.
2. Decompose into independent conjuncts a learner could meet or miss separately.
3. Score severity by conjunct count and conjunct *kind* (substantive failure-mode vs. meta-claim).
4. Recommend `SPLIT` (two criteria), `TIGHTEN` (rephrase to remove ambiguity), or `LEAVE` (AND is paraphrastic, not load-bearing).

### Severity rubric

| Severity | Definition |
|---|---|
| **NONE** | No AND, or AND joins paraphrastic clauses (same idea restated). |
| **MEDIUM** | Two substantive conjuncts of the same kind (e.g., "name failure + explain mechanism"). Grader lenient-met risk is real but well-defined. |
| **HIGH** | Two conjuncts of *different kinds* — one concrete (failure/mechanism), one meta (a constraint, a coupling, a downstream consequence). Meta-claim is the more nuanced concept and is the most likely to be silently missed. |
| **CRITICAL** | Three or more independent substantive conjuncts. (No unit hits this in c2 today.) |

---

## Findings summary

| # | Unit | Position | Severity | Pattern | Recommendation |
|---|------|----------|----------|---------|----------------|
| 1 | tokenization | 1 | NONE | No AND | — |
| 2 | context-window | 2 | MEDIUM | failure-mode + mechanism | SPLIT |
| 3 | latency | 3 | MEDIUM | failure-mode + mechanism | SPLIT |
| 4 | evals | 4 | MEDIUM | failure-mode + mechanism | SPLIT |
| 5 | model-selection | 5 | MEDIUM | failure-mode + mechanism | SPLIT |
| 6 | prompt-design | 6 | MEDIUM | failure-mode + mechanism | SPLIT |
| 7 | hallucination | 7 | MEDIUM | failure-mode + mechanism | SPLIT |
| 8 | cost-dynamics | 8 | MEDIUM | failure-mode + mechanism | SPLIT |
| 9 | customization-trilemma | 9 | MEDIUM | failure-mode + mechanism | SPLIT |
| 10 | vector-search-rag | 10 | HIGH | mechanism + meta-claim ("different fixes") | SPLIT (or move meta to c3) |
| 11 | streaming-ux | 11 | HIGH | mechanism + meta-claim ("decisions constrain each other") | SPLIT |
| 12 | tool-use | 12 | HIGH | mechanism + meta-claim ("decisions constrain each other") | SPLIT |
| 13 | multimodal | 13 | HIGH | mechanism + meta-claim ("cost/eval consequence") | SPLIT |

**Shape of the problem:** a two-tier picture — 8 units (positions 2–9) with the "failure-mode + mechanism" bundle, and 4 units (positions 10–13) with the more dangerous "mechanism + meta-claim" bundle. Unit 1 is clean. None of the criteria are critical-severity (no c2 has 3+ independent conjuncts).

**Out of scope** for this audit but flagged here: criterion 1 also uses AND in 9 of 13 units (treating X as a multi-axis trade-off AND anchoring to the load-bearing decision), and criterion 3 uses AND in 7 of 13 units (typically classification scheme AND a meta-recognition). A follow-up audit should treat c1 and c3 on the same framework.

---

## Per-unit findings

### Unit 1 — tokenization (position 1) — NONE

> Names a concrete scenario where tokenization choice changes a product outcome (e.g. non-English text, code, emoji, or names inflating token counts vs. expectations).

Single-conjunct criterion. No change recommended.

---

### Unit 2 — context-window (position 2) — MEDIUM, SPLIT

> Identifies a concrete failure mode of "just use the long window" beyond hitting the hard limit, **AND** identifies the mechanism behind it — e.g., per-call cost scales linearly because input tokens are priced linearly; effective recall can degrade mid-context, as documented in long-context recall evals; single-document architectures break under multi-document or growing-state load because retrieval and summarization weren't designed in.

**Conjuncts:**
- (a) Names a failure mode beyond "hits the hard limit".
- (b) Explains the mechanism behind it.

**Risk:** A learner can name "cost goes up" or "recall degrades" (a) without articulating *why* (b: input tokens priced linearly; long-context recall evals show degradation mid-context).

**Recommended split:**
- c2: *Names a concrete failure mode of "just use the long window" beyond hitting the hard limit (e.g., per-call cost scaling, mid-context recall degradation, or architectural break under multi-document load).*
- c3 (new): *Explains the mechanism behind the named failure mode — why it happens, not just that it happens.*
- Existing c3 (regime distinction) becomes c4. Rubric grows from 3 to 4 criteria.

---

### Unit 3 — latency (position 3) — MEDIUM, SPLIT

> Identifies a concrete failure mode of treating latency as a single number **AND** explains the mechanism behind it — e.g., picking a fast model that costs accuracy on the load-bearing task; shipping streaming UX where the output is consumed atomically (so partial output is unusable); optimizing TTFT when total completion is what gates the user's next action.

**Conjuncts:** failure mode (single-number treatment) + mechanism.

**Recommended split:** same pattern as Unit 2.

---

### Unit 4 — evals (position 4) — MEDIUM, SPLIT

> Identifies a concrete failure mode of choosing one eval method exclusively **AND** explains the mechanism — e.g., golden-set passes don't catch unknown regressions; LLM-as-judge shares blind spots with the model being evaluated; CSAT lags real failures by days; human eval is small-N and slow.

**Conjuncts:** failure mode + mechanism.

**Recommended split:** same pattern.

---

### Unit 5 — model-selection (position 5) — MEDIUM, SPLIT

> Identifies a concrete failure mode of single-axis model selection **AND** explains the mechanism — e.g., picking by cost alone misses capability-tier breakpoints where the load-bearing-task quality collapses; picking by capability alone hits cost-at-volume cliffs because per-call cost scales linearly; picking by vendor benchmark misses regressions on the load-bearing task that only show up in your own eval.

**Conjuncts:** failure mode + mechanism. Specifically flagged in `UNIT_5_GATE.md` as one of the recurring c2 leniency cases.

**Recommended split:** same pattern.

---

### Unit 6 — prompt-design (position 6) — MEDIUM, SPLIT

> Identifies a concrete failure mode of single-axis prompt design **AND** explains the mechanism — e.g., instruction-only misses edge cases that examples catch; few-shot-only stales when data distribution shifts; putting per-query variability in the system prompt breaks prompt caching and inflates cost-per-call.

**Conjuncts:** failure mode + mechanism.

**Recommended split:** same pattern.

---

### Unit 7 — hallucination (position 7) — MEDIUM, SPLIT

> Identifies a concrete failure mode of single-axis hallucination management **AND** explains the mechanism — e.g., mitigation-only collapses at scale because a "small" rate compounds with volume (0.5% × 10k calls/day = 50 false outputs/day); detection-only without mitigation is just measuring the bleed; containment-only without detection means failures go uncounted.

**Conjuncts:** failure mode + mechanism.

**Recommended split:** same pattern.

---

### Unit 8 — cost-dynamics (position 8) — MEDIUM, SPLIT

> Identifies a concrete failure mode of single-axis cost optimization **AND** explains the mechanism — e.g., enabling caching alone misses the batch-API discount; batch-only without caching pays full input rate on every batched call; committed-use without volume forecasting commits to a higher floor than usage warrants.

**Conjuncts:** failure mode + mechanism.

**Recommended split:** same pattern.

---

### Unit 9 — customization-trilemma (position 9) — MEDIUM, SPLIT

> Identifies a concrete failure mode of mis-matching approach to problem shape **AND** names the mechanism behind it — e.g., fine-tuning a knowledge gap (training shifts behavior distribution rather than loading facts); RAG-ing a behavior issue (retrieval injects content but doesn't change interpretation); prompting a knowledge gap (no amount of instruction retrieves information the model doesn't have).

**Conjuncts:** failure mode + mechanism.

**Recommended split:** same pattern. The unit-9 examples are *particularly tight* (mechanism is genuinely inseparable from the failure-mode statement), so splitting may feel pedantic; even so, splitting makes the grader's per-conjunct judgement explicit and removes the lenient-met failure mode.

---

### Unit 10 — vector-search-rag (position 10) — **HIGH**, SPLIT

> Explains the mechanism behind a failure on at least one of the three dimensions — e.g., recall failure as chunks not being retrieved; groundedness failure as the model not anchoring on the retrieved context; citation-faithfulness failure as the cited passage not actually supporting the claim — **AND identifies that the three failure modes need different fixes**.

**Conjuncts:**
- (a) Explain mechanism for at least one dimension.
- (b) **Meta-claim:** the three failure modes need *different* fixes.

**Risk:** Conjunct (b) is a different *kind* of claim — it's about cross-dimension non-substitutability. A learner can explain (a) cleanly and never reach (b). The grader will read this as a met-criterion because (a) is satisfied.

**Recommendation:** SPLIT. Move the meta-claim out of c2.

**Two options:**
- Option A: New criterion (c2 = mechanism for one dimension; c3 = different-fixes meta; existing c3 about signals becomes c4). Rubric grows from 3 to 4.
- Option B: Move the meta-claim into existing c3 — c3 already talks about per-dimension signals; the "different fixes" insight naturally fits there. Rubric stays at 3 criteria.

**Recommended:** Option B (smaller schema delta, c3 already classifies by dimension).

---

### Unit 11 — streaming-ux (position 11) — **HIGH**, SPLIT

> Explains the mechanism behind a mismatched-combination failure — e.g., token-streaming a structured output into a continuous-flow render shows half-parsed garbage; or no recovery decision being made at all freezes the UI — **AND identifies that the decisions constrain each other** (e.g., continuous-flow rendering rules out partial-accept recovery; token streaming makes partial-accept incoherent).

**Conjuncts:**
- (a) Explain mechanism behind a mismatched combination.
- (b) **Meta-claim:** the decisions constrain each other.

**Risk:** identical shape to Unit 10. The "decisions constrain each other" insight is *the* load-bearing pedagogical move of this unit (it's why the three coupled-decisions abstraction exists), and burying it as a conjunct of c2 hides it.

**Recommendation:** SPLIT. Promote the coupling meta-claim to its own criterion.

---

### Unit 12 — tool-use (position 12) — **HIGH**, SPLIT

> Explains the mechanism behind a mismatched-combination failure — e.g., broad lenient tools with orchestrator-only recovery let errors fall through silently; or strict narrow tools with model-side recovery on a destructive call let the model retry something dangerous — **AND identifies that the decisions constrain each other** (lenient schema implies model-side recovery; an open-ended tool surface cannot be deterministically wrapped).

**Conjuncts:** identical shape to Unit 11 (mechanism + coupling meta-claim).

**Recommendation:** SPLIT. Note Unit 12 passed its gate cleanly under strict-AND pair-authoring discipline, so the *grading* impact may be small. Splitting still buys consistency with Units 10/11 and reduces dependence on regression-pair authoring discipline alone.

---

### Unit 13 — multimodal (position 13) — **HIGH**, SPLIT

> Explains the mechanism behind an approach/shape mismatch — a VLM on a barcode or a fixed-format form overpays and loses evaluability for zero capability gain, while a brittle classical-CV pipeline on open-ended understanding does not degrade gracefully — **AND identifies the cost/eval consequence**, that image tokens are a distinct and often dominant cost line and that free-text output over arbitrary images is hard to evaluate.

**Conjuncts:**
- (a) Mechanism behind approach/shape mismatch.
- (b) **Meta-claim:** cost/eval consequence (image tokens dominate; free-text output over arbitrary images is hard to evaluate).

**Recommendation:** SPLIT. Same reasoning as Units 10–12. Multimodal was authored after the strict-AND pair-authoring lesson, but the rubric still bundles. Splitting now (before publish) is cheaper than splitting after.

---

## Rollout order

Recommended sequencing once approach is approved:

1. **Pilot — Unit 10 (vector-search-rag).** It's the cleanest case of the HIGH-severity pattern *and* it has an Option-B fix (move meta-claim into existing c3) that doesn't grow the rubric. Validates both the split mechanic and the "move meta to c3" mechanic in one PR. Run its regression set before/after.
2. **HIGH-severity batch — Units 11, 12, 13.** Same shape; once Unit 10 is signed off, batch the remaining three. Each gets a rubric version bump + regression-set re-run.
3. **MEDIUM-severity batch — Units 2–9 (eight units).** Mechanical split. Likely doable in a single PR with regression-set re-runs across the batch.

**Rubric versioning:** each unit's `rubrics.version` bumps from 1 → 2. Prior grades (if any in dev DB) continue to reference v1's `rubric_criteria.id` rows via the FK; no data migration needed.

**Schema impact:** all changes are in `content/units/*.md` frontmatter + re-ingest via `scripts/ingest_units.py`. No backend code changes required (the grading service reads criteria positions, not hardcoded counts).

**Regression-set impact:** rubrics that grow from 3 → 4 criteria require updating every pair's `expected.criteria` list in the matching `regression-sets/*.yml`. The 8 MEDIUM-severity splits create the most authoring work (8 × ~20 pairs each, ~160 pair updates). Units 11–13 (if Option B chosen for Unit 10) stay at 3 criteria; only the wording of c2/c3 changes — no pair updates needed beyond re-affirming the per-criterion `met` values.

---

## Open questions

1. **Rubric growth tolerance.** Are we willing to grow the rubric from 3 → 4 criteria for the MEDIUM-severity units, or should we instead TIGHTEN the wording (drop the AND, keep one substantive check, push the other into the unit's "Depth" prose as a teaching point but not a graded criterion)? Tightening is lower-touch but trades some pedagogical signal.

2. **Backfill stance.** Do we re-run *all 13* regression sets against the new rubric versions before merging, or only the units whose criterion text changed? Mixed answer: MEDIUM splits change pair structures, so re-runs are necessary; HIGH splits via Option B may not change grader behavior much, so a spot-check may suffice.

3. **Criterion 1 and 3 follow-up.** This audit defers c1 and c3 AND-clause review. Should I produce a parallel audit doc next, or fold the c1/c3 fixes into the same unit PRs to minimize regression-set churn? (My recommendation: separate doc, single batch later — keeps reviewability tractable.)

4. **Rubric metadata signal.** Should we add a `criterion_kind: failure_mode | mechanism | meta_claim | classification` field to the criterion schema so future authoring lints can flag bundled-kind criteria automatically? (Out of scope here; raising for memory.)

---

## Sign-off

This is a proposal, not an executed change. Before any unit's rubric is edited:

- [ ] Approach approved (split vs. tighten; Option A vs. B for Unit 10).
- [ ] Rollout order confirmed.
- [ ] Regression-set re-run budget acknowledged (LLM call cost: ~$0.009 × ~160 pairs ≈ $1.50 for the full sweep; trivial).
- [ ] Open question #3 (c1/c3 follow-up) answered.

Once signed off, work proceeds on the dev branch unit-by-unit with PRs per unit (or per batch), CI green required, and a final consolidated PR refresh.
