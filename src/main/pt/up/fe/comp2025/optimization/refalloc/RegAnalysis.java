package pt.up.fe.comp2025.optimization.refalloc;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.JavammParser;

import java.lang.annotation.ElementType;
import java.util.*;

public class RegAnalysis {
    public Map<Node, List<Operand>> definition;
    public Map<Node, List<Operand>> usage;
    public Map<Node, List<Operand>> in;
    public Map<Node, List<Operand>> out;


    public void analyse(Method meth) {
        this.definition = new HashMap<>();
        this.usage = new HashMap<>();
        this.in = new HashMap<>();
        this.out = new HashMap<>();
        
        List<Instruction> instructions = new ArrayList<>(meth.getInstructions());
        Collections.reverse(instructions);
        List<Operand> inst_out_op = new ArrayList<>();
        List<Operand> t_inst_out_op = new ArrayList<>();
        boolean stable = true;

        for(Instruction inst : instructions){
            List<Operand> defs = calDefs(inst, meth.getVarTable());
            if(defs != null){
                definition.put(inst, defs);
            }

            List<Operand> uses = calUses(inst);
            if(uses != null){
                usage.put(inst, uses);
            }

            in.put(inst, new ArrayList<>());
            out.put(inst, new ArrayList<>());
        }

        do {
            stable = true;

            for (Instruction inst : instructions) {
                // Salvar valores antigos para verificar estabilidade
                List<Operand> oldIn = new ArrayList<>(in.get(inst));
                List<Operand> oldOut = new ArrayList<>(out.get(inst));

                // Calcular novo Out = união dos In dos sucessores
                List<Operand> newOut = new ArrayList<>();
                if (inst.getSucc1() != null && inst.getSucc1().getNodeType() != NodeType.END) {
                    newOut.addAll(in.get(inst.getSucc1()));
                }
                if (inst.getSucc2() != null && inst.getSucc2().getNodeType() != NodeType.END) {
                    newOut.addAll(in.get(inst.getSucc2()));
                }

                // Atualizar Out
                out.put(inst, newOut);

                // Calcular novo In = Use ∪ (Out - Def)
                List<Operand> newIn = new ArrayList<>();

                // Adicionar Use
                List<Operand> uses = usage.get(inst);
                if (uses != null) {
                    newIn.addAll(uses);
                }

                // Adicionar (Out - Def)
                List<Operand> defs = definition.get(inst);
                for (Operand outOp : newOut) {
                    if (defs == null || !containsOperand(defs, outOp)) {
                        if (!containsOperand(newIn, outOp)) {
                            newIn.add(outOp);
                        }
                    }
                }

                // Atualizar In
                in.put(inst, newIn);

                // Verificar estabilidade
                if (!compareOperandLists(oldIn, newIn) || !compareOperandLists(oldOut, newOut)) {
                    stable = false;
                }
            }

        } while (!stable);
        
    }

    private boolean containsOperand(List<Operand> list, Operand op) {
        for (Operand listOp : list) {
            if (operandsEqual(listOp, op)) {
                return true;
            }
        }
        return false;
    }

    private boolean operandsEqual(Operand op1, Operand op2) {
        if (op1 == null || op2 == null) {
            return op1 == op2;
        }
        return op1.getName().equals(op2.getName());
    }

