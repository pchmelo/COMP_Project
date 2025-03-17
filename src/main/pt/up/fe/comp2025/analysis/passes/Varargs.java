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

public class Varargs extends AnalysisVisitor {

    private String currentMethod;
    private Type currentMethodType;
    private TypeUtils types = new TypeUtils(null);

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);

        addVisit(Kind.RETURN_STMT, this::checkReturnExpression);
        addVisit(Kind.METHOD_CALL_EXPR, this::checkCallMethodExpression);
    }

    private Void visitMethodDecl(JmmNode mainNode, SymbolTable table) {
        currentMethod = mainNode.get("name");
        currentMethodType = table.getReturnType(currentMethod);

        List<JmmNode> children = mainNode.getChildren();
        boolean isVarArg = false;

        for (JmmNode child : children) {
            if (child.getHierarchy().getLast().equals("Argument")) {
                if (child.getChild(0).getHierarchy().getFirst().equals("VarArgType")) {
                    isVarArg = true;
                    continue;
                }

                if (isVarArg) {
                    var message = String.format("VarArg is declared not in the final");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                    );
                    break;
                }

            }
        }

        return null;
    }



    private Void checkReturnExpression(JmmNode mainNode, SymbolTable table){

        Type methodTypeReturn = types.valueReturner(mainNode.getChild(0), table, currentMethod);

        if(!methodTypeReturn.getName().equals(currentMethodType.getName()) && !methodTypeReturn.getName().equals("Import")){
            var message = String.format("Return type is different form the method declared");
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    mainNode.getLine(),
                    mainNode.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }

    private Void checkCallMethodExpression(JmmNode mainNode, SymbolTable table){
        Type typeMainNode = types.valueReturner(mainNode, table, currentMethod);

        if(typeMainNode == null){
            var message = String.format("Method call is not declared");
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    mainNode.getLine(),
                    mainNode.getColumn(),
                    message,
                    null)
            );
            return null;
        }

        if(typeMainNode.getName().equals("Import") || typeMainNode.getName().equals("Super")){
            return null;
        }

        if(!table.getMethods().contains(mainNode.get("name"))){
            var message = String.format("Method '%s' is not declared", mainNode.get("name"));
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    mainNode.getLine(),
                    mainNode.getColumn(),
                    message,
                    null)
            );
            return null;
        }

        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        JmmNode methodClass = mainNode.getChild(0);
        String methodName = mainNode.get("name");

        List<Symbol> parameters = table.getParameters(methodName);
        List<JmmNode> sendedArguments = mainNode.getChildren().subList(1, mainNode.getChildren().size());

        for(int i = 0; i < sendedArguments.size(); i++){
            if(parameters.size() <= i){
                Type currentParamType = parameters.get(i).getType();
                Type sendedParamType = types.valueReturner(sendedArguments.get(i), table, currentMethod);

                if(!currentParamType.getName().equals(sendedParamType.getName())){
                    var message = String.format("Argument type is different from the method declared");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                    );
                }

            }
            else{
                var message = String.format("Method call has more arguments than the method declared");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        mainNode.getLine(),
                        mainNode.getColumn(),
                        message,
                        null)
                );
                break;
            }
        }
        return null;
    }


}
