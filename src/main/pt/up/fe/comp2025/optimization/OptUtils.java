package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp2025.ast.Kind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {


    private final AccumulatorMap<String> temporaries;

    private AccumulatorMap<String> thens;

    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
        this.thens = new AccumulatorMap<>();
    }

    public String nextThen() {

        return nextTemp("then");
    }

    public String nextThen(String prefix) {

        // Subtract 1 because the base is 1
        var nextThenNum = thens.add(prefix) - 1;

        return prefix + nextThenNum;
    }

    public Void resetThen() {
        thens = new AccumulatorMap<>();
        return null ;
    }

    public String nextTemp() {

        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {

        // Subtract 1 because the base is 1
        var nextTempNum = temporaries.add(prefix) - 1;

        return prefix + nextTempNum;
    }


    public String toOllirType(JmmNode typeNode) {

       //HHHHHH??? TYPE.checkOrThrow(typeNode);
        if (typeNode.getKind().equals("VoidType")){
            return toOllirType("void");
        }else if (typeNode.getKind().equals("TypeTagNotUsed")){
            return toOllirType(types.convertType(typeNode.getChild(0)));
        }

        return toOllirType(types.convertType(typeNode));
    }

    public String toOllirType(Type type) {
        if (type.isArray()){
            return ".array" + toOllirType(type.getName());
        }
        return toOllirType(type.getName());
    }

    private String toOllirType(String typeName) {

        String type = "." + switch (typeName) {
            case "int" -> "i32";
            case "boolean" -> "bool";
            case "String" -> "String";
            default -> throw new NotImplementedException(typeName);
        };

        return type;
    }


}
