# Rubric Audit — c1 (framing) and c4 (regime/mapping) AND-Clause Bundling

**Status:** Inventory complete (step 1–3 below). Grader-diagnosis (step 4): the **HIGH tier is closed** — Units 10, 11, 12, 13 all run, all "leave, don't split" (no lenient bundling anywhere; see § Step-4 findings + § HIGH-tier closeout). The MEDIUM c1 units 5–9 remain operator-local and pending.
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
every split unit (positions 2–12), and remains **c3** in the two 3-criterion
units: tokenization (position 1, clean — never had a bundled c2) and multimodal
(position 13, split reverted at gate — coupled conjuncts, see
`RUBRIC_AUDIT.md:26`). Throughout this doc, "regime criterion" means that
criterion regardless of its index.

**c2-sweep coverage (for reference).** The criterion-2 sweep is complete. Units
2–7 and 10–12 split and merged; units 8 (cost-dynamics) and 9
(customization-trilemma) split and merged in PRs #167 / #168; unit 13 split
reverted at the gate (intentional). All of positions 2–12 are 4-criterion. No c2
gap remains.

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
| 8 | cost-dynamics (c4) | map lever → regime **AND** recognize production layers all three | **HIGH** |
| 9 | customization-trilemma (c4) | map approach → problem-shape **AND** recognize layering **AND** eng-cost / reversal-cost sequencing | **HIGH (→ CRITICAL-leaning)** |
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

---

## Step-4 findings

### Unit 10 (vector-search-rag) — c4 regime/signals criterion

**Run.** `run_regression_set vector-search-rag-bundle-0.yml --show-criteria`,
live Sonnet 4.6 grader, post-c2-split 4-criterion rubric. Gate **PASSED**:
76/84 (90%) per-criterion, 20/21 flagged-correct, 1 ERROR (p005, the known
stochastic payload bug — isolate-re-run to clear, no pair change).
Adjusted for the ERROR's 4 unscored criteria: 76/80 (95%).

**The c4 disagreements are all on the *second* conjunct, never the first.**
c4 = (i) *names a concrete signal per dimension* (recall@K/MRR; RAGAS
faithfulness; span-level source-quote validation) **AND** (ii) *recognizes
the PM error of treating any single signal as a sufficient proxy for overall
RAG quality.* Three pairs failed c4, all `expected=true / actual=false`
(grader-strict), and **all three satisfy conjunct (i)** — they each name a
signal per dimension. Every failure is on conjunct (ii).

**Implication: do NOT split c4.** The signal half is universally met;
splitting would isolate a conjunct that is never the point of disagreement.
This is the opposite of the c2 case. The inventory above scored Unit 10 c4
HIGH on the *expectation* of lenient bundling (credit the whole criterion off
signal-naming); the run **refutes that** — the grader does not lenient-credit,
it holds conjunct (ii) strictly. c4 stays a single 4th criterion.

**What it actually surfaced: a c4(ii) language-precision gap.** The criterion
names *one specific* PM error ("single signal as a sufficient proxy"), but
engaged answers naturally express a *valid neighboring* error — "don't collapse
to one composite score; keep per-dimension visibility." The Sonnet grader holds
to the literal wording and withholds credit; the Opus-authored expected values
credited the spirit. That wording mismatch — not separability, not author error
— is the real finding. Candidate fix for a future rubric pass: broaden c4(ii)
to accept the composite-blending framing as satisfying the PM-error
recognition, or explicitly scope it to the single-signal framing so authoring
and grader agree.

**Per-pair disposition** (no edits made on this single stochastic run;
preserve-by-default per § Recommended next step):

