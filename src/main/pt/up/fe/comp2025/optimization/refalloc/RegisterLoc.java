package pt.up.fe.comp2025.optimization.refalloc;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Node;
import org.specs.comp.ollir.Operand;
import pt.up.fe.comp.jmm.ollir.OllirResult;

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
        this.ollirClassUnit.buildVarTables();
        this.ollirClassUnit.buildCFGs();
        RegAnalysis regAnalysis = new RegAnalysis();

        for(Method meth : this.ollirClassUnit.getMethods()){
            regAnalysis.analyse(meth);

        }



    }


}
