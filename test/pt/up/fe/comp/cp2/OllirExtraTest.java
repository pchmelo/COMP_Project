package pt.up.fe.comp.cp2;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class OllirExtraTest {

    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/cp2/ollir/extra_tests" + filename));
    }

    //TODO: IF TESTS

    @Test
    public void SimpleIf() {
        var result = getOllirResult("/SimpleIf.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    @Test
    public void SimpleIfElse() {
        var result = getOllirResult("/SimpleIfElse.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    @Test
    public void SimpleIfElifElse() {
        var result = getOllirResult("/SimpleIfElifElse.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 2, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 2, result);
    }

    @Test
    public void SimpleIfElifElifElifElse() {
        var result = getOllirResult("/SimpleIfElifElifElifElse.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 4, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 4, result);
    }

    @Test
    public void SimpleIfIfElse() {
        var result = getOllirResult("/SimpleIfIfElse.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 2, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 2, result);
    }

    @Test
    public void SimpleIfElseBool() {
        var result = getOllirResult("/SimpleIfElseBool.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    //TODO: Postfix TESTS

    @Test
    public void PostFix() {
        var result = getOllirResult("/PostFix.jmm");
    }

    @Test
    public void PostFixMinus() {
        var result = getOllirResult("/PostFixMinus.jmm");
    }

    //TODO: Parentheses TESTS

    @Test
    public void Parentheses() {
        var result = getOllirResult("/Parentheses.jmm");
    }

    @Test
    public void Parentheses2() {
        var result = getOllirResult("/Parentheses2.jmm");
    }

    @Test
    public void Parentheses3() {
        var result = getOllirResult("/Parentheses3.jmm");
    }

    //TODO: NotExpr TESTS

    @Test
    public void NotExpr() {
        var result = getOllirResult("/NotExpr.jmm");
    }

    //TODO: Array TESTS

    @Test
    public void ArrayTest() {
        var result = getOllirResult("/ArrayTest.jmm");
    }

    //TODO: Varargs TESTS

    @Test
    public void Varargs() {
        var result = getOllirResult("/Varargs.jmm");
    }

    @Test
    public void VarArgsFunctionName() {
        var result = getOllirResult("/VarArgsFunctionName.jmm");
    }

    @Test
    public void VarArgsArrayParam() {
        var result = getOllirResult("/VarArgsArrayParam.jmm");
    }

}