| Pair | Disagreement | Read | Disposition |
|---|---|---|---|
| p007 | c4 `true→false` | Signals all named; push-back is on a *composite score* ("each dimension fails independently, one number hides the problem") — the neighboring meta, not the literal single-signal-as-proxy | **Preserve `true`**; log Sonnet strict read as a c4(ii)-wording calibration gap |
| p008 | c4 `true→false` | Same shape as p007 (composite push-back + independence) | **Preserve `true`**; same gap |
| p021 | c4 `true→false` (run 1–2), `true→true` (run 3) | Signals named, but the answer asserts the single-upstream-cause misconception (*"if we get retrieval right, groundedness and citation faithfulness follow"*) — directly contradicts the independence c4(ii) requires | **Hold `c4=true` (preserve).** Isolated 2–3× re-run (2026-05-22) returned **2 `false` / 1 `true`** — non-reproducible, stochastic at the c4 margin (sub-0.8 confidence zone). The merits lean `false`, but the grader's own inconsistency shows this is genuinely *borderline*, not a *plain* author error — so preserve the Opus value and log as a stochastic calibration gap (same bucket as p007/p008). The re-runs vindicated the rule: realigning after the first two `false`s would have chased noise. |
| p011 | c3 `false→true` | Names only *two* dimensions (collapses citation faithfulness into "grounding"), then claims "the fixes are different" for that 2-way split | Grader **lenient** on c3's 3-way different-fixes meta. **Preserve `false`** (spec-faithful Opus value); realigning to match would degrade the gold standard. Log as a lenient calibration gap |

**Carry-forward for the inventory.** p011 is a fresh c3 (different-fixes meta)
lenient data point, distinct from the c4 cluster above. The c4(ii)
language-precision gap is the same bundling-fragility class the c1/regime
inventory predicts across units 5–13 — but here it manifests as
*grader-strict on a literal clause* + *neighboring-meta under-credit*, not the
lenient-credit the inventory forecast. Future step-4 runs on units 11–13 c4
should watch for the same strict/neighboring-meta shape, not only the lenient
one.

**Method note (the `--pair` re-run earns its keep).** p021's resolution is the
worked example for why borderline disagreements get an isolated 2–3× re-run
before any expected value is touched: run 1 and 2 said `c4=false`, run 3 said
`c4=true`. Acting on the first run — or even the first two — would have flipped
locked ground truth to chase grader noise. The discipline is: confirm
reproducibility with `run_regression_set … --pair <id>` (2–3×); realign only on
a *consistent* signal that also reads as a plain author error; otherwise
preserve and log the margin.

### Unit 11 (streaming-ux) — c1 (coupling) and c4 (map + PM-error)

**Run.** `run_regression_set streaming-ux-bundle-0.yml --show-criteria`, live
Sonnet 4.6 grader, 4-criterion rubric. **97% (82/84)**, zero ERRORs, 21/21
flagged-correct, 19/21 fully passed. Two FAILs, isolated and re-run via `--pair`
(2026-05-22).

**c4 → leave unsplit (confirms Unit 10).** Zero c4 disagreements across all 21
pairs. The c4 differentials behaved exactly as the criterion intends: p007
(maps the combination) and p021 (maps the surface) both returned `c4=false`
because the PM-error / full-mapping conjunct was absent — the grader **withholds
c4 on the second conjunct**, it does not lenient-credit off the
combination-mapping. This replicates the Unit 10 c4 result. The inventory scored
Unit 11 c4 HIGH on the expectation of lenient bundling; the run refutes it. **c4
stays a single 4th criterion.**

