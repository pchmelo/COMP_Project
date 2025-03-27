package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ExtraTests {

    @Test
    public void thisTest() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisTest.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void thisTestError() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisTestError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifSequence() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/IfSequence.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ifSequenceError() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/IfSequenceError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void thisExtend(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisExtend.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ArrayTest.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void methodCall(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodCall.jmm"));
        TestUtils.noErrors(result);
    }


    @Test
    public void methodCallError(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodCallError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void field(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/Field.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void thisStatic(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisStatic.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayOperations(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ArrayOperations.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void thisDecl(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisDecl.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void varArgs(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgs.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varArgsArrayParam(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgsArrayParam.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void varArgsField(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgsFields.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void varArgsDeclaration(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgsDeclaration.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void varArgsMethodReturn(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgsMethodReturn.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void varArgsMethod(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarArgsMethod.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VarAssignStmt(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmt.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarAssignStmtThis(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtThis.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VarAssignStmtThisArray(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtThisArray.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VarAssignStmtThisSuper(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtThisSuper.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VarAssignStmtError(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VarAssignStmtClassError(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtClassError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VarAssignStmtClass(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VarAssignStmtClass.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ExampleCode(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ExampleCode.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ImportExample(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportExample.jmm"));
        TestUtils.noErrors(result);
    }
}
