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

/**
 * Checks if the type of the expression inside an operation is valid
 */


public class WrongOperation extends AnalysisVisitor {

    private String currentMethod;
    private TypeUtils types = new TypeUtils(null);

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignmentExpr);
    }

    private Void visitAssignmentExpr(JmmNode expression, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        Type val0;
        Type val1;

        String operator = expression.get("op");

        if(operator.equals("=")){

            Symbol variable_ = types.valueFromVarReturner(expression.get("var"),table,currentMethod);
            val0 =  types.valueFromTypeReturner(variable_.getType());

            var rightExpression = expression.getChild(0);
            val1 = types.valueReturner(rightExpression, table, currentMethod);

            if((val0.getName().equals("int") || val0.getName().equals("boolean") || val0.getName().equals("String"))) {
                var message = "Type error: cannot assign a in the left side a " + val0.getName() + " type";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
            }
            if(!val0.getName().equals(val1.getName())){
                var message = "Type error: cannot assign a in the left side a " + val0.getName() + " type with a " + val1.getName() + " type in the right side";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
            }
            return null;
        }

        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitBinaryExpr(JmmNode expression, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        JmmNode expression0 = expression.getChild(0);
        Type val0 = types.valueReturner(expression0, table, currentMethod);
        JmmNode expression1 = expression.getChild(1);
        Type val1 = types.valueReturner(expression1, table, currentMethod);

        String operator = expression.get("op");

        if (operator.equals("+")){
            if (val0.getName().equals("String") && val1.getName().equals("String")) {
                return null;
            }
        }

        if (operator.equals("*") || operator.equals("/") || operator.equals("+") || operator.equals("-")){
            if (val0.getName().equals("int") && val1.getName().equals("int")) {
                return null;
            }
        }

        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")){
            if (val0.getName().equals("int") && val1.getName().equals("int")) {
                return null;
            }
        }

        if (operator.equals("==") || operator.equals("!=")){
            if ((val0.getName().equals("int") && val1.getName().equals("int")) || (val0.getName().equals("boolean") && val1.getName().equals("boolean")) || (val0.getName().equals("String") && val1.getName().equals("String"))  || (val0.getName().equals(val1.getName()))) {
                return null;
            }
        }

        if (operator.equals("&&") || operator.equals("||")){
            if (val0.getName().equals("boolean") && val1.getName().equals("boolean")) {
                return null;
            }
        }

        // Create error report
        var message = String.format("Expressions not equal val for op '%s', ig it is: '%s' and '%s'", expression.get("op"), val0.getName(), val1.getName());
        addReport(Report.newError(
                Stage.SEMANTIC,
                expression.getLine(),
                expression.getColumn(),
                message,
                null)
        );

        return null;
    }


}
