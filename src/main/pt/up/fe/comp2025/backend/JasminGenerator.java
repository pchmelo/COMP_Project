package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JasminGenerator {
    private static final String NL = "\n";
    private static final String TAB = "   ";
    private final OllirResult ollirResult;

    List<Report> reports;
    String code;
    Method currentMethod;

    private final JasminUtils types;
    private final FunctionClassMap<TreeNode, String> generators;
    private final HashMap<String, String> imports = new HashMap<>();

    private int stack = 0;
    private int maxStack = 0;

    private int label_number = 0;

    private List<Integer> usedLocals = new ArrayList<>();
    private String current_assign;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;

        types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);

        generators.put(ArrayOperand.class, this::generateOperandArrayGet);

        //new generators
        generators.put(PutFieldInstruction.class, this::generatePutFieldInstruction);
        generators.put(GetFieldInstruction.class, this::generateGetFieldInstruction);
        generators.put(CallInstruction.class, this::generateCallInstruction);

        //aritmétrica
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);

        //conditions
        generators.put(OpCondInstruction.class, this::generateOpConditional);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpConditional);
        generators.put(GotoInstruction.class, this::generateGoTo);

    }

    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }

    private void addStack(int stack) {
        this.stack += stack;
        if (this.stack > maxStack) {
            maxStack = this.stack;
        }
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }


    private String generateClassUnit(ClassUnit classUnit) {
        for(String imp : classUnit.getImports()){
            String importNonQualified = imp.substring(imp.lastIndexOf(".") + 1);
            imp = imp.replace(".", "/");
            imports.put(importNonQualified, imp);
        }

        var code = new StringBuilder();

        var className = classUnit.getClassName();
        code.append(".class ").append(className).append(NL);

        var superClass = getSuperClassName();
        code.append(".super ").append(superClass).append(NL).append(NL);

        for (var field : classUnit.getFields()) {
            code.append(getField(field)).append(NL);
        }


        for (var method : ollirResult.getOllirClass().getMethods()) {
            if (method.isConstructMethod()) {
                var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(superClass);
                code.append(defaultConstructor).append("\n");
            }else{
                code.append(apply(method)).append("\n");
            }
        }

        return code.toString();
    }

    private int generateLimitLocals(Method method){
        int index = 0;
        if (!method.isStaticMethod()) {
            usedLocals.add(0);
            index = 1;
        }

        for (int i = index; i < method.getVarTable().size(); i++){
            if (i < method.getParams().size() + (method.isStaticMethod() ? 0 : 1)){
                usedLocals.add(0);
            } else {
                usedLocals.add(-1);
            }
        }
        return usedLocals.size();
    }

    private String generateMethod(Method method) {
        currentMethod = method;

        var code = new StringBuilder();
        code.append(this.getMethodHeader(method));

        int limitLocals = generateLimitLocals(method); //obrigatorio antes das instruções

        StringBuilder instructions = new StringBuilder();
        for (var inst : method.getInstructions()) {
            for (Map.Entry<String, Instruction> label : method.getLabels().entrySet()){
                if (label.getValue().equals(inst)){
                    instructions.append(TAB).append(label.getKey()).append(":").append(NL);
                }
            }

            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            instructions.append(instCode);
        }

        int actualUsedLocals = 0;
        for (int i = 0; i < usedLocals.size(); i++) {
            if (usedLocals.get(i) >= 0) { // Only count locals that are actually used
                actualUsedLocals++;
            }
        }

        int finalLimitLocals = Math.max(limitLocals, actualUsedLocals);

        code.append(".limit stack ").append(maxStack).append(NL);
        code.append(".limit locals ").append(finalLimitLocals).append(NL);
        code.append(instructions);
        code.append(".end method\n\n");

        // unset method
        currentMethod = null;
        this.maxStack = 0;
        usedLocals.clear();
        return code.toString();
    }

    private String getMethodHeader(Method method){
        var header = new StringBuilder();

        var modifier = method.getMethodAccessModifier() != AccessModifier.DEFAULT ?
                method.getMethodAccessModifier().name().toLowerCase() + " " : "public ";

        if (method.isStaticMethod()) {
            modifier += "static ";
        }

        if (method.isFinalMethod()) {
            modifier += "final ";
        }

        header.append(".method ").append(modifier).append(method.getMethodName());

        // Add parameters
        var params = new ArrayList<String>();
        for (Element param : method.getParams()){
            params.add(types.getDescriptor(param.getType()));
        }

        header.append("(").append(String.join("", params)).append(")");

        // Add return type
        var returnType = types.getDescriptor(method.getReturnType());
        header.append(returnType).append(NL);

        return header.toString();
    }

    private String getSuperClassName() {
        var superClass = ollirResult.getOllirClass().getSuperClass();

        if (superClass == null || superClass.equals("Object")) {
            superClass = "java/lang/Object";
        }

        superClass = imports.getOrDefault(superClass, superClass);
        return superClass;
    }

    private String getField(Field field) {
        var code = new StringBuilder();

        var modifier = field.getFieldAccessModifier() != AccessModifier.DEFAULT ?
                field.getFieldAccessModifier().name().toLowerCase() + " " : "public ";

        if (field.isStaticField()) {
            modifier += "static ";
        }

        if (field.isFinalField()) {
            modifier += "final ";
        }

        code.append(".field ").append(modifier).append("'").append(field.getFieldName()).append("' ");

        // Add type
        var type = types.getDescriptor(field.getFieldType());
        code.append(type).append(NL);

        return code.toString();
    }


    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();

        // store value in the stack in destination
        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        Operand operand = (Operand) lhs;

        this.current_assign = operand.getName();

        if (operand.toString().contains("ArrayOperand")){
            code.append(this.generateStoreAssignArray(operand, assign));
            return code.toString();
        }

        if (assign.getRhs() instanceof BinaryOpInstruction binOp &&
                binOp.getOperation().getOpType() == OperationType.ADD) {

            String iincCode = tryGenerateIinc(operand, binOp);
            if (!iincCode.isEmpty()) {
                return iincCode;
            }
        }

        code.append(apply(assign.getRhs()));

        code.append(this.generateStoreAssign(operand)).append(NL);
        return code.toString();
    }

    private String tryGenerateIinc(Operand dest, BinaryOpInstruction binOp) {
        Element left = binOp.getLeftOperand();
        Element right = binOp.getRightOperand();

        // Check if dest = dest + literal or dest = literal + dest
        if (left instanceof Operand leftOp && right instanceof LiteralElement rightLit) {
            if (leftOp.getName().equals(dest.getName())) {
                return generateIinc(dest, rightLit);
            }
        } else if (left instanceof LiteralElement leftLit && right instanceof Operand rightOp) {
            if (rightOp.getName().equals(dest.getName())) {
                return generateIinc(dest, leftLit);
            }
        }

        return "";
    }

    private String generateIinc(Operand operand, LiteralElement literal) {
        var reg = currentMethod.getVarTable().get(operand.getName());
        int value = Integer.parseInt(literal.getLiteral());

        if (value >= -128 && value <= 127) {
            if (reg.getVirtualReg() < usedLocals.size()) {
                usedLocals.set(reg.getVirtualReg(), 0);
            }
            return "iinc " + reg.getVirtualReg() + " " + value + NL;
        }

        return "";
    }

    private String generateStoreAssign(Operand operand) {

        Descriptor reg = currentMethod.getVarTable().get(operand.getName());
        this.addStack(-1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        Type operandType = operand.getType();

        return types.getStoreInstruction(operandType, reg.getVirtualReg());
    }

    private String generateStoreAssignArray(Operand operand , AssignInstruction assign) {
        StringBuilder code = new StringBuilder();
        this.current_assign = operand.getName();

        operand.getName();
        //dar load do array antes de tudo
        currentMethod.getVarTable().get(operand.getName());
        code.append(generateOperandArrayRef(operand));

        //dar load do temp1
        var indexOperand = (Operand) operand.getChildren().getFirst();
        code.append(generateOperand(indexOperand));

        //generate code for loading what's on the right
        code.append(apply(assign.getRhs()));

        Type operandType = operand.getType();
        code.append(types.getStoreInstructionForArray(operandType));
        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        Integer lit = Integer.parseInt(literal.getLiteral());
        addStack(1);

        if(lit.equals(-1)){
            return "iconst_m1" + NL;
        } else if(lit >= 0 && lit <= 5) {
            return "iconst_" + lit + NL;
        } else if (lit >= Byte.MIN_VALUE && lit <= Byte.MAX_VALUE) {
            return "bipush " + lit + NL;
        } else if (lit >= Short.MIN_VALUE && lit <= Short.MAX_VALUE) {
            return "sipush " + lit + NL;
        }

        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperandArrayRef(Operand operand) {
        var reg = currentMethod.getVarTable().get(operand.getName());
        addStack(1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        String prefix = types.getPrefixStoreLoad(operand.getType());
        String suffix = " ";

        if (operand.toString().contains("ArrayOperand")){
            prefix = "a";
        }

        if((reg.getVirtualReg() == 0 || reg.getVirtualReg() == 1 || reg.getVirtualReg() == 2 || reg.getVirtualReg() == 3)){  //  && prefix.equals("i")
            suffix = "_";
        }
        return prefix + "load" + suffix + reg.getVirtualReg() + "\n";
    }

    private String generateOperandArrayGet(Operand operand) {
        var reg = currentMethod.getVarTable().get(operand.getName());
        addStack(1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        StringBuilder code = new StringBuilder();
        //Load array ref - yes we have to
        code.append(generateOperandArrayRef(operand));

        //Load index of array
        code.append(generateOperand((Operand) operand.getChildren().getFirst()));

        String prefix = types.getPrefixStoreLoad(operand.getType()) + "a";

        String arrayLoad = prefix + "load " + "\n";
        code.append(arrayLoad);
        return code.toString();
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();
        OperationType opType = binaryOp.getOperation().getOpType();

        // For comparison operations, generate comparison code that leaves 0 or 1 on stack
        if (isComparisonOp(opType)) {
            return generateComparison(binaryOp);
        }

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        // apply operation
        String op = types.BinaryOperationType(opType);
        code.append(op).append(NL);

        addStack(-1); // Binary operations consume 2 values and produce 1

        return code.toString();
    }

    private boolean isComparisonOp(OperationType opType) {
        return opType == OperationType.LTH || opType == OperationType.LTE ||
                opType == OperationType.GTH || opType == OperationType.GTE ||
                opType == OperationType.EQ || opType == OperationType.NEQ;
    }

    private String generateComparison(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();
        OperationType opType = binaryOp.getOperation().getOpType();

        // Load operands
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        // Generate comparison
        String trueLabel = "cmp_true_" + label_number;
        String endLabel = "cmp_end_" + label_number++;

        // Subtract to compare (leaves result on stack)
        code.append("isub").append(NL);
        addStack(-1); // isub consumes 2, produces 1

        switch (opType) {
            case LTH -> code.append("iflt ").append(trueLabel);
            case LTE -> code.append("ifle ").append(trueLabel);
            case GTH -> code.append("ifgt ").append(trueLabel);
            case GTE -> code.append("ifge ").append(trueLabel);
            case EQ -> code.append("ifeq ").append(trueLabel);
            case NEQ -> code.append("ifne ").append(trueLabel);
        }
        code.append(NL);

        // False case: push 0
        code.append("iconst_0").append(NL);
        code.append("goto ").append(endLabel).append(NL);

        // True case: push 1
        code.append(trueLabel).append(":").append(NL);
        code.append("iconst_1").append(NL);

        // End label
        code.append(endLabel).append(":").append(NL);

        addStack(-1); // We consumed the comparison result
        addStack(1);  // We pushed the boolean result (0 or 1)

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        StringBuilder code = new StringBuilder();

        if (returnInst.getOperand().isEmpty()) {
            if (types.getDescriptor(returnInst.getReturnType()).equals("V")){
                code.append("return\n");
                return code.toString();
            }

            throw new NotImplementedException("Return without operand");
        }

        var loadOperand = apply(returnInst.getOperand().get());
        code.append(loadOperand);
        code.append(types.getPrefixStoreLoad(returnInst.getReturnType()) + "return\n");
        addStack(-1);

        return code.toString();
    }


    //New function to generate new instruction

    private String generateNewInstruction(NewInstruction newInst) {
        StringBuilder code = new StringBuilder();

        var callerType = newInst.getCaller().getType();
        if (callerType instanceof ArrayType arrayType) {
            SpecsCheck.checkArgument(
                    newInst.getArguments().size() == 1,
                    () -> "Expected number of arguments to be 1 for new array, but got " + newInst.getArguments().size()
            );

            addStack(1);
            addStack(-1);

            Operand argumentOperand = (Operand) newInst.getArguments().getFirst();
            code.append(generateOperand(argumentOperand));

            var typeCode = types.getArrayType(arrayType.getElementType());
            code.append("newarray " + typeCode + "\n");
            return code.toString();
        }
        else{
            String name = this.addImportPath(callerType.toString());
            code.append("new ").append(name).append(NL);

            addStack(1);
        }

        return code.toString();
    }

    private String generateInvokes(CallInstruction callInstruction) {
        StringBuilder code = new StringBuilder();
        String className = callInstruction.getCaller().getType().toString();
        className = this.addImportPath(className);

        String methodName = ((LiteralElement) callInstruction.getMethodName()).getLiteral();

        Operand caller = (Operand) callInstruction.getCaller();
        code.append(generators.apply(caller));

        int numArgs = callInstruction.getArguments().size();
        for(Element arg : callInstruction.getArguments()){
            code.append(generators.apply(arg));
        }

        addStack(-numArgs);
        if(callInstruction instanceof InvokeSpecialInstruction){
            code.append("invokespecial ");
            addStack(-1);
            code.append(className).append("/<init>");
        }
        else if(callInstruction instanceof InvokeStaticInstruction || callInstruction instanceof InvokeVirtualInstruction){
            if(callInstruction instanceof InvokeStaticInstruction){
                code.append("invokestatic ");
                String path = imports.getOrDefault(caller.getName(), caller.getName());
                code.append(path).append("/").append(methodName);
            }
            else{
                code.append("invokevirtual ");
                code.append(className).append("/").append(methodName);
                addStack(-1);
            }
        }
        else{
            throw new NotImplementedException("Type not implemented: " + callInstruction);
        }

        code.append("(");

        for (var arg : callInstruction.getArguments()) {
            code.append(types.getDescriptor(arg.getType()));
        }

        String return_type = types.getDescriptor(callInstruction.getReturnType());
        code.append(")").append(return_type).append(NL);

        if (!return_type.equals("V")) {
            addStack(1);
        }

        return code.toString();
    }

    private String generateLengthInstruction (CallInstruction callInstruction){
        StringBuilder code = new StringBuilder();

        Operand caller = (Operand) callInstruction.getCaller();
        code.append(generateOperand(caller));

        code.append("arraylength\n");

        //put the variable caller inside the usedLocals since it was used to call method
        int nRegCaller = currentMethod.getVarTable().get(caller.getName()).getVirtualReg();
        usedLocals.set(nRegCaller,0);

        return code.toString();
    }

    private String generatePutFieldInstruction(PutFieldInstruction method){
        StringBuilder code = new StringBuilder();
        Descriptor reg = currentMethod.getVarTable().get(method.getField().getName());

        addStack(1);

        code.append("aload");

        if(reg.getVirtualReg() <= 3){
            code.append("_");
        }
        else{
            code.append(" ");
        }

        if (reg.getVirtualReg() < 0){
            code.append("0").append(NL);
        }else{
            code.append(reg.getVirtualReg()).append(NL);
        }

        code.append(generators.apply(method.getValue()));
        code.append("putfield ");

        code.append(ClassFieldName(method.getObject(), method.getField())).append(" ");

        String type = types.getDescriptor(method.getValue().getType());
        code.append(type).append(NL);
        addStack(-2);

        return code.toString();
    }

    private String generateGetFieldInstruction(GetFieldInstruction method){
        StringBuilder code = new StringBuilder();
        Descriptor reg = currentMethod.getVarTable().get(method.getField().getName());

        addStack(1);

        code.append("aload");

        if(reg.getVirtualReg() <= 3){
            code.append("_");
        }
        else{
            code.append(" ");
        }

        if (reg.getVirtualReg() < 0){
            code.append("0").append(NL);
        }else{
            code.append(reg.getVirtualReg()).append(NL);
        }

        code.append("getfield ");

        code.append(ClassFieldName(method.getObject(), method.getField())).append(" ");

        String type = types.getDescriptor(method.getField().getType());
        code.append(type).append(NL);

        return code.toString();
    }

    private String ClassFieldName(Operand classe, Operand field){
        String name = classe.toElement().getType().toString();
        name = name.substring(name.lastIndexOf("(") + 1, name.length() - 1);
        return name + "/" + field.getName();
    }

    private String generateCallInstruction(CallInstruction call) {

        if(call instanceof NewInstruction){
            return generateNewInstruction((NewInstruction) call);
        }
        else if(call instanceof InvokeSpecialInstruction || call instanceof InvokeStaticInstruction || call instanceof InvokeVirtualInstruction){
            return generateInvokes(call);
        }
        else if(call instanceof ArrayLengthInstruction){
            return generateLengthInstruction(call);
        }

        throw new NotImplementedException("Type not implemented: " + call);
    }

    private String addImportPath(String className){
        className = className.substring(className.lastIndexOf("(") + 1, className.length() - 1);
        className = imports.getOrDefault(className, className);
        return className;
    }

    private String generateSingleOpConditional(SingleOpCondInstruction singleOpCondInstruction){
        StringBuilder code = new StringBuilder();

        code.append(generators.apply(singleOpCondInstruction.getCondition()));
        addStack(-1);
        code.append("ifne ");
        code.append(singleOpCondInstruction.getLabel());

        return code.toString();
    }

    private String generateGoTo(GotoInstruction gotoInstruction) {
        StringBuilder code = new StringBuilder();

        code.append("goto ");
        code.append(gotoInstruction.getLabel());

        return code.toString();
    }

    private String generateOperand(Operand operand) {
        if (operand instanceof ArrayOperand op) {
            return generateOperandArrayGet(op);
        }

        Descriptor variable = currentMethod.getVarTable().get(operand.getName());

        if (variable == null) {
            return "";
        }

        addStack(1);

        if (variable.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(variable.getVirtualReg(), 0);
        }

        String prefix = types.getPrefixStoreLoad(operand.getType());
        String suffix = " ";

        if((variable.getVirtualReg() == 0 || variable.getVirtualReg() == 1 || variable.getVirtualReg() == 2 || variable.getVirtualReg() == 3)){ //&& prefix.equals("i")
            suffix = "_";
        }
        return prefix + "load" + suffix + variable.getVirtualReg() + "\n";
    }

    private String generateOpConditional(OpCondInstruction opCondInstruction) {
        StringBuilder code = new StringBuilder();
        Instruction condition = opCondInstruction.getCondition();

        if(condition instanceof BinaryOpInstruction binaryOpInstruction){
            OperationType opType = binaryOpInstruction.getOperation().getOpType();

            // Generate operands
            code.append(apply(binaryOpInstruction.getLeftOperand()));
            code.append(apply(binaryOpInstruction.getRightOperand()));

            switch (opType) {
                case LTH -> code.append("if_icmplt ");
                case LTE -> code.append("if_icmple ");
                case GTH -> code.append("if_icmpgt ");
                case GTE -> code.append("if_icmpge ");
                case EQ -> code.append("if_icmpeq ");
                case NEQ -> code.append("if_icmpne ");
                default -> {
                    code.append(generateComparison(binaryOpInstruction));
                    addStack(-1);
                    code.append("ifne ");
                }
            }

            if (isComparisonOp(opType)) {
                addStack(-2); // Comparison consumes 2 values
            }
        }
        else if(condition instanceof UnaryOpInstruction unaryOpInstruction){
            code.append(generators.apply(unaryOpInstruction.getOperand()));
            addStack(-1);
            code.append("ifeq ");
        }
        else{
            // For other conditions, just check if non-zero
            code.append(apply(condition));
            addStack(-1);
            code.append("ifne ");
        }

        code.append(opCondInstruction.getLabel());

        return code.toString();
    }

    private String generateUnaryOp(UnaryOpInstruction unaryOpInstruction) {
        StringBuilder code = new StringBuilder();

        code.append(generators.apply(unaryOpInstruction.getOperand()));

        // For NOT operation: XOR with 1 (flip the least significant bit)
        code.append("iconst_1").append(NL);
        code.append("ixor").append(NL);

        // addStack is already handled by the operand generation
        // iconst_1 adds 1, ixor consumes 2 and produces 1, net effect is 0

        return code.toString();
    }
}