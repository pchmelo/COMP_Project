package pt.up.fe.comp.cp2;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegAllocExtraTest {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/optimizations/";

    static OllirResult getOllirResult(String filename) {
        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), Collections.emptyMap(), false);
    }

    static OllirResult getOllirResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getOptimize(), "true");

        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    static OllirResult getOllirResultRegalloc(String filename, int maxRegs) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getRegister(), Integer.toString(maxRegs));


        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    @Test
    public void regAllocWithLoops() {
        String filename = "reg_alloc_extra/regalloc_with_loops.jmm";
        int expectedTotalReg = 4;
        int configMaxRegs = 3;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "loopMethod"));

        CpUtils.assertTrue("Expected number of locals in 'loopMethod' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "loopMethod").getVarTable();
        var iReg = varTable.get("i").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'i' and 'sum' to be different", iReg, varTable.get("sum").getVirtualReg(), optimized);
    }

    @Test
    public void regAllocUnusedVariables() {
        String filename = "reg_alloc_extra/regalloc_unused_vars.jmm";
        int expectedTotalReg = 2;
        int configMaxRegs = 1;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "unusedVars"));

        CpUtils.assertTrue("Expected number of locals in 'unusedVars' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "unusedVars").getVarTable();
        CpUtils.assertTrue("Expected unused variable 'temp' to not be in the variable table",
                !varTable.containsKey("temp"),
                optimized);
    }

    @Test
    public void regAllocReusedVariables() {
        String filename = "reg_alloc_extra/regalloc_reused_vars.jmm";
        int expectedTotalReg = 2;
        int configMaxRegs = 1;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "reusedVars"));

        CpUtils.assertTrue("Expected number of locals in 'reusedVars' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "reusedVars").getVarTable();
        CpUtils.assertEquals("Expected both 'x' and 'y' to share the same register",
                varTable.get("x").getVirtualReg(),
                varTable.get("y").getVirtualReg(),
                optimized);
    }

    @Test
    public void regAllocWithConditionals() {
        String filename = "reg_alloc_extra/regalloc_with_conditionals.jmm";
        int expectedTotalReg = 4;
        int configMaxRegs = 2;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "conditionals"));

        CpUtils.assertTrue("Expected number of locals in 'conditionals' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "conditionals").getVarTable();
        var xReg = varTable.get("x").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'x' and 'y' to be different", xReg, varTable.get("y").getVirtualReg(), optimized);
    }


    @Test
    public void regAllocComplexExpressions() {
        String filename = "reg_alloc_extra/regalloc_complex_expressions.jmm";
        int expectedTotalReg = 3;
        int configMaxRegs = 2;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "complexExpressions"));

        CpUtils.assertTrue("Expected number of locals in 'complexExpressions' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "complexExpressions").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        var bReg = varTable.get("b").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'b' to be different", aReg, bReg, optimized);
    }

    @Test
    public void regAllocWithArrayAccess() {
        String filename = "reg_alloc_extra/regalloc_with_array_access.jmm";
        int expectedTotalReg = 2;
        int configMaxRegs = 1;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "arrayAccess"));

        CpUtils.assertTrue("Expected number of locals in 'arrayAccess' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "arrayAccess").getVarTable();
        var arrReg = varTable.get("arr").getVirtualReg();
        var indexReg = varTable.get("index").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'arr' and 'index' to be different", arrReg, indexReg, optimized);
    }

    @Test
    public void regAllocWithNestedLoops() {
        String filename = "reg_alloc_extra/regalloc_nested_loops.jmm";
        int expectedTotalReg = 5;
        int configMaxRegs = 2;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "nestedLoops"));

        CpUtils.assertTrue("Expected number of locals in 'nestedLoops' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);

        var varTable = CpUtils.getMethod(optimized, "nestedLoops").getVarTable();
        var iReg = varTable.get("i").getVirtualReg();
        var jReg = varTable.get("j").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'i' and 'j' to be different", iReg, jReg, optimized);
    }



}
