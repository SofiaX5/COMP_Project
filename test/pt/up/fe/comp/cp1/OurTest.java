package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class OurTest {
    private static final String IMPORT = "importDecl";
    private static final String MAIN_METHOD = "methodDecl";
    private static final String INSTANCE_METHOD = "methodDecl";
    private static final String STATEMENT = "stmt";
    private static final String EXPRESSION = "expr";

    // Geral
    @Test
    public void testAttribution() {
        TestUtils.noErrors(TestUtils.parse("class A {int foo(){int a; a = 3; return 0;}}"));
    }

    @Test
    public void testInvalidInt() {
        TestUtils.mustFail(TestUtils.parse("class A{int foo(){return 05;}}"));
    }

    @Test
    public void testMainVariable() {
        TestUtils.noErrors(TestUtils.parse("class A {int main;}"));
    }

    @Test
    public void testStmtSemiColon() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_29.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void testStmtSemiColon2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_30.jmm"));
        TestUtils.mustFail(result);
    }


    // Return types
    @Test
    public void returnTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_1.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void returnTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void returnTest3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_14.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void returnTest4() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_15.jmm"));
         TestUtils.mustFail(result);
    }

    @Test
    public void returnTest5() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_16.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void returnTest6() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_17.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void returnTest7() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_18.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void returnTest8() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_22.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void returnTest9() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_23.jmm"));
        TestUtils.mustFail(result);
    }


    // void
    @Test
    public void voidTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_3.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void voidTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_4.jmm"));
        TestUtils.mustFail(result);
    }


    // If
    @Test
    public void ifTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_5.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_6.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ifTest3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_7.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest4() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_8.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest5() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_9.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ifTest6() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_10.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ifTest7() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_11.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest8() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_12.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest9() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_13.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void ifTest10() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_26.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void ifTest11() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_27.jmm"));
        TestUtils.mustFail(result);
    }


    // Array
    @Test
    public void arrayTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_19.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void arrayTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_20.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayTest3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_21.jmm"));
        TestUtils.noErrors(result);
    }


    // Import
    @Test
    public void importTest1() {
        TestUtils.noErrors(TestUtils.parse("import a; import a; class A {}"));
    }


    //This tests

    @Test
    public void thisTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/This_test1.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void thisTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/This_test2.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void thisTest3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/This_test3.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void thisTest4() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/This_test4.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void thisTest5() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_35.jmm"));
        System.out.println(result.getReports());
        TestUtils.mustFail(result);
    }



    // Assignments
    @Test
    public void assignTest1() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_24.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void assignTest2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_25.jmm"));
        TestUtils.noErrors(result);
    }

    @Test
    public void assignTest3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_28.jmm"));
        TestUtils.mustFail(result);
    }



    // NÃO É PARA FAZER
    // int a = 3;
    // -1
}
