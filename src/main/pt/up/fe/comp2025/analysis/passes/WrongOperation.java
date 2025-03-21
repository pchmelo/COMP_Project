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
        addVisit(Kind.VAR_ASSIGN_STMT, this::visitVarAssignmentExpr);
    }

    private Void visitVarAssignmentExpr(JmmNode jmmNode, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        Symbol variable_ = types.valueFromVarReturner(jmmNode.get("name"),table,currentMethod);
        Type val0 =  variable_.getType();

        JmmNode expression = jmmNode.getChild(1);
        Type val1 = types.getExprType(expression,table,currentMethod);

        if(val1.getName().equals("this")){
            if (val1.isArray()){
                var message = "Cannot assign an array of 'this' to anything";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
            }

            if (val0.getName().equals(table.getClassName()) || val0.getName().equals(table.getSuper() )){
                return null;
            }
            var message = "Cannot assign 'this' to a variable with type " + val0.getName();
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    expression.getLine(),
                    expression.getColumn(),
                    message,
                    null)
            );
        }


        if(!val0.getName().equals(val1.getName()) || val0.isArray() != val1.isArray() ){
            if(val0.getName().equals(val1.getName()) && expression.getHierarchy().getFirst().equals("NewIntArrayExpr")){
                return null;
            }

            if(expression.getKind().equals("MethodCallExpr")){
                JmmNode variableRightExpression = expression.getChild(0);
                Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
                if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper())) || (type_.getName().equals("this") && !table.getSuper().isEmpty()) ){
                    return null;
                }
            }

            if(val0.getName().equals(table.getClassName()) || val1.getName().equals(table.getClassName())){
                if (!table.getSuper().equals(val1.getName()) && !table.getSuper().equals(val0.getName())){
                    var message = "Type error: cannot assign " + val0.getName() + " type with " + val1.getName() + " type";
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

            if((!table.getImports().contains(val0.getName())) && (!table.getSuper().equals(val0.getName()))){
                var message = "Type error: cannot assign " + val0.getName() + " type with " + val1.getName() + " type";
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

        return null;
    }

    private Void visitAssignmentExpr(JmmNode expression, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        String operator = expression.get("op");

        Symbol variable_ = types.valueFromVarReturner(expression.get("name"),table,currentMethod);
        Type val0 =  variable_.getType();

        var rightExpression = expression.getChild(0);
        Type val1 = types.getExprType(rightExpression, table, currentMethod);

        if(operator.equals("=")){
            if ((boolean) val0.getObject("isConst")) {
                var message = "Cannot assign a value to a constant";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
            }

            if(val1.getName().equals("this")){
                if (val1.isArray()){
                    var message = "Cannot assign an array of 'this' to anything";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expression.getLine(),
                            expression.getColumn(),
                            message,
                            null)
                    );
                }

                if (val0.getName().equals(table.getClassName()) || val0.getName().equals(table.getSuper() )){
                    return null;
                }
                var message = "Cannot assign 'this' to a variable with type " + val0.getName();
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
            }



            if(!val0.getName().equals(val1.getName()) || val0.isArray() != val1.isArray() ){
                if(val0.getName().equals(val1.getName()) && rightExpression.getHierarchy().getFirst().equals("NewIntArrayExpr")){
                    return null;
                }

                if(rightExpression.getKind().equals("MethodCallExpr")){
                    JmmNode variableRightExpression = rightExpression.getChild(0);
                    Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
                    if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper())) || (type_.getName().equals("this") && !table.getSuper().isEmpty()) ){
                        return null;
                    }
                }

                if(val0.getName().equals(table.getClassName()) || val1.getName().equals(table.getClassName())){
                    if (!table.getSuper().equals(val1.getName()) && !table.getSuper().equals(val0.getName())){
                        var message = "Type error: cannot assign " + val0.getName() + " type with " + val1.getName() + " type";
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

                if((!table.getImports().contains(val0.getName())) && (!table.getSuper().equals(val0.getName()))){
                    var message = "Type error: cannot assign " + val0.getName() + " type with " + val1.getName() + " type";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expression.getLine(),
                            expression.getColumn(),
                            message,
                            null)
                    );
                }


            }
            return null;
        }else{
            if(!val0.getName().equals("int") || !val1.getName().equals("int") || val0.isArray() != val1.isArray() ){
                if(!table.getImports().contains(val0.getName())){
                    var message = "Type error: cannot do operation " + operator + " for " + val0.getName() + " type and " + val1.getName() + " type";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expression.getLine(),
                            expression.getColumn(),
                            message,
                            null)
                    );
                }
            }
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
        Type val0 = types.getExprType(expression0, table, currentMethod);
        JmmNode expression1 = expression.getChild(1);
        Type val1 = types.getExprType(expression1, table, currentMethod);

        String operator = expression.get("op");

        if (operator.equals("+")){
            if (val0.getName().equals("String") && val1.getName().equals("String") && !val0.isArray() && !val1.isArray()) {
                return null;
            }
        }

        if (operator.equals("*") || operator.equals("/") || operator.equals("+") || operator.equals("-")){
            if (val0.getName().equals("int") && val1.getName().equals("int") && !val0.isArray() && !val1.isArray()) {
                return null;
            }
        }

        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")){
            if (val0.getName().equals("int") && val1.getName().equals("int") && !val0.isArray() && !val1.isArray()) {
                return null;
            }
        }

        if (operator.equals("==") || operator.equals("!=")){
            if ((val0.getName().equals("int") && val1.getName().equals("int")) || (val0.getName().equals("boolean") && val1.getName().equals("boolean")) || (val0.getName().equals("String") && val1.getName().equals("String"))  || (val0.getName().equals(val1.getName()))) {
                if(val0.isArray() == val1.isArray()) {
                    return null;
                }
            }
        }

        if (operator.equals("&&") || operator.equals("||")){
            if (val0.getName().equals("boolean") && val1.getName().equals("boolean") && !val0.isArray() && !val1.isArray()) {
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
