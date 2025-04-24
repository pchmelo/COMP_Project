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

        setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitArrayLenExpr(JmmNode node, Void unused) {
        //tmp1.i32 :=.i32 arraylength(a.array.i32).i32; tmp1.i32;
        var intType = TypeUtils.newIntType();
        String ollirBooleanType = ollirTypes.toOllirType(intType);
        String currentTemp = ollirTypes.nextTemp() + ollirBooleanType;
        String computation = currentTemp + " " + ASSIGN + ollirBooleanType + " arraylength(" + visit(node.getChild(0)).getCode() + ")" + ollirBooleanType + END_STMT;

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
        var lhsOllirExpr = visit(node.getChild(1));
        var lhsCode =  lhsOllirExpr.getCode();

        String codePart1 = "new(array," + lhsCode  +").array.i32" + lhsOllirExpr.getComputation();

        return new OllirExprResult(codePart1);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        //invokestatic(io, "println", 2.i32).V;
        JmmNode lhs = node.getChild(0);
        String lhsCode;
        if (lhs.getKind().equals("VarRefExpr")){
            lhsCode = lhs.get("name");
        }else{
            var lhsOllirExpr = visit(node.getChild(0));
            lhsCode =  lhsOllirExpr.getComputation();
        }
        StringBuilder rhsCode = new StringBuilder() ;
        StringBuilder code = new StringBuilder();
        for (int i = 1; i < node.getChildren().size() ; i++){
            var rhsOllirExpr = visit(node.getChild(i));
            code.append(rhsOllirExpr.getComputation());
            rhsCode.append(", ").append(rhsOllirExpr.getCode());
        }


        code.append("invokestatic(" + lhsCode + ", \"" + node.get("name") + "\"" + rhsCode + ").V");

        return new OllirExprResult(code.toString());
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
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node,table,currentMethod);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node,table,currentMethod);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

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
