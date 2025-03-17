package pt.up.fe.comp2025.analysis.passes;

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
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        Type methodTypeReturn = types.valueReturner(mainNode.getChild(0), table, currentMethod);

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


}
