CREATE TABLE ast_results (
    id              BIGSERIAL PRIMARY KEY,
    snippet_id      BIGINT NOT NULL REFERENCES snippets(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL,
    language        VARCHAR(30) NOT NULL,
    issues          JSONB       NOT NULL DEFAULT '[]'::jsonb,
    metrics         JSONB       NOT NULL DEFAULT '{}'::jsonb,
    quality_score   INTEGER,
    error_message   VARCHAR(1000),
    analyzed_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    duration_ms     INTEGER,
    rule_version    VARCHAR(20) NOT NULL DEFAULT 'v1'
);

-- One latest analysis per snippet
CREATE UNIQUE INDEX idx_ast_results_snippet
    ON ast_results (snippet_id);

CREATE INDEX idx_ast_results_status
    ON ast_results (status);

CREATE INDEX idx_ast_results_quality
    ON ast_results (quality_score DESC)
    WHERE quality_score IS NOT NULL;

COMMENT ON TABLE ast_results IS 'Latest code analysis result per snippet';
COMMENT ON COLUMN ast_results.status IS 'PENDING, ANALYZED, UNSUPPORTED, FAILED';
COMMENT ON COLUMN ast_results.issues IS 'Array of {ruleId, severity, message, line, column, metadata}';
COMMENT ON COLUMN ast_results.metrics IS 'Aggregate code metrics: LOC, methods, nesting, etc.';
COMMENT ON COLUMN ast_results.rule_version IS 'Version of rule set used; bump when rules change semantically';
