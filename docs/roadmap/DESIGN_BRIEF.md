# Design Brief — Perpenda app, the real screens

> **What this is.** A faithful, code-grounded inventory of every
> screen the app actually has, what real data each one holds, and
> the hard constraints any redesign must respect. Two jobs:
> (1) make the app's true shape legible on one page — "see, don't
> imagine"; (2) be the precise input you paste into Claude Design
> so its mockups come back *buildable*, not fantasy.
>
> **Why this exists.** The earlier Claude Design exploration
> drifted — it invented per-stage progress, a stats dashboard,
> a Notifications/"daily nudge" settings section, multi-path —
> none of which the documented model or the code has. This brief
> is the guardrail so the next round is faithful.
>
> **Status.** Living doc. Grounded in the codebase + `STRATEGY.md`
> / `EXECUTION.md` as of 2026-05-18. No invention.

---

## Hard constraints — apply to every screen / mockup

These are not stylistic; they are what the app *can actually do*.
A mockup that violates one of these is unbuildable.

1. **Minimal documented data model.** The only per-user state is
   **unit-level completion** (a unit is done or not) plus the F5
   spaced-review schedule. There is **no per-stage progress, no
   time tracking, no "sources touched", no streak/score**. Any
   "62/63 stages", "4h 12m invested", progress bars-within-a-unit
   is fantasy — cut it.
2. **No gamification.** `STRATEGY.md:123` explicitly excludes
   *streaks, daily reminders, leaderboards, social*. No
   notification/nudge UI. No badges. No celebratory stats screen.
   This is a deliberate α-PM-audience differentiator, not an
   omission to "fix".
3. **A unit is one scrolling reader, not a 5-stage stepper.** The
   real sequence (definition → 90-second bite → Trade-off framing
   → Depth → Decision prompt → Calibration → Sources, the last
   two after the prompt as a priming guard — see Unit Reader for
   the verbatim list) is *content order within one screen*, with
   Trade-off framing and Depth as collapse-by-default
   disclosures. It is **not** separately-navigable stages with
   their own progress. The Logo-Lab-era "5-stage stepper" mockup
   is a visual reinterpretation the data doesn't support.
4. **The 6-step loop is UX flow, not persisted state.**
   Continue → Bite → Decide → Calibrate → Progress → Return
   (`STRATEGY.md:106-113`) describes a session's shape; only
   *completion* and *review-due* are stored.
5. **Single path, v1.** `STRATEGY.md:119`. No "next path" / path
   switcher. The backend supports multiple paths but v1 is one,
   hardcoded.
6. **Editorial is the chosen direction** (your lean from the
   exploration): serif display, hairline dividers, restrained
   color, reference-document feel over course-app cards. This is
   a **restyle of the screens below — no new screens.**

---

## The screens (this is the whole app)

Five product screens + legacy/demo surfaces. That's the entire
surface — design scope is four screens that matter, not a
redesign marathon.

### 1. Path Home — route `home` (`PathHomeScreen.kt`)

- **Loop:** step 1 (Continue), reflects step 5 (Progress), and
  step 6 (Return).
- **Real content, in order:** path title + description; a "Units"
  section = list of every unit (title, "Unit N · status",
  completed ✓ vs. empty circle); an optional **"Reviews due"**
  section *only when non-empty* (F5 — alongside, never a gate);
  a primary **"Continue · {next unit}"** button, or "Path
  complete" text when none remain.
- **Data it actually has:** the path manifest, the set of
  completed unit ids, the next uncompleted unit, the due-reviews
  list. Nothing finer.
- **Drift to avoid:** progress bars, "% complete", stats,
  streaks, multiple paths, per-stage anything.

### 2. Unit Reader — route `unit/{unitId}` (`UnitReaderScreen.kt`)

The heavy screen — the actual learning loop. **One vertical
scroll, fixed order, for a load-bearing reason.**

