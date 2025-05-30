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

        //teste para a class main
        if(currentMethod.equals("main")){
            //testa para ver se é static
            try{
                var static_var = mainNode.get("st");
            }
            catch (Exception e){
                var message = String.format("Main must be static");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        mainNode.getLine(),
                        mainNode.getColumn(),
                        message,
                        null)
                );
            }

            //testa para ver se é void
            if(!mainNode.getChild(0).getKind().equals("VoidType")){
                var message = String.format("Main must be void");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        mainNode.getLine(),
                        mainNode.getColumn(),
                        message,
                        null)
                );
            }

            //testa para ver se tem 1 argumento "String" []
            Type argument = types.getExprType(mainNode.getChild(1), table, currentMethod);
            if(!argument.getName().equals("String") || !argument.isArray()){
                var message = String.format("Main must have 1 argument of type String []");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        mainNode.getLine(),
                        mainNode.getColumn(),
                        message,
                        null)
                );
            }

            try{
                //check if the main method has any other arguments
                if(mainNode.getChild(2).getKind().equals("Param") || mainNode.getChild(2).getKind().equals("VarArgType")){
                    var message = String.format("Main must not have any other arguments");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                    );
                }
            }
            catch (Exception e){
                //do nothing
            }
        }

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

        /*if (mainNode.getChild(0).getChild(0).getKind().equals("MethodCallExpr")){
            JmmNode variableRightExpression = mainNode.getChild(0).getChild(0).getChild(0);
            Type type_ = types.getExprType(variableRightExpression, table, currentMethod);
            if(table.getImports().contains(type_.getName()) || (!table.getSuper().isEmpty() && type_.getName().equals(table.getSuper()))){
                return null;
            }
            if (type_.getName().equals("String") || type_.isArray()){
                return null;
            }
        }*/

        /**FIX MUITO FEIO :(**/
        if (methodTypeReturn.isArray() && !mainNode.getChild(0).getChildren().isEmpty()){
            JmmNode expressionNode = mainNode.getChild(0).getChild(0);
            if (expressionNode.getKind().equals("ArrayAccessExpr")){
                return null;
            }
        }

        if(methodTypeReturn.getName().equals("undefined")){
            return null;
        }

        if(!methodTypeReturn.getName().equals(currentMethodType.getName()) || methodTypeReturn.isArray() != currentMethodType.isArray() ){
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
                    varagType = table.getParameters(methodName).getLast().getType().getName();
                }
                currentParamType = parameters.get(i).getType();
                if(!currentParamType.getName().equals(sendedParamType.getName())){

                    //vvvv Skip error if the argument is 'this' and method requires className type
                    if (table.getClassName().equals(currentParamType.getName()) && sendedParamType.getName().equals("this")){
                        continue;
                    }

                    var message = String.format("Argument type is different from the method declared");
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            mainNode.getLine(),
                            mainNode.getColumn(),
                            message,
                            null)
                        );

                }
                continue;
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
