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

        // TODO: After your grammar supports more things inside the program (e.g., imports) you will have to change this
        var imports = buildImports(root.getChildren(IMPORT_DECL));
        var classDecl = root.getChild(imports.size()); //Para saltar imediatamente para a ClassDeclaration
        SpecsCheck.checkArgument(CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);
        String className = classDecl.get("name");
        var superName = buildSuperName(classDecl);
        var fields = buildFields(classDecl);

        var methods = buildMethods(classDecl);
        System.out.println(methods.size());
        var returnTypes = buildReturnTypes(classDecl);
        System.out.println(returnTypes.size());
        var params = buildParams(classDecl);
        var locals = buildLocals(classDecl);

        return new JmmSymbolTable(imports,className, superName , fields, methods, returnTypes, params, locals);
    }

    private List<Symbol> buildFields(JmmNode classDecl) {
        List<Symbol> fields = new ArrayList<>();
        List<JmmNode> children = classDecl.getChildren(VAR_DECL);
        for (JmmNode child : children){
            Type returnType = typerReturner(child);
            Symbol tempAux = new Symbol(returnType, child.get("name"));
            fields.add(tempAux);
        }
        return fields;
    }

    /*private List<Symbol> buildA(JmmNode classDecl) {
        List<Symbol> fields = new ArrayList<>();
        System.out.println(classDecl.getChildren(METHOD_DECL).size());
        for (var method : classDecl.getChildren(METHOD_DECL)) {

            JmmNode root_arg = method.getChildren(ARG_DECL).getFirst();  //Só o root de arg - Tb só retornara 1 xd
            List<Symbol> tempField = buildAAux(root_arg);
            System.out.println(fields.addAll(tempField));
        }
        var returnType = TypeUtils.newIntType();
        Symbol tempAux = new Symbol(returnType, "a");
        fields.add(tempAux);
        return fields;
    }

    private List<Symbol> buildAAux(JmmNode rootArg) {
        List<Symbol> tempField = new ArrayList<>();
        for (JmmNode arg : rootArg.getChildren()){
            System.out.println(arg.getChild(0).getKind());
            /*switch (arg.getChild(0).getKind()){
                case :
                    // handle case for SomeKindValue3
                    break;

                default:
                    // handle the case if none of the above match
                    break;
            }*/
            /*var returnType = TypeUtils.newIntType();
            //Symbol tempAux = new Symbol(returnType, arg.get("argName"));
            Symbol tempAux = new Symbol(returnType, "a");
            tempField.add(tempAux);
            //tempField.addAll(buildFieldsAux(arg.getChild(0)));
        }
        return tempField;
    }*/

    private String buildSuperName(JmmNode classDecl) {
        if (classDecl.hasAttribute("superName")) {
            return classDecl.get("superName");
        }
        return "";
    }

    private List<String> buildImports(List<JmmNode> children) {
        List<String> list = new ArrayList<>();
        for (JmmNode child : children) {
                list.add(child.get("value"));
        }
        return list;
    }

    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();
        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            // TODO: After you add more types besides 'int', you will have to update this
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
        switch (kind) {
            case "IntType":
                returnType = TypeUtils.newIntType();
                break;
            case "BooleanType":
                returnType = TypeUtils.newBooleanType();
                break;
            case "ClassType":
                returnType = TypeUtils.newObjectType(child.getChild(0).get("name"));
                break;
            case "IntArrayType":
                returnType = TypeUtils.newIntArrayType();
                break;
            default:
                returnType = TypeUtils.newObjectType(child.getChild(0).get("name"));
                break;
        }
        return returnType;
    }

    private Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();
        /* var params = method.getChildren(PARAM).stream()
                    // TODO: When you support new types, this code has to be updated
                    .map(param -> new Symbol(TypeUtils.newIntType(), param.get("name")))
                    .toList();*/


        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");
            List<JmmNode> children = method.getChildren(PARAM); //SÓ 1 ou 0
            List<Symbol> symbolList = new ArrayList<>();
            for (JmmNode child : children){
                Type returnType = typerReturner(child);
                Symbol aux = new Symbol(returnType, child.get("name"));
                symbolList.add(aux);
            }
            System.out.println( name + "size of final list: " + symbolList.size() );
            map.put(name, symbolList);
        }

        return map;
    }

    private Map<String, List<Symbol>> buildLocals(JmmNode classDecl) {

        var map = new HashMap<String, List<Symbol>>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");

            var locals = method.getChildren(VAR_DECL).stream()
                    // TODO: When you support new types, this code has to be updated
                    .map(varDecl -> new Symbol(TypeUtils.newIntType(), varDecl.get("name")))
                    .toList();


            map.put(name, locals);
        }

        return map;
    }

    private List<String> buildMethods(JmmNode classDecl) {

        /*var methods = classDecl.getChildren(METHOD_DECL).stream()
                .map(method -> method.get("name"))
                .toList();*/
        List<String> methods = new ArrayList<>();
        List<JmmNode> children = classDecl.getChildren(METHOD_DECL);
        for (JmmNode child : children){
            methods.add(child.get("name"));
        }
        // TODO: O que é praticamente fazer direto porque só há um main... ó será que não ?? é que a forma que está a gramatica escrita pode esxitir mais que um
        children = classDecl.getChildren(MAIN_METHOD_DECL);
        for (JmmNode child : children){
            methods.add("main");
        }

        return methods;
    }


}
