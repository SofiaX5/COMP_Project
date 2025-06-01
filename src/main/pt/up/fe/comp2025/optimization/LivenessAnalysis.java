package pt.up.fe.comp2025.optimization;

import java.util.*;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.inst.Instruction;
import java.util.HashMap;
import java.util.Map;

public class LivenessAnalysis {

    public Map<Instruction, Set<String>> DefSet = new HashMap<>();
    public Map<Instruction, Set<String>> UseSet = new HashMap<>();
    public Map<Instruction, Set<String>> InSet = new HashMap<>();
    public Map<Instruction, Set<String>> OutSet = new HashMap<>();

    public LivenessAnalysis(Method method) {
        analyzeMethod(method);
    }


    public void analyzeMethod(Method method) {
        ArrayList<Instruction> instList = method.getInstructions();
        method.buildCFG();

        for (Instruction inst : instList) {
            // Def
            Set<String> def = new HashSet<>();
            if (inst instanceof AssignInstruction assign) {
                Element dest = assign.getDest();
                if (dest instanceof Operand operand) {
                    def.add(operand.getName());
                }
            }

            // Use
            Set<String> use = new HashSet<>(getUse(inst));

            DefSet.put(inst, def);
            UseSet.put(inst, use);
            InSet.put(inst, new HashSet<>());
            OutSet.put(inst, new HashSet<>());
        }

        boolean changed;
        int iterations = 0;
        final int MAX_ITERATIONS = 100;

        do {
            changed = false;
            iterations++;

            for (int i = instList.size() - 1; i >= 0; i--) {
                Instruction inst = instList.get(i);

                Set<String> oldIn = new HashSet<>(InSet.get(inst));
                Set<String> oldOut = new HashSet<>(OutSet.get(inst));

                Set<String> newOut = new HashSet<>();
                List<Instruction> successors = inst.getSuccessorsAsInst();

                if (inst instanceof CondBranchInstruction) {
                    System.out.println("Conditional instruction successors: " + successors.size());
                }

                for (Instruction succ : successors) {
                    Set<String> succIn = InSet.get(succ);
                    if (succIn != null) {
                        newOut.addAll(succIn);
                    }
                }

                Set<String> newIn = new HashSet<>(UseSet.get(inst));
                Set<String> outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(DefSet.get(inst));
                newIn.addAll(outMinusDef);

                OutSet.put(inst, newOut);
                InSet.put(inst, newIn);

                if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
                    changed = true;
                }
            }
        } while (changed && iterations < MAX_ITERATIONS);

        if (iterations >= MAX_ITERATIONS) {
            System.out.println("Warning: Liveness analysis stopped after " + MAX_ITERATIONS + " iterations");
        }
    }



    public Set<String> getUse(Instruction inst) {
        Set<String> used = new HashSet<>();

        if (inst instanceof AssignInstruction assign) {
            if (assign.getRhs() instanceof BinaryOpInstruction binOp) {
                if (binOp.getLeftOperand() instanceof Operand lop) {
                    used.add(lop.getName());
                }
                if (binOp.getRightOperand() instanceof Operand rop) {
                    used.add(rop.getName());
                }
            } else if (assign.getRhs() instanceof SingleOpInstruction singOp) {
                if (singOp.getSingleOperand() instanceof Operand op) {
                    used.add(op.getName());
                }
            } else if (assign.getRhs() instanceof CallInstruction call) {
                if (!call.getArguments().isEmpty() && call.getArguments().getFirst() instanceof Operand op) {
                    used.add(op.getName());
                }
                for (Element arg : call.getOperands()) {
                    if (arg instanceof Operand op) {
                        used.add(op.getName());
                    }
                }
            } else if (assign.getRhs() instanceof GetFieldInstruction gf) {
                if (!gf.getOperands().isEmpty() && gf.getOperands().getFirst() instanceof Operand op) {
                    used.add(op.getName());
                }
            }
        } else if (inst instanceof GetFieldInstruction gf) {
            if (!gf.getOperands().isEmpty() && gf.getOperands().getFirst() instanceof Operand op) {
                used.add(op.getName());
            }
        } else if (inst instanceof PutFieldInstruction pf) {
            List<Element> operands = pf.getOperands();
            // Fix: Check bounds before accessing elements
            if (operands.size() > 0 && operands.get(0) instanceof Operand op) {
                used.add(op.getName());
            }
            if (operands.size() > 2 && operands.get(2) instanceof Operand op) {
                used.add(op.getName());
            }
        } else if (inst instanceof CallInstruction call) {
            if (!call.getArguments().isEmpty() && call.getArguments().getFirst() instanceof Operand op) {
                used.add(op.getName());
            }
            for (Element arg : call.getOperands()) {
                if (arg instanceof Operand op) {
                    used.add(op.getName());
                }
            }
        } else if (inst instanceof ReturnInstruction ret) {
            if (ret.hasReturnValue()) {
                Element returnElement = ret.getOperand().get();
                if (returnElement instanceof Operand op) {
                    used.add(op.getName());
                }
            }
        } else if (inst instanceof CondBranchInstruction cond) {
            for (Element operand : cond.getOperands()) {
                if (operand instanceof Operand op) {
                    used.add(op.getName());
                }
            }
        } else if (inst instanceof GotoInstruction ) {
        } else if (inst instanceof UnaryOpInstruction unary) {
            if (unary.getOperand() instanceof Operand op) {
                used.add(op.getName());
            }
        } else if (inst instanceof BinaryOpInstruction binOp) {
            if (binOp.getLeftOperand() instanceof Operand lop) {
                used.add(lop.getName());
            }
            if (binOp.getRightOperand() instanceof Operand rop) {
                used.add(rop.getName());
            }
        } else if (inst instanceof SingleOpInstruction singOp) {
            if (singOp.getSingleOperand() instanceof Operand op) {
                used.add(op.getName());
            }
        }

        return used;
    }
}

