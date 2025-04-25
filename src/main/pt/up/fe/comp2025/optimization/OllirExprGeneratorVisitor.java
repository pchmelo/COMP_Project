package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

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

        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitNewObjExpr(JmmNode node, Void unused) {
        /* tmp0.io :=.io new(io).io;
invokespecial(tmp0.io, "<init>").V;
d.io :=.io tmp0.io;*/
        Type newObjectType = TypeUtils.newObjectType(node.get("name"));
        String ollirType = ollirTypes.toOllirType(newObjectType);
        String code = ollirTypes.nextTemp() + ollirType;
        String computation = code + SPACE + ASSIGN + ollirType + SPACE + "new(" + node.get("name") + ")" + ollirType + END_STMT
                + "   " + "invokespecial(" + code + ", \"<init>\").V";

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
        if (!ollirFirstExp.getComputation().isEmpty()){
            computation += ollirFirstExp.getComputation() + END_STMT + "   ";
        }
        if (!ollirSecondExp.getComputation().isEmpty()){
            computation += ollirSecondExp.getComputation() + END_STMT + "   ";
        }

        computation += currentTemp + " " + ASSIGN + ollirType + " " + ollirFirstExp.getCode() + "[" + ollirSecondExp.getCode() + "]" + ollirType;

        return new OllirExprResult(currentTemp, computation);
    }

    private OllirExprResult visitArrayLenExpr(JmmNode node, Void unused) {
        //tmp1.i32 :=.i32 arraylength(a.array.i32).i32; tmp1.i32;
        var intType = TypeUtils.newIntType();
        String ollirBooleanType = ollirTypes.toOllirType(intType);
        String currentTemp = ollirTypes.nextTemp() + ollirBooleanType;
        String computation = currentTemp + " " + ASSIGN + ollirBooleanType + " arraylength(" + visit(node.getChild(0)).getCode() + ")" + ollirBooleanType;

        return new OllirExprResult(currentTemp, computation);
    }

    private OllirExprResult visitBooleanExpr(JmmNode node, Void unused) {
        var booleanType = TypeUtils.newBooleanType();
        String ollirBooleanType = ollirTypes.toOllirType(booleanType);
        String code = node.get("value") + ollirBooleanType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitNewIntArrayExpr(JmmNode node, Void unused) {
        //tmp0.array.i32 :=.array.i32 new(array, 1.i32).array.i32;
        var rhsOllirExpr = visit(node.getChild(1));
        var rhsCode =  rhsOllirExpr.getCode();
        String lhsOllirExpr = ".array" + ollirTypes.toOllirType(node.getChild(0));
        String code = ollirTypes.nextTemp() + lhsOllirExpr;
        String computation = rhsOllirExpr.getComputation() + code + SPACE + ASSIGN + lhsOllirExpr + SPACE + "new(array, " + rhsCode  +")" + lhsOllirExpr;

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
            if(!lhsOllirExpr.getComputation().isEmpty()){
                computation.append(lhsOllirExpr.getComputation()).append(END_STMT);
            }
            lhsCode =  lhsOllirExpr.getCode();
        }
        StringBuilder rhsCode = new StringBuilder() ;
        for (int i = 1; i < node.getChildren().size() ; i++){
            var rhsOllirExpr = visit(node.getChild(i));
            if (!rhsOllirExpr.getComputation().isEmpty()){
                computation.append(rhsOllirExpr.getComputation()).append(END_STMT);
            }
            rhsCode.append(", ").append(rhsOllirExpr.getCode());
        }
        String code = "";
        if (!node.getParent().getKind().equals("ExpressionStmt")){
            Type resType = types.getExprType(node.getParent(),table,currentMethod);
            String resOllirType = ollirTypes.toOllirType(resType);
            code += ollirTypes.nextTemp() + resOllirType;
            computation.append(code + SPACE + ASSIGN + resOllirType + SPACE);
        }else{
            computation.append("   ");
        }

        computation.append("invokestatic(" + lhsCode + ", \"" + node.get("name") + "\"" + rhsCode + ").V");
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
        if (!lhs.getComputation().isEmpty()){
            computation.append(lhs.getComputation()).append(END_STMT).append("   ");
        }
        if (!rhs.getComputation().isEmpty()){
            computation.append(rhs.getComputation()).append(END_STMT).append("   ");
        }

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

        String code = id + ollirType;

        return new OllirExprResult(code);
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

}
