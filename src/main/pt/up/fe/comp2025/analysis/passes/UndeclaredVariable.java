package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class UndeclaredVariable extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(Kind.NEW_OBJECT_EXPR, this::visitNewObjectExpr);
        addVisit(Kind.ASSIGN_STMT, this::visitVarRefExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitNewObjectExpr(JmmNode newObject, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        // Check if exists a parameter or variable declaration with the same name as the variable reference
        var newObjName = newObject.get("name");

        if (table.getImports().stream()
                .anyMatch(imp -> imp.equals(newObjName))) {
            return null;
        }

        if (table.getSuper().equals(newObjName)){
            return null;
        }

        if (table.getClassName().equals(newObjName)){
            return null;
        }


        // Create error report
        var message = String.format("Variable '%s' does not exist.", newObjName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                newObject.getLine(),
                newObject.getColumn(),
                message,
                null)
        );

        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        // Check if exists a parameter or variable declaration with the same name as the variable reference
        var varRefName = varRefExpr.get("name");


        // Var is a parameter, return
        if (table.getParameters(currentMethod).stream()
                .anyMatch(param -> param.getName().equals(varRefName))) {
            return null;
        }

        // Var is a declared variable, return
        if (table.getLocalVariables(currentMethod).stream()
                .anyMatch(varDecl -> varDecl.getName().equals(varRefName))) {
            return null;
        }

        if (table.getFields().stream()
                .anyMatch(field -> field.getName().equals(varRefName))) {
            return null;
        }

        if (table.getMethods().stream()
                .anyMatch(method -> method.equals(varRefName))) {
            return null;
        }

        if (table.getImports().stream()
                .anyMatch(imp -> imp.equals(varRefName))) {
            return null;
        }

        if(varRefExpr.getParent().getKind().equals("MethodCallExpr")) {
            if (table.getSuper().equals(varRefName)) {
                return null;
            }
        }


        // Create error report
        var message = String.format("Variable '%s' does not exist.", varRefName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                varRefExpr.getLine(),
                varRefExpr.getColumn(),
                message,
                null)
        );

        return null;
    }


}
