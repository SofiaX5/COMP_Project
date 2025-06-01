/**
 * Copyright 2022 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.cp2;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.LiteralElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OptimizationsTest {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/optimizations/";

    static OllirResult getOllirResult(String filename) {
        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), Collections.emptyMap(), false);
    }

    static OllirResult getOllirResultOpt(String filename) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getOptimize(), "true");

        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    static OllirResult getOllirResultRegalloc(String filename, int maxRegs) {
        Map<String, String> config = new HashMap<>();
        config.put(ConfigOptions.getRegister(), Integer.toString(maxRegs));


        return CpUtils.getOllirResult(SpecsIo.getResource(BASE_PATH + filename), config, true);
    }

    @Test
    public void regAllocSimple() {

        String filename = "reg_alloc/regalloc_no_change.jmm";
        int expectedTotalReg = 4;
        int configMaxRegs = 2;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        // Number of registers might change depending on what temporaries are generated, no use comparing with original

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertNotEquals("Expected registers of variables 'a' and 'b' to be different", aReg, varTable.get("b").getVirtualReg(), optimized);
    }


    @Test
    public void regAllocSequence() {

        String filename = "reg_alloc/regalloc.jmm";
        int expectedTotalReg = 3;
        int configMaxRegs = 1;

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int originalNumReg = CpUtils.countRegisters(CpUtils.getMethod(original, "soManyRegisters"));
        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "soManyRegisters"));

        CpUtils.assertNotEquals("Expected number of registers to change with -r flag\n\nOriginal regs:" + originalNumReg + "\nNew regs: " + actualNumReg,
                originalNumReg, actualNumReg,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + expectedTotalReg + ", is " + actualNumReg,
                actualNumReg == expectedTotalReg,
                optimized);


        var varTable = CpUtils.getMethod(optimized, "soManyRegisters").getVarTable();
        var aReg = varTable.get("a").getVirtualReg();
        CpUtils.assertEquals("Expected registers of variables 'a' and 'b' to be the same", aReg, varTable.get("b").getVirtualReg(), optimized);
        CpUtils.assertEquals("Expected registers of variables 'a' and 'c' to be the same", aReg, varTable.get("c").getVirtualReg(), optimized);
        CpUtils.assertEquals("Expected registers of variables 'a' and 'd' to be the same", aReg, varTable.get("d").getVirtualReg(), optimized);

    }


    @Test
    public void constPropSimple() {

        String filename = "const_prop_fold/PropSimple.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        CpUtils.assertLiteralReturn("1", method, optimized);
    }

    @Test
    public void constPropWithLoop() {

        String filename = "const_prop_fold/PropWithLoop.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        CpUtils.assertLiteralCount("3", method, optimized, 3);
    }

    @Test
    public void constFoldSimple() {

        String filename = "const_prop_fold/FoldSimple.jmm";

        var original = getOllirResult(filename);
        var optimized = getOllirResultOpt(filename);


        CpUtils.assertTrue("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                !original.getOllirCode().equals(optimized.getOllirCode()), optimized);

        var method = CpUtils.getMethod(optimized, "main");
        CpUtils.assertFindLiteral("30", method, optimized);
    }

    @Test
    public void constFoldSequence() {

        String filename = "const_prop_fold/FoldSequence.jmm";

        var original = getOllirResult(filename);
        var optimized = getOllirResultOpt(filename);


        CpUtils.assertTrue("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                !original.getOllirCode().equals(optimized.getOllirCode()), optimized);

        var method = CpUtils.getMethod(optimized, "main");
        CpUtils.assertFindLiteral("14", method, optimized);
    }

    @Test
    public void constPropAnFoldSimple() {

        String filename = "const_prop_fold/PropAndFoldingSimple.jmm";

        var original = getOllirResult(filename);
        var optimized = getOllirResultOpt(filename);


        CpUtils.assertTrue("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                !original.getOllirCode().equals(optimized.getOllirCode()), optimized);

        var method = CpUtils.getMethod(optimized, "main");
        CpUtils.assertFindLiteral("15", method, optimized);
    }

    //===============
    //   Our Test
    //===============

    // Teste 1: Reassignment simples - variável deixa de ser constante
    @Test
    public void OurconstPropReassignment() {
        String filename = "ourtest/OurPropReassignment.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);
        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar a constante porque 'a' é reassigned
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);
    }


    // Teste 2: Assignment dentro de bloco condicional
    @Test
    public void OurconstPropConditionalAssignment() {
        String filename = "ourtest/OurPropConditional.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar a constante porque 'a' pode ser modificada no if
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);    }

    // Teste 3: Assignment dentro de bloco condicional else
    @Test
    public void OurconstPropConditionalElseAssignment() {
        String filename = "ourtest/OurPropConditionalElse.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar a constante porque 'a' pode ser modificada no else
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);    }

    // Teste 4: Assignment dentro de loop while
    @Test
    public void OurconstPropWhileAssignment() {
        String filename = "ourtest/OurPropWhile.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar a constante porque 'a' é modificada no loop
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);
    }

    // Teste 5: Múltiplos reassignments
    @Test
    public void OurconstPropMultipleReassignments() {
        String filename = "ourtest/OurPropMultipleReassign.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Deve propagar apenas a última constante (3)
        CpUtils.assertLiteralReturn("3", method, optimized);
    }

    // Teste 6: Constante válida após bloco condicional que não a modifica
    @Test
    public void OurconstPropValidAfterConditional() {
        String filename = "ourtest/OurPropValidAfterIf.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Deve propagar a constante porque 'a' não é modificada no if
        CpUtils.assertLiteralReturn("5", method, optimized);
    }

    // Teste 7: Variável modificada apenas num branch do if
    @Test
    public void OurconstPropModifiedInOneBranch() {
        String filename = "ourtest/OurPropOneBranch.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar porque 'a' é modificada num dos branches
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);    }

    // Teste 8: Nested conditionals
    @Test
    public void OurconstPropNestedConditionals() {
        String filename = "ourtest/OurPropNested.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag\n\nOriginal code:\n" + original.getOllirCode(),
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "foo");
        // Não deve propagar porque 'a' é modificada em conditional aninhado
        var instructions = method.getInstructions();
        boolean hasLiteralReturn = false;
        for (var instruction : instructions) {
            if (instruction instanceof ReturnInstruction returnInstruction) {
                if (returnInstruction.getOperand().isPresent() &&
                        returnInstruction.getOperand().get() instanceof LiteralElement) {
                    hasLiteralReturn = true;
                    break;
                }
            }
        }
        CpUtils.assertTrue("Expected return to NOT be a literal", !hasLiteralReturn, optimized);    }



    //===============
    //   Our DCE Tests
    //===============

    // Teste DCE 1: Atribuição simples a variável não utilizada
    @Test
    public void OurDceSimpleAssign() {
        String filename = "ourtest/dce/OurDceSimpleAssign.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag (DCE Simple Assign)",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testSimpleDce");
        boolean assignmentToXFound = false;
        for (var instruction : method.getInstructions()) {
            if (instruction.toString().contains("x.i32 :=")) {
                assignmentToXFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected assignment to 'x' to be eliminated", !assignmentToXFound, optimized);

        CpUtils.assertLiteralReturn("0", method, optimized);
    }

    // Teste DCE 2: Múltiplas reatribuições a variável não utilizada
    @Test
    public void OurDceReassignNoUse() {
        String filename = "ourtest/dce/OurDceReassignNoUse.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag (DCE Reassign No Use)",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testReassignDce");
        boolean assignmentToAFound = false;
        for (var instruction : method.getInstructions()) {
            if (instruction.toString().contains("a.i32 :=")) {
                assignmentToAFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected all assignments to 'a' to be eliminated", !assignmentToAFound, optimized);

        CpUtils.assertLiteralReturn("5", method, optimized);
    }

    // Teste DCE 3: Atribuição dentro de condicional a variável não utilizada
    @Test
    public void OurDceConditionalAssign() {
        String filename = "ourtest/dce/OurDceConditionalAssign.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag (DCE Conditional Assign)",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testConditionalDce");
        boolean assignmentToYFound = false;
        for (var instruction : method.getInstructions()) {
            if (instruction.toString().contains("y.i32 :=")) {
                assignmentToYFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected assignments to 'y' in conditional branches to be eliminated", !assignmentToYFound, optimized);

        CpUtils.assertLiteralReturn("100", method, optimized);
    }

    // Teste DCE 4: Atribuição com expressão complexa a variável não utilizada
    @Test
    public void OurDceComplexExpressionAssign() {
        String filename = "ourtest/dce/OurDceComplexExpressionAssign.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag (DCE Complex Expression Assign)",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testComplexDce");
        boolean assignmentToResultFound = false;
        for (var instruction : method.getInstructions()) {
            if (instruction.toString().contains("result.i32 :=")) {
                assignmentToResultFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected assignment to 'result' to be eliminated", !assignmentToResultFound, optimized);

        CpUtils.assertLiteralReturn("0", method, optimized);
    }

    // Teste DCE 5: Atribuição com efeito colateral (side effect) - NÃO DEVE SER ELIMINADO
    @Test
    public void OurDceWithSideEffect() {
        String filename = "ourtest/dce/OurDceWithSideEffect.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertEquals("Expected code to NOT change with -o flag (DCE With Side Effect) as assignment has side effect",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testSideEffectDce");
        boolean assignmentToXFound = false;
        for (var instruction : method.getInstructions()) {
            if (instruction.toString().contains("x.i32 :=")) {
                assignmentToXFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected assignment to 'x' to NOT be eliminated due to side effect", assignmentToXFound, optimized);

        CpUtils.assertLiteralReturn("50", method, optimized);
    }

    @Test
    public void OurDceSimpleAssign2() {
        String filename = "ourtest/dce/OurDceSimpleAssign2.jmm";

        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        CpUtils.assertNotEquals("Expected code to change with -o flag (DCE Simple Assign)",
                original.getOllirCode(), optimized.getOllirCode(),
                optimized);

        var method = CpUtils.getMethod(optimized, "testSimpleDce");

        boolean assignmentFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("x.i32 :=") ||
                    instStr.contains("y.i32 :=") ||
                    instStr.contains("z.i32 :=")) {
                assignmentFound = true;
                break;
            }
        }
        CpUtils.assertTrue("Expected assignments to 'x', 'y', 'z' to be eliminated", !assignmentFound, optimized);

        CpUtils.assertLiteralReturn("100", method, optimized);
    }

    @Test
    public void OurDceMethodCalls() {
        String filename = "ourtest/dce/OurDceMethodCalls.jmm";
        OllirResult original = getOllirResult(filename);
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "testMethodCalls");

        boolean xAssignFound = false, yAssignFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("x.i32 :=")) xAssignFound = true;
            if (instStr.contains("y.i32 :=")) yAssignFound = true;
        }

        CpUtils.assertTrue("Expected x assignment to be eliminated", !xAssignFound, optimized);
        CpUtils.assertTrue("Expected y assignment to be eliminated", !yAssignFound, optimized);
    }


    @Test
    public void OurDceArrayOps() {
        String filename = "ourtest/dce/OurDceArrayOps.jmm";
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "testArrayOps");

        boolean assignmentFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("arr.array.i32 :=") ||
                    instStr.contains("x.i32 :=") ||
                    instStr.contains("y.i32 :=")) {
                assignmentFound = true;
                break;
            }
        }

        CpUtils.assertTrue("Expected all assignments to be eliminated", !assignmentFound, optimized);
    }


    @Test
    public void OurDceConditionals() {
        String filename = "ourtest/dce/OurDceConditionals.jmm";
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "testConditionals");

        boolean xAssignFound = false, yAssignFound = false, zAssignFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("x.i32 :=")) xAssignFound = true;
            if (instStr.contains("y.i32 :=")) yAssignFound = true;
            if (instStr.contains("z.i32 :=")) zAssignFound = true;
        }

        CpUtils.assertTrue("Expected x assignment to be eliminated", !xAssignFound, optimized);
        CpUtils.assertTrue("Expected y assignment to be eliminated", !yAssignFound, optimized);
        CpUtils.assertTrue("Expected z assignment to be eliminated", !zAssignFound, optimized);
    }

    @Test
    public void OurDceReturnVar() {
        String filename = "ourtest/dce/OurDceReturnVar.jmm";
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "testReturnVar");

        boolean xAssignFound = false, yAssignFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("x.i32 :=")) xAssignFound = true;
            if (instStr.contains("y.i32 :=")) yAssignFound = true;
        }

        CpUtils.assertTrue("Expected x assignment to be eliminated", !xAssignFound, optimized);
        CpUtils.assertTrue("Expected y assignment to be eliminated", !yAssignFound, optimized);
    }

    @Test
    public void OurDceChainedOps() {
        String filename = "ourtest/dce/OurDceChainedOps.jmm";
        OllirResult optimized = getOllirResultOpt(filename);

        var method = CpUtils.getMethod(optimized, "testChainedOps");

        boolean aAssignFound = false, bAssignFound = false, cAssignFound = false, dAssignFound = false;
        for (var instruction : method.getInstructions()) {
            String instStr = instruction.toString();
            if (instStr.contains("a.i32 :=")) aAssignFound = true;
            if (instStr.contains("b.i32 :=")) bAssignFound = true;
            if (instStr.contains("c.i32 :=")) cAssignFound = true;
            if (instStr.contains("d.i32 :=")) dAssignFound = true;
        }

        CpUtils.assertTrue("Expected a assignment to be eliminated", !aAssignFound, optimized);
        CpUtils.assertTrue("Expected b assignment to be eliminated", !bAssignFound, optimized);
        CpUtils.assertTrue("Expected c assignment to be eliminated", !cAssignFound, optimized);
        CpUtils.assertTrue("Expected d assignment to be eliminated", !dAssignFound, optimized);
    }

}
