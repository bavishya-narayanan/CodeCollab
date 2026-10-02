-- V2: Create users table
-- Phase 1: Authentication & User Management

CREATE TABLE users (
    id              BIGSERIAL                   PRIMARY KEY,
    username        VARCHAR(50)                 NOT NULL UNIQUE,
    email           VARCHAR(120)                NOT NULL UNIQUE,
    password_hash   VARCHAR(100)                NOT NULL,
    role            VARCHAR(20)                 NOT NULL DEFAULT 'USER',
    status          VARCHAR(20)                 NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP WITH TIME ZONE
);

-- Case-insensitive lookups — the application always lowercases before querying
CREATE INDEX idx_users_username ON users (LOWER(username));
CREATE INDEX idx_users_email    ON users (LOWER(email));
CREATE INDEX idx_users_status   ON users (status);

COMMENT ON TABLE  users                  IS 'Application users — one row per registered account';
COMMENT ON COLUMN users.password_hash    IS 'BCrypt hash at cost 12 — never store plaintext';
COMMENT ON COLUMN users.role             IS 'USER or ADMIN';
COMMENT ON COLUMN users.status           IS 'ACTIVE, BANNED, or PENDING';

-- Auto-update updated_at on every row change
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
