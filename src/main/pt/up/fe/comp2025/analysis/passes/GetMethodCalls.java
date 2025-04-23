package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;

import java.util.HashMap;
import java.util.Map;

public class GetMethodCalls extends AnalysisVisitor {
    private TypeUtils types = new TypeUtils(null);
    private String currentMethod;
    private Map<String, JmmNode> methodCalls = new HashMap<>();

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_CALL_EXPR, this::visitCallExpr);


    }

    private Void visitCallExpr(JmmNode mainNode, SymbolTable symbolTable) {
        methodCalls.put(mainNode.get("name"), mainNode);
        return null;
    }


}
