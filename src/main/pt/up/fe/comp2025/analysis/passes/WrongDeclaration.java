package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class WrongDeclaration extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
    }


    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        String methodName = method.get("name");
        List<String> declaredVariables = new ArrayList<>();

        for (int i = 1 + table.getParameters(methodName).size(); i < method.getChildren().size(); i++ ){
            JmmNode child = method.getChild(i);
            String kind = child.getKind();
            if (kind.equals("VarDecl") || kind.equals("VarAssignStmt")  || kind.equals("ConstStmt") ){
                String name = child.get("name");
                declaredVariables.add(name);
                continue;
            }

            if (kind.equals("AssignStmt") || kind.equals("ArrayAssignStmt")){  //Postfix
                String name = child.get("name");
                if(!declaredVariables.contains(name) ){
                    boolean isDeclaredAsField = false;
                    for (Symbol field : table.getFields()){
                        if (field.getName().equals(name)){
                            isDeclaredAsField = true;
                            break;
                        }
                    }
                    boolean isDeclaredAsLocal = false;
                    for (Symbol param : table.getParameters(methodName)){
                        if (param.getName().equals(name)){
                            isDeclaredAsLocal = true;
                            break;
                        }
                    }

                    if (!isDeclaredAsLocal && !isDeclaredAsField) {
                        // Create error report
                        var message = String.format("Cannot initialize before declare a variable: " + name, method);
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                method.getLine(),
                                method.getColumn(),
                                message,
                                null)
                        );
                    }
                }
            }
        }


        return null;
    }
}