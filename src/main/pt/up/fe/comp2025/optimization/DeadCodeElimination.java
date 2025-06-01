package pt.up.fe.comp2025.optimization;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import java.util.*;
import java.util.stream.Stream;

public class DeadCodeElimination {
    private ClassUnit classUnit;
    private LivenessAnalysis livenessAnalysis;
    private boolean changed = false;

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
        this.livenessAnalysis = new LivenessAnalysis(method);
        boolean methodChanged = false;
        ArrayList<Instruction> instructions = method.getInstructions();
        List<Instruction> toRemove = new ArrayList<>();

        for (Instruction inst : instructions) {
            if (shouldRemoveInstruction(inst)) {
                toRemove.add(inst);
                methodChanged = true;
                System.out.println("Eliminating dead instruction: " + inst);
            }
        }

        for (Instruction deadInst : toRemove) {
            removeInstruction(method, deadInst);
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

    private boolean isDeadAssignment(AssignInstruction assignInst) {
        Set<String> defSet = livenessAnalysis.DefSet.get(assignInst);
        if (defSet == null || defSet.isEmpty()) {
            return false;
        }

        Set<String> outSet = livenessAnalysis.OutSet.get(assignInst);
        if (outSet == null) {
            return true;
        }

        for (String defVar : defSet) {
            if (outSet.contains(defVar)) {
                return false;
            }
        }

        return true;
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

    private void removeInstruction(Method method, Instruction instToRemove) {
        ArrayList<Instruction> instructions = method.getInstructions();
        instructions.remove(instToRemove);

        List<Instruction> predecessors = instToRemove.getPredecessors().stream()
                .filter(n -> n instanceof Instruction)
                .map(Instruction.class::cast)
                .toList();
        List<Instruction> successors = instToRemove.getSuccessorsAsInst();

        for (Instruction pred : predecessors) {
            for (Instruction succ : successors) {
                if (!pred.getSuccessors().contains(succ)) {
                    pred.addSucc(succ);
                }
            }
            pred.getSuccessors().remove(instToRemove);
        }

        for (Instruction succ : successors) {
            for (Instruction pred : predecessors) {
                if (!succ.getPredecessors().contains(pred)) {
                    succ.getPredecessors().add(pred);
                }
            }
            succ.getPredecessors().remove(instToRemove);
        }

        instToRemove.getPredecessors().clear();
        instToRemove.getSuccessors().clear();
    }

    private List<Instruction> getDefiningInstructions(String varName, Method method) {
        List<Instruction> definingInsts = new ArrayList<>();

        for (Instruction inst : method.getInstructions()) {
            Set<String> defSet = livenessAnalysis.DefSet.get(inst);
            if (defSet != null && defSet.contains(varName)) {
                definingInsts.add(inst);
            }
        }

        return definingInsts;
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