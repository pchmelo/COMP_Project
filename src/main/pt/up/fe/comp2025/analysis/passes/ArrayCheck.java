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
    WrongOperation wrongOperation = new WrongOperation();
    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.MAIN_METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.ARRAY_ACCESS_EXPR, this::ArrayAccess);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }


    private Void ArrayAccess(JmmNode mainNode, SymbolTable table){
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        String arrayVar = mainNode.get("name");
        JmmNode arrayIndex = mainNode.getChild(1);

        Symbol variable_ = wrongOperation.valueFromVarReturner(arrayVar, table, currentMethod);
        val0 =  valueFromTypeReturner(variable_.getType());
        String arrayVarType = wrongOperation.valueReturner(arrayVar, table, currentMethod);
        String indexVarType = wrongOperation.valueReturner(arrayVar, table, currentMethod);

        // Error if the index is not an integer
        if (!indexVarType.equals("int")) {
            var message = "Trying to access array with index that is not an Integer";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
        }
        /*
        if (!arrayVarType.isArray()) {
            var message = "Trying to access a non-array variable as an array";
            addReport(Report.newError(Stage.SEMANTIC, mainNode.getLine(), arrayVar.getColumn(), message, null));
        }
        */


        return null;
    }
    /*
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
    */

}
