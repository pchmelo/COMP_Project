package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
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
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    public Void ifCheck(JmmNode mainNode, SymbolTable table){
        List<JmmNode> children = mainNode.getChildren();

        for (JmmNode child : children) {
            if(child.getHierarchy().getLast().equals("Expression")){
                Type child_type = types.valueReturner(child, table, currentMethod);
                if (!child_type.getName().equals("boolean")) {
                    addReport(newError(child, "Expected boolean expression in if statement"));
                }
            }
        }

        return null;
    }






}
