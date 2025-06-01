package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";
    private final String COLON = ":";
    private final String GOTO = "goto ";
    private final String R_PAREN = ")";
    private final String TAB = "   ";

    private String currentMethod;
    private String currentSpace;

    private final SymbolTable table;

    private final TypeUtils types;
    private OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table);
    }


    @Override
    protected void buildVisitor() {

        addVisit(PROGRAM, this::visitProgram);
        addVisit(IMPORT_DECL, this::visitImportDecl);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(EXPRESSION_STMT, this::visitExpr);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(BRACKET_STMT, this::visitBracketStmt);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
        addVisit(WHILE_STMT, this::whileStmt);
        addVisit(CONST_STMT, this::visitConstStmt);
        addVisit(VAR_ASSIGN_STMT, this::visitConstStmt);

        setDefaultVisit(this::defaultVisit);
    }

    private String visitConstStmt(JmmNode node, Void unused) {
        var rhs = exprVisitor.visit(node.getChild(1));

        StringBuilder code = new StringBuilder();
        code.append(currentSpace);
        // code to compute the children
        var expressionComputation = rhs.getComputation();
        if (!expressionComputation.isEmpty()){
            code.append(expressionComputation);
            code.append(END_STMT);
            code.append(currentSpace);
            code.append(TAB);
        }

        Type lhsType = types.valueFromVarReturner(node.get("name"),table,currentMethod).getType();
        String typeString = ollirTypes.toOllirType(lhsType);
        var varCode = node.get("name") + typeString;

        code.append(varCode);
        code.append(SPACE);
        code.append(ASSIGN);
        code.append(typeString);
        code.append(SPACE);
        code.append(rhs.getCode());
        code.append(END_STMT);

        return code.toString();
    }

    private String visitArrayAssignStmt(JmmNode node, Void unused) {
        //a[0.i32].i32 :=.i32 1.i32;
        StringBuilder code = new StringBuilder();
        var ollirIndexExpr = exprVisitor.visit(node.getChild(0));
        var ollirRhsExpr = exprVisitor.visit(node.getChild(1));
        if (!ollirIndexExpr.getComputation().isEmpty()) {
            code.append(currentSpace).append(ollirIndexExpr.getComputation()).append(END_STMT);
        }
        if (!ollirRhsExpr.getComputation().isEmpty()){
            code.append(currentSpace).append(ollirRhsExpr.getComputation()).append(END_STMT);
        }
        Type variableType = types.valueFromVarReturner(node.get("name"),table,currentMethod).getType();
        Type newVariableType = new Type(variableType.getName(),false);
        String ollirNewVariableType = ollirTypes.toOllirType(newVariableType);
        if (types.isVarField(node.get("name"),table,currentMethod)){
            //tmp0.array.i32 :=.array.i32 getfield(this, intField.array.i32).array.i32;
            //tmp0.array.i32[2.i32].i32 :=.i32 value.i32;
            // putfield(this, intField.array.i32, tmp0.array.i32).V;
            String nextTemp = ollirTypes.nextTemp();
            String arrayType = ".array" + ollirNewVariableType;
            code.append(currentSpace).append(nextTemp).append(arrayType).append(SPACE).append(ASSIGN).append(arrayType).append(SPACE).append("getfield(this, ").append(node.get("name")).append(arrayType).append(R_PAREN).append(arrayType).append(END_STMT);
            code.append(currentSpace).append(nextTemp).append(arrayType).append("[").append(ollirIndexExpr.getCode()).append("]").append(ollirNewVariableType).append(SPACE).append(ASSIGN).append(ollirNewVariableType).append(SPACE).append(ollirRhsExpr.getCode()).append(END_STMT);
            code.append(currentSpace).append("putfield(this, ").append(node.get("name")).append(arrayType).append(",").append(SPACE).append(nextTemp).append(arrayType).append(R_PAREN).append(".V").append(END_STMT);
        }else {
            code.append(currentSpace).append(node.get("name")).append("[").append(ollirIndexExpr.getCode()).append("]").append(ollirNewVariableType).append(SPACE).append(ASSIGN).append(ollirNewVariableType).append(SPACE).append(ollirRhsExpr.getCode()).append(END_STMT);
        }

        return code.toString();
    }

    private String visitBracketStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        for (JmmNode child : node.getChildren()){
            code.append(TAB).append(visit(child));
        }
        return code.toString();
    }

    private String visitIfStmt(JmmNode node, Void unused) {
        //this.ollirTypes = exprVisitor.getOllirTypes();

        int size = node.getChildren().size();
        int tempNums = ollirTypes.currentThen();
        String ifCurrentSpace = currentSpace;

        StringBuilder code = new StringBuilder();
        StringBuilder ifSpace = new StringBuilder(currentSpace);
        for (int i=0; i < size - 1 ; i += 2 ){
            tempNums = ollirTypes.nextThen();
            String thenNameInside = "then" + tempNums ;
            var condition = exprVisitor.visit(node.getChild(i));
            if (!condition.getComputation().isEmpty()){
                code.append(currentSpace).append(condition.getComputation()).append(END_STMT);
            }
            String ifCondition = currentSpace + "if (" + condition.getCode() + R_PAREN + SPACE + GOTO + thenNameInside + END_STMT;
            code.append(ifCondition);
            ifSpace.append(TAB);
            currentSpace = ifSpace.toString();
        }
        ifSpace.delete(0,3);
        currentSpace = ifSpace.toString();
        if (size % 2 == 1) {
            code.append(visit(node.getChildren().getLast()));
        }
        currentSpace = ifSpace.toString();
        for (int i= size / 2 * 2 - 1; i > 0; i -= 2){
            String thenNameInside = "then" + tempNums ;
            String endNameInside = "endif" + tempNums ;
            code.append(currentSpace).append(GOTO).append(endNameInside).append(END_STMT);
            code.append(currentSpace).append(thenNameInside).append(COLON).append(NL);
            code.append(visit(node.getChild(i)));
            code.append(currentSpace).append(endNameInside).append(COLON).append(NL);
            ifSpace.delete(0,3);
            currentSpace = ifSpace.toString();
            tempNums -= 1;  //ollirTypes.previousThen();
        }

        currentSpace = ifCurrentSpace;
        return code.toString();
    }

    private String visitImportDecl(JmmNode importNode, Void unused) {
        //To transform "[io, io2]" in ["io","io2"] and then "io.io2"
        List<String> wordList = List.of(importNode.get("name").substring(1, importNode.get("name").length() - 1).split(", "));
        StringBuilder importName = new StringBuilder();
        for(int i = 0; i < wordList.size() - 1; i++){
            importName.append(wordList.get(i)).append(".");
        }
        importName.append(wordList.getLast());
        return "import " + importName + END_STMT;
    }

    private String visitAssignStmtAsNewIntArrayExpr(JmmNode node, Void unused) {
        var rhs = exprVisitor.visit(node.getChild(0));

        StringBuilder code = new StringBuilder();

        // code to compute the children
        if (!rhs.getComputation().isEmpty()){
            code.append(currentSpace).append(rhs.getComputation()).append(END_STMT);
        }

        // code to compute self
        // statement has type of lhs

        Type lhsType = types.valueFromVarReturner(node.get("name"),table,currentMethod).getType();
        String typeString = ollirTypes.toOllirType(lhsType);
        var varCode = node.get("name") + typeString;

        //        String codePart2 = "\n k.array.i32 :=.array.i32 tmp0.array.i32" ;
            code.append(currentSpace);
            code.append(varCode);
            code.append(SPACE);

            code.append(ASSIGN);
            code.append(typeString);
            code.append(SPACE);

            code.append(rhs.getCode());

            code.append(END_STMT);


        return code.toString();
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        if(node.getChild(0).getKind().equals("NewIntArrayExpr")){
            return visitAssignStmtAsNewIntArrayExpr(node,unused);
        }

        StringBuilder code = new StringBuilder();

        String nodeName = node.get("name");

        // code to compute self
        // statement has type of lhs

        Type lhsType = types.valueFromVarReturner(node.get("name"),table,currentMethod).getType();
        String typeString = ollirTypes.toOllirType(lhsType);
        var varCode = node.get("name") + typeString;

        var rhs = exprVisitor.visit(node.getChild(0));

        code.append(currentSpace);
        // code to compute the children
        var expressionComputation = rhs.getComputation();
        if (!expressionComputation.isEmpty()){
            code.append(expressionComputation);
            code.append(END_STMT);
            code.append(currentSpace);
        }

        String temp = rhs.getCode();
        boolean isField = types.isVarField(nodeName,table,currentMethod);

        if (!node.get("op").equals("=")) {
            String objToCalculate = varCode;

            if (isField){
                //tmp0.i32 :=.i32 getfield(this, intField.i32).i32;
                temp = ollirTypes.nextTemp() + typeString;
                objToCalculate = temp;
                code.append(temp).append(SPACE).append(ASSIGN).append(typeString).append(SPACE).append("getfield(this, ").append(varCode).append(R_PAREN).append(typeString).append(END_STMT).append(TAB);
            }

            temp = ollirTypes.nextTemp() + typeString;
            String operation = node.get("op").substring(0, 1);
            code.append(temp).append(SPACE).append(ASSIGN).append(typeString).append(SPACE).append(objToCalculate).append(SPACE).append(operation).append(typeString).append(SPACE).append(rhs.getCode()).append(END_STMT).append(TAB);
        }

        //for fields, bc he feels like the special kid of the family
        if (isField){

            // putfield(this, intField.i32, value.i32).V;
            code.append("putfield(this, ").append(varCode).append(",").append(SPACE).append(temp).append(R_PAREN).append(".V").append(END_STMT);
            return code.toString();
        }else {

            code.append(varCode);
            code.append(SPACE);
            code.append(ASSIGN);
            code.append(typeString);
            code.append(SPACE);
            code.append(temp);
            code.append(END_STMT);
        }
        return code.toString();
    }


    private String visitReturn(JmmNode node, Void unused) {
        Type retType = TypeUtils.newIntType();

        StringBuilder code = new StringBuilder(currentSpace);
        JmmNode returnStatement = node.getChild(0);


        var expr = returnStatement.getNumChildren() > 0 ? exprVisitor.visit(returnStatement.getChild(0)) : OllirExprResult.EMPTY;

        var expressionComputation = expr.getComputation();
        if (!expressionComputation.isEmpty()){
            code.append(expressionComputation);
            code.append(END_STMT);
            code.append(currentSpace);
        }
        code.append("ret");
        code.append(ollirTypes.toOllirType(retType));
        code.append(SPACE);

        code.append(expr.getCode());

        code.append(END_STMT);

        return code.toString();
    }


    private String visitParam(JmmNode node, Void unused) {
        JmmNode typeChild = node.getChild(0);
        String typeCode;
        if (typeChild.getKind().equals("ClassType")){
            typeCode = "." + typeChild.getObject("name");
        }else{
            typeCode = ollirTypes.toOllirType(typeChild);
        }

        var id = node.get("name");

        return id + typeCode;
    }


    private String visitMethodDecl(JmmNode node, Void unused) {
        currentMethod = node.get("name");
        exprVisitor.ChangeCurrentMethod(currentMethod);
        currentSpace = TAB;

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = node.getBoolean("isPublic", false);

        if (isPublic) {
            code.append("public ");
        }

        Map<String, Boolean> staticMethods = (Map<String, Boolean>) table.getObject("staticMethods");
        boolean isStatic = staticMethods.get(currentMethod);

        if (isStatic) {
            code.append("static ");
        }

        List<JmmNode> varArgList = node.getChildren(VAR_ARG_TYPE); //its only one per method but function requires list
        String varArgString = "";
        if (!varArgList.isEmpty()){
            String varArgName = varArgList.getFirst().get("name");
            Type varArgType = types.valueFromVarReturner(varArgName,table,currentMethod).getType();
            varArgString = varArgName + ollirTypes.toOllirType(varArgType);
            code.append("varargs ");
        }

        // name
        var name = node.get("name");

        if (name.equals("varargs")){
            code.append("\"").append(name).append("\"");
        }else{
            code.append(name);
        }

        // params
        // TODO: Hardcoded for a single parameter, needs to be expanded
        //var paramsCode = visit(node.getChild(1));
        //code.append("(" + paramsCode + ")");
        code.append("(");
        List<JmmNode> paramList = node.getChildren(PARAM);  //doesn't include varargs
        var paramsCode = paramList.stream()
                .map(this::visit)
                .collect(Collectors.joining(",", "", ""));

        code.append(paramsCode);

        if (!paramList.isEmpty() && !varArgList.isEmpty()){
            code.append(",");
        }
        code.append(varArgString);

        code.append(R_PAREN);

        // type
        String retType = ollirTypes.toOllirType(node.getChild(0));
        code.append(retType);
        code.append(L_BRACKET);

        List<JmmNode> listNode = node.getChildren();
        int count = 1 + table.getParameters(name).size();
        List<JmmNode> childNodeList = listNode.subList(count, listNode.size());

        StringBuilder stmtsCode = new StringBuilder();
        for(int i=0; i < childNodeList.size(); i++){
            JmmNode childNode = childNodeList.get(i);
            if (!childNode.getKind().equals("VarDecl")) {
                var childCode = visit(childNode);
                stmtsCode.append("").append(childCode).append("\n");
            }
        }


        // rest of its children stmts
        /*var stmtsCode = childNodeList.stream()
                .map(this::visit)
                .collect(Collectors.joining("\n   ", "   ", ""));*/




        code.append(stmtsCode);
        if(retType.equals(".V")){
            code.append(currentSpace).append("ret.V;").append(NL);
        }
        code.append(R_BRACKET);
        code.append(NL);

        return code.toString();
    }


    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());

        if (node.hasAttribute("superName")){
            code.append(" extends ").append(table.getSuper());
        }
        
        code.append(L_BRACKET);
        code.append(NL);
        code.append(NL);

        for (Symbol field : table.getFields()) {
            //.field public intField.i32;
            Type fieldType = field.getType();
            String ollirField = ollirTypes.toOllirType(fieldType);
            String fieldCode = ".field public " + field.getName() + ollirField + END_STMT;
            code.append(fieldCode);
        }

        code.append(NL);
        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        code.append(R_BRACKET);

        return code.toString();
    }

    private String buildConstructor() {

        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }


    private String visitProgram(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        node.getChildren().stream()
                .map(this::visit)
                .forEach(code::append);

        return code.toString();
    }

    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
            exprVisitor.visit(node.getChild(0));
        }

        return "hey, im " + node.getKind();
    }

    private String visitExpr(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();
        var exprVisit= exprVisitor.visit(node.getChild(0));
        var exprComputation = exprVisit.getComputation();
        var exprCode = exprVisit.getCode();
        if (!exprComputation.isEmpty()){
            code.append(currentSpace).append(exprComputation).append(END_STMT);
        }

        //code.append(currentSpace).append(exprCode).append(END_STMT);

        return code.toString();
    }

    private String whileStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        code.append(NL);
        int whileTempNums = ollirTypes.nextWhile();
        int ifLabelNum = ollirTypes.nextThen();
        String whileSpace = currentSpace;
        currentSpace += TAB;
        String whileName = "while" + whileTempNums + COLON;
        OllirExprResult ollirCondition = exprVisitor.visit(node.getChild(0));
        String condition = ollirCondition.getCode();
        String ifName = "if(!.bool " + condition + R_PAREN + SPACE + GOTO + "endif" + ifLabelNum + END_STMT;

        String statementCode = visit(node.getChild(1));
        currentSpace = whileSpace;

        String goTo = GOTO + "while" + whileTempNums + END_STMT;
        String endIf = "endif" + ifLabelNum + COLON;

        code.append(currentSpace).append(whileName).append(NL).append(currentSpace).append(TAB);
        if (!ollirCondition.getComputation().isEmpty()){
            code.append(ollirCondition.getComputation()).append(END_STMT).append(currentSpace).append(TAB);
        }

        code.append(ifName);
        code.append(statementCode).append(NL);
        code.append(currentSpace).append(goTo);
        code.append(currentSpace).append(endIf).append(NL);


        return code.toString();
    }
}
