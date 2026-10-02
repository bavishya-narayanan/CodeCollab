CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    snippet_id      BIGINT       NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       BIGINT       REFERENCES comments(id) ON DELETE SET NULL,
    content         VARCHAR(1000) NOT NULL,
    helpful         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_snippet       ON comments (snippet_id) WHERE deleted = FALSE;
CREATE INDEX idx_comments_parent        ON comments (parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_comments_user          ON comments (user_id);
CREATE INDEX idx_comments_created_at    ON comments (created_at DESC);

CREATE TRIGGER comments_updated_at
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE comments IS
    'Flat-threaded comments on snippets (max depth 1)';
COMMENT ON COLUMN comments.parent_id IS
    'NULL for top-level comments; set to root comment id for replies';
COMMENT ON COLUMN comments.helpful IS
    'Marks a comment as helpful (one flag per comment, set by author)';
COMMENT ON COLUMN comments.deleted IS
    'Soft delete; children remain with parent_id pointing to deleted row';
