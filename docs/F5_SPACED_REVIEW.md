# F5 — Spaced Review Scheduler (Design, decisions locked)

> **What this is.** The decision-locking design doc for F5 (Loop
> step 6, "Return") — the same "lock decisions in F5_*.md before
> implementation" discipline `docs/PHASE_3_4_ROADMAP.md` mandates
> for F6. Implementation PRs do not begin until this doc is
> approved.
>
> **Grounding.** Every decision below is tied to quoted text in
> `docs/STRATEGY.md` / `docs/EXECUTION.md` or to the verified
> schema in `backend/db/migrations/020_add_review_schedule.sql`.
> Where the source underspecifies, that is stated explicitly and
> the doc-faithful default is taken — nothing is inferred
> silently.

---

## Why F5 now

F5 is documented Phase-3 engineering that runs **in parallel**
with unit authoring, not after Unit 20 — `PHASE_3_4_ROADMAP.md`:
*"engineering has slack capacity that should be used for
F5/F6/… in parallel with the remaining … unit-authoring
cycles."* It never started. It is the only step of the documented
six-step loop with **zero implementation**: `review_schedule`
exists as a shape-only table (migration 020) and no application
code references it. STRATEGY is explicit on stakes:

> *"Spaced review. In v1. Without it, the path evaporates."* —
> `STRATEGY.md:118`

The loop does not close as a system today: a learner finishes a
unit and nothing brings them back.

## Verified scaffold (migration 020)

`review_schedule`: `id, user_id→users, unit_id→units, due_at
TIMESTAMPTZ, interval_days INTEGER DEFAULT 1 CHECK(≥0),
last_reviewed_at, created_at, updated_at, UNIQUE(user_id,
unit_id)`, index `(user_id, due_at)`. The migration comment
states the algorithm was deliberately deferred and "whichever
algorithm Phase 3 picks must populate `due_at`, `interval_days`,
`last_reviewed_at`." There is **no ease-factor / stability /
difficulty column** — a fact that constrains the algorithm
choice below.

---

## Locked decisions

### D1 — Algorithm: interval-doubling

`1 → 3 → 7 → 21 → 60` days, then capped at 60. Chosen over SM-2
and FSRS on three independent, documented grounds:

1. **Schema fit.** Interval-doubling needs only
   `interval_days`/`due_at`/`last_reviewed_at` — the exact
   columns migration 020 provides. **Zero new migration.** SM-2
   needs an ease-factor column + a learner quality 0–5 input;
   FSRS needs stability/difficulty state — each a new migration.
2. **Audience / strategy.** SM-2 and FSRS require a learner
   self-rated recall signal. `STRATEGY.md:117` —
   *"Multiple-choice and **self-grade are explicitly
   rejected**."* and `:123` excludes Duolingo-flavored
   mechanics. The quality-rating UI those algorithms need is
   precisely the excluded friction.
3. **Documented lean.** `PHASE_3_4_ROADMAP.md` Track 1 already
   names interval-doubling the *"honest default for v1."*
   `EXECUTION.md:157` permits any of the three; this one is the
   only one simultaneously schema-ready, audience-appropriate,
   and doc-aligned.

SM-2/FSRS are recorded as **post-launch optimizations**, not v1
(see Deferred).

### D2 — A review is a re-surface, not a re-grade

`STRATEGY.md:103` defines a session as *"1 unit + decision
prompt + **maybe a review**"* — listing the decision-prompt+grade
cycle and "a review" as **separate** components. `:113` calls
step 6 *"Spaced review of older units."* The source **never**
states a review re-runs the decision prompt through the grader.

**Locked (doc-faithful default):** a review re-surfaces the older
unit's **bite + calibration/canonical content** (the concept and
its trade-off, plus what's settled/contested) — it does **not**
re-invoke the LLM grader and does **not** create a new
`completion`/`grade` cycle. Rationale, all doc/cost grounded:
the session definition lists the grade cycle separately; re-grade
would add an Anthropic grader call **per review** (a recurring
per-user cost the docs never budget for); self-grade is
explicitly rejected so a review also cannot be a self-rated
recall check.

> **Flagged, not inferred:** STRATEGY underspecifies the *in-review
> interaction*. "Re-test on review" (re-run the decision prompt →
> grader) is a coherent product idea but is an **addition beyond
> documented scope** with a recurring cost implication. It is
> **out of F5 v1** and recorded as an open question for an
> explicit future decision — not silently adopted or rejected.

### D3 — Write trigger: seed on completion, +1 day

On a successful `POST /api/v1/completions` (the existing
idempotent `record_completion` path), upsert one
`review_schedule` row for `(user_id, unit_id)` with
`interval_days = 1`, `due_at = now() + 1 day`,
`last_reviewed_at = NULL`. Grounded: `STRATEGY.md:113` —
reviews *"show up **the next day**."* Idempotent and
`UNIQUE(user_id, unit_id)`-safe: re-completing a unit does not
duplicate or reset an existing schedule row (first-write-wins;
re-completion is already idempotent upstream).

### D4 — Interleave: new unit is the spine, review is alongside/optional

`STRATEGY.md:103` — *"1 unit + **maybe** a review"*; `:113` —
*"alongside the next new unit."* **Locked:** the "Continue"
surface presents the **next new unit as primary**; if ≥1 review
is due, it surfaces **alongside** as an optional affordance — not
a gate, not forced before new content. This *corrects an earlier
working lean* ("review-priority-then-new"), which contradicted
the source. When no new unit remains (path complete), due reviews
become the primary Continue surface (consistent with `:113` and
the path-complete state in the design exploration).

