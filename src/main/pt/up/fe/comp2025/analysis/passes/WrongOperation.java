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
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class WrongOperation extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(Kind.ASSIGN_STMT, this::visitBinaryExpr);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitBinaryExpr(JmmNode expression, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        String val0;
        String val1;

        String operator = expression.get("op");

        if(operator.equals("=")){

            Symbol variable_ = valueFromVarReturner(expression.get("var"),table,currentMethod);
            val0 =  valueFromTypeReturner(variable_.getType());

            var rightExpression = expression.getChild(0);
            val1 = valueReturner(rightExpression, table, currentMethod);

            if((val0.equals("int") || val0.equals("boolean") || val0.equals("String"))) {
                var message = "Type error: cannot assign a in the left side a " + val0 + " type";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        expression.getLine(),
                        expression.getColumn(),
                        message,
                        null)
                );
                return null;
            }
            if(!val0.equals(val1)){
                var message = "Type error: cannot assign a in the left side a " + val0 + " type with a " + val1 + " type in the right side";
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

        var expression0 = expression.getChild(0);
        val0 = valueReturner(expression0, table, currentMethod);
        var expression1 = expression.getChild(1);
        val1 = valueReturner(expression1, table, currentMethod);

        if (operator.equals("+")){
            if (val0.equals("String") && val1.equals("String")) {
                return null;
            }
        }

        if (operator.equals("*") || operator.equals("/") || operator.equals("+") || operator.equals("-")){
            if (val0.equals("int") && val1.equals("int")) {
                return null;
            }
        }

        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")){
            if (val0.equals("int") && val1.equals("int")) {
                return null;
            }
        }

        if (operator.equals("==") || operator.equals("!=")){
            if ((val0.equals("int") && val1.equals("int")) || (val0.equals("boolean") && val1.equals("boolean")) || (val0.equals("String") && val1.equals("String"))  || (val0.equals(val1))) {
                return null;
            }
        }

        if (operator.equals("&&") || operator.equals("||")){
            if (val0.equals("boolean") && val1.equals("boolean")) {
                return null;
            }
        }

        // Create error report
        var message = String.format("Expressions not equal val for op '%s', ig it is: '%s' and '%s'", expression.get("op"), val0, val1);
        addReport(Report.newError(
                Stage.SEMANTIC,
                expression.getLine(),
                expression.getColumn(),
                message,
                null)
        );

        return null;
    }


    private String valueReturner(JmmNode node, SymbolTable table, String currentMethod) {
        String kind = node.getKind();
        switch (kind){
            case "BinaryExpr":  //o codigo de binary op não precisa disto mas pus porque pode ser necessario ig
                // rezando que as expressoes são do mesmo tipo
                String operator = node.get("op");
                if (operator.equals("&&") || operator.equals("||") || operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=") || operator.equals("==") || operator.equals("!=") ){
                    return "boolean";
                }else if (operator.equals("*") || operator.equals("/") || operator.equals("-")){
                    return "int";
                }else{
                    //For '+' guessing based on the left node
                    return valueReturner(node.getChild(0), table, currentMethod);
                }

            case "IntegerExpr", "ArrayLengthExpr", "Postfix":
                return "int";
            case "TrueExpr" , "FalseExpr":
                return "boolean";
            case "MethodCallExpr":
                String methodName = node.get("name");
                Type returnType = table.getReturnType(methodName);
                return valueFromTypeReturner(returnType);
            case "ArrayAccessExpr":
                Symbol variable = valueFromVarReturner(node.get("name"),table,currentMethod);
                return variable.getType().getName();
            case "VarRefExpr":
                Symbol variable_ = valueFromVarReturner(node.get("name"),table,currentMethod);
                return valueFromTypeReturner(variable_.getType());
            case "ThisExpr":
                return "this";  //tecnicamente dará sempre erro sozinho. só não dá erro quando this.metodo pois o type é return type do metodo e para this.varivel que é a variavel...
            case "ParenthesesExpr":
                return valueReturner(node.getChild(0),table,currentMethod);
            default:
                System.out.println("I am "+ kind);
                return "outro";
        }
    }

    private Symbol valueFromVarReturner(String name, SymbolTable table, String currentMethod) {
        for (Symbol field : table.getFields()){
            if (field.getName().equals(name)){
                return field;
            }
        }
        for (Symbol local : table.getLocalVariables(currentMethod)){
            if (local.getName().equals(name)){
                return local;
            }
        }
        for (Symbol param : table.getParameters(currentMethod)){
            if (param.getName().equals(name)){
                return param;
            }
        }
        return new Symbol(new Type("errado",false),"errado");  //se temos undeclaredvariables muito improvavel de chegar aqui
    }

    private String valueFromTypeReturner(Type returnType){
        if (returnType.isArray()) {
            return returnType.getName()+"Array";
        }
        return returnType.getName();
    }




}
