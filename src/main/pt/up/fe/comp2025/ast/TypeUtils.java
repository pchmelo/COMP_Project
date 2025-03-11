package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

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

    public static Type newIntArrayType() { return new Type("int", true); }

    public static Type newBooleanType() {
        return new Type("boolean", false);
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


}