### D5 — Read endpoint

`GET /api/v1/review-schedule?due_before=<ISO8601, default now>`
→ list of due units for the authenticated user (unit id, slug,
title, due_at, interval_days), ordered by `due_at` then unit
`position`. `AUDIT.md` § 2.4 flagged this as a required new
endpoint. Auth: same JWT middleware as `/api/v1/completions`.

### D6 — The review tick (advance / no reset)

A review is marked done via `POST
/api/v1/review-schedule/{unit_id}/reviewed`. Effect:
`interval_days` advances to the **next value in the explicit
ladder `[1, 3, 7, 21, 60]`** (the D1 sequence verbatim), staying
at `60` once reached; `last_reviewed_at = now()`; `due_at = now()
+ interval_days`. The ladder is normative — there is **no
multiply formula** (a `×3` rule would produce `1→3→9→27→60` and
contradict D1; implementations use the ladder array directly, in
SQL and app logic). **No quality signal, no reset path** — pure
time-based progression, consistent with D1/D2 (no
self-grade). A unit, once on the ladder, advances monotonically;
it never falls back. (Reset-on-failure is an SM-2/FSRS behavior
explicitly deferred.)

#### D6 amendment — due-gate on the tick (added 2026-05-18)

The original D6 was silent on what happens if `/reviewed` is
called *before* `due_at`. Surfaced by Codex review on PR #131
(post-merge): without a gate, a double-submit or a script can
advance `1→3→7→…` ahead of schedule and push the next review
far into the future, defeating the cadence the feature exists to
enforce. Founder-approved 2026-05-18.

**Rule:** a tick is **rejected** if the unit is not yet due
(`due_at > clock_timestamp()`, evaluated DB-side inside the same
`FOR UPDATE`-locked SELECT as the advance). Only a due review
advances the ladder. The gate uses `clock_timestamp()` (real
wall time at evaluation), **not** `NOW()`: `NOW()` is the
transaction-start timestamp and would go stale while the SELECT
waits on the row lock under contention, yielding a false 409 for
a request that became due during the wait (Codex review, PR
#132).

- **Error, not silent no-op** — a no-op would hide client bugs.
- **HTTP `409 Conflict`, code `REVIEW_NOT_DUE`** — the resource
  state does not permit the action. (`425 Too Early` was
  considered and rejected: niche, poorly supported.)
- **No grace window.** Ladder intervals are in days; sub-second
  clock skew is irrelevant and a tolerance knob is a tunable
  nobody asked for (YAGNI). Strict `due_at > clock_timestamp()`.

This composes with the existing not-scheduled case: a missing
schedule row is still `404 REVIEW_NOT_SCHEDULED` (nothing to
advance); a present-but-early row is `409 REVIEW_NOT_DUE`
(something to advance, just not yet).

---

## Implementation plan (PRs, only after this doc is approved)

Sequenced; each is independently mergeable and gate-neutral
(touches no unit content or regression sets):

1. **Backend write + tick.** `review_repository.py`
   (seed-on-completion hook into `record_completion`; the
   `/reviewed` tick). Unit tests for the interval ladder + the
   idempotency/uniqueness behavior.
2. **Backend read endpoint.** `GET /api/v1/review-schedule`
   + the `POST …/reviewed` route in `main.py`, JWT-guarded.
3. **Android: Continue-surface integration.** Surface a due
   review *alongside* the next new unit per D4; "review" opens
   the unit reader in a re-surface (read-only, no decision
   prompt) mode per D2; completing the re-surface calls
   `/reviewed`.
4. **End-to-end check.** `EXECUTION.md:167` acceptance: a user
   can complete the path on Android *"including spaced reviews of
   earlier units appearing on schedule."*

No new migration (D1). No change to the documented data model,
the grader, or any regression set. No gate re-runs triggered.

## Explicitly out of scope (v1)

- Re-test-on-review (D2 open question) — addition beyond docs.
- SM-2 / FSRS / any quality-rated algorithm (D1).
- Reset-on-failure / lapse handling (D6).
- Review streaks, counters, "X reviews done" stats — `STRATEGY.md:123`
  excludes Duolingo-flavored mechanics; F5 adds none.
- Per-stage review (the documented model is whole-unit; F5 stays
  whole-unit).
- Multi-path review (single-path v1 per `STRATEGY.md:119`).

## Deferred / open (recorded, not actioned)

- **Re-test-on-review.** Coherent but beyond documented scope and
  carries recurring grader cost. Needs an explicit, sourced
  decision (likely a STRATEGY amendment) before adoption.
- **SM-2 / FSRS.** Post-launch optimization if interval-doubling
  underperforms on real cohort retention data. Each requires a
  schema migration for algorithm state.
- **Notification/delivery of due reviews.** STRATEGY excludes
  "daily reminders" (`:123`); F5 surfaces due reviews *in-app on
  open* only. Any push-delivery is explicitly excluded, not
  deferred.

---

## Status

**Decisions D1–D6 locked pending founder approval of this doc.**
On approval, implementation proceeds per the plan above
(propose→approve→execute; no implementation PR precedes this
doc's merge). Authoring of Units 13–20 continues in parallel and
is unaffected — F5 touches no curriculum content.
