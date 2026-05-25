-- Per-account rate limiting for the auth endpoints (login / signup).
--
-- Login and signup were unthrottled: an attacker could brute-force a
-- known account's password or hammer signup (bcrypt CPU + email-probing).
-- The grade limiter (migration 023) is keyed by user_id and doesn't help
-- pre-auth. This log backs a per-account limiter keyed by an opaque
-- attempt_key (e.g. "login:<email>") — NOT a user_id, because a failed
-- login or signup probe may reference an email with no account.
--
-- Per-account (not per-IP) is deliberate: behind a proxy the observed
-- client IP is the proxy's, so an IP limiter would either lock out all
-- users (shared bucket) or be spoofable via X-Forwarded-For. Keying on
-- the email caps targeted brute-force of one account without that hazard.
--
-- No FK (the key isn't a user id). The repository prunes rows older than
-- the window on each check/record, so the table stays bounded.

CREATE TABLE IF NOT EXISTS auth_attempts (
    id BIGSERIAL PRIMARY KEY,
    attempt_key TEXT NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auth_attempts_key_time
    ON auth_attempts(attempt_key, attempted_at DESC);

-- Supports the global expired-row prune (DELETE WHERE attempted_at < cutoff),
-- which the per-key index above can't serve (key is its leading column).
CREATE INDEX IF NOT EXISTS idx_auth_attempts_time
    ON auth_attempts(attempted_at);
