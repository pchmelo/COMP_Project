package pt.up.fe.comp2025.optimization.refalloc;

import org.antlr.v4.misc.Graph;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterLoc {

    private int numRegs;
    private ClassUnit ollirClassUnit;

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
            regAnalysis.analyse(meth);

            RegGraph regGraph = new RegGraph(meth);
            regGraph.calculate(regAnalysis.in);
            regGraph.calculate(regAnalysis.out);

            Map<String, Descriptor> new_table = regGraph.getTable(this.numRegs);

            if(new_table == null){
                return;
            }

            for(var old_entry : meth.getVarTable().entrySet()){
                for(var new_entry : new_table.entrySet()){
                    if(old_entry.getKey().equals(new_entry.getKey())){
                        meth.getVarTable().replace(old_entry.getKey(), old_entry.getValue(), new_entry.getValue());
                    }

                    if(!new_table.containsKey(old_entry.getKey())){
                        meth.getVarTable().remove(old_entry.getKey());
                    }

                }

            }

        }



    }


}
