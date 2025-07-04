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

    @Test
    public void ImportCheck(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCheck.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ImportCheck2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCheck2.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ImportCheck3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCheck3.jmm"));
        TestUtils.noErrors(result);
    }

    //im not doing every test to fail I NEED FOOD
    @Test
    public void ImportCheck4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCheck4.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ImportCheck5(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCheck5.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void FieldNamedMain(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/FieldNamedMain.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedImports(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedImports.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedImports2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedImports2.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedImports3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedImports3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedFields(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedFields.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedFields2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedFields2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedFields3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedFields3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedFields4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedFields4.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedParams(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedParams2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedParams3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams3.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedParams4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams4.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedParams5(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams5.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedParams6(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams6.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedParams7(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedParams7.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void DuplicatedMethods(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedMethods.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedMethods2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedMethods2.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedMethods3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedMethods3.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedMethods4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedMethods4.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicatedReturns(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicatedReturns.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void NoReturns(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/NoReturns.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void NoReturnsVoid(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/NoReturnsVoid.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffAfterReturn(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffAfterReturn.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void VoidMethodEmpty(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VoidMethodEmpty.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ReturnIntSike(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ReturnIntSike.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ReturnIntSike2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ReturnIntSike2.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ReturnIntSike3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ReturnIntSike3.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void MainHasThis(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainHasThis.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void StuffWithArrays(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffWithArrays2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffWithArrays3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffWithArrays4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays4.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffWithArrays5(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays5.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void StuffWithArrays6(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays6.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StuffWithArrays7(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays7.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void StuffWithArrays8(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays8.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void StuffWithArrays9(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StuffWithArrays9.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void CallHasThisParam(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/CallHasThisParam.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest4.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest5(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest5.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodTest6(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodTest6.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void MainError1Test(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainError1.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void MainError2Test(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainError2.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void MainError3Test(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainError3.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void MainError4Test(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainError4.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void MainError5Test(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainError5.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void MainNormalTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MainNormal.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void NameFunctionsErrorTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/NameFunctionsError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicateArgumentsErrorTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicateArgumentsError.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicateLocalVar(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicateLocalVar.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void DuplicateVarNamesTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/DuplicateVarNames.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void SwitchDeclarationAndInitializationTest(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/SwitchDeclarationAndInitialization.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void SwitchDeclarationAndInitializationTest2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/SwitchDeclarationAndInitialization2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void SwitchDeclarationAndInitializationTest3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/SwitchDeclarationAndInitialization3.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void SwitchDeclarationAndInitializationTest4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/SwitchDeclarationAndInitialization4.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ThisAsArgument(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ThisAsArgument.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodCallOnClass(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodCallOnClass.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void  methodSignatureSimpleOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodSignatureSimpleOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void MethodSignatureWrongNumberOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/MethodSignatureWrongNumberOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void methodSignatureWrongTypesOk(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/methodSignatureWrongTypesOk.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void StaticMethodCantAcessField(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/StaticMethodCantAcessField.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void verifyMethodsOfClass(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/verifyMethodsOfClass.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void VerifyParamsInitArray(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/VerifyParamsInitArray.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void methodsClassAExtendsB(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/methodsClassAExtendsB.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ImportCallsMethod(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/ImportCallsMethod.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void AssignImportCallsMethod(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/AssignImportCallsMethod.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void WrongAssignFields(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongAssignFields.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void WrongAssignFields2(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongAssignFields2.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void WrongAssignFields3(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongAssignFields3.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void WrongAssignFields4(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongAssignFields4.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void WrongNumberParam(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongNumberParam.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void WrongTypeParam(){
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/extratest/WrongTypeParam.jmm"));
        TestUtils.mustFail(result);
    }

}
