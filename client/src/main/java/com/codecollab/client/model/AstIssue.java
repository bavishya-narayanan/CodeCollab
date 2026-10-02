package com.codecollab.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AstIssue {
    private String ruleName;
    private String severity;
    private String message;
    private int    line;

    public AstIssue() {}

    public String getRuleName()             { return ruleName; }
    public void   setRuleName(String r)     { this.ruleName = r; }

    public String getSeverity()             { return severity; }
    public void   setSeverity(String s)     { this.severity = s; }

    public String getMessage()              { return message; }
    public void   setMessage(String m)      { this.message = m; }

    public int    getLine()                 { return line; }
    public void   setLine(int l)            { this.line = l; }
}
