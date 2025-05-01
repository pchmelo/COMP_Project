package pt.up.fe.comp2025.optimization.refalloc;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class RegGraph {
    public List<Report> reports = new ArrayList<>();
    private final Map<Integer, Set<Integer>> connections = new HashMap<>();
    private final Map<Integer, List<Integer>> colors = new HashMap<>();
    private final Stack<Integer> stack = new Stack<>();

    private final Method method;
    private Integer minNumRegs;
    private Integer numRegs;


    public RegGraph(Method method) {
        this.method = method;
        this.init();
    }

    private void init(){
        int res = 0;
        for(Descriptor descriptor : method.getVarTable().values()){
            VarScope scope = descriptor.getScope();
            if(scope == VarScope.PARAMETER || scope == VarScope.LOCAL){
                res++;
            }
            else{
                this.connections.put(descriptor.getVirtualReg(), new HashSet<>());
            }
        }
        if(!this.method.isStaticMethod()){
            res++;
        }
        this.minNumRegs = res;
    }

    public void calculate(Map<Node, List<Operand>> meth_op){
        for(Node node : meth_op.keySet()){
            for(Operand op1 : meth_op.get(node)){
                for(Operand op2 : meth_op.get(node)){
                    if(!op1.equals(op2)){
                        Descriptor descriptor = this.method.getVarTable().get(op1.getName());
                        if(connections.get(descriptor.getVirtualReg()) != null){
                            var inx = this.method.getVarTable().get(op1.getName()).getVirtualReg();
                            var add = this.method.getVarTable().get(op2.getName()).getVirtualReg();
                            this.connections.get(inx).add(add);
                        }
                    }
                }
            }
        }
    }

    public Map<String, Descriptor> getTable(int reg){
        if(!this.canAllocateMoreReg(reg)){
            return null;
        }

        for(Integer i = this.minNumRegs; i < this.numRegs; i++){
            this.colors.put(i, new ArrayList<>());
        }

        this.fill_var_stack();

        while(!this.stack.isEmpty()){
            if(this.updateColors()){
                stack.clear();
                return this.getTable(this.numRegs + 1);
            }

        }

        return this.updateTable();
    }

    private Boolean canAllocateMoreReg(int reg){

        //if reg == 0 -> reg vai ser o mínimo possivel
        if(reg == 0){
            this.numRegs = this.minNumRegs;
            return true;
        }

        //se o numero pedido é impossivel é um erro
        if(reg < this.minNumRegs){
            var message = "Impossible to allocate " + reg + " registers, minimum is " + this.minNumRegs;
            this.reports.add(Report.newError(
                    Stage.SEMANTIC,0, 0,
                    message,
                    null)
            );
            return false;
        }
        this.numRegs = reg;
        return true;
    }

    private Boolean updateColors(){
        Integer reg_idx = this.stack.pop();

        for(Integer color : this.colors.keySet()){
            boolean can_be_colored = true;
            for(Integer var : this.connections.get(reg_idx)){
                //var is already colored
                if(this.colors.get(color).contains(var)){
                    can_be_colored = false;
                    break;
                }
            }
            if(can_be_colored){
                this.colors.get(color).add(reg_idx);
                return false;
            }

        }
        return true;
    }

    private void fill_var_stack(){
        Map<Integer, Set<Integer>> res = new HashMap<>(this.connections);

        this.stack.clear();
        while(!res.isEmpty()){
            for(var entry : res.entrySet()){
                if(entry.getValue().size() < this.numRegs){
                    this.stack.push(entry.getKey());
                    res.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    private Map<String, Descriptor> updateTable(){
        Map<String, Descriptor> res = new HashMap<>(this.method.getVarTable());

        for (Map.Entry<Integer, List<Integer>> entry : this.colors.entrySet()){
            Integer color = entry.getKey();
            List<Integer> vars = entry.getValue();

            for (Integer var : vars) {
                for (Map.Entry<String, Descriptor> entry2 : res.entrySet()) {
                    Descriptor descriptor_1 = entry2.getValue();
                    Descriptor descriptor_2 = null;

                    for(Descriptor descri : this.method.getVarTable().values()){
                        if(var == descri.getVirtualReg()){
                            descriptor_2 = descri;
                            break;
                        }
                    }

                    if(descriptor_1 == descriptor_2){
                        String key = entry2.getKey();
                        res.put(key, new Descriptor(descriptor_1.getScope(), color, descriptor_1.getVarType()));
                    }
                    
                }
            }
        }

        return res;
    }

}