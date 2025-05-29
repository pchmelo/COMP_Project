package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
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
    private int maxLocals = 0;

    private List<Integer> usedLocals = new ArrayList<>();

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

        //new generators
        generators.put(PutFieldInstruction.class, this::generatePutFieldInstruction);
        generators.put(GetFieldInstruction.class, this::generateGetFieldInstruction);
        generators.put(CallInstruction.class, this::generateCallInstruction);
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

    private void addLocals(int locals) {
        maxLocals = Math.max(maxLocals, locals + 1);
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
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

        // generate class name
        var className = classUnit.getClassName();
        code.append(".class ").append(className).append(NL);

        // DONE: When you support 'extends', this must be updated
        var superClass = getSuperClassName();
        code.append(".super ").append(superClass).append(NL).append(NL);

        for (var field : classUnit.getFields()) {
            code.append(getField(field)).append(NL);
        }


        // generate code for all other methods
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

    private int generateLimitLocals(String methodName){
        int limitLocals = 0;
        for (Method method : ollirResult.getOllirClass().getMethods()){
            if (method.getMethodName().equals(methodName)){
                limitLocals = method.getVarTable().size(); //-1 because we are not counting the this local variable that is included in each instance method
                usedLocals.add(0);
                for (int i = 1; i < method.getVarTable().size(); i++){
                    usedLocals.add(-1);
                }
                break;
            }
        }

        return limitLocals;
    }


    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;

        var code = new StringBuilder();
        code.append(this.getMethodHeader(method));

        var methodName = method.getMethodName();
        int limitLocals = generateLimitLocals(methodName); //obrigatorio antes das instruções

        StringBuilder instructions = new StringBuilder();
        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            instructions.append(instCode);
        }

        for (int i = 0; i < usedLocals.size() ; i++){
            limitLocals += usedLocals.get(i);
        }

        code.append(".limit stack ").append(maxStack).append(NL);
        code.append(".limit locals ").append(limitLocals).append(NL);
        code.append(instructions);
        code.append(".end method\n\n");

        System.out.println("METHOD LOCALS " + limitLocals);

        // unset method
        currentMethod = null;
        this.maxStack = 0;
        this.maxLocals = 0;
        usedLocals.clear();
        System.out.println("ENDING METHOD " + method.getMethodName());
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

        var operand = (Operand) lhs;

        if (operand.toString().contains("ArrayOperand")){
            code.append(this.generateStoreAssignArray(operand, assign));
            return code.toString();
        }

        // generate code for loading what's on the right
        code.append(apply(assign.getRhs()));

        code.append(this.generateStoreAssign(operand)).append(NL);
        return code.toString();
    }

    private String generateStoreAssign(Operand operand) {
        Descriptor reg = currentMethod.getVarTable().get(operand.getName());
        this.addLocals(reg.getVirtualReg());
        this.addStack(-1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        Type operandType = operand.getType();

        return types.getStoreInstruction(operandType, reg.getVirtualReg());
    }

    private String generateStoreAssignArray(Operand operand , AssignInstruction assign) {
        StringBuilder code = new StringBuilder();

        operand.getName();
        //dar load do array antes de tudo
        currentMethod.getVarTable().get(operand.getName());
        code.append(generateOperand(operand));

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

    private String generateOperand(Operand operand) {
        var reg = currentMethod.getVarTable().get(operand.getName());
        addLocals(reg.getVirtualReg());
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

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        addStack(-2);
        addStack(1);

        // TODO: Hardcoded for int type, needs to be expanded
        var typePrefix = "i";

        // apply operation
        var op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "add";
            case MUL -> "mul";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(typePrefix + op).append(NL);

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

            //if ollir is storing the params as a temp , and then it stores the temp, we must load it again
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

        //if its not an import then it must be a "this" or a varaible...
        if (!ollirResult.getOllirClass().getImports().contains(caller.getName())){
            code.append(generateOperand(caller));
        }else{
            //NAO FAZ NADA PORQUE JA VEM COM LOAD SOMEHOW
        }

        int numArgs = callInstruction.getArguments().size();
        for(var arg : callInstruction.getArguments()){
            code.append(generateOperand((Operand) arg));
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
            }
            else{
                code.append("invokevirtual ");
                addStack(-1);
            }
            //To create the ArrayInit/method or ioPlus/method
            if (caller.getName().equals("this")){
                code.append(className);
            }else{
                code.append(caller.getName());
            }
            code.append("/").append(methodName);
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

        //TODO: ns  se é necessário ter o name da operation - AFINAL ISTO NAO É NECESSÁRIO FOR SOME REASON :(
       /* ArrayType tipo = (ArrayType) caller.getType();
        Type tipo_elem = tipo.getElementType();
        Operand storeOperand = new Operand("temp1",tipo_elem);
        code.append(generateStoreAssign(storeOperand));
       // for (a : currentMethod.getVarTable().){

        //}*/
        return code.toString();
    }

    private String generatePutFieldInstruction(PutFieldInstruction method){
        StringBuilder code = new StringBuilder();
        Descriptor reg = currentMethod.getVarTable().get(method.getField().getName());

        addLocals(reg.getVirtualReg());
        addStack(1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        code.append("aload");

        if(reg.getVirtualReg() <= 3 && reg.getVirtualReg() >= 0){
            code.append("_");
        }
        else{
            code.append(" ");
        }

        code.append(reg.getVirtualReg()).append(NL);
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

        addLocals(reg.getVirtualReg());
        addStack(1);

        if (reg.getVirtualReg() < usedLocals.size() ){
            usedLocals.set(reg.getVirtualReg(), 0);
        }

        code.append("aload");

        if(reg.getVirtualReg() <= 3 && reg.getVirtualReg() >= 0){
            code.append("_");
        }
        else{
            code.append(" ");
        }

        code.append(reg.getVirtualReg()).append(NL);
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


}