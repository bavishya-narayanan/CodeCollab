CREATE TABLE snippets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    description     VARCHAR(2000),
    code            TEXT         NOT NULL,
    language        VARCHAR(30)  NOT NULL,
    view_count      INTEGER      NOT NULL DEFAULT 0,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snippets_user_id       ON snippets (user_id) WHERE deleted = FALSE;
CREATE INDEX idx_snippets_language      ON snippets (language) WHERE deleted = FALSE;
CREATE INDEX idx_snippets_created_at    ON snippets (created_at DESC) WHERE deleted = FALSE;
CREATE INDEX idx_snippets_view_count    ON snippets (view_count DESC) WHERE deleted = FALSE;

-- Reuse set_updated_at() function created in V2
CREATE TRIGGER snippets_updated_at
    BEFORE UPDATE ON snippets
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE snippets IS 'User-submitted code snippets';
COMMENT ON COLUMN snippets.deleted IS 'Soft delete flag';
COMMENT ON COLUMN snippets.language IS 'Lowercase language identifier, e.g. java, python';
