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
    public void postFix(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/PostFix.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void postFixError(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/PostFixError.jmm"));
        TestUtils.mustFail(result);
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


}
