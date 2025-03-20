package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

import java.util.List;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

    public static Type newArrayType(String name) { return new Type(name, true); }

    public static Type newBooleanType() {
        return new Type("boolean", false);
    }

    public static Type newStringType() {
        return new Type("String", false);
    }

    public static Type newVoidType() {
        return new Type("void", false);
    }

    public static Type newObjectType(String name) { return new Type(name, false);
    }

    public static Type convertType(JmmNode typeNode) {

        // TODO: When you support new types, this must be updated
        var name = typeNode.get("name");
        var isArray = false;

        return new Type(name, isArray);
    }


    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public Type getExprType(JmmNode expr) {

        // TODO: Update when there are new types
        return new Type("int", false);
    }

    public Type getExprType(JmmNode node, SymbolTable table, String currentMethod) {
        String kind = node.getKind();
        switch (kind){
            case "BinaryExpr":  //o codigo de binary op não precisa disto mas pus porque pode ser necessario ig
                // rezando que as expressoes são do mesmo tipo
                String operator = node.get("op");
                if (operator.equals("&&") || operator.equals("||") || operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=") || operator.equals("==") || operator.equals("!=") ){
                    return new Type("boolean", false);
                }else if (operator.equals("*") || operator.equals("/") || operator.equals("-")){
                    return new Type("int", false);
                }else{
                    //For '+' guessing based on the left node
                    Type left = getExprType(node.getChild(0), table, currentMethod);
                    Type right = getExprType(node.getChild(1), table, currentMethod);
                    return left;
                }
            case "#DflType":
                return new Type(node.get("name"), false);

            case "IntegerExpr", "ArrayLengthExpr", "Postfix", "IntType":
                return new Type("int", false);
            case "TrueExpr" , "FalseExpr", "NotExpr", "BooleanType":
                return new Type("boolean", false);
            case "StringType":
                return new Type("String", false);
            case "MethodCallExpr":
                String methodName = node.get("name");
                Type type = table.getReturnType(methodName);
                if (type == null){
                    return new Type("undefined", false);
                }
                return type;
            case "ArrayAccessExpr":
                return getExprType(node.getChild(0), table, currentMethod);
            case "VarRefExpr":
                Symbol variable_ = valueFromVarReturner(node.get("name"),table,currentMethod);
                return variable_.getType();
            case "ArrayInit":
                List<JmmNode> arrayElements = node.getChildren();

                if(arrayElements.isEmpty()){
                    return null;
                }
                Type type_ = getExprType(arrayElements.get(0), table, currentMethod);
                return new Type(type_.getName(),true);
            case "ThisExpr":
                 return new Type("this", false);  //tecnicamente dará sempre erro sozinho. só não dá erro quando this.metodo pois o type é return type do metodo e para this.varivel que é a variavel...
            case "ParenthesesExpr":
                return getExprType(node.getChild(0),table,currentMethod);
            case "NewObjectExpr":
                return new Type(node.get("name"), false);
            case "VarArgType":
                return getExprType(node.getChild(0),table,currentMethod);
            case "ReturnStatement":
                List<JmmNode> children = node.getChildren();
                if(children.isEmpty()){
                    return new Type("void", false);
                }
                return getExprType(children.getFirst(), table, currentMethod);
            case "NewIntArrayExpr":
                return getExprType(node.getChild(0), table, currentMethod);
            default:
                System.out.println("I am "+ kind);
                return new Type("outro", false);
        }
    }

    public Symbol valueFromVarReturner(String name, SymbolTable table, String currentMethod) {
        for (Symbol field : table.getFields()){
            if (field.getName().equals(name)){
                return field;
            }
        }
        for (Symbol local : table.getLocalVariables(currentMethod)){
            if (local.getName().equals(name)){
                return local;
            }
        }
        for (Symbol param : table.getParameters(currentMethod)){
            if (param.getName().equals(name)){
                return param;
            }
        }
        if(table.getImports().contains(name)){
            return new Symbol(new Type(name,false),name);
        }
        if (!table.getSuper().isEmpty()){
            if(name.equals(table.getSuper())){
                return new Symbol(new Type(name,false),name);
            }
        }
        return new Symbol(new Type("errado",false),"errado");  //se temos undeclaredvariables muito improvavel de chegar aqui
    }



}
