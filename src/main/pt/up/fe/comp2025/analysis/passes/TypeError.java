package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;

public class TypeError extends AnalysisVisitor {

    @Override
    public void buildVisitor() {
        //addVisit(Kind.BINARY_EXPR, this::TypeCheck);
        addVisit(Kind.ASSIGN_STMT, this::TypeCheckAssignment);

    }

    private Void TypeCheckAssignment(JmmNode mainNode, SymbolTable table) {
        JmmNode leftNode = mainNode.getChild(0);
        JmmNode rightNode = mainNode.getChild(1);

        Type leftType = TypeUtils.getExprType(leftNode, table); //get the type of the left node
        Type rightType = TypeUtils.getExprType(rightNode, table); //get the type of the right node

        //testa se o lado esquerdo é de um tipo compativel para um assignment
        if(leftType.getName().equals("int") || leftType.getName().equals("boolean") || leftType.getName().equals("String") || leftType.getName().equals("expression")) {
            var message = "Type error: cannot assign a in the left side a " + leftType.getName() + " type";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    mainNode.getLine(),
                    mainNode.getColumn(),
                    message,
                    null)
            );
            return null;
        }

        return null;
    }

    private Void ArrayAccess(JmmNode mainNode, SymbolTable table){
        JmmNode arrayVar = mainNode.getChild(0);
        JmmNode arrayIndex = mainNode.getChild(1);

        Type arrayVarType = TypeUtils.getExprType(arrayVar, table);
        Type indexVarType = TypeUtils.getExprType(arrayIndex, table);

        // Error if the index is not an integer
        if (!indexVarType.getName().equals("int")) {
            var message = "Trying to access array with index that is not an Integer";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
        }

        if (!arrayVarType.isArray()) {
            var message = "Trying to access a non-array variable as an array";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
        }

        return null;
    }

    //Verifica se o tipo do array é compativel com o tipo dos elementos que o inicializam
    private Void ArrayInicialization(JmmNode mainNode, SymbolTable table){
        JmmNode arrayVar = mainNode.getChild(0);
        List<JmmNode> arrayElements = arrayVar.getChildren();

        if(arrayElements.isEmpty()){
            return null;
        }

        Type firstArrayElementType = TypeUtils.getExprType(arrayElements.getFirst(), table);

        for (int i = 0; i < arrayElements.size(); i++) {
            Type nextArrayElementType = TypeUtils.getExprType(arrayElements.get(i), table);

            if (!firstArrayElementType.equals(nextArrayElementType)) {
                var message = "Trying to initialize an array with different types in the elements";
                addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
                break;
            }
        }


        return null;
    }

    //Verifica se o tipo que inicializa o array é um inteiro (int)
    private Void ArrayLengthInitCheck(JmmNode mainNode, SymbolTable table){
        JmmNode arrayLengthNode = mainNode.getChild(1);
        Type arrayLengthType = TypeUtils.getExprType(arrayLengthNode, table);

        if(!arrayLengthType.getName().equals("int")){
            String message = "Array must have integer length";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
        }

        return null;

    }




}
