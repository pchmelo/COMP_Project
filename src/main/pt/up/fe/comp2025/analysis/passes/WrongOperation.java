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

import java.util.HashMap;
import java.util.Map;

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

    private void handleBombs(SymbolTable table, String key, String value, JmmNode expression, String message) {
        Map<String, String> bombs = (Map<String, String>) table.getObject("bombs");
        if (bombs.get(key).equals(value)) {
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    expression.getLine(),
                    expression.getColumn(),
                    message,
                    null
            ));
        } else {
            bombs.put(value, key);
            table.putObject("bombs", bombs);
        }
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

        //if a = new A(); then a is instantiated
        if (expression.getKind().equals("NewObjectExpr")){
            Map<String, Boolean> isObjectInstantiatedMap = (Map<String, Boolean>) table.getObject("isObjectInstantiatedMap");
            isObjectInstantiatedMap.put(variable_.getName(),true);
            table.putObject("isObjectInstantiatedMap", isObjectInstantiatedMap);
        }

        if(!val0.getName().equals(val1.getName()) || val0.isArray() != val1.isArray() ){
            if(val0.getName().equals(val1.getName()) && expression.getHierarchy().getFirst().equals("NewIntArrayExpr")){
                return null;
            }

            if (expression.getKind().equals("MethodCall")){
                String methodCallName = expression.get("name");
                Type methodType = types.getExprType(expression,table,currentMethod);
                //if method call has never been called
                if (methodType.getName().equals("undefined")){
                    //method doesn't exist on class => assume current left type and insert it on table
                    Map<String, Type> methodCallType = (Map<String, Type>) table.getObject("methodCallType");
                    methodCallType.put(methodCallName, val0);
                }else if (methodType != val0){ //if it has been called before and doesn't match current assignment
                    var message = "Type error: cannot assign " + val0.getName() + " type with a method call of type " + val1.getName() + ".";
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

            if(expression.getKind().equals("MethodCallExpr")){
                JmmNode variableRightExpression = expression.getChild(0);
                Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
                if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper())) || (type_.getName().equals("this") && !table.getSuper().isEmpty()) ){
                    return null;
                }

                if (type_.getName().equals("String") || type_.isArray()){
                    return null;
                }

            }

            //class = extend
            if (val0.getName().equals(table.getClassName()) && val1.getName().equals(table.getSuper())){
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

            if(table.getImports().contains(val1.getName()) || val1.getName().equals(table.getClassName()) || val1.getName().equals(table.getSuper())){
                if (expression.getKind().equals("VarRefExpr")){
                    Map<String, Boolean> isObjectInstantiatedMap = (Map<String, Boolean>) table.getObject("isObjectInstantiatedMap");
                    if (!isObjectInstantiatedMap.get(expression.get("name"))){
                        var message = "Type error: Object variable used in the right expression wasn't instantiated before assignment";
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
            }

            //IM DONE WITH SIMPLIFYING CODE
            Map<String, String> bombs = (Map<String, String>) table.getObject("bombs");

            // Case 1: val0 is className and val1 is imports
            if (val0.getName().equals(table.getClassName()) && table.getImports().contains(val1.getName())) {
                handleBombs(table,val0.getName(), val1.getName(), expression,
                        "Type error: cannot assign class type to something of an Import class. Boom");
                return null;
            }
            // Case 2: val0 is super and val1 is imports
            if (table.getSuper().contains(val0.getName()) && table.getImports().contains(val1.getName())) {
                handleBombs(table, val0.getName(), val1.getName(), expression,
                        "Type error: cannot assign extended class type to something of an Import class. Boom");
                return null;
            }
            // Case 3: val0 is imports and val1 className or super
            if (table.getImports().contains(val0.getName()) && (val1.getName().equals(table.getClassName()) || val1.getName().equals(table.getSuper()))) {
                //cannot do import = class or super if no super
                if(table.getSuper().isEmpty()){
                    var message = "Type error: cannot assign something of an Import class to a variable of type class, if class doesn't extend";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expression.getLine(),
                            expression.getColumn(),
                            message,
                            null)
                    );
                    return null;
                }

                String insideBomb = bombs.get(val0.getName());
                if (!insideBomb.isEmpty()) {
                    var message = "Type error: cannot assign something of an Import class (which was assumed to be extended) to a variable of type class or extended class. Boom";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            expression.getLine(),
                            expression.getColumn(),
                            message,
                            null)
                    );
                } else {
                    bombs.put(val1.getName(), val0.getName());
                }
                return null;
            }

            if(val0.getName().equals(table.getClassName()) || val1.getName().equals(table.getClassName())){
                if (!table.getSuper().equals(val1.getName()) && !table.getSuper().equals(val0.getName())) {
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

            //if a = new A(); then a is instantiated
            if (rightExpression.getKind().equals("NewObjectExpr")){
                Map<String, Boolean> isObjectInstantiatedMap = (Map<String, Boolean>) table.getObject("isObjectInstantiatedMap");
                isObjectInstantiatedMap.put(variable_.getName(),true);
                table.putObject("isObjectInstantiatedMap", isObjectInstantiatedMap);
            }

            if(!val0.getName().equals(val1.getName()) || val0.isArray() != val1.isArray() ){
                if(val0.getName().equals(val1.getName()) && rightExpression.getHierarchy().getFirst().equals("NewIntArrayExpr")){
                    return null;
                }

                //if a = f2() and f2() is a methodCall
                if (rightExpression.getKind().equals("MethodCall")){
                    String methodCallName = rightExpression.get("name");
                    Type methodType = types.getExprType(rightExpression,table,currentMethod);
                    //if method call has never been called
                    if (methodType.getName().equals("undefined")){
                        //method doesn't exist on class => assume current left type and insert it on table
                        Map<String, Type> methodCallType = (Map<String, Type>) table.getObject("methodCallType");
                        methodCallType.put(methodCallName, val0);
                    }else if (methodType != val0){ //if it has been called before and doesn't match current assignment
                        var message = "Type error: cannot assign " + val0.getName() + " type with a method call of type " + val1.getName() + ".";
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

                if(rightExpression.getKind().equals("MethodCallExpr")){
                    JmmNode variableRightExpression = rightExpression.getChild(0);
                    Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
                    if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper())) || (type_.getName().equals("this") && !table.getSuper().isEmpty()) ){
                        return null;
                    }

                    if (type_.getName().equals("String") || type_.isArray()){
                        return null;
                    }
                }

                //class = extend
                if (val0.getName().equals(table.getClassName()) && val1.getName().equals(table.getSuper())){
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

                if(table.getImports().contains(val1.getName()) || val1.getName().equals(table.getClassName()) || val1.getName().equals(table.getSuper())){
                    if (rightExpression.getKind().equals("VarRefExpr")){
                        Map<String, Boolean> isObjectInstantiatedMap = (Map<String, Boolean>) table.getObject("isObjectInstantiatedMap");
                        if (!isObjectInstantiatedMap.get(rightExpression.get("name"))){
                            var message = "Type error: Object variable used in the right expression wasn't instantiated before assignment";
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
                }

                //IM DONE WITH SIMPLIFYING CODE
                Map<String, String> bombs = (Map<String, String>) table.getObject("bombs");

                // Case 1: val0 is className and val1 is imports
                if (val0.getName().equals(table.getClassName()) && table.getImports().contains(val1.getName())) {
                    handleBombs(table,val0.getName(), val1.getName(), expression,
                            "Type error: cannot assign class type to something of an Import class. Boom");
                    return null;
                }
                // Case 2: val0 is super and val1 is imports
                if (table.getSuper().contains(val0.getName()) && table.getImports().contains(val1.getName())) {
                    handleBombs(table, val0.getName(), val1.getName(), expression,
                            "Type error: cannot assign extended class type to something of an Import class. Boom");
                    return null;
                }
                // Case 3: val0 is imports and val1 className or super
                if (table.getImports().contains(val0.getName()) && (val1.getName().equals(table.getClassName()) || val1.getName().equals(table.getSuper()))) {
                    //cannot do import = class or super if no super
                    if(table.getSuper().isEmpty()){
                        var message = "Type error: cannot assign something of an Import class to a variable of type class, if class doesn't extend";
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                expression.getLine(),
                                expression.getColumn(),
                                message,
                                null)
                        );
                        return null;
                    }

                    String insideBomb = bombs.get(val0.getName());
                    if (!insideBomb.isEmpty()) {
                        var message = "Type error: cannot assign something of an Import class (which was assumed to be extended) to a variable of type class or extended class. Boom";
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                expression.getLine(),
                                expression.getColumn(),
                                message,
                                null)
                        );
                    } else {
                        bombs.put(val1.getName(), val0.getName());
                    }
                    return null;
                }


                if(val0.getName().equals(table.getClassName()) || val1.getName().equals(table.getClassName())){
                    if ( (!table.getSuper().equals(val1.getName()) && !table.getSuper().equals(val0.getName())) ){
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

        //if f2() + 2 and f2() is a methodCall and method call has never been called =>  assume the opposite type and insert it on table
        if (expression0.getKind().equals("MethodCall") && val0.getName().equals("undefined")){
            val0 = val1;
            Map<String, Type> methodCallType = (Map<String, Type>) table.getObject("methodCallType");
            methodCallType.put(expression0.get("name"), val0);
        }
        // 2 + f2() and f2 is ......
        if (expression1.getKind().equals("MethodCall") && val1.getName().equals("undefined")){
            val1 = val0;
            Map<String, Type> methodCallType = (Map<String, Type>) table.getObject("methodCallType");
            methodCallType.put(expression1.get("name"), val1);
        }

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
