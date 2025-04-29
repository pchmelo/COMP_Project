package pt.up.fe.comp2025.optimization.passes;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.List;

public class ConstFoldOpVisitor extends AJmmVisitor<Void, Void> {
    public boolean hasChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit("AssignStmt", this::assignStmt);
        addVisit("VarAssignStmt", this::varAssignStmt);
        addVisit("BinaryExpr", this::binaryExpr);
        addVisit("NotExpr", this::notExpr);
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
        visit(jmmNode.getChild(0));
        return unused;
    }

    private Void binaryExpr(JmmNode jmmNode, Void unused) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getChild(0);
        JmmNode right = jmmNode.getChild(1);

        visit(left);
        visit(right);

        int leftValueInt;
        int rightValueInt;

        boolean leftValueBoolean;
        boolean rightValueBoolean;

        JmmNode result = null;

        switch (op){
            case "+":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    result = createIntegerNode(leftValueInt + rightValueInt);
                }
                break;
            case "-":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    result = createIntegerNode(leftValueInt - rightValueInt);
                }
                break;
            case "*":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    result = createIntegerNode(leftValueInt * rightValueInt);
                }
                break;
            case "/":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    result = createIntegerNode(leftValueInt / rightValueInt);
                }
                break;
            case "&&":
                if((left.getKind().equals("TrueExpr") || left.getKind().equals("FalseExpr")) && (right.getKind().equals("TrueExpr") || right.getKind().equals("FalseExpr"))) {
                    leftValueBoolean = Boolean.parseBoolean(left.get("value"));
                    rightValueBoolean = Boolean.parseBoolean(right.get("value"));
                    if(leftValueBoolean&& rightValueBoolean){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case "||":
                if((left.getKind().equals("TrueExpr") || left.getKind().equals("FalseExpr")) && (right.getKind().equals("TrueExpr") || right.getKind().equals("FalseExpr"))) {
                    leftValueBoolean = Boolean.parseBoolean(left.get("value"));
                    rightValueBoolean = Boolean.parseBoolean(right.get("value"));
                    if(leftValueBoolean|| rightValueBoolean){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case "==":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt == rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                } else if ((left.getKind().equals("TrueExpr") || left.getKind().equals("FalseExpr")) && (right.getKind().equals("TrueExpr") || right.getKind().equals("FalseExpr"))) {
                    leftValueBoolean = Boolean.parseBoolean(left.get("value"));
                    rightValueBoolean = Boolean.parseBoolean(right.get("value"));
                    if(leftValueBoolean== rightValueBoolean){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case "!=":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt != rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                } else if ((left.getKind().equals("TrueExpr") || left.getKind().equals("FalseExpr")) && (right.getKind().equals("TrueExpr") || right.getKind().equals("FalseExpr"))) {
                    leftValueBoolean = Boolean.parseBoolean(left.get("value"));
                    rightValueBoolean = Boolean.parseBoolean(right.get("value"));
                    if(leftValueBoolean!= rightValueBoolean){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case "<":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt < rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case "<=":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt <= rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case ">":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt > rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
                break;
            case ">=":
                if(left.getKind().equals("IntegerExpr") && right.getKind().equals("IntegerExpr")){
                    leftValueInt = Integer.parseInt(left.get("value"));
                    rightValueInt = Integer.parseInt(right.get("value"));
                    if(leftValueInt >= rightValueInt){
                        result = createTrueNode();
                    } else {
                        result = createFalseNode();
                    }
                }
            default:
                break;
        }

        if(result != null){
            this.hasChanged = true;
            JmmNode parent = jmmNode.getParent();
            int idx = jmmNode.getIndexOfSelf();
            parent.setChild(result, idx);

            return unused;
        }

        return unused;
    }

    private Void notExpr(JmmNode jmmNode, Void unused) {
        JmmNode child = jmmNode.getChild(0);
        if(child.getKind().equals("TrueExpr")){
            this.hasChanged = true;
            JmmNode parent = jmmNode.getParent();
            int idx = jmmNode.getIndexOfSelf();
            parent.setChild(createFalseNode(), idx);


        } else if(child.getKind().equals("FalseExpr")){
            this.hasChanged = true;
            JmmNode parent = jmmNode.getParent();
            int idx = jmmNode.getIndexOfSelf();
            parent.setChild(createTrueNode(), idx);

        }
        return unused;
    }

    private Void varAssignStmt(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getChild(1));
        return unused;
    }

    private JmmNode createIntegerNode(int value){
        JmmNode node = new JmmNodeImpl(List.of("IntegerExpr"));
        node.put("value", String.valueOf(value));
        return node;
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
