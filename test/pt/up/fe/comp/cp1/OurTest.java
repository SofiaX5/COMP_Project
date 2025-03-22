package pt.up.fe.comp.cp1;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;

public class OurTest {
    private static final String IMPORT = "importDecl";
    private static final String MAIN_METHOD = "methodDecl";
    private static final String INSTANCE_METHOD = "methodDecl";
    private static final String STATEMENT = "stmt";
    private static final String EXPRESSION = "expr";

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


    // NÃO É PARA FAZER
    // int a = 3;
    // -1

}
