-- V1: Initial marker migration
-- Purpose: Verify Flyway is wired correctly before real domain tables exist.
-- This table acts as a "canary" that proves migrations are running end-to-end.
-- It will be superseded (but not dropped) once real migrations arrive in Phase 1.

CREATE TABLE IF NOT EXISTS schema_marker (
    id         BIGSERIAL                   PRIMARY KEY,
    version    VARCHAR(50)                 NOT NULL,
    applied_at TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);

INSERT INTO schema_marker (version) VALUES ('v1-init');

COMMENT ON TABLE schema_marker IS 'Marker table for Phase 0 verification — safe to ignore in later phases';
