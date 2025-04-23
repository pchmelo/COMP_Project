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


public class OverdeclaredCheck extends AnalysisVisitor {

    private String currentMethod;
    private TypeUtils types = new TypeUtils(null);

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        //addVisit(Kind.VAR_DECL, this::visitVarDecl);
        //addVisit(Kind.PARAM, this::visitVarDecl);
        //addVisit(Kind.VAR_ASSIGN_STMT, this::visitVarDecl);
        //addVisit(Kind.VAR_ARG_TYPE, this::visitVarArgDecl);
        addVisit(Kind.IMPORT_DECL, this::visitImportDecl);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");

        //Actual Test
        int count = 0;
        for (String methodOther : table.getMethods()) {
            if (methodOther.equals(currentMethod)){
                    count++;
            }
        }

        if (count > 1) {
            var message = String.format("Cannot have more than 1 methods with same signature", method);
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

    /*
    private Void visitVarArgDecl(JmmNode node, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        Type type = types.getExprType(node.getChild(0), table, currentMethod);
        int count = 0;

        for (Symbol field : table.getFields()){
            if (field.getName().equals(node.get("name")) && field.getType().getName().equals(type.getName()) && field.getType().isArray()){
                count += 1;
            }
        }

        for (Symbol param : table.getParameters(currentMethod)) {
            if (param.getName().equals(node.get("name")) && param.getType().getName().equals(type.getName()) && param.getType().isArray()) {
                count += 1;
            }
        }

        for (Symbol local : table.getLocalVariables(currentMethod)) {
            if (local.getName().equals(node.get("name")) && local.getType().getName().equals(type.getName()) && local.getType().isArray()) {
                count += 1;
            }
        }

        if (count > 1) {
            var message = String.format("Cannot have duplicated variables with same type", node);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    node.getLine(),
                    node.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }
    /*
    private Void visitVarDecl(JmmNode node, SymbolTable table) {
        int count = 0;
        for (Symbol field : table.getFields()){
            if (field.getName().equals(node.get("name")) && field.getType().equals(types.getExprType(node.getChild(0),table,currentMethod))  ){
                count += 1;
            }
        }

        if (currentMethod != null) {


            for (Symbol param : table.getParameters(currentMethod)) {
                if (param.getName().equals(node.get("name")) && param.getType().equals(types.getExprType(node.getChild(0), table, currentMethod))) {
                    count += 1;
                }
            }

            for (Symbol local : table.getLocalVariables(currentMethod)) {
                if (local.getName().equals(node.get("name")) && local.getType().equals(types.getExprType(node.getChild(0), table, currentMethod))) {
                    count += 1;
                }
            }
        }

            if (count > 1) {
                var message = String.format("Cannot have duplicated variables with same type", node);
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        node.getLine(),
                        node.getColumn(),
                        message,
                        null)
                );
            }

        return null;
    }
    */

    private Void visitImportDecl(JmmNode node, SymbolTable table) {
        int count = 0;
        for (String import_name : table.getImports()){
            String result = List.of(node.get("name").substring(1, node.get("name").length() - 1).split(", ")).getLast();
            if (import_name.equals(result)){
                count += 1;
            }
        }

        if (count > 1){
            var message = String.format("Cannot have duplicated imports", node);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    node.getLine(),
                    node.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }




}
