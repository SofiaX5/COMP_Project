package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class CheckTest {
    // 3.1.1
    @Test
    public void testUndefinedVariable() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_31.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void testDefinedLocalVariable() {
        TestUtils.noErrors(TestUtils.parse("class A {int foo(){int x; x = 5; return x;}}"));
    }

    @Test
    public void testUndefinedLocalVariable() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_32.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void testDefinedMethodParameter() {
        TestUtils.noErrors(TestUtils.parse("class A {int foo(int param){return param;}}"));
    }

    @Test
    public void testUndefinedImportedClass() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_33.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void testDefinedImportedClass() {
        TestUtils.noErrors(TestUtils.parse("import java.util.List; class A {List foo(){return null;}}"));
    }

    @Test
    public void testUndefinedMethodCall() {
        TestUtils.mustFail(TestUtils.parse("class A {int foo(){return bar();}}"));
    }

    @Test
    public void testDefinedMethodCall() {
        TestUtils.noErrors(TestUtils.parse("class A { int bar() { return 1; } int foo() { return this.bar(); } }"));
    }


    // 3.1.3
    @Test
    public void testArrayArithmeticOperation() {
        TestUtils.mustFail(TestUtils.parse("class A {int[] foo(){int[] a, b; return a + b;}}"));
    }

    @Test
    public void testArrayArithmeticWithScalar() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp1/ourtest/Test_34.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void testValidArrayAssignment() {
        TestUtils.noErrors(TestUtils.parse("class A {int[] foo(){int[] a; int[] b; a = b; return a;}}"));
    }
}
