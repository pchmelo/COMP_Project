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

public class ArrayCheck extends AnalysisVisitor {
    private TypeUtils types = new TypeUtils(null);
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::ArrayAccess);
        addVisit(Kind.ARRAY_INIT, this::ArrayInicialization);
        addVisit(Kind.ARRAY_LENGTH_EXPR, this::IsExpressionArray);
        addVisit(Kind.ARRAY_ASSIGN_STMT, this::IsArrayAssignmentRight);
    }

    private Void IsArrayAssignmentRight(JmmNode jmmNode, SymbolTable table) {
        String variable = jmmNode.get("name");
        JmmNode indexExpression = jmmNode.getChild(0);
        JmmNode assigningExpression = jmmNode.getChild(1);

        Symbol variable_ = types.valueFromVarReturner(variable,table,currentMethod);
        Type variableType = variable_.getType();
        Type indexType = types.getExprType(indexExpression,table,currentMethod);
        Type assigningType = types.getExprType(assigningExpression,table,currentMethod);

        if(!variableType.isArray()){
            String message = "Array variable '" + variable + "' which is having its element assigned is not actually an array";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        if(!assigningType.getName().equals(variableType.getName())){
            String message = "An element of the array variable '" + variable + "' and type '" + variableType.getName() +"' is being assigned with the type '" + assigningType.getName() +"'";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        if(!indexType.getName().equals("int")){
            String message = "Array variable '" + variable + "' is having one of its elements being accessed with a value of type '" + indexType.getName() +"'";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        if(indexType.isArray()){
            String message = "Array variable '" + variable + "' is having one of its elements being accessed with an array";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        return null;
    }

    private Void IsExpressionArray(JmmNode jmmNode, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        JmmNode expression = jmmNode.getChild(0);
        Type expressionType = types.getExprType(expression, table, currentMethod);

        if(!expressionType.isArray()){
            String message = "Length method must be accessed by an array expression";
            addReport(Report.newError(Stage.SEMANTIC, jmmNode.getLine(), jmmNode.getColumn(), message, null));
        }

        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }


    private Void ArrayAccess(JmmNode mainNode, SymbolTable table){
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        Type arrayVarType =  types.getExprType(mainNode.getChild(0), table, currentMethod);
        Type indexVarType = types.getExprType(mainNode.getChild(1), table, currentMethod);

        // Error if the index is not an integer
        if (!indexVarType.getName().equals("int")) {
            var message = "Trying to access array with index that is not an Integer";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), mainNode.getColumn(), message, null));
        }

        if (!arrayVarType.isArray()) {
            var message = "Trying to access a non-array variable as an array";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), mainNode.getColumn(), message, null));
        }

        return null;
    }


    //Verifica se o tipo do array é compativel com o tipo dos elementos que o inicializam
    private Void ArrayInicialization(JmmNode mainNode, SymbolTable table){
        List<JmmNode> arrayElements = mainNode.getChildren();

        if(arrayElements.isEmpty()){
            return null;
        }

        Type firstArrayElementType = types.getExprType(arrayElements.get(0), table, currentMethod);

        for (int i = 1; i < arrayElements.size(); i++) {
            Type nextArrayElementType = types.getExprType(arrayElements.get(i), table, currentMethod);

            if (!firstArrayElementType.getName().equals(nextArrayElementType.getName())) {
                var message = "Trying to initialize an array with different types in the elements";
                addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), mainNode.getColumn(), message, null));
                break;
            }
        }


        return null;
    }


    //Verifica se o tipo que inicializa o array é um inteiro (int)
    private Void ArrayLengthInitCheck(JmmNode mainNode, SymbolTable table){
        JmmNode arrayLengthNode = mainNode.getChild(1);
        Type arrayLengthType = types.getExprType(arrayLengthNode.getChild(0), table, currentMethod);

        if(!arrayLengthType.getName().equals("int")){
            String message = "Array must have integer length";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), mainNode.getColumn(), message, null));
        }

        return null;

    }




}

