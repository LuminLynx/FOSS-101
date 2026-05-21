# Rubric Audit — c1 (framing) and c4 (regime/mapping) AND-Clause Bundling

**Status:** Inventory complete (step 1–3 below). Grader-diagnosis (step 4) is operator-local and pending.
**Date:** 2026-05-21
**Scope:** All 13 published / authored units in `content/units/`
**Predecessor:** `docs/RUBRIC_AUDIT.md` (the criterion-2 sweep). This is the deferred c1/c3 follow-up promised there (`RUBRIC_AUDIT.md:13`, `:91`).

## Why this doc exists

The c2 audit found one recurring grader failure: when a single criterion ANDs
two substantive checks, the Sonnet 4.6 grader satisfies the first conjunct,
marks the criterion `met: true`, and reports **high confidence** — even when the
learner missed the second. That is the worst calibration shape the project
guards against: confident-but-wrong judgements. The c2 sweep flagged — but
explicitly deferred — the same pattern in **c1** ("9 of 13 units") and the
**regime criterion** ("7 of 13"). This doc runs that inventory.

**Numbering note.** The c2 split renumbered the split units' rubrics 3 → 4. The
"regime / mapping" criterion the original audit called *c3* is now **c4** in
every split unit, and remains **c3** in the units that were never split
(tokenization, cost-dynamics, customization-trilemma, multimodal). Throughout
this doc, "regime criterion" means that criterion regardless of its index.

---

## Discovered during inventory — the c2 sweep is incomplete

While extracting rubrics I found that **units 8 (cost-dynamics) and 9
(customization-trilemma) were never split** — both still carry the bundled c2
("identifies a concrete failure mode … AND explains/names the mechanism") and
have 3 criteria, confirmed against their regression sets (no `position: 4`).
The original audit table (`RUBRIC_AUDIT.md:82-83`) marked both `SPLIT`, and the
prior summary described the sweep as covering "2–9".

Actual c2-split coverage:

| Range | Status |
|-------|--------|
| 2, 3, 4, 5, 6, 7 | split, merged |
| 10, 11, 12 | split, merged |
| 13 | split **reverted** at gate (coupled conjuncts — intentional) |
| **8, 9** | **never split — bundled c2 still live** |

Unit 13 staying 3-criterion is by design. Units 8 and 9 are a **gap**, not a
decision. This is a c2-rollout item, not part of the c1/c4 audit — flagged here
for visibility; recommend a separate decision on whether to finish 8/9 before
or after this c1/c4 work. **Not actioned in this doc.**

---

## Method (mirror of the c2 audit)

For each unit's c1 and regime criterion:
1. Extract the clause verbatim from the YAML frontmatter.
2. Decompose into independent conjuncts a learner could meet or miss separately.
3. Score severity (same rubric as the c2 audit, below).
4. **(Operator-local, pending)** For MEDIUM+ criteria, run the live Sonnet 4.6
   grader against a *differential pair* — an answer that satisfies one conjunct
   but not the other — and confirm whether the grader marks the criterion met
   leniently. Split only where the evidence shows lenient grading; leave bundled
   where the conjuncts are genuinely coupled (the Unit 13 lesson).

Severity rubric (unchanged from `RUBRIC_AUDIT.md`):

| Severity | Definition |
|---|---|
| **NONE** | No AND, or AND joins paraphrastic clauses (same idea restated). |
| **MEDIUM** | Two substantive conjuncts of the same kind. |
| **HIGH** | Two conjuncts of *different kinds* — one concrete, one meta (a framing reframe, a coupling claim, a downstream consequence, a "common PM error"). The meta-claim is the more nuanced concept and the most likely to be silently missed. |
| **CRITICAL** | Three or more independent substantive conjuncts. |

---

## Findings summary — c1 (the framing criterion)

| # | Unit | c1 conjuncts | Severity |
|---|------|--------------|----------|
| 1 | tokenization | single claim ("token count drives cost/latency/ceiling") — list, no AND | **NONE** |
| 2 | context-window | name 3-way trade-off **AND** treat window as a budget not a capacity | **MEDIUM** |
| 3 | latency | name multi-axis trade-off **AND** treat user-perceived speed as load-bearing | **MEDIUM** |
| 4 | evals | name multi-method choice **AND** treat as layered strategy **AND** recognize no single method suffices | **HIGH** |
| 5 | model-selection | name multi-axis **AND** treat as a measurement decision anchored to load-bearing task at scale | **HIGH** |
| 6 | prompt-design | name multi-axis **AND** treat as a behavior-shaping contract anchored to spec needs | **MEDIUM** |
| 7 | hallucination | frame as structural base-rate problem **AND** treat reliability as multi-axis discipline anchored to cost-of-failure | **HIGH** |
| 8 | cost-dynamics | name multi-axis (distinct from per-call) **AND** anchor to annualized cost at projected volume | **MEDIUM** |
| 9 | customization-trilemma | name the trilemma → problem-shape mapping **AND** treat diagnosing the failure mode as the load-bearing first step | **HIGH** |
| 10 | vector-search-rag | name 3 independent dimensions **AND** treat them as separately measurable | **MEDIUM (mildest — near-paraphrastic)** |
| 11 | streaming-ux | name 3 coupled decisions **AND** treat them as coupled (combination produces the feel) | **HIGH** |
| 12 | tool-use | name 3 coupled decisions **AND** treat them as coupled (combination sets the feel) | **HIGH** |
| 13 | multimodal | name 3 approaches **AND** frame the decision as diagnosing task shape first | **MEDIUM** |

