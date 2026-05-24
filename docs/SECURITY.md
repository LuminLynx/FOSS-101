# Security Posture

**Status:** Living document. Describes *how* Libella reasons about security,
not a live list of weaknesses.
**Scope:** The Libella Android client, its FastAPI backend, and the
Claude-based grader (F4).
**Anchored to:** [`STRATEGY.md`](./STRATEGY.md), [`PHASE_2_GATE.md`](./PHASE_2_GATE.md),
[`REGRESSION_GATE.md`](./REGRESSION_GATE.md).

This document is deliberately **posture-level, not a recipe**. It states the
principles we hold and the threat categories we design against. It does **not**
publish current gaps, exploit steps, thresholds, or the contents of any
control that derives its strength from not being public. The repository is
public; this file is written on the assumption that an adversary reads it.

---

## 0. Why this exists

Libella runs an LLM in a position of trust: the grader (F4) reads a learner's
free-text answer and decides whether rubric criteria are met. The moment an
LLM consumes untrusted input and its output drives a stored decision, the
system inherits a class of failure modes that ordinary CRUD apps do not have.
Those failure modes do not have a single fix. This document records the
mental model we use so that every contributor reasons about them the same way.

---

## 1. The defense mental model (no silver bullet)

There is no one control that makes an LLM feature safe. Security here is the
overlap of several independent layers, each of which assumes the others can
fail. We design every LLM-adjacent surface against these six questions:

1. **Trust boundary** — *What input is data, and what input is instruction?*
   Untrusted text (a learner answer, a looked-up record, a web page) is
   fenced and labelled as data. The model is told, in the system prompt, that
   the fenced region is content to be analysed and never a source of new
   instructions.

2. **Constrain the output** — *Can the model's answer be malformed?*
   The grader is forced into a structured tool call with a fixed schema rather
   than free prose. A response that does not fit the schema is rejected before
   it can touch the database.

3. **Least privilege on actions** — *What can the model actually cause to
   happen?* The grader's only effect is to write a grade for a known unit. It
   holds no credentials, moves no money, and cannot reach systems outside its
   one job. The blast radius of a fully-compromised grader is bounded to that
   one capability by design, not by hoping the prompt holds.

4. **Treat model output as untrusted** — *What if the model is wrong or
   captured?* Output is validated against an independent allow-list (the
   criteria the grader was actually asked about), checked for internal
   consistency, and rendered without granting it any authority it didn't earn.

5. **Detect** — *Would we notice?* Low-confidence or self-inconsistent grades
   are flagged for human review rather than silently trusted. Token usage and
   model calls are observable so abuse and runaway cost have a signal.

6. **Contain the blast radius** — *When a layer fails, how far does it
   spread?* Surfaces are scoped so that the worst realistic outcome of a
   single broken control is small and local (e.g. one learner inflating their
   own grade), never lateral movement or access to other users' data.

A control is only as valuable as the layer beneath it when it fails. We never
rely on the prompt alone, because a prompt is the layer most exposed to the
adversary.

---

## 2. Threat taxonomy — OWASP LLM Top 10 (2025)

We use the OWASP Top 10 for LLM Applications as the shared vocabulary for
what can go wrong. Each contributor working on an LLM surface is expected to
ask which of these apply:

| ID | Category | How it shows up in Libella |
| --- | --- | --- |
| LLM01 | Prompt Injection | A learner answer (direct) or looked-up record (indirect) tries to steer the grader. |
| LLM02 | Sensitive Information Disclosure | The grader must not leak rubric internals, other learners' data, or system context. |
| LLM03 | Supply Chain | Model provider, SDK, and dependency integrity. |
| LLM04 | Data & Model Poisoning | Authored content and regression sets feed the grader's context and its gate. |
| LLM05 | Improper Output Handling | A grade is consumed downstream; output is validated before it is trusted or rendered. |
| LLM06 | Excessive Agency | The grader is held to one capability; it cannot take open-ended actions. |
| LLM07 | System Prompt Leakage | The posture assumes the prompt may leak; strength lives in the layers below it. |
| LLM08 | Vector/Embedding Weaknesses | Applies if/when retrieval is added; not a current surface. |
| LLM09 | Misinformation | A wrong-but-confident grade is a product harm; detection + human review address it. |
| LLM10 | Unbounded Consumption | Model calls cost money; usage is bounded and observed. |

The table is a checklist of *categories to reason about*, not a status report.
Which controls are in place for each is intentionally not enumerated here.

---

## 3. Surface inventory

These are the places where untrusted input, model output, or cost enter the
system. Each is designed against the model in §1 and the categories in §2.
We list the surfaces and the defense *categories* that apply — not the
specific configuration of any control.

- **Grader input** — learner free-text answers. (Trust boundary; constrain
  output; treat output as untrusted.)
- **Grader context** — authored unit content and rubric criteria that travel
  in the prompt. (Data/model integrity; least privilege.)
- **Grader cost** — every grade is a paid model call. (Bounded consumption;
  detection.)
- **API & data access** — endpoints that read and write learner records.
  (Authorization; own-data-only scoping; blast-radius containment.)
- **Output handling** — grades that are stored and shown back to the learner.
  (Output validation; safe rendering.)
- **Authentication & tokens** — session credentials. (Least privilege;
  lifetime and revocation.)
- **Web/link preview** — any externally-fetched content shown in the app.
  (Treat external content as untrusted.)
- **Secrets** — API keys and signing material. (Kept out of the repository and
  out of model context.)

---

## 4. The secrecy principle

Following Kerckhoffs's principle: **secrecy belongs in keys, not in
mechanisms.** A control whose only protection is that an attacker doesn't
know it exists is not a control. We therefore:

- Keep API keys, signing secrets, and credentials out of the repository, out
  of model prompts, and out of model output.
- Keep exploit-enabling specifics — exact thresholds, the full text of
  hardening prompts, live gap lists, and step-by-step weaknesses — **out of
  public artifacts** (this file, PR descriptions, commit messages, issues).
  This is not security-by-obscurity for the *mechanism*; the mechanisms are
  described here in posture form. It is operational hygiene: we don't hand an
  adversary a map.
- Design every public-by-default mechanism to remain sound even when fully
  understood by an attacker.

If a security measure can only work while it's hidden, it is treated as a key
and managed as a secret — not described in a public document.

---

## 5. Responsible disclosure

If you believe you've found a security issue in Libella, please report it
privately rather than opening a public issue or PR that describes the
weakness.

- **Contact:** joaoport@pm.me
- Please include enough detail to reproduce, and give us reasonable time to
  remediate before any public discussion.
- We will not pursue good-faith research that respects user privacy, avoids
  data destruction, and stays within the scope above.

Do not include working exploit payloads in public channels. A private report
with a clear description is more useful and keeps other users safe.

---

## 6. How to use this document

When you build or change an LLM-adjacent surface:

1. Identify which §3 surface(s) you touch.
2. Walk the six questions in §1 for that surface.
3. Map the risk to the §2 categories and confirm a layer addresses each
   relevant one — never relying on the prompt alone.
4. Keep any exploit-enabling specifics out of the public diff (§4); track them
   privately.

The goal is not a finished checklist but a shared reflex: assume the
adversary reads everything public, and make sure the system is still sound
when they do.
