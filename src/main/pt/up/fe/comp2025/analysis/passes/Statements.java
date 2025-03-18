package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class Statements extends AnalysisVisitor {
    private String currentMethod;
    private TypeUtils types = new TypeUtils(null);

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.IF_STMT, this::ifCheck);
        addVisit(Kind.WHILE_STMT, this::whileCheck);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    public Void ifCheck(JmmNode mainNode, SymbolTable table){
        List<JmmNode> children = mainNode.getChildren();

        for (JmmNode child : children) {
            if(child.getHierarchy().getLast().equals("Expression")){
                Type child_type = types.getExprType(child, table, currentMethod);
                if (!child_type.getName().equals("boolean") && !child_type.isArray()) {
                    var message = "If/Else if condition must be a boolean expression";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                    );                    }
            }
        }

        return null;
    }

    public Void whileCheck(JmmNode mainNode, SymbolTable table){
        JmmNode child = mainNode.getChild(0);

        Type child_type = types.getExprType(child, table, currentMethod);
        if (!child_type.getName().equals("boolean") || child_type.isArray()) {
            var message = "While condition must be a boolean expression";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    mainNode.getLine(),
                    mainNode.getColumn(),
                    message,
                    null)
            );
        }


        return null;
    }






}