**c1 read:** 12 of 13 contain a substantive AND (tokenization is clean;
vector-search-rag is the mildest, nearly paraphrastic). The dangerous shape is
the HIGH group (4, 5, 7, 9, 11, 12), where the second conjunct is a *framing
reframe* of different kind from "name the axes" — a learner can correctly
enumerate the trade-off axes while completely missing the reframe (budget /
measurement-decision / base-rate / coupling), and the grader is liable to credit
the whole criterion off the enumeration alone.

> My count (12) is higher than the original audit's "9 of 13" because I score
> the HIGH units' coupling/framing clauses as substantive ANDs. Reasonable
> graders differ on the borderline MEDIUMs; the operator grader run (step 4)
> resolves which actually mis-grade.

## Findings summary — the regime / mapping criterion

| # | Unit | regime-criterion conjuncts | Severity |
|---|------|----------------------------|----------|
| 1 | tokenization (c3) | single distinguish: when-it-matters vs. when-ignorable | **NONE** |
| 2 | context-window (c4) | single distinguish: long-window-default regime vs. forces-retrieval regime | **NONE** |
| 3 | latency (c4) | single distinguish: stream-by-default regime vs. don't-stream regime | **NONE** |
| 4 | evals (c4) | map method → regime (one classification task across 4 methods) | **NONE / LOW** |
| 5 | model-selection (c4) | map class → regime **AND** recognize the cross-tier hybrid pattern | **HIGH** |
| 6 | prompt-design (c4) | map approach → regime **AND** recognize the hybrid PM-default | **HIGH** |
| 7 | hallucination (c4) | map approach → regime **AND** recognize high-stakes needs all three layered | **HIGH** |
| 8 | cost-dynamics (c3) | map lever → regime **AND** recognize production layers all three | **HIGH** |
| 9 | customization-trilemma (c3) | map approach → problem-shape **AND** recognize layering **AND** eng-cost / reversal-cost sequencing | **HIGH (→ CRITICAL-leaning)** |
| 10 | vector-search-rag (c4) | name a signal per dimension **AND** recognize the PM-error of single-signal-as-proxy | **HIGH** |
| 11 | streaming-ux (c4) | map combination → surface + why **AND** recognize the "add streaming is one decision" PM-error | **HIGH** |
| 12 | tool-use (c4) | map combination → surface by stakes + why **AND** recognize the "tool use is one decision" PM-error | **HIGH** |
| 13 | multimodal (c3) | map approach → task-shape + why **AND** recognize the "reach for most capable model" PM-error | **HIGH** |

**Regime read:** a clean two-tier split.
- **Units 1–4: clean.** A single distinguish/mapping judgment, no appended
  meta. No action.
- **Units 5–13 (9 units): bundled.** Every one appends a *meta-recognition* to
  the mapping — "…AND recognizes the hybrid default / layering / common PM
  error." This is the exact c2 failure shape transplanted to the last
  criterion: a learner who maps the regimes competently but never states the
  hybrid/layering/PM-error insight should miss the meta-conjunct, yet the grader
  is liable to credit the whole criterion off the mapping alone. The "common PM
  error" clause in the HIGH units (10–13) is the most load-bearing meta-claim in
  the curriculum and the most likely to be silently dropped.

---

## Recommended next step (operator-local)

The inventory is the cheap part. The expensive, evidence-gated part is the
grader run, which needs the Claude API (local-only per project setup). For each
MEDIUM+ criterion above, the operator should:

1. Pull or author a **differential pair** — an answer that clearly satisfies the
   first conjunct (names the axes / maps the regimes) but clearly fails the
   second (no reframe / no meta-recognition). Several existing regression pairs
   already isolate one conjunct; those are the cheapest starting evidence.
2. Run it through the live grader 2–3× (the grader is stochastic).
3. Record whether the criterion comes back `met: true` with high confidence. If
   it does, that confirms lenient bundling and the criterion is a split
   candidate; if the grader already splits the conjuncts cleanly, leave it.

**Split only on evidence.** Each split renumbers the rubric again (4 → 5 in the
split units) and forces another full pass of expected-vector edits *and* label
reconciliation across that unit's regression set — the same churn the c2 sweep
absorbed twice. The Unit 13 revert is the standing reminder that not every AND
is a bad AND: some join genuinely-coupled concepts the grader can't separate.

**Preserve-by-default on any split that proceeds:** faithfully decompose the
existing Opus-authored expected values; never realign the gold standard to chase
the weaker grader; document Sonnet disagreements as calibration gaps.

## Priority order if the work proceeds

1. **Regime criterion, HIGH units 10–13** — the "common PM error" meta-claim is
   the highest-value, most-droppable concept; start here.
2. **Regime criterion, units 5–9** — the hybrid/layering meta-recognition.
3. **c1 HIGH units (4, 5, 7, 9, 11, 12)** — the framing reframe.
4. **c1 / regime MEDIUMs** — only if step-4 evidence shows lenient grading.

The MEDIUM c1s and the near-paraphrastic vector-search-rag c1 may well be
"LEAVE" once the grader is run; do not pre-commit to splitting them.
