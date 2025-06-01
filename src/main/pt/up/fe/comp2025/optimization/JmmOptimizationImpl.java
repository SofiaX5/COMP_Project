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
        if (semanticsResult.getConfig().getOrDefault("optimize", "false").equals("false")) {
            return semanticsResult;
        }

        var constProp = new ConstPropVisitor();
        var constFold = new ConstFoldVisitor();

        boolean changedProp, changedFold;
        int iterations = 0;
        final int MAX_ITERATIONS = 10;

        do {
            changedProp = constProp.visit(semanticsResult.getRootNode());
            changedFold = constFold.visit(semanticsResult.getRootNode(), new HashMap<>());
            iterations++;

            if (changedProp || changedFold ) {
                System.out.println("AST changed during optimization iteration " + iterations);
            }
        } while ((changedProp || changedFold) && iterations < MAX_ITERATIONS);

        if (iterations >= MAX_ITERATIONS) {
            System.out.println("Warning: Optimization stopped after " + MAX_ITERATIONS + " iterations.");
        }

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        if (ollirResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            try {
                ClassUnit classUnit = ollirResult.getOllirClass();

                DeadCodeElimination deadCodeEliminator = new DeadCodeElimination(classUnit);
                boolean dceChanged = deadCodeEliminator.optimizeIteratively();

                if (dceChanged) {
                    System.out.println("Dead code elimination made changes to OLLIR");
                } else {
                    System.out.println("No dead code found to eliminate");
                }

            } catch (Exception e) {
                System.err.println("Error during dead code elimination: " + e.getMessage());
                e.printStackTrace();
            }
        }

        int registerLimit;
        String registerLimitStr = ollirResult.getConfig().get("registerAllocation");

        try {
            registerLimit = Integer.parseInt(registerLimitStr);
        } catch (NumberFormatException e) {
            registerLimit = -1;
        }

        if (registerLimit >= 0) {
            RegisterAllocator allocator = new RegisterAllocator(ollirResult, registerLimit);
            allocator.allocate();
            //System.out.println("Applied register allocation with limit: " + registerLimit);
        } else {
            System.out.println("Using default register allocation (OLLIR representation)");
        }

        return ollirResult;
    }
}