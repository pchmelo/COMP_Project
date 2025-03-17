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

public class NewCheck extends AnalysisVisitor {
    private TypeUtils types = new TypeUtils(null);
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.NEW_INT_ARRAY_EXPR, this::isNewAttributeInt);
        addVisit(Kind.NOT_EXPR, this::isNotExprBool);
        addVisit(Kind.POSTFIX, this::isPostfixExprInt);
    }

    private Void isPostfixExprInt(JmmNode jmmNode, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        String expression = jmmNode.get("name");
        Symbol variable_ = types.valueFromVarReturner(expression,table,currentMethod);
        Type expressionType = variable_.getType();

        if(!expressionType.getName().equals("int") || expressionType.isArray()){
            String message = "expression with postfix operator must be int";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        return null;
    }

    private Void isNotExprBool(JmmNode jmmNode, SymbolTable table) {
        JmmNode expression = jmmNode.getChild(0);
        Type expressionType = types.valueReturner(expression, table, currentMethod);

        if(!expressionType.getName().equals("boolean") || expressionType.isArray()){
            String message = "expression with operator '!' must be boolean";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void isNewAttributeInt(JmmNode jmmNode, SymbolTable table) {
        JmmNode newArrayLength = jmmNode.getChild(0);
        Type newArrayType = types.valueReturner(newArrayLength, table, currentMethod);

        if(!newArrayType.getName().equals("int") || newArrayType.isArray()){
            String message = "Array must have integer length";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        return null;
    }



}

