package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var optimizedResult = semanticsResult;
        if (semanticsResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            optimizedResult = optimize(semanticsResult);
        }

        var visitor = new OllirGeneratorVisitor(optimizedResult.getSymbolTable());
        var ollirCode = visitor.visit(optimizedResult.getRootNode());

        System.out.println("Generated method code: " + ollirCode);
        System.out.println("OLLIR:\n" + ollirCode);

        return new OllirResult(optimizedResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // AST Optimizations: Constant Propagation and Constant Folding

        var constProp = new ConstPropVisitor();
        var constFold = new ConstFoldVisitor();

        boolean changedProp = false, changedFold = false;
        do {
            changedProp = constProp.visit(semanticsResult.getRootNode());
            changedFold = constFold.visit(semanticsResult.getRootNode());
        } while (changedProp || changedFold); // Repeat until no further changes occur

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        String registerLimitStr = ollirResult.getConfig().getOrDefault("registerAllocation", "-1");
        int registerLimit;

        try {
            registerLimit = Integer.parseInt(registerLimitStr);
        } catch (NumberFormatException e) {
            registerLimit = -1;
        }

        if (registerLimit >= 0) {
            ClassUnit classUnit = ollirResult.getOllirClass();

            for (Method method : classUnit.getMethods()) {
                if (!method.isConstructMethod()) {
                    RegisterAllocator allocator = new RegisterAllocator(method, registerLimit);
                    allocator.allocate();
                }
            }

            System.out.println("Applied register allocation with limit: " + registerLimit);
        }

        return ollirResult;
    }
}