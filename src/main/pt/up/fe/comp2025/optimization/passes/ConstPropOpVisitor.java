package pt.up.fe.comp2025.optimization.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstPropOpVisitor extends AJmmVisitor<Void, Void> {
    private final Map<String, JmmNode> varDecl = new HashMap<>();
    public boolean hasChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit("AssignStmt", this::assignStmt);
        addVisit("VarRefExpr", this::varRefExpr);
        addVisit("VarAssignStmt", this::varAssignStmt);
        addVisit("Postfix", this::postfix);
        addVisit("WhileStmt", this::whileStmt);

        setDefaultVisit(this::visitDefault);
    }




    private Void visitDefault(JmmNode jmmNode, Void unused) {
        List<JmmNode> children = List.copyOf(jmmNode.getChildren());

        for(int i = 0; i < children.size(); i++){
            visit(children.get(i));
        }
        return unused;
    }

    private Void assignStmt(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        JmmNode right = jmmNode.getChild(0);
        String rightType = right.getKind();
        visit(right);

        if(rightType.equals("IntegerExpr") || rightType.equals("TrueExpr") || rightType.equals("FalseExpr")){
            if(this.varDecl.containsKey(varName)){
                Integer val = null;
                switch (jmmNode.get("op")){
                    case "+=":
                        val = Integer.parseInt(this.varDecl.get(varName).get("value")) + Integer.parseInt(right.get("value"));
                        break;
                    case "-=":
                        val = Integer.parseInt(this.varDecl.get(varName).get("value")) - Integer.parseInt(right.get("value"));
                        break;
                    case "*=":
                        val = Integer.parseInt(this.varDecl.get(varName).get("value")) * Integer.parseInt(right.get("value"));
                        break;
                    case "/=":
                        val = Integer.parseInt(this.varDecl.get(varName).get("value")) / Integer.parseInt(right.get("value"));
                        break;
                    default:
                        break;
                }
                if(val != null){
                    this.hasChanged = true;
                    right = createIntegerNode(val);
                }
            }

            if(right.getKind().equals("IntegerExpr")){
                varDecl.put(varName, createIntegerNode(Integer.parseInt(right.get("value"))));
            } else if(right.getKind().equals("TrueExpr")){
                varDecl.put(varName, createTrueNode());
            } else if (right.getKind().equals("FalseExpr")) {
                varDecl.put(varName, createFalseNode());
            }

        }

        return unused;
    }

    private Void varRefExpr(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        JmmNode parent = jmmNode.getParent();

        /*
        if (!jmmNode.getParent().getKind().equals("AssignStmt") && !jmmNode.getParent().getKind().equals("VarAssignStmt")) {
            return unused;
        }
         */
        JmmNode grandParent = parent.getParent();
        while(grandParent != null){
            if (grandParent.getKind().equals("BracketStmt")) {
                if(grandParent.getParent().getKind().equals("WhileStmt") || grandParent.getParent().getKind().equals("IfStmt")){
                    this.varDecl.remove(varName);
                    return unused;
                }
            }
            grandParent = grandParent.getParent();
        }

        if(this.varDecl.containsKey(varName)){
            this.hasChanged = true;
            int idx = jmmNode.getIndexOfSelf();
            parent.setChild(this.varDecl.get(varName), idx);
        }

        return unused;
    }

    private JmmNode createIntegerNode(int value){
        JmmNode node = new JmmNodeImpl(List.of("IntegerExpr"));
        node.put("value", String.valueOf(value));
        return node;
    }

    private Void varAssignStmt(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        JmmNode right = jmmNode.getChild(0);
        String rightType = right.getKind();
        if(rightType.equals("IntegerExpr") || rightType.equals("TrueExpr") || rightType.equals("FalseExpr")){
            varDecl.put(varName, right);
        }
        return unused;
    }

    private Void postfix(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        Integer val = null;
        int idx = jmmNode.getIndexOfSelf();

        if(this.varDecl.containsKey(varName)){
            switch (jmmNode.get("op")){
                case "++":
                    val = Integer.parseInt(this.varDecl.get(varName).get("value")) + 1;
                    this.hasChanged = true;
                    jmmNode.getParent().setChild(createIntegerNode(val), idx);

                case "--":
                    val = Integer.parseInt(this.varDecl.get(varName).get("value")) - 1;
                    this.hasChanged = true;
                    jmmNode.getParent().removeChild(idx);
                    jmmNode.getParent().setChild(createIntegerNode(val), idx);

                default:
                    break;
            }
        }
        return unused;
    }

    private Void whileStmt(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getChild(1));
        visit(jmmNode.getChild(0));
        return unused;
    }

    private JmmNode createTrueNode(){
        JmmNode node = new JmmNodeImpl(List.of("TrueExpr"));
        node.put("value", "true");
        return node;
    }

    private JmmNode createFalseNode() {
        JmmNode node = new JmmNodeImpl(List.of("FalseExpr"));
        node.put("value", "false");
        return node;
    }
}
