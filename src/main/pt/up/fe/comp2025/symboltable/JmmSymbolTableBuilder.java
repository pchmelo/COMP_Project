package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.*;

public class JmmSymbolTableBuilder {

    // In case we want to already check for some semantic errors during symbol table building.
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                node.getLine(),
                node.getColumn(),
                message,
                null);
    }

    public JmmSymbolTable build(JmmNode root) {

        reports = new ArrayList<>();
        Map<String, Boolean> staticMethods = new HashMap<>();
        Map<String, Boolean> isObjectInstantiatedMap = new HashMap<>();
        Map<String, String> bomb = new HashMap<>();
        Map<String, Type> methodCallType = new HashMap<>();

        var imports = buildImports(root.getChildren(IMPORT_DECL), bomb);
        var classDecl = root.getChildren(CLASS_DECL).getFirst();
        SpecsCheck.checkArgument(CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);
        String className = classDecl.get("name");
        var superName = buildSuperName(classDecl);
        var fields = buildFields(classDecl, isObjectInstantiatedMap);

        var methods = buildMethods(classDecl, staticMethods);
        var returnTypes = buildReturnTypes(classDecl);
        var params = buildParams(classDecl, isObjectInstantiatedMap);
        var locals = buildLocals(classDecl, isObjectInstantiatedMap);
        var varargs = buildVarArgs(classDecl);

        JmmSymbolTable table = new JmmSymbolTable(imports,className, superName , fields, methods, returnTypes, params, locals);
        table.putObject("staticMethods", staticMethods);
        table.putObject("isObjectInstantiatedMap",isObjectInstantiatedMap);
        bomb.put(className,"");
        bomb.put(superName,"");
        table.putObject("bombs", bomb);
        table.putObject("methodCallType", methodCallType);
        table.putObject("varargs", varargs);

        return table;
    }

    private List<String> buildVarArgs(JmmNode classDecl) {
        List<String> varargs = new ArrayList<>();
        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            List<JmmNode> listParam = method.getChildren(VAR_ARG_TYPE);
            if (!listParam.isEmpty()) {
                varargs.add(name);
            }
        }
        return varargs;
    }

    private List<Symbol> buildFields(JmmNode classDecl, Map<String,Boolean> isObjectInstantiatedMap) {
        List<Symbol> fields = new ArrayList<>();
        List<JmmNode> children = classDecl.getChildren(VAR_DECL);
        for (JmmNode child : children){
            Type returnType = typerReturner(child);
            Symbol tempAux = new Symbol(returnType, child.get("name"));

            boolean exists = fields.stream().anyMatch(symbol -> symbol.getName().equals(tempAux.getName()));
            if (exists) {
                var message = String.format("Cannot have more than 1 field with the same name: %s", child.get("name"));
                reports.add(newError(child, message));
            }

            fields.add(tempAux);
            isObjectInstantiatedMap.put(child.get("name"), false);
        }
        return fields;
    }

    private String buildSuperName(JmmNode classDecl) {
        if (classDecl.hasAttribute("superName")) {
            return classDecl.get("superName");
        }
        return "";
    }

    private List<String> buildImports(List<JmmNode> children, Map<String, String> bomb) {
        List<String> list = new ArrayList<>();
        List<String> resultList = List.of();
        for (JmmNode child : children) {

            //To transform "[io, io2]" in ["io2"]
            resultList = List.of(child.get("name").substring(1, child.get("name").length() - 1).split(", "));
            list.add(resultList.getLast());
            bomb.put(resultList.getLast(),"");
        }
        return list;
    }

    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();
        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            JmmNode child = method.getChild(0);
            Type returnType;
            if (child.getKind().equals("VoidType")){
                returnType = TypeUtils.newVoidType();
            }else {
                returnType = typerReturner(child);
            }
            map.put(name, returnType);
        }
        for (var method : classDecl.getChildren(MAIN_METHOD_DECL)) {
            map.put("main", TypeUtils.newVoidType());
        }


        return map;
    }

    private Type typerReturner(JmmNode child){
        Type returnType;
        String kind = child.getChild(0).getKind();
        if(kind.equals("DflType")){
            kind = child.getChild(0).getChild(0).getKind();
            switch (kind) {
                case "IntType":
                    returnType = TypeUtils.newIntType();
                    break;
                case "BooleanType":
                    returnType = TypeUtils.newBooleanType();
                    break;
                case "StringType":
                    returnType = TypeUtils.newStringType();
                    break;
                default:
                    returnType = TypeUtils.newObjectType(child.getChild(0).get("name"));
                    break;
            }
        }else{
            switch (kind) {
                case "ClassType":
                    returnType = TypeUtils.newObjectType(child.getChild(0).get("name"));
                    break;
                case "ArrayType":
                    returnType = TypeUtils.newArrayType(child.getChild(0).getChild(0).get("name"));
                    break;
                default:
                    returnType = TypeUtils.newObjectType(child.getChild(0).get("name"));
                    break;
            }
        }
        return returnType;
    }

    private Map<String, List<Symbol>> buildParams(JmmNode classDecl, Map<String,Boolean> isObjectInstantiatedMap) {
        Map<String, List<Symbol>> map = new HashMap<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            List<JmmNode> children = method.getChildren(PARAM); //SÓ 1 ou 0
            List<Symbol> symbolList = new ArrayList<>();
            for (JmmNode child : children){
                Type returnType = typerReturner(child);
                returnType.putObject("isConst", false);
                
                Symbol aux = new Symbol(returnType, child.get("name"));

                boolean exists = symbolList.stream().anyMatch(symbol -> symbol.getName().equals(aux.getName()));
                if (exists) {
                    var message = String.format("Cannot have more than 1 parameters with same name", child);
                    reports.add(newError(child, message));
                }
                symbolList.add(aux);
                isObjectInstantiatedMap.put(child.get("name"), true);


            }

            List<JmmNode> vararchildren = method.getChildren(VAR_ARG_TYPE);
            for (JmmNode varargNode : vararchildren) {
                Type returnType = typerReturner(varargNode);
                returnType.putObject("isConst", false);
                Type newReturnType = TypeUtils.newArrayType(returnType.getName());
                newReturnType.putObject("isVarArg", true);
                Symbol aux = new Symbol(newReturnType, varargNode.get("name"));

                boolean exists = symbolList.stream().anyMatch(symbol -> symbol.getName().equals(aux.getName()));
                if (exists) {
                    var message = String.format("Cannot have more than 1 parameters with same name", varargNode);
                    reports.add(newError(varargNode, message));
                }

                symbolList.add(aux);
            }

            map.put(name, symbolList);
        }
        for (var method : classDecl.getChildren(MAIN_METHOD_DECL)) {
            List<Symbol> symbolList = new ArrayList<>();
            symbolList.add(new Symbol(TypeUtils.newArrayType("String"), method.get("argName")));
            map.put("main", symbolList);
        }

        return map;
    }

    private Map<String, List<Symbol>> buildLocals(JmmNode classDecl, Map<String,Boolean> isObjectInstantiatedMap) {

        var map = new HashMap<String, List<Symbol>>();
        List<JmmNode> bigList = classDecl.getChildren(METHOD_DECL);
        List<JmmNode> bigList2 = classDecl.getChildren(MAIN_METHOD_DECL);
        List<JmmNode> combinedList = new ArrayList<>(bigList);
        combinedList.addAll(bigList2);

        for (var method : combinedList) {
            var name = method.get("name");
            List<Symbol> locals = new ArrayList<>();
            List<JmmNode> children = method.getChildren(VAR_DECL);
            for (JmmNode child : children){
                Type returnType = typerReturner(child);
                returnType.putObject("isConst", false);

                Symbol tempAux = new Symbol(returnType, child.get("name"));

                boolean exists = false;
                for (Symbol symbol : locals) {
                    if (symbol.getName().equals(tempAux.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    var message = String.format("Cannot have more than 1 local var with same name", child);
                    reports.add(newError(child, message));
                }

                locals.add(tempAux);
                isObjectInstantiatedMap.put(child.get("name"), false);
            }
            children = method.getChildren(CONST_STMT);
            for (JmmNode child : children){
                Type returnType = typerReturner(child);
                returnType.putObject("isConst", true);

                Symbol tempAux = new Symbol(returnType, child.get("name"));

                boolean exists = false;
                for (Symbol symbol : locals) {
                    if (symbol.getName().equals(tempAux.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    var message = String.format("Cannot have more than 1 local var with same name", child);
                    reports.add(newError(child, message));
                }

                locals.add(tempAux);
                isObjectInstantiatedMap.put(child.get("name"), false);
            }
            children = method.getChildren(VAR_ASSIGN_STMT);
            for (JmmNode child : children){
                Type returnType = typerReturner(child);
                returnType.putObject("isConst", false);

                Symbol tempAux = new Symbol(returnType, child.get("name"));

                boolean exists = false;
                for (Symbol symbol : locals) {
                    if (symbol.getName().equals(tempAux.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    var message = String.format("Cannot have more than 1 local var with same name", child);
                    reports.add(newError(child, message));
                }
                locals.add(tempAux);
                isObjectInstantiatedMap.put(child.get("name"), false);
            }
            map.put(name, locals);
        }

        return map;
    }

    private List<String> buildMethods(JmmNode classDecl, Map<String, Boolean> staticMethods) {
        List<String> methods = new ArrayList<>();
        List<JmmNode> children = classDecl.getChildren(METHOD_DECL);
        for (JmmNode child : children){
            methods.add(child.get("name"));

            try{
                String static_ = child.get("st");
                staticMethods.put(child.get("name"), true);
            }
            catch(Exception e){
                staticMethods.put(child.get("name"), false);
            }

            try{
                child.get("pub");
                child.put("isPublic","true");
            }
            catch(Exception e){
                child.put("isPublic","false");
            }

        }

        return methods;
    }


}
