# Google Play — Data Safety form answers

Draft answers for the Play Console **Data safety** section, grounded in an
audit of the actual app + backend (HTTPS-only, no analytics/ads SDKs, no
device or advertising IDs). Copy each answer into the Console; edit if the
backend changes.

## Overview

- **Does your app collect or share any of the required user data types?**
  → **Yes** (it collects data; it does **not** share data with third parties).
- **Is all of the user data collected by your app encrypted in transit?**
  → **Yes.** All backend calls are HTTPS; cleartext traffic is blocked in
  `network_security_config.xml` (`cleartextTrafficPermitted="false"`).
- **Do you provide a way for users to request that their data be deleted?**
  → **Yes.** In-app: About → Delete account (calls `DELETE /api/v1/auth/me`,
  cascading delete of account + all activity). Also by email to
  hello@perpenda.com. Documented at
  https://perpenda.com/legal/account-deletion.html.

## Data types collected

For every type below: **Collected = Yes**, **Shared = No**,
**Processed ephemerally = No**, **Data is encrypted in transit = Yes**.
Set the per-type "Is this required or optional?" as noted.

### Personal info

| Data type | Required? | Purposes | Notes |
|-----------|-----------|----------|-------|
| **Email address** | Required | Account management; App functionality | Used for sign-in/sign-up; stored server-side, normalized. |
| **Name** | Required | Account management; App functionality | The display name sent at sign-up; used to attribute contributions. |
| **User IDs** | Required | Account management; App functionality | Server-assigned account ID tied to the user; JWT auth token. |

> **Passwords** are sent (HTTPS) and stored only as a bcrypt hash. Google's
> Data Safety taxonomy has no "password" data type, so there is nothing to
> declare for it beyond the security answers above.

### App activity

| Data type | Required? | Purposes | Notes |
|-----------|-----------|----------|-------|
| **Other user-generated content** | Required | App functionality | Decision-prompt answers — open-ended free text the learner writes, graded against a rubric (`POST /api/v1/units/{unit_id}/grade`). |
| **App interactions** | Required | App functionality | In-app activity records: unit completions (`POST /api/v1/completions`, `backend/app/main.py:296`) and spaced-review ticks (`POST /api/v1/review-schedule/{unitId}/reviewed`, `backend/app/main.py:387`). These are user *actions*, not user-generated content. |

### App info and performance

| Data type | Required? | Purposes | Notes |
|-----------|-----------|----------|-------|
| **Crash logs** | Optional | App functionality; Analytics (diagnostics) | Sent via **Sentry** (EU region) on release builds only. Reports include the stack trace, Android version, device model, app version, coarse locale. We explicitly do **not** attach email, sign-in token, decision-prompt answers, or grades. |

### NOT collected (declare "No" / leave unchecked)

- Location (precise or approximate)
- Financial info
- Health & fitness
- Messages, Photos/videos, Audio, Files & docs, Calendar, Contacts
- Web browsing history
- **App info & performance — Diagnostics / Other** (only Crash logs above
  are collected; no general telemetry, ANR reporting, or session tracing)
- **Device or other IDs** (no advertising ID / GAID, no device ID)

## Security practices summary (for the form's "Security practices")

- Data is encrypted in transit: **Yes** (HTTPS, cleartext blocked).
- Users can request data deletion: **Yes** (in-app + email).
- Committed to Play Families Policy / targets children: **No** (not a
  children's app).
- Independent security review: **No** (leave unchecked unless one is done).
