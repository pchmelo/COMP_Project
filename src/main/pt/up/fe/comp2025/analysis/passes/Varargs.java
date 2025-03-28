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

        Type methodTypeReturn = types.getExprType(mainNode.getChild(0), table, currentMethod);

        if (mainNode.getChild(0).getChild(0).getKind().equals("MethodCallExpr")){
            JmmNode variableRightExpression = mainNode.getChild(0).getChild(0).getChild(0);
            Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
            if(table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper()))){
                return null;
            }
            if (type_.getName().equals("String") || type_.isArray()){
                return null;
            }
        }


        if(!methodTypeReturn.getName().equals(currentMethodType.getName())){
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

        Type type_ = types.getExprType(mainNode.getChild(0), table, currentMethod);
        if (table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper())) || ((type_.getName().equals("this") || (type_.getName().equals(table.getClassName())) ) && !table.getSuper().isEmpty()) ) {
            return null;
        }


        if (type_.getName().equals("String") || type_.isArray()){
            return null;
        }

        /* Type typeMainNode = types.getExprType(mainNode, table, currentMethod);

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
        }*/

        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        JmmNode methodClass = mainNode.getChild(0);
        String methodName = mainNode.get("name");

        List<Symbol> parameters = table.getParameters(methodName);
        List<JmmNode> sendedArguments = mainNode.getChildren().subList(1, mainNode.getChildren().size());

        boolean isVarArg = false;
        String varagType = null;

        Type sendedParamType;
        Type currentParamType;

        for(int i = 0; i < sendedArguments.size(); i++){
            sendedParamType = types.getExprType(sendedArguments.get(i), table, currentMethod);

            if(parameters.size() >= i && !isVarArg){

                if(!table.getParameters(methodName).get(i).getType().getAttributes().isEmpty()){
                    isVarArg = true;
                    varagType = table.getParameters(methodName).get(i).getType().getName();
                }
                currentParamType = parameters.get(i).getType();
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
            else if(!isVarArg){
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
            if(isVarArg){
                if(sendedParamType.getName().equals(varagType)){
                    continue;
                }
                else{
                    var message = String.format("Argument type is different from the vararg method declared");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                        );
                }
            }

        }
        return null;
    }


}
