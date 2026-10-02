package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AstMetrics {
    private int linesOfCode;
    private int methodCount;
    private int fieldCount;
    private int maxNestingDepth;
    private int cyclomaticComplexity;

    public AstMetrics() {}

    public int getLinesOfCode()                     { return linesOfCode; }
    public void setLinesOfCode(int l)               { this.linesOfCode = l; }

    public int getMethodCount()                     { return methodCount; }
    public void setMethodCount(int m)               { this.methodCount = m; }

    public int getFieldCount()                      { return fieldCount; }
    public void setFieldCount(int f)                { this.fieldCount = f; }

    public int getMaxNestingDepth()                 { return maxNestingDepth; }
    public void setMaxNestingDepth(int d)           { this.maxNestingDepth = d; }

    public int getCyclomaticComplexity()            { return cyclomaticComplexity; }
    public void setCyclomaticComplexity(int c)      { this.cyclomaticComplexity = c; }
}
