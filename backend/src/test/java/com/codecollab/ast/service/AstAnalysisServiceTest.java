package com.codecollab.ast.service;

import com.codecollab.ast.dto.AnalysisStatus;
import com.codecollab.ast.dto.AstResultResponse;
import com.codecollab.ast.entity.AstResult;
import com.codecollab.ast.mapper.AstResultMapper;
import com.codecollab.ast.metrics.MetricsCalculator;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.repository.AstResultRepository;
import com.codecollab.ast.rule.AstRuleRegistry;
import com.codecollab.ast.service.impl.AstAnalysisServiceImpl;
import com.codecollab.snippet.repository.SnippetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AstAnalysisServiceTest {

    @Mock private JavaCodeParser parser;
    @Mock private AstRuleRegistry ruleRegistry;
    @Mock private MetricsCalculator metricsCalculator;
    @Mock private AstResultRepository repository;
    @Mock private AstResultMapper mapper;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;
    @Mock private SnippetRepository snippetRepository;

    private AstAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AstAnalysisServiceImpl(parser, ruleRegistry, metricsCalculator, repository, mapper, redis, objectMapper, snippetRepository);
    }

    @Test
    void analyzeNonJavaLanguageReturnsUnsupported() {
        AstResult mockEntity = new AstResult();
        when(repository.findBySnippetId(1L)).thenReturn(Optional.of(mockEntity));
        when(repository.save(any(AstResult.class))).thenReturn(mockEntity);
        
        AstResultResponse mockResponse = new AstResultResponse(1L, AnalysisStatus.UNSUPPORTED, "PYTHON", List.of(), null, null, null, null, null, "v1");
        when(mapper.toResponse(mockEntity)).thenReturn(mockResponse);

        AstResultResponse response = service.analyze(1L, "print('hello')", "PYTHON");

        assertEquals(AnalysisStatus.UNSUPPORTED, response.status());
        verify(repository).save(any(AstResult.class));
    }
}