**c1 → leave unsplit; preserve p014 `c1=true`.** The headline Unit 11 finding.
c1 = (i) *names the three coupled decisions* AND (ii) *treats them as coupled*.
p014 (label: "names the three coupled decisions, **vague on coupling**, no
combination mapped") returned `c1 expected=true / actual=false` on the full-set
run AND the isolated re-run — and `UNIT_11_C2_SPLIT_GATE.md` already recorded
the same disagreement and deferred it here. That is **three consistent
observations**, not stochastic (contrast p021). Two consequences:

- By the audit's own method, a grader that *strictly* withholds the criterion
  when the second conjunct is weak is **separating the conjuncts cleanly →
  leave c1 unsplit** (split is only warranted for *lenient* bundling).
- On the expected value: this is the weaker Sonnet grader being *stricter* than
  the Opus author on a *borderline* ("vague") coupling. Per the gold-standard
  principle, a reproducible *stricter* read on a borderline is **not** grounds
  to flip the Opus `true` — reproducibility makes it a *cleaner documented
  calibration gap*, not a realign. **Preserve `c1=true`; log as a reproducible
  grader-strict-on-coupling gap.** (Note: a "c1-only" fixture whose c1 the
  grader reproducibly rejects is awkward — it reads as all-missed to the
  grader. If p014 should actually *exercise* c1-met, the fix is to strengthen
  the **answer's** coupling language, an authoring change — never to chase the
  grader by editing the expected value.)

**p011 `c2=true` (out of c1/c4 scope).** p011 (all-missed on-topic, "explicitly
decouples and dismisses recovery") returned `c2 expected=false / actual=true`
on both the full-set and isolated runs — a reproducible grader-*lenient* c2 read
crediting a mechanism on an all-missed answer. c2 was already swept, so this is
**record-only**: preserve `c2=false` (don't chase the lenient read), log as a
c2-lenient calibration gap. Worth a flag for any future c2 revisit, since a
reproducible lenient c2 on an all-missed pair is the confident-but-wrong shape
the project guards against.

**Net for Unit 11: no content/YAML change.** Same shape as Unit 10 — both
AND-clause criteria (c1, c4) stay unsplit; the disagreements are documented
calibration gaps, all preserve-by-default. **Two of two HIGH regime/c1 units
(10, 11) now say "leave, don't split."**

### Unit 13 (multimodal) — c1 and c3 (the 3-criterion / split-reverted unit)

**Note on numbering.** Unit 13 is one of the two 3-criterion units (its c2
split was *reverted* at the gate — coupled conjuncts, see `RUBRIC_AUDIT.md`
§ Rollout findings), so its regime criterion is **c3**, not c4. c2 (the coupled
mechanism + cost/eval criterion) is out of this audit's scope.

**Run.** `run_regression_set multimodal-bundle-0.yml --show-criteria` (live
Sonnet 4.6): **98% (62/63)**, zero ERRORs, 21/21 flagged-correct, 20/21 fully
passed. A single disagreement (p008 c3), isolated and re-run 3× via `--pair`.

**c1 (MEDIUM) → leave unsplit, no leniency.** c1 = (i) *names the three
approaches* AND (ii) *frames the decision as diagnosing task shape first*. p008
(the c1 differential: names only two approaches cleanly, hybrid left implicit)
returned `c1=false` — the grader did **not** lenient-credit "names three" off
the implicit hybrid or the strong shape-first frame. No leniency on c1.

**c3 (HIGH regime) → leave unsplit, no lenient bundling.** c3 = (i) *maps an
approach to a task shape + why* AND (ii) *names the "reach for the most capable
model" PM error*. p006 (the clean c3 differential: explains mechanism/cost-eval
but never maps the receipt to an approach) correctly returned `c3=false` — the
grader holds the mapping+PM-error conjuncts strictly. **Third HIGH regime/c1
criterion (after 10, 11) to refute the lenient-bundling forecast → leave.**

**The one disagreement — p008 `c3 expected=true / actual=false`, and what it
reveals (a c1↔c3 coupling).** Rock-solid reproducible: 4 consistent
observations (full-set + 3 isolated), every one `c3=false` — not stochastic
(contrast p021). On the merits p008 *does* map the right shape ("high-volume
mostly-stable-with-a-tail") to the right behavior ("extract first + escalate")
and names the PM error, so a fair reader credits c3. The grader withholds it
because p008 never cleanly *names* the hybrid (its c1 gap), so the c3 mapping
points at an approach the answer never established and reads as ungrounded — **the
c1 incompleteness couples into the c3 mapping judgment.** The criteria are not
fully independent for this answer shape.

- This is *strict coupling*, the **inverse** of lenient bundling — and strict
  behavior says *leave* (a split addresses leniency, not strictness).
- **Preserve `c3=true`** (don't realign). Reproducible *stricter* Sonnet read on
  a *borderline* (label-vs-substance: the approach is described but unnamed) is a
  documented calibration gap, not a plain author error — same disposition as
  Unit 11 p014. Reproducibility makes it a *cleaner* logged gap, not a license to
  flip the Opus value. (If p008 should cleanly earn c3, the fix is an *authoring*
  change — name the hybrid in the answer so c1 passes and the c3 mapping is
  grounded — never an expected-value edit.)

**Net for Unit 13: no content/YAML change.** c1 and c3 both stay unsplit. **Three
of three HIGH regime/c1 units (10, 11, 13) say "leave, don't split"** — the
inventory's lenient-bundling forecast is refuted across the whole HIGH tier so
far. The novel finding here is the **c1↔c3 coupling** (incomplete approach-naming
in c1 propagates into a withheld c3 mapping), logged for the record.

### Unit 12 (tool-use) — c4 (the last HIGH unit; closes the tier)

**Run.** `run_regression_set tool-use-bundle-0.yml --show-criteria` (live Sonnet
4.6): **98% (83/84)**, zero ERRORs, 21/21 flagged-correct, 20/21 fully passed.
A single disagreement (p018 c4), isolated and re-run via `--pair`.

**c4 (HIGH regime) → leave unsplit, no lenient bundling.** The c4 differentials
all held strict: p006 (names decisions + mechanism, never maps a combination),
p008 (gestures vaguely at the PM error instead of naming it), and p021 (stakes
mapping omits the why + PM error) **all correctly returned `c4=false`**. The
grader holds c4's map-by-stakes + PM-error conjuncts strictly — no lenient
bundling.

**p018 c4 → stochastic; preserve `c4=true`.** p018 ("all four met, short-form")
returned `c4=false` on the full-set run but `c4=true` on both isolated re-runs —
**1 false / 2 true, non-reproducible** (the p021 profile, not the reproducible
p014/p008 one). Reading the answer confirms the substance is present: it maps the
refund (high-stakes, irreversible) → narrow + strict + orchestrator recovery
*with the why*, and names the PM error ("just-define-the-functions inherits
broad/lenient/model-retries… force the recovery decision before launch"). The
full-set FAIL was a margin noise event. **Preserve `c4=true`; log as a stochastic
borderline** (same disposition as Unit 10 p021).

**Net for Unit 12: no content/YAML change. The HIGH tier is closed: 4 of 4
(10, 11, 12, 13) say "leave, don't split."**

### HIGH-tier closeout (10, 11, 12, 13)

The inventory scored the regime/c1 criteria of Units 10–13 HIGH on the
*expectation of lenient bundling* — that the grader would credit the whole
criterion off the first conjunct and silently drop the meta. **All four refute
that.** The grader holds the second conjunct strictly everywhere; not one HIGH
criterion lenient-bundled. So **none of the HIGH regime/c1 criteria are split
candidates** — splitting addresses leniency, and there is none here.

The grader's *actual* error mode across the tier is the **inverse — over-strictness**, and it appears in two flavours, both preserve-by-default (never realign down to chase the stricter Sonnet read):

- **Reproducible strict** on a weak/incomplete second conjunct or a compressed
  answer: Unit 11 p014 (c1, vague coupling, 3×), Unit 13 p008 (c3, unnamed-hybrid
  → ungrounded mapping, 4× — the c1↔c3 coupling).
- **Stochastic strict** at the margin: Unit 10 p021 (c4, 2F/1T), Unit 12 p018
  (c4, 1F/2T).

A practical implication for any future rubric work: because the failure mode is
strictness on terse/compressed answers, **adding more separately-scored
conjuncts (a split) would make it worse** — more boxes to tick in limited words.
This is independent evidence for leave-don't-split beyond the no-lenient-bundling
result. Out-of-scope side data point: Unit 11 p011 (c2 lenient on an all-missed
pair) remains logged for a future c2 revisit.

**Remaining step-4 work:** the MEDIUM c1 units (5–9), per the priority order —
expected to be even less split-prone than the HIGH tier, but gate-confirmed
the same way.
