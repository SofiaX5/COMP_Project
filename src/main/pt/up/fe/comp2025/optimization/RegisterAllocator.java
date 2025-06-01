package pt.up.fe.comp2025.optimization;

import java.util.*;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.inst.Instruction;

public class RegisterAllocator {
    private ClassUnit classUnit;
    private int maxRegisters;

    private Map<String, Map<String, Integer>> allocationPerMethod = new HashMap<>();


    public RegisterAllocator(OllirResult ollirResult, int maxRegisters) {
        this.classUnit = ollirResult.getOllirClass();
        this.maxRegisters = maxRegisters;
    }

    public void allocateForAllMethods() {
        classUnit.buildCFGs();

        for (Method method : classUnit.getMethods()) {
            LivenessAnalysis liveAnalysis = new LivenessAnalysis(method);

            Map<String, Set<String>> interferenceGraph = buildInterferenceGraph(method, liveAnalysis);
            System.out.println("Interference graph: " + interferenceGraph);

            Map<String, Descriptor> varTable = method.getVarTable();
            int numColorInit = getNumRegInit(method, varTable);
            Map<String, Integer> graphColor = colorGraph(interferenceGraph, numColorInit);

            allocationPerMethod.put(method.getMethodName(), graphColor);

            updateVarTable(method, graphColor, varTable);
        }
    }


    private Map<String, Set<String>> buildInterferenceGraph(Method method, LivenessAnalysis liveAnalysis) {
        Map<String, Set<String>> interferenceGraph = new HashMap<>();

        for (Instruction instr : method.getInstructions()) {
            Set<String> defInst = liveAnalysis.DefSet.getOrDefault(instr, new HashSet<>());
            Set<String> outInst = liveAnalysis.OutSet.getOrDefault(instr, new HashSet<>());
            System.out.println("DefSet: " + defInst + ", OutSet: " + outInst);

            for (String def : defInst) {
                // Add def to graph
                interferenceGraph.putIfAbsent(def, new HashSet<>());

                for (String out : outInst) {
                    // Add out if def != out
                    if (!out.equals(def)) {
                        interferenceGraph.get(def).add(out);

                        // Symetric
                        interferenceGraph.putIfAbsent(out, new HashSet<>());
                        interferenceGraph.get(out).add(def);
                    }
                }
            }
        }

        return interferenceGraph;
    }

    public Map<String, Integer> colorGraph(Map<String, Set<String>> interferenceGraph, int numColorsInit) {
        Map<String, Integer> colorAssignment = new HashMap<>();

        // Lista de variáveis (nós) a colorir
        List<String> variables = new ArrayList<>(interferenceGraph.keySet());

        variables.sort((v1, v2) ->
                Integer.compare(interferenceGraph.get(v2).size(), interferenceGraph.get(v1).size()));

        for (String variable : variables) {
            Set<Integer> usedColors = new HashSet<>();

            // Verifica as cores dos vizinhos
            for (String neighbor : interferenceGraph.getOrDefault(variable, Set.of())) {
                if (colorAssignment.containsKey(neighbor)) {
                    usedColors.add(colorAssignment.get(neighbor));
                }
            }


            int color = numColorsInit;
            while (usedColors.contains(color)) {
                color++;
            }

            if (color >= maxRegisters) {
                System.err.println("ERROR: Cannot allocate with only " + maxRegisters +
                        " registers. Minimum required: " + maxRegisters);

            }


            colorAssignment.put(variable, color);
        }

        return colorAssignment;
    }

    public void updateVarTable(Method method, Map<String, Integer> registerAllocation, Map<String, Descriptor> varTable) {
        for (Map.Entry<String, Integer> entry : registerAllocation.entrySet()) {
            String varName = entry.getKey();
            int reg = entry.getValue();

            Descriptor desc = varTable.get(varName);
            if (desc != null) {
                setVirtualReg(desc, reg);
                System.out.println("Variável atualizada: " + varName);
            } else {
                System.out.println("Variável não encontrada no varTable: " + varName);
            }
        }
    }

    private void setVirtualReg(Descriptor descriptor, int regNum) {
        try {
            java.lang.reflect.Method setVirtualRegMethod = descriptor.getClass().getMethod("setVirtualReg", int.class);
            setVirtualRegMethod.invoke(descriptor, regNum);
        } catch (Exception e) {
            System.err.println("Failed to set virtual register: " + e.getMessage());
        }
    }

    private int getNumRegInit(Method method, Map<String, Descriptor> varTable) {
        int num = 0;
        for (Descriptor descriptor : varTable.values()) {
            VarScope scope = descriptor.getScope();
            if (scope == VarScope.FIELD || scope == VarScope.PARAMETER) num++;
        }
        if (!method.isStaticMethod()) num++;
        return num;
    }
}