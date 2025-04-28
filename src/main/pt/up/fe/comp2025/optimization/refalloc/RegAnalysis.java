package pt.up.fe.comp2025.optimization.refalloc;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.Instruction;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.lang.annotation.ElementType;
import java.util.*;

public class RegAnalysis {
    public Map<Node, List<Operand>> definition = new HashMap<>();
    public Map<Node, List<Operand>> usage = new HashMap<>();
    public Map<Node, List<Operand>> in = new HashMap<>();
    public Map<Node, List<Operand>> out = new HashMap<>();


    public void analyse(Method meth) {

        List<Instruction> instructions = new ArrayList<>(meth.getInstructions());
        Collections.reverse(instructions);

        for(Instruction inst : instructions){
            List<Operand> defs = calDefs(inst, meth.getVarTable());
            if(defs != null){
                definition.put(inst, defs);
            }

            List<Operand> uses = calUses(inst);
            if(uses != null){
                usage.put(inst, uses);
            }
            
            
            
        }

        int i = 0;

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
            if((callInstruction.getArguments().getFirst() instanceof Operand)){
                res.add((Operand) callInstruction.getArguments().getFirst());
            }


        } else if (type == InstructionType.GETFIELD) {
            
        }
        else if (type == InstructionType.PUTFIELD){
            
        }
        else if (type == InstructionType.RETURN){
            
        } else if (type == InstructionType.BRANCH) {

            
        }else if(type == InstructionType.ASSIGN){

        } else if (type == InstructionType.UNARYOPER) {
            
        } else if (type == InstructionType.BINARYOPER) {
            
        } else if (type == InstructionType.NOPER) {
            
        } else{
            return null;
        }

        return res;
    }

}
