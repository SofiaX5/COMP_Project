package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

        System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // AST Optimizations: Constant Propagation and Constant Folding
        /*
        var constProp = new ConstPropVisitor();
        var constFold = new ConstFoldVisitor();

        boolean changed;
        do {
            changed = false;
            if (constProp.visit(semanticsResult.getRootNode())) changed = true;
            if (constFold.visit(semanticsResult.getRootNode())) changed = true;
        } while (changed); // Repeat until no further changes occur
        */
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // Perform AST-level optimizations if enabled


        return ollirResult;
    }
}
