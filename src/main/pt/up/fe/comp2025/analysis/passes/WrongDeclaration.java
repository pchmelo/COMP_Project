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
        addVisit(Kind.VAR_DECL, this::visitVarInitial);
        addVisit(Kind.CONST_STMT, this::visitVarInitial);
        addVisit(Kind.VAR_ASSIGN_STMT, this::visitVarInitial);  //JUST TO CHECK IF ITS ACTUALLY BEING STORE ON LOCALS
    }

    private Void visitVarInitial(JmmNode jmmNode, SymbolTable table) {

        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        String methodName = method.get("name");
        List<String> declaredVariables = new ArrayList<>();

        for (int i = 1 + table.getParameters(methodName).size(); i < method.getChildren().size(); i++ ){
            JmmNode child = method.getChild(i);
            String kind = child.getKind();
            String name = child.get("name");
            if (kind.equals("VarDecl") || kind.equals("VarAssignStmt")  || kind.equals("ConstStmt") ){
                declaredVariables.add(name);
            }

            if (kind.equals("AssignStmt") || kind.equals("ArrayAssignStmt")){  //Postfix
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


            System.out.println(kind);
            System.out.println(child.get("name"));
        }


        return null;
    }
}