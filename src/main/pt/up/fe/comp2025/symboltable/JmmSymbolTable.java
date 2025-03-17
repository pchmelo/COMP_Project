package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.*;
import java.util.stream.Collectors;

public class JmmSymbolTable extends AJmmSymbolTable {

    private final List<String> imports;
    private final String className;
    private final String superName;
    private final List<String> methods;
    private final Map<String, Type> returnTypes;
    private final List<Symbol> fields;
    private final Map<String, List<Symbol>> params;
    private final Map<String, List<Symbol>> locals;
    private final Map<String, String> varargs;


    public JmmSymbolTable(List<String> imports,
                          String className,
                          String superName,
                          List<Symbol> fields,
                          List<String> methods,
                          Map<String, Type> returnTypes,
                          Map<String, List<Symbol>> params,
                          Map<String, List<Symbol>> locals,
                          Map<String, String> varargs) {

        this.imports = imports;
        this.className = className;
        this.superName = superName;
        this.fields = fields;
        this.methods = methods;
        this.returnTypes = returnTypes;
        this.params = params;
        this.locals = locals;
        this.varargs = varargs;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }


    public Map<String, String> getVarargs() {
        return varargs;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superName;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }


    @Override
    public List<String> getMethods() {
        return methods;
    }


    @Override
    public Type getReturnType(String methodSignature) {
        // TODO: Simple implementation that needs to be expanded
        //returnTypes.get(methodSignature);

        return returnTypes.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        if(methodSignature.equals("main")) {
            return new ArrayList<>();
        }
        return params.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        if(methodSignature.equals("main")) {
            return new ArrayList<>();
        }
        return locals.get(methodSignature);
    }

    @Override
    public String toString() {
        return print();
    }


}
