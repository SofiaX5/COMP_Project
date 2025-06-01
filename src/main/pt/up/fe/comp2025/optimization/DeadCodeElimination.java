package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import java.util.*;
import java.util.stream.Stream;

public class DeadCodeElimination {
    private ClassUnit classUnit;
    private LivenessAnalysis livenessAnalysis;
    private boolean changed = false;
    private Method currentMethod; // Add this field

    public DeadCodeElimination(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public boolean optimize() {
        changed = false;

        for (Method method : classUnit.getMethods()) {
            if (eliminateDeadCodeInMethod(method)) {
                changed = true;
            }
        }

        return changed;
    }

    private boolean eliminateDeadCodeInMethod(Method method) {
        this.currentMethod = method;
        this.livenessAnalysis = new LivenessAnalysis(method);
        boolean methodChanged = false;
        ArrayList<Instruction> instructions = method.getInstructions();

        ArrayList<Instruction> newInstructions = new ArrayList<>();
        List<Instruction> removedInstructions = new ArrayList<>();

        for (Instruction inst : instructions) {
            if (shouldRemoveInstruction(inst)) {
                removedInstructions.add(inst);
                methodChanged = true;
                System.out.println("Eliminating dead instruction: " + inst);
            } else {
                newInstructions.add(inst);
            }
        }

        if (methodChanged) {
            instructions.clear();
            instructions.addAll(newInstructions);

            method.buildCFG();
            method.buildVarTable();

            for (int i = 0; i < instructions.size(); i++) {
                instructions.get(i).setId(i);
            }

            for (Instruction removed : removedInstructions) {
                removed.getPredecessors().clear();
                removed.getSuccessors().clear();
            }
        }

        return methodChanged;
    }

    private boolean shouldRemoveInstruction(Instruction inst) {
        if (hasSideEffects(inst)) {
            return false;
        }

        if (inst instanceof AssignInstruction assignInst) {
            return isDeadAssignment(assignInst);
        }

        return false;
    }

    private boolean wasUsedInConstantFoldedCondition(String varName, Method method) {
        for (Instruction inst : method.getInstructions()) {
            if (inst instanceof CondBranchInstruction condBranch) {
                List<Element> operands = condBranch.getOperands();
                boolean hasLiteral = operands.stream()
                        .anyMatch(op -> op instanceof LiteralElement);

                if (hasLiteral) {
                    boolean varUsedInCondition = operands.stream()
                            .anyMatch(op -> op instanceof Operand operand &&
                                    operand.getName().equals(varName));

                    if (varUsedInCondition) {
                        System.out.println("Found constant-folded condition using variable: " + varName);
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private boolean isDeadAssignment(AssignInstruction assignInst) {
        Element dest = assignInst.getDest();
        if (!(dest instanceof Operand operand)) {
            return false;
        }

        String varName = operand.getName();

        if (wasUsedInConstantFoldedCondition(varName, currentMethod)) {
            System.out.println("Variable '" + varName + "' preserved - likely used in constant-folded condition");
            return false;
        }

        Set<String> outSet = livenessAnalysis.OutSet.get(assignInst);
        if (outSet == null) {
            System.out.println("Warning: No outSet for instruction: " + assignInst);
            return false;
        }

        boolean isDead = !outSet.contains(varName);

        if (isDead) {
            System.out.println("Variable '" + varName + "' is dead after assignment: " + assignInst);
            System.out.println("OutSet: " + outSet);
        } else {
            System.out.println("Variable '" + varName + "' is live after assignment: " + assignInst);
            System.out.println("OutSet: " + outSet);
        }

        return isDead;
    }

    private boolean hasSideEffects(Instruction inst) {
        if (inst instanceof CallInstruction ||
                inst instanceof ReturnInstruction ||
                inst instanceof PutFieldInstruction ||
                inst instanceof GotoInstruction ||
                inst instanceof CondBranchInstruction ||
                inst instanceof OpCondInstruction) {
            return true;
        }

        if (inst instanceof AssignInstruction assignInst) {
            return hasRhsSideEffects(assignInst.getRhs());
        }

        return false;
    }

    private boolean hasRhsSideEffects(Instruction rhs) {
        if (rhs == null) {
            return false;
        }

        if (rhs instanceof CallInstruction) {
            return true;
        }

        if (rhs instanceof NewInstruction) {
            return true;
        }

        if (rhs instanceof GetFieldInstruction) {
            return false;
        }

        if (rhs instanceof BinaryOpInstruction ||
                rhs instanceof SingleOpInstruction ||
                rhs instanceof UnaryOpInstruction) {
            return false;
        }


        return false;
    }

    public boolean optimizeIteratively() {
        boolean overallChanged = false;
        boolean iterationChanged;
        int iterations = 0;
        final int MAX_ITERATIONS = 10;

        do {
            iterationChanged = optimize();
            if (iterationChanged) {
                overallChanged = true;
                iterations++;
                System.out.println("Dead code elimination iteration " + iterations + " made changes");
            }
        } while (iterationChanged && iterations < MAX_ITERATIONS);

        if (iterations >= MAX_ITERATIONS) {
            System.out.println("Warning: Dead code elimination stopped after " + MAX_ITERATIONS + " iterations");
        }

        return overallChanged;
    }
}