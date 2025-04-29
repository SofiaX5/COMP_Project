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

public class OllirTest {

    static OllirResult getOllirResult(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/cp2/ollir/" + filename));
    }

    public void compileBasic(ClassUnit classUnit) {
        // Test name of the class and super
        assertEquals("Class name not what was expected", "CompileBasic", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());

        // Test method 1
        Method method1 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method1"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method1", method1);

        var retInst1 = method1.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method1", retInst1.isPresent());

        // Test method 2
        Method method2 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method2"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method2'", method2);

        var retInst2 = method2.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method2", retInst2.isPresent());
    }

    public void compileBasicWithFields(ClassUnit classUnit) {
        // Test name of the class and super
        assertEquals("Class name not what was expected", "CompileBasic", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());

        // Test fields
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        var fieldNames = new HashSet<>(Arrays.asList("intField", "boolField"));
        assertThat(fieldNames, hasItem(classUnit.getField(0).getFieldName()));
        assertThat(fieldNames, hasItem(classUnit.getField(1).getFieldName()));

        // Test method 1
        Method method1 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method1"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method1", method1);

        var retInst1 = method1.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method1", retInst1.isPresent());

        // Test method 2
        Method method2 = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals("method2"))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method2'", method2);

        var retInst2 = method2.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method2", retInst2.isPresent());
    }

    public void compileArithmetic(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileArithmetic", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var binOpInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(instr -> (AssignInstruction) instr)
                .filter(assign -> assign.getRhs() instanceof BinaryOpInstruction)
                .findFirst();

        assertTrue("Could not find a binary op instruction in method " + methodName, binOpInst.isPresent());

        var retInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method " + methodName, retInst.isPresent());
    }

    public void compileMethodInvocation(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileMethodInvocation", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var callInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof CallInstruction)
                .map(CallInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find a call instruction in method " + methodName, callInst.isPresent());

        assertEquals("Invocation type not what was expected", InvokeStaticInstruction.class,
                callInst.get().getClass());
    }

    public void compileAssignment(ClassUnit classUnit) {
        // Test name of the class
        assertEquals("Class name not what was expected", "CompileAssignment", classUnit.getClassName());

        // Test foo
        var methodName = "foo";
        Method methodFoo = classUnit.getMethods().stream()
                .filter(method -> method.getMethodName().equals(methodName))
                .findFirst()
                .orElse(null);

        assertNotNull("Could not find method " + methodName, methodFoo);

        var assignInst = methodFoo.getInstructions().stream()
                .filter(inst -> inst instanceof AssignInstruction)
                .map(AssignInstruction.class::cast)
                .findFirst();
        assertTrue("Could not find an assign instruction in method " + methodName, assignInst.isPresent());

        assertEquals("Assignment does not have the expected type", BuiltinKind.INT32, CpUtils.toBuiltinKind(assignInst.get().getTypeOfAssign()));
    }


    @Test
    public void section1_Basic_Class() {
        var result = getOllirResult("basic/BasicClass.jmm");

        compileBasic(result.getOllirClass());
    }

    @Test
    public void section1_Basic_Class_With_Fields() {
        var result = getOllirResult("basic/BasicClassWithFields.jmm");

        compileBasic(result.getOllirClass());
    }

    @Test
    public void section1_Basic_Assignment() {
        var result = getOllirResult("basic/BasicAssignment.jmm");

        compileAssignment(result.getOllirClass());
    }

    @Test
    public void section1_Basic_Method_Invocation() {
        var result = getOllirResult("basic/BasicMethodInvocation.jmm");

        compileMethodInvocation(result.getOllirClass());
    }


    /*checks if method declaration is correct (array)*/
    @Test
    public void section1_Basic_Method_Declaration_Array() {
        var result = getOllirResult("basic/BasicMethodsArray.jmm");

        var method = CpUtils.getMethod(result, "func4");

        CpUtils.assertEquals("Method return type", "int[]", CpUtils.toString(method.getReturnType()), result);
    }

    @Test
    public void section2_Arithmetic_Simple_add() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_add.jmm");

        compileArithmetic(ollirResult.getOllirClass());
    }

    @Test
    public void section2_Arithmetic_Simple_and() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_and.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.ANDB, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_less() {
        var ollirResult = getOllirResult("arithmetic/Arithmetic_less.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.LTH, method, ollirResult);

    }

    @Test
    public void section3_ControlFlow_If_Simple_Single_goto() {

        var result = getOllirResult("control_flow/SimpleIfElseStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 1, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 1 goto", gotos.size() >= 1, result);
    }

    @Test
    public void section3_ControlFlow_If_Switch() {

        var result = getOllirResult("control_flow/SwitchStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Number of branches", 6, branches.size(), result);

        var gotos = CpUtils.assertInstExists(GotoInstruction.class, method, result);
        CpUtils.assertTrue("Has at least 6 gotos", gotos.size() >= 6, result);
    }

    @Test
    public void section3_ControlFlow_While_Simple() {

        var result = getOllirResult("control_flow/SimpleWhileStat.jmm");

        var method = CpUtils.getMethod(result, "func");

        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);

        CpUtils.assertTrue("Number of branches between 1 and 2", branches.size() > 0 && branches.size() < 3, result);
    }


    /*checks if an array is correctly initialized*/
    @Test
    public void section4_Arrays_Init_Array() {
        var result = getOllirResult("arrays/ArrayInit.jmm");

        var method = CpUtils.getMethod(result, "main");

        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);

        CpUtils.assertEquals("Number of calls", 3, calls.size(), result);

        // Get new
        var newCalls = calls.stream().filter(call -> call instanceof NewInstruction)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of 'new' calls", 1, newCalls.size(), result);

        // Get length
        var lengthCalls = calls.stream().filter(call -> call instanceof ArrayLengthInstruction)
                .collect(Collectors.toList());

        CpUtils.assertEquals("Number of 'arraylenght' calls", 1, lengthCalls.size(), result);
    }

    /*checks if the access to the elements of array is correct*/
    @Test
    public void section4_Arrays_Access_Array() {
        var result = getOllirResult("arrays/ArrayAccess.jmm");

        var method = CpUtils.getMethod(result, "foo");

        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array stores", 5, numArrayStores, result);

        var numArrayReads = assigns.stream()
                .flatMap(assign -> CpUtils.getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array reads", 5, numArrayReads, result);
    }

    /*checks multiple expressions as indexes to access the elements of an array*/
    @Test
    public void section4_Arrays_Load_ComplexArrayAccess() {
        // Just parse
        var result = getOllirResult("arrays/ComplexArrayAccess.jmm");

        System.out.println("---------------------- OLLIR ----------------------");
        System.out.println(result.getOllirCode());
        System.out.println("---------------------- OLLIR ----------------------");

        var method = CpUtils.getMethod(result, "main");

        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array stores", 5, numArrayStores, result);

        var numArrayReads = assigns.stream()
                .flatMap(assign -> CpUtils.getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand).count();
        CpUtils.assertEquals("Number of array reads", 6, numArrayReads, result);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static OllirResult getOllirResult2(String filename) {
        return TestUtils.optimize(SpecsIo.getResource("pt/up/fe/comp/cp2/ollir_ours/" + filename));
    }

    @Test
    public void section2_Arithmetic_Simple_add2() {
        var ollirResult = getOllirResult2("Arithmetic_add.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.ADD, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_and2() {
        var ollirResult = getOllirResult2("Arithmetic_and.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.ANDB, method, ollirResult);
    }
    @Test
    public void section2_Arithmetic_Simple_div() {
        var ollirResult = getOllirResult2("Arithmetic_div.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.DIV, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_mul() {
        var ollirResult = getOllirResult2("Arithmetic_mul.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.MUL, method, ollirResult);
    }

    @Test
    public void section2_Arithmetic_Simple_sub() {
        var ollirResult = getOllirResult2("Arithmetic_sub.jmm");

        var method = CpUtils.getMethod(ollirResult, "main");

        CpUtils.assertHasOperation(OperationType.SUB, method, ollirResult);
    }

    @Test
    public void section1_Basic_Fields_Declaration() {
        var result = getOllirResult2("BasicFields.jmm");

        // Test name of the class and super
        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicFields", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Other", classUnit.getSuperClass());

        // Test fields
        assertEquals("Class should have four fields", 4, classUnit.getNumFields());
        var fieldNames = new HashSet<>(Arrays.asList("a", "b", "p", "c"));
        var fieldTypes = new HashSet<>(Arrays.asList("int", "bool", "BasicFields", "int[]"));

        for (int i = 0; i < classUnit.getNumFields(); i++) {
            assertThat("Field name should be in expected set",
                    fieldNames, hasItem(classUnit.getField(i).getFieldName()));

            // ????????????????????????????????????????
            String fieldType = CpUtils.toString(classUnit.getField(i).getFieldType());
            assertThat("Field type should be in expected set",
                    fieldTypes, hasItem(fieldType));
        }

        // Test methods existence
        var methodNames = Arrays.asList("func1", "func2", "func3", "func4", "main");
        for (String methodName : methodNames) {
            Method method = classUnit.getMethods().stream()
                    .filter(m -> m.getMethodName().equals(methodName))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, method);
        }
    }

    @Test
    public void section1_Basic_Fields_Method_Int() {
        var result = getOllirResult2("BasicFields.jmm");

        var method = CpUtils.getMethod(result, "func1");

        // Check return type
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(method.getReturnType()), result);

        // Check return instruction exists
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func1", retInst.isPresent());
    }

    @Test
    public void section1_Basic_Fields_Method_Array() {
        var result = getOllirResult2("BasicFields.jmm");

        var method = CpUtils.getMethod(result, "func4");

        // Check return type
        CpUtils.assertEquals("Method return type", "int[]", CpUtils.toString(method.getReturnType()), result);

        // Check for array initialization
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one new instruction", newInsts.size() >= 1, result);
    }

    @Test
    public void section1_Basic_Methods_Bool() {
        var result = getOllirResult2("BasicMethodsBool.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicMethods", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Other", classUnit.getSuperClass());

        var method = CpUtils.getMethod(result, "func2");

        // Check return type
        // ????????????????????????????????????????
        CpUtils.assertEquals("Method return type", "bool", CpUtils.toString(method.getReturnType()), result);

        // Check return instruction exists
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func2", retInst.isPresent());
    }

    @Test
    public void section1_Basic_Methods_Class() {
        var result = getOllirResult2("BasicMethodsClass.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicMethods", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Other", classUnit.getSuperClass());

        var method = CpUtils.getMethod(result, "func3");

        // Check return type
        CpUtils.assertEquals("Method return type", "BasicMethods", CpUtils.toString(method.getReturnType()), result);

        // Check for class instantiation
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one new instruction", newInsts.size() >= 1, result);

        // Check return instruction exists
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func3", retInst.isPresent());
    }

    @Test
    public void section1_Basic_Methods_Int() {
        var result = getOllirResult2("BasicMethodsInt.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicMethods", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Other", classUnit.getSuperClass());

        var method = CpUtils.getMethod(result, "func1");

        // Check return type
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(method.getReturnType()), result);

        // Check return instruction exists
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func1", retInst.isPresent());
    }

    // ByteCodeIndexes Tests
    @Test
    public void section1_ByteCodeIndexes1() {
        var result = getOllirResult2("ByteCodeIndexes1.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ByteCodeIndexes1", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "func");

        // Check that method exists and has correct parameters
        assertNotNull("Could not find method func", method);
        CpUtils.assertEquals("Method parameter count", 1, method.getParams().size(), result);
        CpUtils.assertEquals("Method parameter type", "int", CpUtils.toString(method.getParams().get(0).getType()), result);

        // Check return type
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(method.getReturnType()), result);

        // Check there's a return instruction
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func", retInst.isPresent());
    }

    @Test
    public void section1_ByteCodeIndexes2() {
        var result = getOllirResult2("ByteCodeIndexes2.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ByteCodeIndexes2", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "func");

        // Check that method exists and has correct parameters
        assertNotNull("Could not find method func", method);
        CpUtils.assertEquals("Method parameter count", 1, method.getParams().size(), result);

        // Check for variable declaration and assignment
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one assignment instruction", assigns.size() >= 1, result);

        // Check there's a return instruction
        var retInst = method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func", retInst.isPresent());
    }

    // Complex Arithmetic Tests
    @Test
    public void section2_ComplexAdd() {
        var result = getOllirResult2("ComplexAdd.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexAdd", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for addition operations
        CpUtils.assertHasOperation(OperationType.ADD, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexAddDiv() {
        var result = getOllirResult2("ComplexAddDiv.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexAddDiv", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for addition and division operations
        CpUtils.assertHasOperation(OperationType.ADD, method, result);
        CpUtils.assertHasOperation(OperationType.DIV, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexAddMul() {
        var result = getOllirResult2("ComplexAddMul.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexAddMul", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for addition and multiplication operations
        CpUtils.assertHasOperation(OperationType.ADD, method, result);
        CpUtils.assertHasOperation(OperationType.MUL, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexAddSub() {
        var result = getOllirResult2("ComplexAddSub.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexAddSub", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for addition and subtraction operations
        CpUtils.assertHasOperation(OperationType.ADD, method, result);
        CpUtils.assertHasOperation(OperationType.SUB, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexArithmetic() {
        var result = getOllirResult2("ComplexArithmetic.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexArithmetic", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for all arithmetic operations
        CpUtils.assertHasOperation(OperationType.ADD, method, result);
        CpUtils.assertHasOperation(OperationType.SUB, method, result);
        CpUtils.assertHasOperation(OperationType.MUL, method, result);
        CpUtils.assertHasOperation(OperationType.DIV, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexDiv() {
        var result = getOllirResult2("ComplexDiv.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexDiv", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for division operations
        CpUtils.assertHasOperation(OperationType.DIV, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexDivMul() {
        var result = getOllirResult2("ComplexDivMul.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexDivMul", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for division and multiplication operations
        CpUtils.assertHasOperation(OperationType.DIV, method, result);
        CpUtils.assertHasOperation(OperationType.MUL, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexMulDiv() {
        var result = getOllirResult2("ComplexMulDiv.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexMulDiv", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for division and multiplication operations
        CpUtils.assertHasOperation(OperationType.DIV, method, result);
        CpUtils.assertHasOperation(OperationType.MUL, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexProd() {
        var result = getOllirResult2("ComplexProd.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexProd", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for multiplication operations
        CpUtils.assertHasOperation(OperationType.MUL, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section3_ComplexStat() {
        var result = getOllirResult2("ComplexStat.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexStat", classUnit.getClassName());

        // Check main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check object instantiation
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least one new instruction", newInsts.size() >= 1, result);

        // Check func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);

        // Check for conditional branches in func method (if/while)
        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, funcMethod, result);
        CpUtils.assertTrue("Should have multiple conditional branches", branches.size() > 2, result);

        // Check for method calls in func method
        var calls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertTrue("Should have multiple call instructions", calls.size() >= 4, result);

        // Check there's a return instruction
        var retInst = funcMethod.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func", retInst.isPresent());
    }

    @Test
    public void section2_ComplexSub() {
        var result = getOllirResult2("ComplexSub.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexSub", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for subtraction operations
        CpUtils.assertHasOperation(OperationType.SUB, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexSubDiv() {
        var result = getOllirResult2("ComplexSubDiv.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexSubDiv", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for subtraction and division operations
        CpUtils.assertHasOperation(OperationType.SUB, method, result);
        CpUtils.assertHasOperation(OperationType.DIV, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section2_ComplexSubMul() {
        var result = getOllirResult2("ComplexSubMul.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexSubMul", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for subtraction and multiplication operations
        CpUtils.assertHasOperation(OperationType.SUB, method, result);
        CpUtils.assertHasOperation(OperationType.MUL, method, result);

        // Check for method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);
    }

    @Test
    public void section4_FindMaximum() {
        var result = getOllirResult2("FindMaximum.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "FindMaximum", classUnit.getClassName());

        // Test field declaration
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        assertEquals("Field name not what was expected", "test_arr", classUnit.getField(0).getFieldName());
        assertEquals("Field type not what was expected", "int[]", CpUtils.toString(classUnit.getField(0).getFieldType()));

        // Test find_maximum method
        var findMaxMethod = CpUtils.getMethod(result, "find_maximum");
        assertNotNull("Could not find method find_maximum", findMaxMethod);

        // Check parameter type
        CpUtils.assertEquals("Method parameter count", 1, findMaxMethod.getParams().size(), result);
        CpUtils.assertEquals("Method parameter type", "int[]", CpUtils.toString(findMaxMethod.getParams().get(0).getType()), result);

        // Check for array access
        var arrayAccesses = findMaxMethod.getInstructions().stream()
                .flatMap(inst -> CpUtils.getElements(inst).stream())
                .filter(element -> element instanceof ArrayOperand)
                .count();
        CpUtils.assertTrue("Should have array access operations", arrayAccesses > 0, result);

        // Check for conditionals (if and while)
        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, findMaxMethod, result);
        CpUtils.assertTrue("Should have conditional branches", branches.size() >= 2, result);

        // Check build_test_arr method
        var buildArrMethod = CpUtils.getMethod(result, "build_test_arr");
        assertNotNull("Could not find method build_test_arr", buildArrMethod);

        // Check for array initialization
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, buildArrMethod, result);
        CpUtils.assertTrue("Should have new array instruction", newInsts.size() >= 1, result);

        // Check for array assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, buildArrMethod, result);
        var arrayStores = assigns.stream()
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();
        CpUtils.assertTrue("Should have array store operations", arrayStores >= 5, result);

        // Test get_array method
        var getArrayMethod = CpUtils.getMethod(result, "get_array");
        assertNotNull("Could not find method get_array", getArrayMethod);
        CpUtils.assertEquals("Method return type", "int[]", CpUtils.toString(getArrayMethod.getReturnType()), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiation
        var mainNewInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have object instantiation", mainNewInsts.size() >= 1, result);

        // Check for method calls
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have method calls", calls.size() >= 3, result);
    }

    @Test
    public void section3_FlowControl_if_icmpge() {
        var result = getOllirResult2("FlowControl_if_icmpge.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "FlowControl_if_icmpge", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for conditional branch (if)
        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Should have exactly one conditional branch", 1, branches.size(), result);

        // Check for less than operation
        CpUtils.assertHasOperation(OperationType.LTH, method, result);
    }

    @Test
    public void section3_If_lt() {
        var result = getOllirResult2("if_lt.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "If_lt", classUnit.getClassName());

        var method = CpUtils.getMethod(result, "main");

        // Check for variable declaration and initialization
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, result);
        CpUtils.assertTrue("Should have multiple assignments", assigns.size() >= 3, result);

        // Check for conditional branch (if)
        var branches = CpUtils.assertInstExists(CondBranchInstruction.class, method, result);
        CpUtils.assertEquals("Should have exactly one conditional branch", 1, branches.size(), result);

        // Check for less than operation
        CpUtils.assertHasOperation(OperationType.LTH, method, result);
    }

    @Test
    public void section1_ImportsComplex() {
        var result = getOllirResult2("ImportsComplex.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicImportsPackages", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 3 imports", imports.size() == 3, result);

        // Check that specific imports exist
        CpUtils.assertTrue("Should import foo.bar.A", imports.contains("foo.bar.A"), result);
        CpUtils.assertTrue("Should import pt.up.fe.comp.Comp", imports.contains("pt.up.fe.comp.Comp"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        var method = CpUtils.getMethod(result, "main");

        // Check for method invocations
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least 4 method calls", calls.size() >= 4, result);

        // Verify static method calls
        boolean hasStaticCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("Should have static method calls", hasStaticCall);
    }

    @Test
    public void section1_ImportsSimilar() {
        var result = getOllirResult2("ImportsSimilar.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicImportsPackages", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 3 imports", imports.size() == 3, result);

        // Check that specific imports exist
        CpUtils.assertTrue("Should import foo.bar.A", imports.contains("foo.bar.A"), result);
        CpUtils.assertTrue("Should import foo.bar.B", imports.contains("foo.bar.B"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        var method = CpUtils.getMethod(result, "main");

        // Check for method invocations
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertTrue("Should have at least 4 method calls", calls.size() >= 4, result);

        // Verify static method calls
        boolean hasStaticCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("Should have static method calls", hasStaticCall);
    }

    @Test
    public void section1_ImportsSimple() {
        var result = getOllirResult2("ImportsSimple.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "BasicImports", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);

        // Check that specific imports exist
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        var method = CpUtils.getMethod(result, "main");

        // Check for method invocations
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, result);
        CpUtils.assertEquals("Should have exactly 2 method calls", 2, calls.size(), result);

        // Verify static method calls
        boolean hasStaticCall = calls.stream()
                .allMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("All calls should be static method calls", hasStaticCall);
    }

    /*
    @Test
    public void section1_InvokeDiscardResult() {
        var result = getOllirResult2("InvokeDiscardResult.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "Method_Call_Discard_Result", classUnit.getClassName());

        // Test foo method
        var fooMethod = CpUtils.getMethod(result, "foo");
        assertNotNull("Could not find method foo", fooMethod);
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(fooMethod.getReturnType()), result);

        // Test bar method
        var barMethod = CpUtils.getMethod(result, "bar");
        assertNotNull("Could not find method bar", barMethod);

        // Check for method invocation of foo inside bar
        var calls = CpUtils.assertInstExists(CallInstruction.class, barMethod, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);

        // Check that there is at least one instance where the result of a method call is discarded
        boolean hasDiscardedResult = calls.stream()
                .anyMatch(call -> !(call.getParent() instanceof AssignInstruction));
        assertTrue("Should have a method call where result is discarded", hasDiscardedResult);

        // Check return instruction exists
        var retInst = barMethod.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method bar", retInst.isPresent());
    }*/

    @Test
    public void section1_InvokeImportStaticAndInstance() {
        var result = getOllirResult2("InvokeImportStaticAndInstance.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "InvokeImportMixed", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import Auxi", imports.contains("Auxi"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiation in main
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least two new instructions", newInsts.size() >= 2, result);

        // Test foo method
        var fooMethod = CpUtils.getMethod(result, "foo");
        assertNotNull("Could not find method foo", fooMethod);
        CpUtils.assertEquals("Method parameter count", 1, fooMethod.getParams().size(), result);
        CpUtils.assertEquals("Method parameter type", "Auxi", CpUtils.toString(fooMethod.getParams().get(0).getType()), result);

        // Check for method calls in foo method
        var calls = CpUtils.assertInstExists(CallInstruction.class, fooMethod, result);
        CpUtils.assertTrue("Should have at least three call instructions", calls.size() >= 3, result);

        // Check that there's at least one static method call
        boolean hasStaticCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("Should have static method call", hasStaticCall);

        // Check that there's at least one virtual method call
        boolean hasVirtualCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeVirtualInstruction);
        assertTrue("Should have virtual method call", hasVirtualCall);
    }

    @Test
    public void section1_InvokeStatic() {
        var result = getOllirResult2("InvokeStatic.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "InvokeStatic", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(funcMethod.getReturnType()), result);

        // Check for static method call
        var calls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertEquals("Should have exactly one call instruction", 1, calls.size(), result);

        // Verify it's a static method call
        boolean isStaticCall = calls.get(0) instanceof InvokeStaticInstruction;
        assertTrue("Should be a static method call", isStaticCall);

        // Check return instruction exists
        var retInst = funcMethod.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func", retInst.isPresent());
    }

    /*
    @Test
    public void section1_InvokeThisAndImportSameMethodDiffReturn() {
        var result = getOllirResult2("InvokeThisAndImportSameMethodDiffReturn.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "InvokeThisAndImportSame", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import Auxi", imports.contains("Auxi"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Test foo method
        var fooMethod = CpUtils.getMethod(result, "foo");
        assertNotNull("Could not find method foo", fooMethod);

        // Check for method calls in foo method
        var calls = CpUtils.assertInstExists(CallInstruction.class, fooMethod, result);
        CpUtils.assertTrue("Should have at least four call instructions", calls.size() >= 4, result);

        // Check that there are both virtual and this method calls
        boolean hasVirtualCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeVirtualInstruction &&
                        !((InvokeVirtualInstruction)call).getInvocationType().toString().equals("invokevirtual$this"));
        boolean hasThisCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeVirtualInstruction &&
                        ((InvokeVirtualInstruction)call).getInvocationType().toString().equals("invokevirtual$this"));

        assertTrue("Should have virtual method call", hasVirtualCall);
        assertTrue("Should have 'this' method call", hasThisCall);

        // Test instanceCallBool method
        var instanceCallMethod = CpUtils.getMethod(result, "instanceCallBool");
        assertNotNull("Could not find method instanceCallBool", instanceCallMethod);
        CpUtils.assertEquals("Method return type", "int", CpUtils.toString(instanceCallMethod.getReturnType()), result);
    }

    @Test
    public void section1_LocalLimits() {
        var result = getOllirResult2("LocalLimits.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "LocalLimits", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import dummy", imports.contains("dummy"), result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 2, funcMethod.getParams().size(), result);

        // Check for complex arithmetic operations
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, funcMethod, result);
        CpUtils.assertTrue("Should have at least two assignment instructions", assigns.size() >= 2, result);

        // Check for method calls to dummy.add
        var calls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertTrue("Should have at least two call instructions", calls.size() >= 2, result);

        // Verify they're static method calls
        boolean allStaticCalls = calls.stream().allMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("All calls should be static method calls", allStaticCalls);

        // Check for arithmetic operations
        CpUtils.assertHasOperation(OperationType.ADD, funcMethod, result);
        CpUtils.assertHasOperation(OperationType.MUL, funcMethod, result);
    }*/

    @Test
    public void section1_MiscArithmeticArgsFuncCall() {
        var result = getOllirResult2("MiscArithmeticArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ArithmeticArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for method calls with arithmetic operations in arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 3, funcMethod.getParams().size(), result);

        // Check for static method calls in func
        var funcCalls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertEquals("Should have exactly three call instructions", 3, funcCalls.size(), result);

        boolean allStaticCalls = funcCalls.stream().allMatch(call -> call instanceof InvokeStaticInstruction);
        assertTrue("All calls in func should be static method calls", allStaticCalls);
    }

    @Test
    public void section1_MiscComplexArgsFuncCall() {
        var result = getOllirResult2("MiscComplexArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ComplexArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiations in main
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least two new instructions", newInsts.size() >= 2, result);

        // Check for complex method calls with nested method call arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least three call instructions", calls.size() >= 3, result);

        // Test func, f1, and f2 methods
        assertNotNull("Could not find method func", CpUtils.getMethod(result, "func"));
        assertNotNull("Could not find method f1", CpUtils.getMethod(result, "f1"));
        assertNotNull("Could not find method f2", CpUtils.getMethod(result, "f2"));
    }

    @Test
    public void section1_MiscConditionArgsFuncCall() {
        var result = getOllirResult2("MiscConditionArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ConditionArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for method call with conditional arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);

        // Check for boolean operations
        CpUtils.assertHasOperation(OperationType.LTH, mainMethod, result);
        CpUtils.assertHasOperation(OperationType.ANDB, mainMethod, result);
        CpUtils.assertHasOperation(OperationType.NOTB, mainMethod, result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 4, funcMethod.getParams().size(), result);

        // ????????????????????????????????????????
        for (int i = 0; i < 4; i++) {
            CpUtils.assertEquals("Method parameter type should be boolean",
                    "bool", CpUtils.toString(funcMethod.getParams().get(i).getType()), result);
        }
    }

    @Test
    public void section1_MiscConstArgsFuncCall() {
        var result = getOllirResult2("MiscConstArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "ConstArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for method call with constant arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 3, funcMethod.getParams().size(), result);

        // Check for static method calls in func
        var funcCalls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertEquals("Should have exactly three call instructions", 3, funcCalls.size(), result);
    }

    @Test
    public void section1_MiscFuncArgsFuncCall() {
        var result = getOllirResult2("MiscFuncArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "FuncArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test methods existence
        assertNotNull("Could not find method main", CpUtils.getMethod(result, "main"));
        assertNotNull("Could not find method f1", CpUtils.getMethod(result, "f1"));
        assertNotNull("Could not find method f2", CpUtils.getMethod(result, "f2"));
        assertNotNull("Could not find method func", CpUtils.getMethod(result, "func"));

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");

        // Check for method calls with function call results as arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least three call instructions", calls.size() >= 3, result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        CpUtils.assertEquals("Method parameter count", 2, funcMethod.getParams().size(), result);
        CpUtils.assertEquals("First parameter type", "int", CpUtils.toString(funcMethod.getParams().get(0).getType()), result);
        // ????????????????????????????????????????
        CpUtils.assertEquals("Second parameter type", "bool", CpUtils.toString(funcMethod.getParams().get(1).getType()), result);
    }

    @Test
    public void section1_MiscOneArgFuncCall() {
        var result = getOllirResult2("MiscOneArgFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "OneArgFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for method calls
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertEquals("Should have exactly two call instructions", 2, calls.size(), result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 1, funcMethod.getParams().size(), result);
        CpUtils.assertEquals("Parameter type", "int", CpUtils.toString(funcMethod.getParams().get(0).getType()), result);

        // Check return instruction exists
        var retInst = funcMethod.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func", retInst.isPresent());
    }

    @Test
    public void section1_MiscVarArgsFuncCall() {
        var result = getOllirResult2("MiscVarArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "VarArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import ioPlus", imports.contains("ioPlus"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for method calls with variable arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least one call instruction", calls.size() >= 1, result);

        // Test func method
        var funcMethod = CpUtils.getMethod(result, "func");
        assertNotNull("Could not find method func", funcMethod);
        CpUtils.assertEquals("Method parameter count", 3, funcMethod.getParams().size(), result);

        // Check for static method calls in func
        var funcCalls = CpUtils.assertInstExists(CallInstruction.class, funcMethod, result);
        CpUtils.assertEquals("Should have exactly three call instructions", 3, funcCalls.size(), result);
    }

    @Test
    public void section1_NoArgsFuncCall() {
        var result = getOllirResult2("NoArgsFuncCall.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "NoArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 1 import", imports.size() == 1, result);
        CpUtils.assertTrue("Should import Dummy", imports.contains("Dummy"), result);

        // Test bar method
        var barMethod = CpUtils.getMethod(result, "bar");
        assertNotNull("Could not find method bar", barMethod);
        CpUtils.assertEquals("Method parameter count", 1, barMethod.getParams().size(), result);
        CpUtils.assertEquals("Parameter type", "Dummy", CpUtils.toString(barMethod.getParams().get(0).getType()), result);

        // Check for method call with no arguments
        var calls = CpUtils.assertInstExists(CallInstruction.class, barMethod, result);
        CpUtils.assertEquals("Should have exactly one call instruction", 1, calls.size(), result);

        // Verify it's a virtual method call with no arguments
        boolean isVirtualCall = calls.get(0) instanceof InvokeVirtualInstruction;
        assertTrue("Should be a virtual method call", isVirtualCall);
        CpUtils.assertEquals("Call should have no arguments", 0, ((CallInstruction)calls.get(0)).getArguments().size(), result);
    }

    @Test
    public void section1_NoArgsFuncCallWithAssign() {
        var result = getOllirResult2("NoArgsFuncCallWithAssign.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "NoArgsFuncCall", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import GetterAndSetter", imports.contains("GetterAndSetter"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiations
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertEquals("Should have exactly two new instructions", 2, newInsts.size(), result);

        // Test bar method
        var barMethod = CpUtils.getMethod(result, "bar");
        assertNotNull("Could not find method bar", barMethod);

        // Check for method call with no arguments and result assignment
        var calls = CpUtils.assertInstExists(CallInstruction.class, barMethod, result);
        CpUtils.assertEquals("Should have exactly one call instruction", 1, calls.size(), result);

        // Verify it's a virtual method call
        boolean isVirtualCall = calls.get(0) instanceof InvokeVirtualInstruction;
        assertTrue("Should be a virtual method call", isVirtualCall);

        // Check that the result is assigned to a variable
        boolean resultAssigned = barMethod.getInstructions().stream()
                .anyMatch(inst -> inst instanceof AssignInstruction &&
                        ((AssignInstruction)inst).getRhs() instanceof CallInstruction);
        assertTrue("Method call result should be assigned to a variable", resultAssigned);
    }

    @Test
    public void section1_PrintOtherClassFromParam() {
        var result = getOllirResult2("PrintOtherClassFromParam.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "PrintOtherClassFromParam", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import GetterAndSetter", imports.contains("GetterAndSetter"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test print method
        var printMethod = CpUtils.getMethod(result, "print");
        assertNotNull("Could not find method print", printMethod);
        CpUtils.assertEquals("Method parameter count", 1, printMethod.getParams().size(), result);
        CpUtils.assertEquals("Parameter type", "GetterAndSetter", CpUtils.toString(printMethod.getParams().get(0).getType()), result);

        // Check for method calls in print method
        var printCalls = CpUtils.assertInstExists(CallInstruction.class, printMethod, result);
        CpUtils.assertEquals("Should have exactly two call instructions", 2, printCalls.size(), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiations in main
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertEquals("Should have exactly two new instructions", 2, newInsts.size(), result);

        // Check for method calls in main
        var mainCalls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertTrue("Should have at least two call instructions", mainCalls.size() >= 2, result);
    }

    @Test
    public void section1_PrintOtherClassInline() {
        var result = getOllirResult2("PrintOtherClassInline.jmm");

        ClassUnit classUnit = result.getOllirClass();
        assertEquals("Class name not what was expected", "PrintOtherClassInline", classUnit.getClassName());

        // Check that the imports exist
        var imports = classUnit.getImports();
        CpUtils.assertTrue("Should have 2 imports", imports.size() == 2, result);
        CpUtils.assertTrue("Should import GetterAndSetter", imports.contains("GetterAndSetter"), result);
        CpUtils.assertTrue("Should import io", imports.contains("io"), result);

        // Test main method
        var mainMethod = CpUtils.getMethod(result, "main");
        assertNotNull("Could not find method main", mainMethod);

        // Check for object instantiation
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, result);
        CpUtils.assertEquals("Should have exactly one new instruction", 1, newInsts.size(), result);

        // Check for method calls
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, result);
        CpUtils.assertEquals("Should have exactly three call instructions", 3, calls.size(), result);

        // Verify there are both virtual and static method calls
        boolean hasVirtualCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeVirtualInstruction);
        boolean hasStaticCall = calls.stream()
                .anyMatch(call -> call instanceof InvokeStaticInstruction);

        assertTrue("Should have virtual method calls", hasVirtualCall);
        assertTrue("Should have static method call", hasStaticCall);
    }


    @Test
    public void test_SimpleAdd() {
        var ollirResult = getOllirResult2("SimpleAdd.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "SimpleAdd", classUnit.getClassName());

        // Test method exists
        var method = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", method);

        // Test for addition operation
        CpUtils.assertHasOperation(OperationType.ADD, method, ollirResult);

        // Check for variable assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, ollirResult);
        assertTrue("Should have at least 3 assignments", assigns.size() >= 3);

        // Check for method call to printResult
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, ollirResult);
        assertTrue("Should have at least one method call", calls.size() >= 1);
    }

    @Test
    public void test_SimpleDiv() {
        var ollirResult = getOllirResult2("SimpleDiv.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "SimpleDiv", classUnit.getClassName());

        // Test method exists
        var method = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", method);

        // Test for division operation
        CpUtils.assertHasOperation(OperationType.DIV, method, ollirResult);

        // Check for variable assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, ollirResult);
        assertTrue("Should have at least 3 assignments", assigns.size() >= 3);

        // Check for method call to printResult
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, ollirResult);
        assertTrue("Should have at least one method call", calls.size() >= 1);
    }

    @Test
    public void test_SimpleProd() {
        var ollirResult = getOllirResult2("SimpleProd.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "SimpleProd", classUnit.getClassName());

        // Test method exists
        var method = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", method);

        // Test for multiplication operation
        CpUtils.assertHasOperation(OperationType.MUL, method, ollirResult);

        // Check for variable assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, ollirResult);
        assertTrue("Should have at least 3 assignments", assigns.size() >= 3);

        // Check for method call to printResult
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, ollirResult);
        assertTrue("Should have at least one method call", calls.size() >= 1);
    }

    @Test
    public void test_SimpleSub() {
        var ollirResult = getOllirResult2("SimpleSub.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "SimpleSub", classUnit.getClassName());

        // Test method exists
        var method = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", method);

        // Test for subtraction operation
        CpUtils.assertHasOperation(OperationType.SUB, method, ollirResult);

        // Check for variable assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, method, ollirResult);
        assertTrue("Should have at least 3 assignments", assigns.size() >= 3);

        // Check for method call to printResult
        var calls = CpUtils.assertInstExists(CallInstruction.class, method, ollirResult);
        assertTrue("Should have at least one method call", calls.size() >= 1);
    }

    @Test
    public void test_Structure_class() {
        var ollirResult = getOllirResult2("Structure_class.jmm");

        // Test class name and super class
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "Structure_class", classUnit.getClassName());
        assertEquals("Super class name not what was expected", "Super_class", classUnit.getSuperClass());

        // Test main method exists
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", mainMethod);
    }

    @Test
    public void test_Structure_fields() {
        var ollirResult = getOllirResult2("Structure_fields.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "Structure_fields", classUnit.getClassName());

        // Test fields
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        var fieldNames = new HashSet<>(Arrays.asList("a", "b"));
        assertThat(fieldNames, hasItem(classUnit.getField(0).getFieldName()));
        assertThat(fieldNames, hasItem(classUnit.getField(1).getFieldName()));

        // Test methods existence
        var methodNames = Arrays.asList("main", "setA", "setB", "a", "b");
        for (String methodName : methodNames) {
            Method method = classUnit.getMethods().stream()
                    .filter(m -> m.getMethodName().equals(methodName))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, method);
        }

        // Test for method calls
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, ollirResult);
        assertTrue("Should have at least 4 method calls", calls.size() >= 4);

        // Test for new instruction
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, ollirResult);
        assertEquals("Should have one new instruction", 1, newInsts.size());
    }

    @Test
    public void test_Structure_fields_bool() {
        var ollirResult = getOllirResult2("Structure_fields_bool.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "Structure_fields", classUnit.getClassName());

        // Test fields
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        assertEquals("Field name not what was expected", "a", classUnit.getField(0).getFieldName());
        assertEquals("Field type not what was expected", BuiltinKind.BOOLEAN,
                CpUtils.toBuiltinKind(classUnit.getField(0).getFieldType()));

        // Test main method exists
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", mainMethod);
    }

    @Test
    public void test_Structure_fields_int() {
        var ollirResult = getOllirResult2("Structure_fields_int.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "Structure_fields", classUnit.getClassName());

        // Test fields
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        assertEquals("Field name not what was expected", "a", classUnit.getField(0).getFieldName());
        assertEquals("Field type not what was expected", BuiltinKind.INT32,
                CpUtils.toBuiltinKind(classUnit.getField(0).getFieldType()));

        // Test main method exists
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", mainMethod);
    }

    @Test
    public void test_Structure_fields_intarray() {
        var ollirResult = getOllirResult2("Structure_fields_intarray.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "Structure_fields", classUnit.getClassName());

        // Test fields
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        assertEquals("Field name not what was expected", "a", classUnit.getField(0).getFieldName());

        // Test field type is array
        String fieldType = CpUtils.toString(classUnit.getField(0).getFieldType());
        assertEquals("Field type should be int array", "int[]", fieldType);

        // Test main method exists
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        assertNotNull("Could not find main method", mainMethod);
    }

    @Test
    public void test_UsesPop() {
        var ollirResult = getOllirResult2("UsesPop.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "UsesPop", classUnit.getClassName());

        // Test methods existence
        var methodNames = Arrays.asList("func5", "func1");
        for (String methodName : methodNames) {
            Method method = classUnit.getMethods().stream()
                    .filter(m -> m.getMethodName().equals(methodName))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, method);
        }

        // Test for method call in func5
        var func5Method = CpUtils.getMethod(ollirResult, "func5");
        var calls = CpUtils.assertInstExists(CallInstruction.class, func5Method, ollirResult);
        assertEquals("Should have one method call", 1, calls.size());

        // Test for return instructions
        var retInst1 = func5Method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func5", retInst1.isPresent());

        var func1Method = CpUtils.getMethod(ollirResult, "func1");
        var retInst2 = func1Method.getInstructions().stream()
                .filter(inst -> inst instanceof ReturnInstruction)
                .findFirst();
        assertTrue("Could not find a return instruction in method func1", retInst2.isPresent());
    }

    @Test
    public void test_VarArgsFuncCall() {
        var ollirResult = getOllirResult2("VarArgsFuncCall.jmm");

        // Test class name
        ClassUnit classUnit = ollirResult.getOllirClass();
        assertEquals("Class name not what was expected", "VarArgsFuncCall", classUnit.getClassName());

        // Test methods existence
        var methodNames = Arrays.asList("main", "func");
        for (String methodName : methodNames) {
            Method method = classUnit.getMethods().stream()
                    .filter(m -> m.getMethodName().equals(methodName))
                    .findFirst()
                    .orElse(null);

            assertNotNull("Could not find method " + methodName, method);
        }

        // Test func method parameters
        var funcMethod = CpUtils.getMethod(ollirResult, "func");
        assertEquals("Method func should have 3 parameters", 3, funcMethod.getParams().size());

        // Test for method calls in main
        var mainMethod = CpUtils.getMethod(ollirResult, "main");
        var calls = CpUtils.assertInstExists(CallInstruction.class, mainMethod, ollirResult);
        assertTrue("Should have at least 2 method calls", calls.size() >= 2);

        // Test for new instruction
        var newInsts = CpUtils.assertInstExists(NewInstruction.class, mainMethod, ollirResult);
        assertEquals("Should have one new instruction", 1, newInsts.size());

        // Test for variable assignments
        var assigns = CpUtils.assertInstExists(AssignInstruction.class, mainMethod, ollirResult);
        assertTrue("Should have at least 4 assignments", assigns.size() >= 4);
    }

}
