package com.codecollab.ast.service;

import com.codecollab.ast.dto.AstResultResponse;

public interface AstAnalysisService {

    AstResultResponse analyze(Long snippetId, String code, String language);

    AstResultResponse getBySnippetId(Long snippetId);

    AstResultResponse reanalyze(Long snippetId, Long actorId);

    void analyzeAsync(Long snippetId, String code, String language);
}
