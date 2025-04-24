package pt.up.fe.comp2025.optimization.passes;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.List;

public class ConstFoldOpVisitor extends AJmmVisitor<Void, JmmNode> {
    public boolean hasChanged = false;

    @Override
    protected void buildVisitor() {
        addVisit("AssignStmt", this::assignStmt);
        addVisit("VarAssignStmt", this::varAssignStmt);
        addVisit("BinaryExpr", this::binaryExpr);
        addVisit("NotExpr", this::notExpr);
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
        jmmNode.setChild(visit(jmmNode.getChild(0)), 0);
        return jmmNode;
    }

    private JmmNode binaryExpr(JmmNode jmmNode, Void unused) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getChild(0);
        JmmNode right = jmmNode.getChild(1);

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
            return result;
        }

        return jmmNode;
    }

    private JmmNode notExpr(JmmNode jmmNode, Void unused) {
        JmmNode child = jmmNode.getChild(0);
        if(child.getKind().equals("TrueExpr")){
            this.hasChanged = true;
            return createFalseNode();
        } else if(child.getKind().equals("FalseExpr")){
            this.hasChanged = true;
            return createTrueNode();
        }
        return jmmNode;
    }

    private JmmNode varAssignStmt(JmmNode jmmNode, Void unused) {
        jmmNode.setChild(visit(jmmNode.getChild(1)), 1);
        return jmmNode;
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
