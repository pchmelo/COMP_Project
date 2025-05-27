package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class JasminUtils {

    private final OllirResult ollirResult;

    public JasminUtils(OllirResult ollirResult) {
        // Can be useful to have if you expand this class with more methods
        this.ollirResult = ollirResult;
    }


    public String getModifier(AccessModifier accessModifier) {
        return accessModifier != AccessModifier.DEFAULT ?
                accessModifier.name().toLowerCase() + " " :
                "";
    }

    public String getArrayType(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "int";
                default -> throw new RuntimeException("Unknown type: " + builtinType);
            };
        }

        throw new NotImplementedException("Type not implemented: " + type);
    }

    public String getPrefix(Type type) {
        if (type instanceof ArrayType arrayType) {
            return "a";
        }

        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "i";
                case BOOLEAN -> "z";
                case STRING -> "Ljava/lang/String;";
                default -> throw new RuntimeException("Unknown type: " + builtinType);
            };
        }

        throw new NotImplementedException("Type not implemented: " + type);
    }

    public String getDescriptor(Type type) {
        if (type instanceof ArrayType arrayType) {
            return "[" + getDescriptor(arrayType.getElementType());
        }

        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case STRING -> "Ljava/lang/String;";
                case VOID -> "V";
                default -> throw new RuntimeException("Unknown type: " + builtinType);
            };
        }

        if (type instanceof ClassType classType) {
            return "L" + classType.getName().replace(".", "/") + ";";
        }

        throw new NotImplementedException("Type not implemented: " + type);
    }

    public String getStoreInstruction(Type type, Integer reg) {
        StringBuilder code = new StringBuilder();
        code.append(getPrefixStoreLoad(type));
        code.append("store");
        if(reg <= 3 && reg >= 0){
            code.append("_");
        }
        else{
            code.append(" ");
        }
        code.append(reg);
        return code.toString();
    }

    public String getPrefixStoreLoad(Type type){
        StringBuilder prefix = new StringBuilder();
        if (type instanceof BuiltinType builtinType) {
            switch (builtinType.getKind()) {
                case INT32, BOOLEAN -> {
                    prefix.append("i");
                }
                case STRING -> {
                    prefix.append("a");
                }
                default -> throw new NotImplementedException("Type not implemented: " + type);
            };
        }
        return prefix.toString();
    }



}
