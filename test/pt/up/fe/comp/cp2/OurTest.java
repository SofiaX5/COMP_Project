package pt.up.fe.comp.cp2;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.type.BuiltinKind;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2025.ConfigOptions;
import pt.up.fe.specs.util.SpecsIo;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class OurTest {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/tests/";

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
    public void methodTest() {
        var result = getOllirResult("methodCall.jmm");
        System.out.println(result.getOllirCode());
    }
    @Test
    public void methodTest2() {
        var result = getOllirResult("methodCall2.jmm");
        System.out.println(result.getOllirCode());
    }

    @Test
    public void regAlloc1() {
        String filename = "alloc1.jmm";
        //int expectedTotalReg = 1;
        int configMaxRegs = 2;

        OllirResult optimized = getOllirResultRegalloc(filename, configMaxRegs);

        int actualNumReg = CpUtils.countRegisters(CpUtils.getMethod(optimized, "A"));
        int actualNumReg2 = CpUtils.countRegisters(CpUtils.getMethod(optimized, "main"));
        int actualNumReg3 = CpUtils.countRegisters(CpUtils.getMethod(optimized, "foo"));
        int actualNumReg4 = CpUtils.countRegisters(CpUtils.getMethod(optimized, "foo2"));
        int actualNumReg5 = CpUtils.countRegisters(CpUtils.getMethod(optimized, "foo3"));
        int actualNumReg6 = CpUtils.countRegisters(CpUtils.getMethod(optimized, "callfoos"));

        // Number of registers might change depending on what temporaries are generated, no use comparing with original

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 1 + ", is " + actualNumReg,
                actualNumReg == 1,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 1 + ", is " + actualNumReg,
                actualNumReg2 == 1,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 1 + ", is " + actualNumReg,
                actualNumReg3 == 2,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 1 + ", is " + actualNumReg,
                actualNumReg4 == 3,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 1 + ", is " + actualNumReg,
                actualNumReg5 == 4,
                optimized);

        CpUtils.assertTrue("Expected number of locals in 'soManyRegisters' to be equal to " + 4 + ", is " + actualNumReg,
                actualNumReg6 == 4,
                optimized);

    }
}