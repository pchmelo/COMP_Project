package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

public class NewFeatures extends AnalysisVisitor {
    private TypeUtils types = new TypeUtils(null);
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.CONST_STMT, this::visitConst);

    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitConst(JmmNode mainNode, SymbolTable table) {
        Type leftPart = types.getExprType(mainNode.getChild(0), table, currentMethod);
        Type rightPart = types.getExprType(mainNode.getChild(1), table, currentMethod);

        if(!leftPart.getName().equals(rightPart.getName())){
            String message = "The type of the constant is not the same as the type of the variable";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), mainNode.getColumn(), message, null));
        }

        return null;
    }

}