- **Loop:** steps 2 (Bite), 3 (Decide), 4 (Calibrate).
- **Real content, in this exact order (verbatim from
  `UnitReaderScreen.kt` `LoadedBody`; do not reorder in any
  redesign):**
  1. App-bar title = unit title, subtitle = "Unit {position}".
  2. **Definition** (`unit.definition`) — a single
     plain-text sentence at the very top of the scroll, before
     the bite. Always shown.
  3. **"90-second bite"** section (`biteMd`).
  4. **"Trade-off framing"** — a *labeled, tap-to-expand
     disclosure* (`tradeOffFraming`), collapsed by default,
     between bite and depth. It is its own discrete element, not
     buried inside bite or depth.
  5. **"Depth"** — also a *labeled, tap-to-expand disclosure*
     (`depthMd`), collapsed by default (P4 "bite first, depth
     on tap"). Same treatment as Trade-off framing.
  6. **"Decision prompt"** section (`decisionPrompt.promptMd`),
     present only if the unit has one, + an open-ended answer
     text field + submit.
  7. **Grade output** (after submit) — per-criterion
     met/confidence/rationale against the rubric criteria.
  8. **"Calibration"** section (settled / contested tags) then
     **"Sources"** section — rendered **after** the prompt *on
     purpose*: showing consensus/sources before the user answers
     would prime the answer (P2/P4). **A redesign must preserve
     this after-the-prompt ordering.**
  9. If completed in a prior session (and no fresh grade
     loaded), a quiet "completed" confirmation row (grades are
     not re-fetched on open — known, acceptable).
- **Drift to avoid:** a tab/stepper that lets the user jump to
  "Calibration"/"Sources" before answering (breaks the priming
  guard); per-stage completion ticks / per-stage progress. Note
  the screen *does* have discrete labeled sections (definition,
  90-second bite, Trade-off framing [collapsible], Depth
  [collapsible], Decision prompt, Calibration, Sources) in **one
  scroll** — a redesign may restyle these sections, but they are
  not a navigable multi-stage stepper with their own progress,
  and the two collapsibles must stay collapsed-by-default.

### 3. Auth — routes `auth_login` / `auth_signup` (`AuthScreen.kt`)

- **Loop:** none — boundary screen.
- **Real content:** optional Display name (signup only), Email,
  Password (with show/hide), inline error text, primary submit,
  a tertiary toggle between login and signup.
- **Drift to avoid:** social/SSO buttons (no social, per
  constraint 2), onboarding carousels that imply features the
  app lacks.

### 4. Settings — route `settings` (`SettingsScreen.kt`)

The real Settings is **much smaller than the mockup drew.**

- **Real sections, all of them:** **Account**, **Library**,
  **Appearance** (Theme only), **About**. That is the entire
  screen.
- **Drift to avoid — explicitly:** the exploration mockup added
  a **"Reading"** section (stage estimates / inline citations /
  monospace) and a **"Notifications"** section (**"Daily nudge"**,
  reminder time) and a **"Path"** section. **None exist, and
  "Daily nudge" directly contradicts `STRATEGY.md:123`** (no
  daily reminders). Do not mock these. If Appearance grows, it
  grows within the documented model only (e.g. light/dark/system
  — already implied by the theme), not toward notifications or
  per-stage reading settings.

### 5. Legacy / demo — not core, do not redesign now

- `glossary` (`GlossaryLibraryScreen.kt`) — legacy term library,
  pre-path-pivot. Out of scope for the v1 visual pass.
- `preview_tokenization`, `preview_tokenization_bite`
  (`TokenizationProofScreen.kt`, `BiteFeedScreen.kt`) — demo
  proof surfaces. Out of scope.
- Flagging them only so a "redesign every screen" instinct
  doesn't burn effort here. If they survive to launch they
  inherit the theme automatically; they need no bespoke design.

---

## What the earlier mockups got wrong (so we don't repeat it)

| Mockup showed | Reality |
|---|---|
| 5-stage stepper per unit, per-stage progress | One scrolling reader, fixed order; only unit-level completion stored |
| "Stages read 60/60", "Sources touched 48", "Time invested 4h 12m" | No such data exists; gamification-adjacent, excluded |
| Path-complete stats dashboard | No stats model; soft gamification, off-brand |
| Settings: Notifications / "Daily nudge" / reading toggles | Real Settings = Account/Library/Appearance/About; daily nudge contradicts STRATEGY:123 |
| "Next path", multi-path | Single path v1 (STRATEGY:119) |

The visual *language* of the Editorial exploration (serif,
hairline, restraint) was good and is the chosen direction. The
*information architecture* in those mockups was not — this brief
fixes the IA so the next mockups are faithful.

---

## Workflow this enables

1. **This doc** = the precise input. Paste the relevant screen
   section into Claude Design when mocking that screen.
2. **You + Claude Design** explore the Editorial treatment of
   each real screen → you pick the direction (you are the eyes;
   the loop is fast and visual and yours).
3. **I implement** the chosen look in Compose
   (`Type.kt/Color.kt/Shape.kt/Theme.kt` + the screens), **one
   small PR per screen**, each one you build and *see* on a
   device. Faithful build + doc-consistency + engineering is my
   half; I cannot see the app from here, so the seeing stays
   with you.
4. The logo (Asymmetric mark, your earlier lean) is a **separate
   blocked track**: it needs the SVG exported from your Claude
   Design session — I will not hand-draw an approximate mark.
   Editorial type/color proceeds without it.

This replaces "working in the dark" with see → choose → build →
see, with the seeing in your hands and the faithful building in
mine.
