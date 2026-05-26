"""Path-centric repositories per docs/strategy/STRATEGY.md and docs/audits/AUDIT.md §2.7.

Thin pure-function modules over psycopg connections. No ORM. The legacy
term-centric reads still live in backend/app/repository.py and will be
trimmed as the term surface retires across Phase 1.
"""
