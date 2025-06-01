package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.*;

public class StackSimulator {

    private final Method method;
    private final JasminUtils jasminUtils;
    private int maxStackSize;
    private int maxLocals;
    private int currentStackSize;

    public StackSimulator(Method method, JasminUtils jasminUtils) {
        this.method = method;
        this.jasminUtils = jasminUtils;
        this.maxStackSize = 0;
        this.maxLocals = 0;
        this.currentStackSize = 0;

        calculateLimits();
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    private void calculateLimits() {
        calculateMaxLocals();

        for (Instruction instruction : method.getInstructions()) {
            simulateInstruction(instruction);
        }
    }

    private void calculateMaxLocals() {
        int localsCount = method.isStaticMethod() ? 0 : 1;

        localsCount += method.getParams().size();

        for (Descriptor descriptor : method.getVarTable().values()) {
            int reg = descriptor.getVirtualReg();
            localsCount = Math.max(localsCount, reg + 1);
        }

        this.maxLocals = localsCount;
    }

    private void simulateInstruction(Instruction instruction) {
        if (instruction instanceof AssignInstruction) {
            simulateAssignInstruction((AssignInstruction) instruction);
        } else if (instruction instanceof CallInstruction) {
            simulateCallInstruction((CallInstruction) instruction);
        } else if (instruction instanceof ReturnInstruction) {
            simulateReturnInstruction((ReturnInstruction) instruction);
        } else if (instruction instanceof CondBranchInstruction) {
            simulateBranchInstruction((CondBranchInstruction) instruction);
        } else if (instruction instanceof GetFieldInstruction) {
            simulateGetFieldInstruction((GetFieldInstruction) instruction);
        } else if (instruction instanceof PutFieldInstruction) {
            simulatePutFieldInstruction((PutFieldInstruction) instruction);
        } else if (instruction instanceof UnaryOpInstruction) {
            simulateUnaryOpInstruction((UnaryOpInstruction) instruction);
        } else if (instruction instanceof SingleOpInstruction) {
            simulateSingleOpInstruction((SingleOpInstruction) instruction);
        } else if (instruction instanceof ArrayLengthInstruction) {
            simulateArrayLengthInstruction((ArrayLengthInstruction) instruction);
        } else if (instruction instanceof InvokeVirtualInstruction) {
            simulateInvokeVirtualInstruction((InvokeVirtualInstruction) instruction);
        } else if (instruction instanceof BinaryOpInstruction) {
            simulateBinaryOpInstruction((BinaryOpInstruction) instruction);
        } 

    }

    private void simulateAssignInstruction(AssignInstruction assign) {
        simulateInstruction(assign.getRhs());

        popStack(1);
    }

    private void simulateCallInstruction(CallInstruction call) {
        if (!call.getReturnType().toString().equals("invokestatic") && !call.getArguments().isEmpty()) {
            simulateElement(call.getArguments().getFirst());
        }

        for (Element arg : call.getOperands()) {
            simulateElement(arg);
        }

        int argsCount = call.getOperands().size();
        if (!call.getReturnType().toString().equals("invokestatic")) {
            argsCount++;
        }
        popStack(argsCount);

        if (!jasminUtils.convertType(call.getReturnType()).equals("V")) {
            pushStack(1);
        }
    }

    private void simulateReturnInstruction(ReturnInstruction returnInst) {
        if (returnInst.hasReturnValue()) {
            simulateElement(returnInst.getOperand().get());
            popStack(1);
        }
    }

    private void simulateBranchInstruction(CondBranchInstruction branch) {
        for (Element operand : branch.getOperands()) {
            simulateElement(operand);
        }

        popStack(branch.getOperands().size());
    }

    private void simulateGetFieldInstruction(GetFieldInstruction getField) {
        simulateElement(getField.getOperands().getFirst());

    }

    private void simulatePutFieldInstruction(PutFieldInstruction putField) {
        simulateElement(putField.getOperands().get(1));

        simulateElement(putField.getOperands().getFirst());

        popStack(2);
    }

    private void simulateUnaryOpInstruction(UnaryOpInstruction unaryOp) {
        simulateElement(unaryOp.getOperand());
    }

    private void simulateSingleOpInstruction(SingleOpInstruction singleOp) {
        simulateElement(singleOp.getSingleOperand());
    }

    private void simulateElement(Element element) {
        if (element instanceof LiteralElement) {
            pushStack(1);
        } else if (element instanceof Operand) {
            pushStack(1);
        }
    }

    private void simulateBinaryOpInstruction(BinaryOpInstruction binaryOp) {
        simulateElement(binaryOp.getLeftOperand());
        simulateElement(binaryOp.getRightOperand());

        popStack(2);
        pushStack(1);
    }

    private void simulateArrayOperand(ArrayOperand arrayOp) {
        simulateElement(arrayOp.toElement());

        for (Element index : arrayOp.getIndexOperands()) {
            simulateElement(index);
        }

        popStack(1 + arrayOp.getIndexOperands().size());
        pushStack(1);
    }

    private void pushStack(int count) {
        currentStackSize += count;
        maxStackSize = Math.max(maxStackSize, currentStackSize);
    }

    private void popStack(int count) {
        currentStackSize = Math.max(0, currentStackSize - count);
    }

    private void simulateArrayLengthInstruction(ArrayLengthInstruction arrayLength) {
        simulateElement(arrayLength.getOperands().getFirst());
        popStack(1);
        pushStack(1);
    }

    private void simulateInvokeVirtualInstruction(InvokeVirtualInstruction invokeVirtual) {
        simulateElement(invokeVirtual.getOperands().getFirst());

        for (int i = 1; i < invokeVirtual.getOperands().size(); i++) {
            simulateElement(invokeVirtual.getOperands().get(i));
        }

        popStack(invokeVirtual.getOperands().size());

        if (!jasminUtils.convertType(invokeVirtual.getReturnType()).equals("V")) {
            pushStack(1);
        }
    }


    
}