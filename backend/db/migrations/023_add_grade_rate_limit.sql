-- Per-user rate limiting for the F4 grader endpoint.
--
-- Every POST /api/v1/units/{unit_id}/grade triggers a paid Claude call.
-- The endpoint is auth-gated and per-call cost is bounded (answer is
-- capped at 8000 chars), but nothing bounded the *rate*: one logged-in
-- account could loop the endpoint and run up the provider bill. This is
-- OWASP LLM10 (Unbounded Consumption); the defense is to cap grade
-- attempts per user over a sliding window.
--
-- An append-only attempts log is the simplest correct shape for a
-- sliding window. The rate-limit repository deletes a user's rows older
-- than the window on each attempt, so the table stays bounded to roughly
-- (active users x window-cap) rows rather than growing forever.
--
-- Counting + insert run under a per-user transactional advisory lock in
-- the repository, so concurrent requests from the same user can't both
-- read an under-limit count and both insert past the cap.

CREATE TABLE IF NOT EXISTS grade_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_grade_attempts_user_time
    ON grade_attempts(user_id, attempted_at DESC);
