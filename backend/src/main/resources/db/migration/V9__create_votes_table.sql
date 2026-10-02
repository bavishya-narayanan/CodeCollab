CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,
    snippet_id      BIGINT      NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vote_type       SMALLINT    NOT NULL CHECK (vote_type IN (1, -1)),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_votes_snippet_user UNIQUE (snippet_id, user_id)
);

CREATE INDEX idx_votes_snippet ON votes (snippet_id);
CREATE INDEX idx_votes_user    ON votes (user_id);

CREATE TRIGGER votes_updated_at
    BEFORE UPDATE ON votes
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE votes IS 'User votes on snippets: +1 upvote, -1 downvote';
COMMENT ON COLUMN votes.vote_type IS '1 = upvote, -1 = downvote';
