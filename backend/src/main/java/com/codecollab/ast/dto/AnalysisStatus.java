package com.codecollab.ast.dto;

public enum AnalysisStatus {
    PENDING,        // queued for analysis
    ANALYZED,       // completed successfully
    UNSUPPORTED,    // language not supported (non-Java)
    FAILED          // parser error
}
