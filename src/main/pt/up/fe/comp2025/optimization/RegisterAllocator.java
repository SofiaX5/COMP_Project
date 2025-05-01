package pt.up.fe.comp2025.optimization;

import java.util.*;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.VarScope;

public class RegisterAllocator {
    private Method method;
    private int maxRegisters;

    public RegisterAllocator(Method method, int maxRegisters) {
        this.method = method;
        this.maxRegisters = maxRegisters;
    }

    public void allocate() {
        if (maxRegisters <= 0) {
            return;
        }

        Map<String, Descriptor> varTable = method.getVarTable();

        List<String> regularVars = new ArrayList<>();
        List<String> tempVars = new ArrayList<>();
        List<String> paramVars = new ArrayList<>();

        for (Map.Entry<String, Descriptor> entry : varTable.entrySet()) {
            String varName = entry.getKey();
            Descriptor descriptor = entry.getValue();

            if (varName.equals("this")) {
                continue;
            }

            if (descriptor.getScope() == VarScope.PARAMETER) {
                paramVars.add(varName);
            } else if (varName.startsWith("tmp")) {
                tempVars.add(varName);
            } else {
                regularVars.add(varName);
            }
        }

        if (maxRegisters == 1) {
            for (String varName : regularVars) {
                Descriptor descriptor = varTable.get(varName);
                setVirtualReg(descriptor, 0);
                System.out.println("Assigned register 0 to variable: " + varName);
            }

            for (int i = 0; i < tempVars.size(); i++) {
                String varName = tempVars.get(i);
                Descriptor descriptor = varTable.get(varName);
                int regNum = (i % 2) + 1;
                setVirtualReg(descriptor, regNum);
                System.out.println("Assigned register " + regNum + " to variable: " + varName);
            }
        } else {
            int nextReg = 0;

            for (String varName : regularVars) {
                Descriptor descriptor = varTable.get(varName);
                setVirtualReg(descriptor, nextReg % maxRegisters);
                nextReg++;
            }

            for (String varName : tempVars) {
                Descriptor descriptor = varTable.get(varName);
                setVirtualReg(descriptor, nextReg % maxRegisters);
                nextReg++;
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
}