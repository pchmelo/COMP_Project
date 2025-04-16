package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.List;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 */


public class ReturnCheck extends AnalysisVisitor {

    private String currentMethod;
    private TypeUtils types = new TypeUtils(null);

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitNormalMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMainMethodDecl);
    }

    private Void visitMainMethodDecl(JmmNode method , SymbolTable table) {
        visitMethodDecl(method, table, true);
        return null;
    }

    private Void visitNormalMethodDecl(JmmNode method, SymbolTable table) {
        JmmNode returnType = method.getChild(0);
        boolean isVoidReturn = returnType.getKind().equals("VoidType");
        visitMethodDecl(method, table, isVoidReturn);
        return null;
    }

    /**Check number of returns**/
    private Void visitMethodDecl(JmmNode method, SymbolTable table, boolean isVoidReturn) {
        currentMethod = method.get("name");

        List<JmmNode> children = method.getChildren();
        int count = 0;
        boolean isThereStuff = false;

        for (JmmNode child : children){
            if (child.getKind().equals("ReturnStmt")){
                count++;
            }else{
                //vvv This just to check if child is a statement or var declaration. if there is any, means method not empty
                if (! (child.getKind().equals("VarArgType") || child.getKind().equals("Param") || child.getKind().equals("Param") || child.getKind().equals("TypeTagNotUsed") || child.getKind().equals("VoidType") )  ) {
                    isThereStuff = true;
                }
                if (count == 1){
                    var message = String.format("Cannot have more stuff after return has been mentioned", method);
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            message,
                            null)
                    );
                }
            }
        }

        if (count > 1) {
            var message = String.format("Cannot have more than 1 returns in the same method", method);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    method.getLine(),
                    method.getColumn(),
                    message,
                    null)
            );
        } else if (count == 0 && !isVoidReturn) {
            var message = String.format("Cannot have a return type different than void and not return something", method);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    method.getLine(),
                    method.getColumn(),
                    message,
                    null)
            );
        }

        if (!isThereStuff && isVoidReturn){
            var message = String.format("You forgot to add stuff before returning void. Empty useless method", method);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    method.getLine(),
                    method.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }

}
