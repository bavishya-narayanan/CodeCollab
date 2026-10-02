-- Add a generated tsvector column for full-text search.
-- Weighted:
--   A = title (highest priority)
--   B = description
--   C = code
-- We use 'english' for title+description (stems English words) and
-- 'simple' for code (code is not English prose; simple avoids mis-stemming).

ALTER TABLE snippets
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(description, '')), 'B') ||
        setweight(to_tsvector('simple',  coalesce(code, '')), 'C')
    ) STORED;

CREATE INDEX idx_snippets_search_vector
    ON snippets USING GIN (search_vector);

COMMENT ON COLUMN snippets.search_vector IS
    'Weighted tsvector: A=title, B=description, C=code. Auto-updates on row change.';
