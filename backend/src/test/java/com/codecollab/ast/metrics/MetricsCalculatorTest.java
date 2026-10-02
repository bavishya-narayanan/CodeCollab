package com.codecollab.ast.metrics;

import com.codecollab.ast.dto.AstMetrics;
import com.codecollab.ast.parser.JavaCodeParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsCalculatorTest {

    private final JavaCodeParser parser = new JavaCodeParser();
    private final CyclomaticComplexityCalculator ccCalculator = new CyclomaticComplexityCalculator();
    private final MetricsCalculator metricsCalculator = new MetricsCalculator(ccCalculator);

    @Test
    void testMetricsCalculation() {
        String code = "public class A {\n" +
                      "  // comment\n" +
                      "  public void test1() { if (true) {} }\n" +
                      "  public void test2() {}\n" +
                      "}\n";
                      
        AstMetrics metrics = metricsCalculator.calculate(parser.parse(code).compilationUnit(), code);
        
        assertEquals(1, metrics.classCount());
        assertEquals(2, metrics.methodCount());
        assertTrue(metrics.totalLines() >= 5);
        assertTrue(metrics.cyclomaticComplexity() > 1);
    }
}
