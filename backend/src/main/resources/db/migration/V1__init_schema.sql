-- Enable btree_gist extension required for Exclusion Constraints on range & scalar types
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Initial schema baseline verification table (optional diagnostic / schema check)
CREATE TABLE IF NOT EXISTS schema_baseline (
    id SERIAL PRIMARY KEY,
    initialized_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
