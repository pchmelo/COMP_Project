package pt.up.fe.comp2025.optimization.refalloc;

import org.antlr.v4.misc.Graph;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.Instruction;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

public class RegisterLoc {

    private int numRegs;
    private ClassUnit ollirClassUnit;
    private final List<Report> reports = new ArrayList<>();


    public RegisterLoc(OllirResult ollirResult, int numRegs) {
        this.ollirClassUnit = ollirResult.getOllirClass();
        this.numRegs = numRegs;
    }

    public void optimize() {
        List<Report> rep = new ArrayList<>();
        this.ollirClassUnit.buildVarTables();
        this.ollirClassUnit.buildCFGs();
        RegAnalysis regAnalysis = new RegAnalysis();

        for(Method meth : this.ollirClassUnit.getMethods()){
            if(meth.isConstructMethod()){
                continue;
            }

            regAnalysis.analyse(meth);
            Map<Instruction, Set<String>> liveOut = convertLivenessData(regAnalysis.out);


            RegGraph regGraph = new RegGraph(meth);
            regGraph.buildInterferenceGraph(liveOut);

            Map<String, Descriptor> new_table = regGraph.colorGraph(numRegs);

            reports.addAll(regGraph.getReports());
            if (!reports.isEmpty() || new_table == null) {
                return;
            }

            meth.getVarTable().clear();
            meth.getVarTable().putAll(new_table);

        }
    }

    private Map<Instruction, Set<String>> convertLivenessData(Map<Node, List<Operand>> outMap) {
        Map<Instruction, Set<String>> result = new HashMap<>();

        for (Map.Entry<Node, List<Operand>> entry : outMap.entrySet()) {
            if (entry.getKey() instanceof Instruction) {
                Instruction inst = (Instruction) entry.getKey();
                Set<String> varNames = new HashSet<>();

                // Converter cada Operand para seu nome
                for (Operand op : entry.getValue()) {
                    if (op != null) {
                        varNames.add(op.getName());
                    }
                }

                result.put(inst, varNames);
            }
        }

        return result;
    }


}
