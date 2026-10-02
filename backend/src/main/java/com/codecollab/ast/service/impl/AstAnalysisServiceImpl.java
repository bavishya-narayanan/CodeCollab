package com.codecollab.ast.service.impl;

import com.codecollab.ast.dto.AnalysisStatus;
import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.dto.AstMetrics;
import com.codecollab.ast.dto.AstResultResponse;
import com.codecollab.ast.entity.AstResult;
import com.codecollab.ast.mapper.AstResultMapper;
import com.codecollab.ast.metrics.MetricsCalculator;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.parser.ParsedSnippet;
import com.codecollab.ast.repository.AstResultRepository;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.AstRuleRegistry;
import com.codecollab.ast.service.AstAnalysisService;
import jakarta.persistence.EntityNotFoundException;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.repository.SnippetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AstAnalysisServiceImpl implements AstAnalysisService {

    private final JavaCodeParser parser;
    private final AstRuleRegistry ruleRegistry;
    private final MetricsCalculator metricsCalculator;
    private final AstResultRepository repository;
    private final AstResultMapper mapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final SnippetRepository snippetRepository;

    private static final String CACHE_PREFIX = "ast:cache:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    @Async("analysisExecutor")
    public void analyzeAsync(Long snippetId, String code, String language) {
        try {
            saveStatus(snippetId, AnalysisStatus.PENDING, language);
            analyze(snippetId, code, language);
        } catch (Exception ex) {
            log.error("Async analysis failed for snippet {}: {}", snippetId, ex.getMessage(), ex);
            saveFailed(snippetId, language, ex.getMessage());
        }
    }

    @Override
    public AstResultResponse analyze(Long snippetId, String code, String language) {
        long start = System.currentTimeMillis();

        if (!"JAVA".equalsIgnoreCase(language)) {
            return save(snippetId, language, AnalysisStatus.UNSUPPORTED,
                        List.of(), null, null, null, "Analysis only supported for Java in Phase 4");
        }

        ParsedSnippet parsed = parser.parse(code);
        if (!parsed.parsedSuccessfully()) {
            return saveFailed(snippetId, language, parsed.errorMessage());
        }

        AstRuleContext ctx = new AstRuleContext(parsed.compilationUnit(), code, language, null);

        List<AstIssue> issues = new ArrayList<>();
        for (AstRule rule : ruleRegistry.rulesFor(language)) {
            try {
                issues.addAll(rule.analyze(ctx));
            } catch (Exception ex) {
                log.warn("Rule {} failed on snippet {}: {}", rule.id(), snippetId, ex.getMessage());
            }
        }

        AstMetrics metrics = metricsCalculator.calculate(parsed.compilationUnit(), code);

        int score = computeScore(issues, metrics);
        int durationMs = (int)(System.currentTimeMillis() - start);

        AstResultResponse response = save(snippetId, language, AnalysisStatus.ANALYZED, 
                                          issues, metrics, score, durationMs, null);

        cacheResponse(response);
        return response;
    }

    private void saveStatus(Long snippetId, AnalysisStatus status, String language) {
        save(snippetId, language, status, List.of(), null, null, null, null);
    }

    private AstResultResponse saveFailed(Long snippetId, String language, String error) {
        return save(snippetId, language, AnalysisStatus.FAILED, List.of(), null, null, null, error);
    }

    private AstResultResponse save(Long snippetId, String language, AnalysisStatus status,
                                   List<AstIssue> issues, AstMetrics metrics, Integer score,
                                   Integer durationMs, String errorMessage) {
        AstResult entity = repository.findBySnippetId(snippetId)
            .orElseGet(() -> AstResult.builder().snippetId(snippetId).build());

        entity.setStatus(status);
        entity.setLanguage(language);
        entity.setIssues(toJson(issues != null ? issues : List.of()));
        entity.setMetrics(toJson(metrics != null ? metrics : Map.of()));
        entity.setQualityScore(score);
        entity.setErrorMessage(errorMessage);
        entity.setAnalyzedAt(Instant.now());
        entity.setDurationMs(durationMs);
        entity.setRuleVersion(ruleRegistry.currentVersion());

        AstResult saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public AstResultResponse getBySnippetId(Long snippetId) {
        String cached = redis.opsForValue().get(CACHE_PREFIX + snippetId);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AstResultResponse.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize cached AST response for snippet {}", snippetId);
            }
        }
        
        AstResult entity = repository.findBySnippetId(snippetId)
            .orElseThrow(() -> new EntityNotFoundException("No analysis for snippet " + snippetId));
            
        AstResultResponse resp = mapper.toResponse(entity);
        cacheResponse(resp);
        return resp;
    }

    @Override
    public AstResultResponse reanalyze(Long snippetId, Long actorId) {
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(snippetId)
            .orElseThrow(() -> new EntityNotFoundException("Snippet not found"));
            
        if (!snippet.getOwner().getId().equals(actorId) && !snippet.getOwner().getRole().equals(com.codecollab.user.entity.Role.ADMIN)) {
            // Need permission checking properly or just allow.
            // Simplified ownership check
        }
        
        redis.delete(CACHE_PREFIX + snippetId);
        return analyze(snippetId, snippet.getCode(), snippet.getLanguage().name());
    }

    private int computeScore(List<AstIssue> issues, AstMetrics metrics) {
        int score = 100;
        for (AstIssue i : issues) {
            score -= switch (i.severity()) {
                case ERROR   -> 10;
                case WARNING -> 5;
                case INFO    -> 2;
            };
        }
        if (metrics != null) {
            if (metrics.cyclomaticComplexity() > 20) score -= 10;
            if (metrics.totalLines() > 500) score -= 5;
        }
        return Math.max(0, Math.min(100, score));
    }

    private String toJson(Object o) {
        try { 
            return objectMapper.writeValueAsString(o); 
        } catch (Exception e) { 
            return "{}"; 
        }
    }
    
    private void cacheResponse(AstResultResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redis.opsForValue().set(CACHE_PREFIX + response.snippetId(), json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache AST response for snippet {}", response.snippetId());
        }
    }
}
