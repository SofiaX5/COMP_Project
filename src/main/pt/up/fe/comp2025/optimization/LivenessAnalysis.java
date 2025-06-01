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
        int instSize = instList.size();

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


        // In and out
        boolean livedChanged;
        do {
            livedChanged = false;

            for (int i = instSize - 1; i >= 0; i--) {
                Instruction inst = instList.get(i);

                Set<String> inOld = new HashSet<>(InSet.get(inst));
                Set<String> outOld = new HashSet<>(OutSet.get(inst));

                // Out[n] = ∪ In[sucessors]
                Set<String> out = new HashSet<>();
                for (Instruction succInst : inst.getSuccessorsAsInst()) {
                    out.addAll(InSet.getOrDefault(succInst , new HashSet<>()));
                }

                // In[n] = Use[n] ∪ (Out[n] - Def[n])
                Set<String> in = new HashSet<> (UseSet.get(inst));
                Set<String> outMinusDef = new HashSet<>(out);
                outMinusDef.removeAll(DefSet.get(inst));
                in.addAll(outMinusDef);

                InSet.put(inst, in);
                OutSet.put(inst, out);

                if (!in.equals(inOld) || !out.equals(outOld)) {
                    livedChanged = true;
                }
            }
        } while (livedChanged);
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
            }

        } else if (inst instanceof GetFieldInstruction gf) {
            used.add(gf.getField().getName());
        }

        return used;
    }
}

