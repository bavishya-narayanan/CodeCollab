package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AstResult {
    private Long        snippetId;
    private int         qualityScore;
    private List<AstIssue>  issues;
    private AstMetrics  metrics;
    private Instant     analyzedAt;

    public AstResult() {}

    public Long   getSnippetId()                    { return snippetId; }
    public void   setSnippetId(Long s)              { this.snippetId = s; }

    public int    getQualityScore()                 { return qualityScore; }
    public void   setQualityScore(int q)            { this.qualityScore = q; }

    public List<AstIssue> getIssues()               { return issues; }
    public void           setIssues(List<AstIssue> i){ this.issues = i; }

    public AstMetrics getMetrics()                  { return metrics; }
    public void       setMetrics(AstMetrics m)      { this.metrics = m; }

    public Instant getAnalyzedAt()                  { return analyzedAt; }
    public void    setAnalyzedAt(Instant a)         { this.analyzedAt = a; }
}