    private boolean compareOperandLists(List<Operand> list1, List<Operand> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (Operand op1 : list1) {
            boolean found = false;
            for (Operand op2 : list2) {
                if (operandsEqual(op1, op2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    //tem que ser um assign instruction
    //tem que cumprir com os seguintes requisitos:
    //1. o destino do assign não pode ser um field
    //2. o destino do assign não pode ser um parametro
    //3. o destino do assign não pode ser um literal
    //4. o destino do assign não pode se um this
    private List<Operand> calDefs(Instruction instructs, HashMap<String, Descriptor> methodVarTable) {
        List<Operand> defs = new ArrayList<>();

        if(instructs.getInstType() == InstructionType.ASSIGN){
            AssignInstruction assignInstruction = (AssignInstruction) instructs;
            Element element = assignInstruction.getDest();
            Descriptor descriptor = methodVarTable.get(((Operand) element).getName());

            if(descriptor.getScope().name().equals("FIELD") || element.isLiteral() || ((Operand) element).isParameter() || ((Operand) element).getName().equals("this")){
                return null;
            }


            defs.add((Operand) element);
            return defs;
        }
        return null;
    }
    
    private List<Operand> calUses(Instruction instructs){
        InstructionType type = instructs.getInstType();
        List<Operand> res = new ArrayList<>();
        Operand op;

        if(type == InstructionType.CALL){
            //adicionar o primeiro parametro caso seja um operand
            CallInstruction callInstruction = (CallInstruction) instructs;
            if((!callInstruction.getArguments().isEmpty())){
                if(callInstruction.getArguments().getFirst() instanceof Operand){
                    res.add((Operand) callInstruction.getArguments().getFirst());
                }
            }

            //se não é um object-call tem que ser adicionado o segundo argumento
            if(!callInstruction.getOperands().isEmpty()){
                if(callInstruction.getInvocationKind().equals("NEW")){
                    if(callInstruction.getArguments().size() > 1){
                        if(callInstruction.getArguments().get(1) instanceof Operand){
                            res.add((Operand) callInstruction.getArguments().get(1));
                        }
                    }
                }
            }

            //adiciona os outros operandos
            for(Element oper : callInstruction.getOperands()){
                if(oper instanceof Operand){
                    res.add((Operand) oper);
                }
            }

        } else if (type == InstructionType.GETFIELD) {
            GetFieldInstruction getFieldInstruction = (GetFieldInstruction) instructs;

            //busca os operandos do GET
            if(!getFieldInstruction.getOperands().isEmpty()){
                if(getFieldInstruction.getOperands().getFirst() instanceof Operand){
                    res.add((Operand) getFieldInstruction.getOperands().getFirst());
                }
            }
            if(getFieldInstruction.getOperands().size() > 1){
                if(getFieldInstruction.getOperands().get(1) instanceof Operand){
                    res.add((Operand) getFieldInstruction.getOperands().get(1));
                }
            }

        }
        else if (type == InstructionType.PUTFIELD){
            PutFieldInstruction putFieldInstruction = (PutFieldInstruction) instructs;
            if(putFieldInstruction.getOperands().size() > 3){
                if(putFieldInstruction.getOperands().get(2) instanceof Operand){
                    res.add((Operand) putFieldInstruction.getOperands().get(2));
                }
            }
        }
        else if (type == InstructionType.RETURN){
            ReturnInstruction returnInstruction = (ReturnInstruction) instructs;

            //Adiciona o operando do return em caso de não ser void
            if(returnInstruction.hasReturnValue()){
                if (returnInstruction.getOperand().isPresent() && returnInstruction.getOperand().get() instanceof Operand) {
                    res.add((Operand) returnInstruction.getOperand().get());
                }
            }
            
        } else if (type == InstructionType.BRANCH) {
            CondBranchInstruction branchInstruction = (CondBranchInstruction) instructs;

            //adiciona todos os operandos da branch instruction
            for(Element oper : branchInstruction.getOperands()){
                if(oper instanceof Operand){
                    res.add((Operand) oper);
                }
            }
            
        }else if(type == InstructionType.ASSIGN){
            AssignInstruction assignInstruction = (AssignInstruction) instructs;

            // a = b + c  -->  uses(b + c)
            res = calUses(assignInstruction.getRhs());

        } else if (type == InstructionType.UNARYOPER) {
            UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) instructs;

            //adiciona o operando
            if(unaryOpInstruction.getOperand() instanceof  Operand){
                res.add((Operand) unaryOpInstruction.getOperand());
            }
            
        } else if (type == InstructionType.BINARYOPER) {
            BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instructs;
            if(!binaryOpInstruction.getOperands().isEmpty()){
                for(Element elem : binaryOpInstruction.getOperands()){
                    if(elem instanceof Operand){
                        res.add((Operand) elem);
                    }
                }
            }
            
        } else if (type == InstructionType.NOPER) {
            SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instructs;
            if(singleOpInstruction.getSingleOperand() instanceof Operand){
                res.add((Operand) singleOpInstruction.getSingleOperand());
            }
        } else{
            return null;
        }

        return res;
    }

}
