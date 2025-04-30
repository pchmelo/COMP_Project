package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String R_PAREN = ")";
    private final String TAB = "   ";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;
    private String currentMethod;

    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }

    public void ChangeCurrentMethod(String currentMethod){
        this.currentMethod = currentMethod;
    }


    @Override
    protected void buildVisitor() {
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(INTEGER_EXPR, this::visitInteger);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(TRUE_EXPR, this::visitBooleanExpr);
        addVisit(FALSE_EXPR, this::visitBooleanExpr);
        addVisit(ARRAY_LENGTH_EXPR, this::visitArrayLenExpr);
        addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObjExpr);
        addVisit(NOT_EXPR, this::visitNotExpr);
        addVisit(PARENTHESES_EXPR, this::visitParenthesesExpr);
        addVisit(POSTFIX, this::visitPostfix);
        addVisit(THIS_EXPR, this::visitThisExpr);
        addVisit(ARRAY_INIT, this::visitArrayInit);
        addVisit(METHOD_CALL, this::visitMethodCall);

        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
//invokestatic(io, "println", 2.i32).V;
        StringBuilder computation = new StringBuilder();

        //To deal with Parameters and VarArgs
        StringBuilder rhsCode = new StringBuilder() ;
        String methodName = node.get("name");

        List<Symbol> symbolParamList = table.getParameters(methodName); //includes all Param and varArgType present in table
        int actualParamSize = symbolParamList.size();                   //size of Param and varArgType together
        int currentParamSize = node.getChildren().size();               //includes all arguments the current node is passing to the method call

        List<String> varargsListMethod = (List<String>) table.getObject("varargs");     //verify if method call has a vararg, so we don't consider it when printing normal params
        boolean hasVarargs = varargsListMethod.contains(methodName);
        String suposedVarArgType = "";
        //Change the way to print normal parameters if method has varargs and get the type of the vararg for vararg computation
        if (hasVarargs) {
            Type varArgType = symbolParamList.getLast().getType();
            Type newVarArgType = new Type(varArgType.getName(), false);
            suposedVarArgType = ollirTypes.toOllirType(newVarArgType);
            actualParamSize--;
        }
        //Print Normal Parameters
        for (int i = 0; i < actualParamSize ; i++){
            var rhsOllirExpr = visit(node.getChild(i));
            computation.append(printComputation(rhsOllirExpr.getComputation()));
            rhsCode.append(", ").append(rhsOllirExpr.getCode());
        }
        //Print VarArg
        if (hasVarargs) {
            var nextTemp = ollirTypes.nextTemp();
            for (int i = actualParamSize; i < currentParamSize ; i++){
                var rhsOllirExpr = visit(node.getChild(i));
                //tmp0[0.i32].i32 :=.i32 10.i32;
                computation.append(printComputation(rhsOllirExpr.getComputation()));
                computation.append(nextTemp).append("[").append(i-actualParamSize).append(".i32]").append(suposedVarArgType).append(SPACE).append(ASSIGN).append(suposedVarArgType).append(SPACE).append(rhsOllirExpr.getCode()).append(END_STMT).append(TAB);
            }
            rhsCode.append(", ").append(nextTemp).append(".array").append(suposedVarArgType);
        }

        String code = "";
        if (!node.getParent().getKind().equals("ExpressionStmt")){
            if (node.getParent().getKind().equals("AssignStmt")){
                Type resType = types.valueFromVarReturner(node.getParent().get("name"),table,currentMethod).getType();
                String resOllirType = ollirTypes.toOllirType(resType);
                code += ollirTypes.nextTemp() + resOllirType;
                computation.append(code + SPACE + ASSIGN + resOllirType + SPACE);
            }else{
                Type resType = types.getExprType(node.getParent(),table,currentMethod);
                String resOllirType = ollirTypes.toOllirType(resType);
                code += ollirTypes.nextTemp() + resOllirType;
                computation.append(code + SPACE + ASSIGN + resOllirType + SPACE);
            }
        }else{
            computation.append(TAB);
        }

        String returnTypeMethodCall =  ollirTypes.toOllirType(table.getReturnType(methodName));
        computation.append("invokevirtual(");

        computation.append( "this." + table.getClassName() + ", \"" + methodName + "\"" + rhsCode + R_PAREN + returnTypeMethodCall );
        return new OllirExprResult(code, computation.toString());
    }

    private OllirExprResult visitArrayInit(JmmNode node, Void unused) {
        /*tmp0.array.i32 :=.array.i32 new(array, 3.i32).array.i32;
tmp0[0.i32].i32 :=.i32 10.i32;
tmp0[1.i32].i32 :=.i32 20.i32;
tmp0[2.i32].i32 :=.i32 30.i32;*/
        Type nodeType = types.getExprType(node,table,currentMethod);
        Type newNodeType = new Type(nodeType.getName(),false);
        String nodeCode = ollirTypes.toOllirType( newNodeType);
        var nextTemp = ollirTypes.nextTemp();
        String code = nextTemp + ".array" + nodeCode;
        StringBuilder computation = new StringBuilder();
        List<JmmNode> children = node.getChildren();
        int childrenSize = children.size();
        computation.append(code).append(SPACE).append(ASSIGN).append(".array").append(nodeCode).append(SPACE).append("new(array, ").append(childrenSize).append(".i32").append(R_PAREN).append( ".array").append(nodeCode);

        for (int i = 0; i < childrenSize ; i++){
            var rhsOllirExpr = visit(node.getChild(i));
            //tmp0[0.i32].i32 :=.i32 10.i32;
            computation.append(END_STMT).append(TAB);
            computation.append(printComputation(rhsOllirExpr.getComputation()));
            computation.append(nextTemp).append("[").append(i).append(".i32]").append(nodeCode).append(SPACE).append(ASSIGN).append(nodeCode).append(SPACE).append(rhsOllirExpr.getCode());
        }
        return new OllirExprResult(code, computation.toString());
    }

    private OllirExprResult visitThisExpr(JmmNode node, Void unused) {
        return new OllirExprResult("this." + table.getClassName(), "");
    }

    private OllirExprResult visitPostfix(JmmNode node, Void unused) {
        //tmp0.i32 :=.i32 a.i32 +.i32 a.i32; a.i32 :=.i32 tmp0.i32;
        StringBuilder computation = new StringBuilder();

        Type intType = TypeUtils.newIntType();
        String resOllirType = ollirTypes.toOllirType(intType);

        var id = node.get("name");

        boolean isField = types.isVarField(id,table,currentMethod);

        String temporaryField = "";
        if (isField){
            temporaryField = ollirTypes.nextTemp() + resOllirType;
        }

        String code = ollirTypes.nextTemp() + resOllirType;

        String variable = id + resOllirType;

        String operation;
        if (node.get("op").equals("++")){
            operation = "+";
        }else{
            operation = "-";
        }

        if (isField){
            computation.append(temporaryField).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE).append("getfield(this, ").append(variable).append(R_PAREN).append(resOllirType).append(END_STMT).append(TAB);
            computation.append(code).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE).append(temporaryField).append(SPACE).append(operation).append(resOllirType).append(SPACE).append("1.i32").append(END_STMT).append(TAB);
            computation.append("putfield(this, ").append(variable).append(",").append(SPACE).append(code).append(R_PAREN).append(".V");
        }else{
            computation.append(code).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE).append(variable).append(SPACE).append(operation).append(resOllirType).append(SPACE).append("1.i32").append(END_STMT).append(TAB);
            computation.append(variable).append(SPACE).append(ASSIGN).append(resOllirType).append(SPACE).append(code);
        }

        return new OllirExprResult("", computation);
    }

    private OllirExprResult visitParenthesesExpr(JmmNode node, Void unused) {
        var ollirExp = visit(node.getChild(0));
        return new OllirExprResult(ollirExp.getCode(), ollirExp.getComputation());
    }

    private OllirExprResult visitNotExpr(JmmNode node, Void unused) {
        // tmp0.bool :=.bool !.bool 0.bool;
        Type newBooleanType = TypeUtils.newBooleanType();
        String ollirType = ollirTypes.toOllirType(newBooleanType);
        String code = ollirTypes.nextTemp() + ollirType;
        String computation = "";

        var ollirExp = visit(node.getChild(0));
        computation += printComputation(ollirExp.getComputation());

        computation += code + SPACE + ASSIGN + ollirType + SPACE + "!" + ollirType + SPACE + ollirExp.getCode();

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitNewObjExpr(JmmNode node, Void unused) {
        /* tmp0.io :=.io new(io).io;
invokespecial(tmp0.io, "<init>").V;
d.io :=.io tmp0.io;*/
        Type newObjectType = TypeUtils.newObjectType(node.get("name"));
        String ollirType = ollirTypes.toOllirType(newObjectType);
        String code = ollirTypes.nextTemp() + ollirType;
        String computation = code + SPACE + ASSIGN + ollirType + SPACE + "new(" + node.get("name") + R_PAREN + ollirType + END_STMT
                + TAB + "invokespecial(" + code + ", \"<init>\").V";

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitArrayAccessExpr(JmmNode node, Void unused) {
        //tmp0.i32 :=.i32 a.array.i32[0.i32].i32;    tmp0.i32
        Type type = types.getExprType(node,table,currentMethod);
        Type newType = new Type(type.getName(),false);
        String ollirType = ollirTypes.toOllirType(newType);
        String currentTemp = ollirTypes.nextTemp() + ollirType;
        var ollirFirstExp = visit(node.getChild(0));
        var ollirSecondExp = visit(node.getChild(1));
        String computation = "";
        computation += printComputationTab(ollirFirstExp.getComputation());
        computation += printComputationTab(ollirSecondExp.getComputation());

        computation += currentTemp + SPACE + ASSIGN + ollirType + SPACE + ollirFirstExp.getCode() + "[" + ollirSecondExp.getCode() + "]" + ollirType;

        return new OllirExprResult(currentTemp, computation);
    }

    private OllirExprResult visitArrayLenExpr(JmmNode node, Void unused) {
        //tmp1.i32 :=.i32 arraylength(a.array.i32).i32; tmp1.i32;
        var intType = TypeUtils.newIntType();
        String ollirBooleanType = ollirTypes.toOllirType(intType);
        String currentTemp = ollirTypes.nextTemp() + ollirBooleanType;
        String computation = currentTemp + SPACE + ASSIGN + ollirBooleanType + " arraylength(" + visit(node.getChild(0)).getCode() + R_PAREN + ollirBooleanType;

        return new OllirExprResult(currentTemp, computation);
    }

    private OllirExprResult visitBooleanExpr(JmmNode node, Void unused) {
        var booleanType = TypeUtils.newBooleanType();
        String ollirBooleanType = ollirTypes.toOllirType(booleanType);
        String code;
        if (node.get("value").equals("true")){
            code = "1";
        }else{
            code = "0";
        }
        code += ollirBooleanType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitNewIntArrayExpr(JmmNode node, Void unused) {
        //tmp0.array.i32 :=.array.i32 new(array, 1.i32).array.i32;
        var rhsOllirExpr = visit(node.getChild(1));
        var rhsCode =  rhsOllirExpr.getCode();
        String lhsOllirExpr = ".array" + ollirTypes.toOllirType(node.getChild(0));
        String code = ollirTypes.nextTemp() + lhsOllirExpr;
        String computation = rhsOllirExpr.getComputation() + code + SPACE + ASSIGN + lhsOllirExpr + SPACE + "new(array, " + rhsCode  + R_PAREN + lhsOllirExpr;

        return new OllirExprResult(code,computation);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        //invokestatic(io, "println", 2.i32).V;
        StringBuilder computation = new StringBuilder();
        JmmNode lhs = node.getChild(0);
        String lhsCode;
        if (lhs.getKind().equals("VarRefExpr")){
            lhsCode = lhs.get("name");
        }else{
            var lhsOllirExpr = visit(node.getChild(0));
            computation.append(printComputation(lhsOllirExpr.getComputation()));
            lhsCode =  lhsOllirExpr.getCode();
        }
        //To deal with Parameters and VarArgs
        StringBuilder rhsCode = new StringBuilder() ;
        String methodName = node.get("name");

        int currentParamSize = node.getChildren().size();               //includes all arguments the current node is passing to the method call

        if (node.getChild(0).getKind().equals("ThisExpr")) {
            List<Symbol> symbolParamList = table.getParameters(methodName); //includes all Param and varArgType present in table
            int actualParamSize = symbolParamList.size();                   //size of Param and varArgType together

            List<String> varargsListMethod = (List<String>) table.getObject("varargs");     //verify if method call has a vararg, so we don't consider it when printing normal params
            boolean hasVarargs = varargsListMethod.contains(methodName);
            String suposedVarArgType = "";
            //Change the way to print normal parameters if method has varargs and get the type of the vararg for vararg computation
            if (hasVarargs) {
                Type varArgType = symbolParamList.getLast().getType();
                Type newVarArgType = new Type(varArgType.getName(), false);
                suposedVarArgType = ollirTypes.toOllirType(newVarArgType);
                actualParamSize--;
            }
            //Print Normal Parameters
            for (int i = 0; i < actualParamSize; i++) {
                var rhsOllirExpr = visit(node.getChild(i + 1));
                computation.append(printComputation(rhsOllirExpr.getComputation()));
                rhsCode.append(", ").append(rhsOllirExpr.getCode());
            }
            //Print VarArg
            if (hasVarargs) {
                var nextTemp = ollirTypes.nextTemp();
                for (int i = actualParamSize + 1; i < currentParamSize; i++) {
                    var rhsOllirExpr = visit(node.getChild(i));
                    //tmp0[0.i32].i32 :=.i32 10.i32;
                    computation.append(printComputation(rhsOllirExpr.getComputation()));
                    computation.append(nextTemp).append("[").append(i - actualParamSize - 1).append(".i32]").append(suposedVarArgType).append(SPACE).append(ASSIGN).append(suposedVarArgType).append(SPACE).append(rhsOllirExpr.getCode()).append(END_STMT).append(TAB);
                }
                rhsCode.append(", ").append(nextTemp).append(".array").append(suposedVarArgType);
            }
        }else{
            for (int i = 1; i < currentParamSize; i++) {
                var rhsOllirExpr = visit(node.getChild(i));
                computation.append(printComputation(rhsOllirExpr.getComputation()));
                rhsCode.append(", ").append(rhsOllirExpr.getCode());
            }
        }

        String code = "";
        if (!node.getParent().getKind().equals("ExpressionStmt")){
            if (node.getParent().getKind().equals("AssignStmt")){
                Type resType = types.valueFromVarReturner(node.getParent().get("name"),table,currentMethod).getType();
                String resOllirType = ollirTypes.toOllirType(resType);
                code += ollirTypes.nextTemp() + resOllirType;
                computation.append(code + SPACE + ASSIGN + resOllirType + SPACE);
            }else{
                Type resType = types.getExprType(node.getParent(),table,currentMethod);
                String resOllirType = ollirTypes.toOllirType(resType);
                code += ollirTypes.nextTemp() + resOllirType;
                computation.append(code + SPACE + ASSIGN + resOllirType + SPACE);
            }
        }else{
            computation.append(TAB);
        }

        String returnTypeMethodCall = ".V";
        if (node.getChild(0).getKind().equals("ThisExpr")){
            computation.append("invokevirtual(");
            returnTypeMethodCall =  ollirTypes.toOllirType(table.getReturnType(methodName));
        }else{
            computation.append("invokestatic(");
        }

        computation.append( lhsCode + ", \"" + methodName + "\"" + rhsCode + R_PAREN + returnTypeMethodCall );
        return new OllirExprResult(code, computation.toString());
    }


    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);
        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(printComputationTab(lhs.getComputation()));
        computation.append(printComputationTab(rhs.getComputation()));

        // code to compute self
        Type resType = types.getExprType(node,table,currentMethod);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node,table,currentMethod);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode());

        return new OllirExprResult(code, computation);
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node,table,currentMethod);
        String ollirType = ollirTypes.toOllirType(type);
        boolean isField = types.isVarField(id,table,currentMethod);

        String code;
        String computation;

        //For general case when the variable called its not a field
        code = id + ollirType;
        computation = "";

        if (isField) {
            for (Symbol field : table.getFields()) {
                if (field.getName().equals(id)) {
                    //tmp0.i32 :=.i32 getfield(this, intField.i32).i32;
                    var nextTemp = ollirTypes.nextTemp();
                    code = nextTemp + ollirType;
                    computation = nextTemp + ollirType + SPACE + ASSIGN + ollirType + SPACE + "getfield(this, " + id + ollirType + R_PAREN + ollirType;
                    break;
                }
            }
        }

        return new OllirExprResult(code, computation);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

    private String printComputation(String computation){
        if (!computation.isEmpty()){
            return computation + END_STMT;
        }
        return "";
    }

    private String printComputationTab(String computation){
        if (!computation.isEmpty()){
            return computation + END_STMT + TAB;
        }
        return "";
    }

}
