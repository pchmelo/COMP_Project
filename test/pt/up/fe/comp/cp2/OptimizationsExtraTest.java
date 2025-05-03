package pt.up.fe.comp.cp2;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OptimizationsExtraTest {
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
    public void constPropSimpleForward(){
        String filename = "const_prop_fold_extra/PropSimpleForward.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        CpUtils.assertLiteralReturn("1", method, optimized);
    }

    @Test
    public void PropSimpleDiffAssignments(){
        String filename = "const_prop_fold_extra/PropSimpleDiffAssignments.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method1 = CpUtils.getMethod(optimized, "foo1");
        CpUtils.assertLiteralReturn("2", method1, optimized);

        var method2 = CpUtils.getMethod(optimized, "foo2");
        CpUtils.assertLiteralReturn("0", method2, optimized);

        var method3 = CpUtils.getMethod(optimized, "foo3");
        CpUtils.assertLiteralReturn("1", method3, optimized);

        var method4 = CpUtils.getMethod(optimized, "foo4");
        CpUtils.assertLiteralReturn("5", method4, optimized);
    }

    @Test
    public void PropSimplePosFix(){
        String filename = "const_prop_fold_extra/PropSimplePosFix.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method1 = CpUtils.getMethod(optimized, "foo1");
        CpUtils.assertLiteralReturn("2", method1, optimized);

        var method2 = CpUtils.getMethod(optimized, "foo2");
        CpUtils.assertLiteralReturn("0", method2, optimized);
    }

    @Test
    public void FoldSimpleForward() {
        String filename = "const_prop_fold/FoldSimpleForward.jmm";

        var original = getOllirResult(filename);
        var optimized = getOllirResultOpt(filename);

        CpUtils.assertTrue("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                !original.getOllirCode().equals(optimized.getOllirCode()), optimized);

        var method1 = CpUtils.getMethod(optimized, "foo1");
        CpUtils.assertFindLiteral("30", method1, optimized);

        var method2 = CpUtils.getMethod(optimized, "foo2");
        CpUtils.assertFindLiteral("false", method2, optimized);
    }

    @Test
    public void IfElseIfElse(){
        String filename = "const_prop_fold_extra/IfElseIfElse.jmm";

        var original = getOllirResult(filename);
        var optimized = getOllirResultOpt(filename);

        CpUtils.assertTrue("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                !original.getOllirCode().equals(optimized.getOllirCode()), optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        CpUtils.assertLiteralCount("3", method, optimized, 3);

        CpUtils.assertLiteralCount("9", method, optimized, 1);

        CpUtils.assertLiteralCount("2", method, optimized, 1);

        CpUtils.assertLiteralCount("4", method, optimized, 1);


    }






}
