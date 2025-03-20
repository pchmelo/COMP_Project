package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class NewFeaturesTest {
    @Test
    public void consts() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/newfeatures/Consts.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void constsDeclWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/newfeatures/ConstsDeclWrong.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void constsAssignWrong() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/newfeatures/ConstsAssignWrong.jmm"));
        TestUtils.mustFail(result);
    }

}
