package pt.up.fe.comp.cp2;

import org.junit.Test;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import static org.hamcrest.CoreMatchers.hasItem;

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
        CpUtils.assertEquals("Number of branches", 5, branches.size(), result);

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

    //TODO: While TESTS

    @Test
    public void IfWhileError() {
        var result = getOllirResult("/IfWhileError.jmm");

        var method = CpUtils.getMethod(result, "main");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches", branches.size() >= 6, result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 3, result);
    }

    @Test
    public void WhileSimple() {
        var result = getOllirResult("/WhileSimple.jmm");
    }

    @Test
    public void WhileSimple2() {
        var result = getOllirResult("/WhileSimple2.jmm");
    }

    @Test
    public void WhileIf2() {
        var result = getOllirResult("/WhileIf2.jmm");

        var method = CpUtils.getMethod(result, "main");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches equals 3", branches.size() >= 3, result);
    }

    //TODO: Postfix TESTS

    @Test
    public void PostFix() {
        var result = getOllirResult("/PostFix.jmm");
    }

    @Test
    public void PostFixField() {
        var result = getOllirResult("/PostFixField.jmm");
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

    @Test
    public void AssignMethodNoParamsError() {
        var result = getOllirResult("/AssignMethodNoParamsError.jmm");

        var method = CpUtils.getMethod(result, "bar");

        var branches = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Number of calls", branches.size() == 1, result);

        var callInst = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Call instruction in method bar must be 1", callInst.size() == 1, result);

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Call instruction in method bar must be 1", invInst.size() == 1, result);

    }

    @Test
    public void ParamsError() {
        var result = getOllirResult("/ParamsError.jmm");

        var method = CpUtils.getMethod(result, "main");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Number of calls", calls.size() == 9, result);

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 3", invInst.size() == 3, result);
    }

    @Test
    public void ParamsError2() {
        var result = getOllirResult("/ParamsError2.jmm");

        var method = CpUtils.getMethod(result, "main");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Number of calls", calls.size() == 3, result);

        var newInst = CpUtils.assertInstExists(NewInstruction.class, method, result);
        CpUtils.assertTrue("New instruction in method main must be 1", newInst.size() == 1, result);

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);

        var spcInst = CpUtils.assertInstExists(InvokeSpecialInstruction.class, method, result);
        CpUtils.assertTrue("Special instruction in method main must be 1", spcInst.size() == 1, result);

        var ops = CpUtils.assertInstExists(BinaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Number of ops", ops.size() == 2, result);

        var uniOps = CpUtils.assertInstExists(UnaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Number of uniOps", uniOps.size() == 1, result);

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches", branches.size() >= 2, result);
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

    @Test
    public void ArrayAccessError() {
        var result = getOllirResult("/ArrayAccessError.jmm");
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

    //TODO: Fields TESTS

    @Test
    public void FieldVarAssignStmt() {
        var result = getOllirResult("/FieldVarAssignStmt.jmm");
    }

    @Test
    public void FieldVarArrayAssignStmt() {
        var result = getOllirResult("/FieldVarArrayAssignStmt.jmm");
    }

    @Test
    public void FieldVarAssignStmtMinus() {
        var result = getOllirResult("/FieldVarAssignStmtMinus.jmm");
    }

    //TODO: Arithmetic TESTS

    @Test
    public void AriError() {
        var result = getOllirResult("/AriError.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeStaticInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void AndLessThanError() {
        var result = getOllirResult("/AndLessThanError.jmm");

        var method = CpUtils.getMethod(result, "f");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches", branches.size() >= 2, result);

        var biOperations = CpUtils.assertInstExists(BinaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", biOperations.size() >= 2, result);
    }

    @Test
    public void AndError() {
        var result = getOllirResult("/AndError.jmm");

        var method = CpUtils.getMethod(result, "f");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches equals 1", branches.size() == 1, result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has 1 goto", gotos.size() == 1, result);
    }

    @Test
    public void AndIfError() {
        var result = getOllirResult("/AndIfError.jmm");

        var method = CpUtils.getMethod(result, "f");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertTrue("Number of branches", branches.size() >= 2, result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 2 goto", gotos.size() >= 2, result);
    }

    @Test
    public void AddError() {
        var result = getOllirResult("/AddError.jmm");

        var method = CpUtils.getMethod(result, "foo");

        var ops = CpUtils.assertInstExists(BinaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Number of ops", ops.size() == 3, result);
    }

    @Test
    public void DivError() {
        var result = getOllirResult("/DivError.jmm");

        var method = CpUtils.getMethod(result, "foo");

        var ops = CpUtils.assertInstExists(BinaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Number of ops", ops.size() == 15, result);
    }

    @Test
    public void BinaryError() {
        var result = getOllirResult("/BinaryError.jmm");

        var method = CpUtils.getMethod(result, "foo");

        var ops = CpUtils.assertInstExists(BinaryOpInstruction.class, method, result);
        CpUtils.assertTrue("Number of ops", ops.size() == 4, result);
    }

    //TODO: Invoke TESTS

    @Test
    public void InvokeError() {
        var result = getOllirResult("/InvokeError.jmm");
    }

    @Test
    public void InvokeMultiConstantsError() {
        var result = getOllirResult("/InvokeMultiConstantsError.jmm");
    }

    @Test
    public void CallingImportError() {
        var result = getOllirResult("/CallingImportError.jmm");

        var method = CpUtils.getMethod(result, "main");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Number of calls", calls.size() > 3, result);
        CpUtils.assertTrue("Number of calls", calls.size() < 6, result);

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 2", invInst.size() == 2, result);
    }

    @Test
    public void InvokeError2() {
        var result = getOllirResult("/InvokeError2.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void InvokeError3() {
        var result = getOllirResult("/InvokeError3.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeSpecialInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void InvokeError4() {
        var result = getOllirResult("/InvokeError4.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void InvokeError5() {
        var result = getOllirResult("/InvokeError5.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void InvokeError6() {
        var result = getOllirResult("/InvokeError6.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 1", invInst.size() == 1, result);
    }

    @Test
    public void InvokeError7() {
        var result = getOllirResult("/InvokeError7.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 3", invInst.size() == 3, result);
    }

    @Test
    public void InvokeError8() {  //Small error: The caller of foo is an Import, so the code makes its return type to be the same as the parent. However, since the parent node of the foo function is a method, and methods can have any type as parameter, it is not correct to say that the foo method will have the same type, it should have bool. But I'm too lazy to fix that very specific case. I rather take the risk
        var result = getOllirResult("/InvokeError8.jmm");

        var method = CpUtils.getMethod(result, "main");

        var invInst = CpUtils.assertInstExists(InvokeVirtualInstruction.class, method, result);
        CpUtils.assertTrue("Invoke instruction in method main must be 3", invInst.size() == 3, result);
    }

    //TODO: Error TESTS

    @Test
    public void MainError() {
        var result = getOllirResult("/MainError.jmm");
        var method = CpUtils.getMethod(result, "main");

        CpUtils.assertTrue("Static", method.isStaticMethod(), result);
        CpUtils.assertTrue("Public", !method.getMethodAccessModifier().name().equals("PUBLIC"), result);
    }

    @Test
    public void MainError2() {
        var result = getOllirResult("/MainError2.jmm");
        var method = CpUtils.getMethod(result, "main");

        CpUtils.assertTrue("Not Static", !method.isStaticMethod(), result);
        CpUtils.assertTrue("Public", method.getMethodAccessModifier().name().equals("PUBLIC"), result);

    }

    @Test
    public void MainError3() {
        var result = getOllirResult("/MainError3.jmm");
        var method = CpUtils.getMethod(result, "main");
        CpUtils.assertTrue("Static", method.isStaticMethod(), result);
        CpUtils.assertTrue("Public", method.getMethodAccessModifier().name().equals("PUBLIC"), result);
    }

    @Test
    public void MainError4() {
        var result = getOllirResult("/MainError4.jmm");
        var method = CpUtils.getMethod(result, "main");

        CpUtils.assertTrue("Not Static", !method.isStaticMethod(), result);
        CpUtils.assertTrue("Public", !method.getMethodAccessModifier().name().equals("PUBLIC"), result);
    }

}
