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
    }

    @SuppressWarnings("LanguageDetectionInspection")
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
        int parametersNumber = jmmNode.getNumChildren() - 1; //excluido a primeira expressão que é antes do '.'

        //Teste inutil já que falha sempre quando a expressão não é array (que é o caso do this). Mas fica aqui como double proof
        if (firstType.getName().equals("this")){

            if (table.getMethods().stream().noneMatch(method -> method.equals(methodName))) {
                var message = "Method '" + methodName +"' not found inside class";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        jmmNode.getLine(),
                        jmmNode.getColumn(),
                        message,
                        null)
                );
                return null;
            }

        }else{
            Type expressionType = types.getExprType(jmmNode, table, currentMethod);
            if (expressionType == null){
                var message = "Method '" + methodName +"'is not found/supported.";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        jmmNode.getLine(),
                        jmmNode.getColumn(),
                        message,
                        null)
                );
            }else if (expressionType.getName().equals("errado") || firstType.getName().equals("errado")){
                var message = "Idk what are you doing with Method '" + methodName +"' but whatever it is...it is WRONG";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        jmmNode.getLine(),
                        jmmNode.getColumn(),
                        message,
                        null)
                );
            }

        }
        return null;
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

