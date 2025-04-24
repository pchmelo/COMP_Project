package pt.up.fe.comp2025.optimization.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2025.ast.Kind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstPropOpVisitor extends AJmmVisitor<Void, JmmNode> {
    private final Map<String, JmmNode> varDecl = new HashMap<>();
    public boolean hasChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit("AssignStmt", this::assignStmt);
        addVisit("VarRefExpr", this::varRefExpr);
        addVisit("VarAssignStmt", this::varAssignStmt);
        addVisit("Postfix", this::postfix);

        setDefaultVisit(this::visitDefault);
    }


    private JmmNode visitDefault(JmmNode jmmNode, Void unused) {
        List<JmmNode> children = List.copyOf(jmmNode.getChildren());

        for(int i = 0; i < children.size(); i++){
            JmmNode new_child_visited = visit(children.get(i));
            if (new_child_visited == jmmNode.getChild(i)){
                continue;
            }
            jmmNode.setChild(new_child_visited, i);

        }
        return jmmNode;
    }

    private JmmNode assignStmt(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        JmmNode right = jmmNode.getChild(0);
        String rightType = right.getKind();

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

            varDecl.put(varName, right);
        }

        return jmmNode;
    }

    private JmmNode varRefExpr(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");

        if(this.varDecl.containsKey(varName)){
            this.hasChanged = true;
            return this.varDecl.get(varName);
        }

        return jmmNode;
    }

    private JmmNode createIntegerNode(int value){
        JmmNode node = new JmmNodeImpl(List.of("IntegerExpr"));
        node.put("value", String.valueOf(value));
        return node;
    }

    private JmmNode varAssignStmt(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        JmmNode right = jmmNode.getChild(0);
        String rightType = right.getKind();
        if(rightType.equals("IntegerExpr") || rightType.equals("TrueExpr") || rightType.equals("FalseExpr")){
            varDecl.put(varName, right);
        }
        return jmmNode;
    }

    private JmmNode postfix(JmmNode jmmNode, Void unused) {
        String varName = jmmNode.get("name");
        Integer val = null;

        if(this.varDecl.containsKey(varName)){
            switch (jmmNode.get("op")){
                case "++":
                    val = Integer.parseInt(this.varDecl.get(varName).get("value")) + 1;
                    this.hasChanged = true;
                    return createIntegerNode(val);
                case "--":
                    val = Integer.parseInt(this.varDecl.get(varName).get("value")) - 1;
                    this.hasChanged = true;
                    return createIntegerNode(val);
                default:
                    break;
            }
        }
        return jmmNode;
    }
}
