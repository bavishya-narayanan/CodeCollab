package com.codecollab.ast.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

@Component
public class CyclomaticComplexityCalculator {

    public int calculate(CompilationUnit cu) {
        int cc = 1;
        cc += cu.findAll(IfStmt.class).size();
        cc += cu.findAll(ForStmt.class).size();
        cc += cu.findAll(ForEachStmt.class).size();
        cc += cu.findAll(WhileStmt.class).size();
        cc += cu.findAll(DoStmt.class).size();
        cc += cu.findAll(CatchClause.class).size();
        cc += cu.findAll(ConditionalExpr.class).size();
        cc += cu.findAll(SwitchEntry.class).stream()
                .filter(e -> e.getLabels().size() > 0)
                .count();
        return cc;
    }
}
