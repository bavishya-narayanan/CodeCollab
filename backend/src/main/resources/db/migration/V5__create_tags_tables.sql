-- Tags are unique, lowercase, short strings like "jwt", "spring-boot"
CREATE TABLE tags (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    usage_count     INTEGER     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tags_name  ON tags (name);
CREATE INDEX idx_tags_usage ON tags (usage_count DESC);

-- Join table
CREATE TABLE snippet_tags (
    snippet_id      BIGINT NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    tag_id          BIGINT NOT NULL REFERENCES tags(id)     ON DELETE CASCADE,
    PRIMARY KEY (snippet_id, tag_id)
);

CREATE INDEX idx_snippet_tags_tag ON snippet_tags (tag_id);

COMMENT ON TABLE tags IS 'Normalized tag names, lowercase';
COMMENT ON COLUMN tags.usage_count IS 'Denormalized count of snippets using this tag';
