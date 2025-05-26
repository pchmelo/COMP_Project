package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.specs.comp.ollir.OperandType.ARRAYREF;
import static org.specs.comp.ollir.OperandType.INT32;
import static org.specs.comp.ollir.OperandType.BOOLEAN;
import static org.specs.comp.ollir.OperandType.STRING;
import static org.specs.comp.ollir.OperandType.VOID;
import static org.specs.comp.ollir.OperandType.OBJECTREF;


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
        generators.put(NewInstruction.class, this::generateNewInstruction);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
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
        var className = ollirResult.getOllirClass().getClassName();
        code.append(".class ").append(className).append(NL).append(NL);

        // TODO: When you support 'extends', this must be updated
        var fullSuperClass = "java/lang/Object";

        code.append(".super ").append(fullSuperClass).append(NL);

        // generate a single constructor method
        var defaultConstructor = """
                ;default constructor
                .method public <init>()V
                    aload_0
                    invokespecial %s/<init>()V
                    return
                .end method
                """.formatted(fullSuperClass);
        code.append(defaultConstructor);

        // generate code for all other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }

        return code.toString();
    }


    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;

        var code = new StringBuilder();

        // calculate modifier
        var modifier = types.getModifier(method.getMethodAccessModifier());

        var methodName = method.getMethodName();

        // TODO: Hardcoded param types and return type, needs to be expanded
        var params = method.getParams().stream()
                .map(param -> types.getDescriptor(param.getType()))
                .collect(Collectors.joining());
        var returnType = "I";

        code.append("\n.method ").append(modifier)
                .append(methodName)
                .append("(" + params + ")" + returnType).append(NL);

        // Add limits
        code.append(TAB).append(".limit stack 99").append(NL);
        code.append(TAB).append(".limit locals 99").append(NL);

        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            code.append(instCode);
        }

        code.append(".end method\n");

        // unset method
        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String getMethodStart(Method method){
        StringBuilder code = new StringBuilder();

        String modifier;

        if(method.getMethodAccessModifier() != AccessModifier.DEFAULT){
            modifier = method.getMethodAccessModifier().name().toLowerCase() + " ";
        }
        else{
            modifier = "public ";
        }

        if (method.isStaticMethod()) {
            modifier += "static ";
        }

        if (method.isFinalMethod()) {
            modifier += "final ";
        }

        code.append(".method ").append(modifier).append(method.getMethodName());

        StringBuilder params = new StringBuilder("(");

        for(Element param : method.getParams()){
            params.append(generateType(param.getType()));
        }
        code.append(params);
        code.append(")");

        String returnType = generateType(method.getReturnType());
        code.append(returnType).append(NL);

        return code.toString();
    }

    private String generateType(org.specs.comp.ollir.type.Type type) {
        return switch (type.toString()) {
            case "INT32" -> "I";
            case "STRING[]" -> "Ljava/lang/String;";
            case "BOOLEAN" -> "Z";
            case "ARRAYREF" -> {
                ArrayType arrayType = (ArrayType) type;
                yield "[" + generateType(arrayType.getElementType());
            }
            case "VOID" -> "V";
            case "OBJECTREF" -> {
                ClassType classType = (ClassType) type;
                String name = classType.getName();
                yield "L" + imports.getOrDefault(name, name) + ";";
            }
            default -> throw new NotImplementedException(type);
        };
    }



    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();

        // generate code for loading what's on the right
        code.append(apply(assign.getRhs()));

        // store value in the stack in destination
        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());


        // TODO: Hardcoded for int type, needs to be expanded
        code.append("istore ").append(reg.getVirtualReg()).append(NL);

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        // get register

        var reg = currentMethod.getVarTable().get(operand.getName());

        // TODO: Hardcoded for int type, needs to be expanded
        return "iload " + reg.getVirtualReg() + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

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
            throw new NotImplementedException("Return without operand");
        }

        var loadOperand = apply(returnInst.getOperand().get());
        code.append(loadOperand);
        code.append(types.getPrefix(returnInst.getReturnType()) + "return\n");

        return code.toString();
    }


    //                             New function to generate new instruction

    private String generateNewInstruction(NewInstruction newInst) {
        StringBuilder code = new StringBuilder();
        var callerType = newInst.getCaller().getType();
        if (callerType instanceof ArrayType arrayType) {
            SpecsCheck.checkArgument(
                    newInst.getArguments().size() == 1,
                    () -> "Expected number of arguments to be 1 for new array, but got " + newInst.getArguments().size()
            );

            code.append(apply(newInst.getArguments().getFirst()));
            var typeCode = types.getArrayType(arrayType.getElementType());
            code.append("newarray " + typeCode + "\n");
            return code.toString();
        }

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var className = invokeSpecial.toString();
        className = className.substring(className.lastIndexOf("(") + 1, className.length() - 1);
        className = imports.getOrDefault(className, className);

        return "invokespecial " + className + "/<init>";
    }

    private String storeAux(Operand operand) {
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());
        return prefix + "store " + reg.getVirtualReg() + "\n";
    }

    private String loadAux(Operand operand){
        var reg = currentMethod.getVarTable().get(operand.getName());

        var prefix = types.getPrefix(operand.getType());
        return prefix + "load " + reg.getVirtualReg() + "\n";
    }


}