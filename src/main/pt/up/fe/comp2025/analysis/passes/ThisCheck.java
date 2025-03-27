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

import java.util.ArrayList;
import java.util.HashMap;

public class ThisCheck extends AnalysisVisitor {
    private TypeUtils types = new TypeUtils(null);
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ARRAY_LENGTH_EXPR, this::visitArrayLengthThis);
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCallThis);
        addVisit(Kind.PARENTHESES_EXPR, this::visitSmthingThatShoulntHaveThis);
        addVisit(Kind.EXPRESSION_STMT, this::visitSmthingThatShoulntHaveThis);
        addVisit(Kind.CLASS_TYPE, this::visitClassType);
    }

    private Void visitClassType(JmmNode jmmNode, SymbolTable table) {

        if (table.getImports().contains(jmmNode.get("name")) ||table.getClassName().equals(jmmNode.get("name")) || (!table.getSuper().isEmpty() && jmmNode.get("name").equals(table.getSuper()))) {
            return null;
        }else{
            var message = "Type of variable '" + jmmNode.get("name") +"' is not defined";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    jmmNode.getLine(),
                    jmmNode.getColumn(),
                    message,
                    null)
            );
        }
        return null;
    }


    private Void visitSmthingThatShoulntHaveThis(JmmNode jmmNode, SymbolTable table) {
        JmmNode child = jmmNode.getChild(0);
        Type childType = types.getExprType(child,table,currentMethod);
        if (childType!=null && childType.getName().equals("this")){
            var message = "'this' expression shouldn't be used in this place like this";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    child.getLine(),
                    child.getColumn(),
                    message,
                    null)
            );
        }
        return null;
    }


    private Void visitMethodCallThis(JmmNode jmmNode, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        JmmNode firstExpression =  jmmNode.getChild(0);
        Type firstType = types.getExprType(firstExpression,table,currentMethod);

        String methodName = jmmNode.get("name");

        Type expressionType = types.getExprType(jmmNode, table, currentMethod);
        //FUNCAO nao EXISTE?
        if (expressionType.getName().equals("undefined")){
            if (!firstType.getName().equals("this") && !table.getClassName().equals(firstType.getName())){
                if (firstType.getName().equals("String") || firstType.isArray()){
                    return null;
                } else if (firstType.getName().equals("int") || firstType.getName().equals("boolean")) {

                    var message = "Maybe the type of variable cannot call method '" + methodName + "'.";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            jmmNode.getLine(),
                            jmmNode.getColumn(),
                            message,
                            null)
                    );
                }
                return null;
            }
        }else{
            HashMap<String, Boolean> staticMethods = (HashMap<String, Boolean>) table.getObject("staticMethods");

            if ((firstType.getName().equals("this")) || table.getClassName().equals(firstType.getName())){
                if(firstType.getName().equals("this") && staticMethods.get(methodName)){
                    var message = "Method '" + methodName + "' is static and cannot be called with 'this'";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            jmmNode.getLine(),
                            jmmNode.getColumn(),
                            message,
                            null)
                    );
                }

                return null;
            }
        }

        Type type_ = types.getExprType(jmmNode.getChild(0), table, currentMethod);
        if(type_.getName().equals("errado")){
            var message = "Idk what are you doing with Method '" + methodName +"' but whatever it is...it is WRONG";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    jmmNode.getLine(),
                    jmmNode.getColumn(),
                    message,
                    null)
            );
            return null;
        }

        if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper()))) {
            return null;
        }else if ((type_.getName().equals(table.getClassName()) || (type_.getName().equals("this")))  && !table.getSuper().isEmpty()){
            return null;
        }else{
            if(expressionType.getName().equals("undefined") && (firstType.getName().equals("this") || table.getClassName().equals(firstType.getName()))) {
                var message = "Method '" + methodName + "' is not declared";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        jmmNode.getLine(),
                        jmmNode.getColumn(),
                        message,
                        null)
                );
            }
            return null;
        }

    }

    private Void visitArrayLengthThis(JmmNode jmmNode, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");
        JmmNode firstExpression =  jmmNode.getChild(0);
        Type firstType = types.getExprType(firstExpression,table,currentMethod);

        //Teste inutil já que falha sempre quando a expressão não é array (que é o caso do this). Mas fica aqui como double proof
        if (firstType.getName().equals("this")){
            var message = "'.length' function cannot be used with 'this'";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    firstExpression.getLine(),
                    firstExpression.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }


    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");


        return null;
    }


}

