package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.specs.comp.ollir.OperationType.*;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    List<Report> reports;

    String code;

    Method currentMethod;

    private final JasminUtils types;

    private final FunctionClassMap<TreeNode, String> generators;

    private int labelCounter = 0;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;

        types = new JasminUtils(ollirResult);

        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(OpCondInstruction.class, this::generateOpCond);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(InvokeStaticInstruction.class, this::generateInvokeStatic);
        generators.put(SingleOpCondInstruction.class, this::generateSingleOpCond);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
        generators.put(InvokeVirtualInstruction.class, this::generateInvokeVirtual);
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);
        generators.put(ArrayOperand.class, this::generateArrayOperand);
    }

    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        code.append("; ").append(node).append(NL);
        code.append(generators.apply(node));
        System.out.println("JASMIN????");
        System.out.println(code.toString());


        return code.toString();
    }


    public List<Report> getReports() {
        return reports;
    }

    public String build() {
        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());

        }

        return code;
    }


    private String generateClassUnit(ClassUnit classUnit) {
        var code = new StringBuilder();

        // Generate class name
        var className = classUnit.getClassName();
        code.append(".class public ").append(className).append(NL);
        
        var fullSuperClass = "java/lang/Object";
        if (classUnit.getSuperClass() != null) {
            fullSuperClass = classUnit.getSuperClass().replace(".", "/");
        }
        code.append(".super ").append(fullSuperClass).append(NL);
        
        for (var field : classUnit.getFields()) {
            code.append(generateField(field));
        }
        
        boolean hasConstructor = classUnit.getMethods().stream()
                .anyMatch(Method::isConstructMethod);

        if (!hasConstructor) {
            code.append(".method public <init>()V").append(NL);
            code.append("    aload_0").append(NL);
            code.append("    invokespecial ").append(fullSuperClass).append("/<init>()V").append(NL);
            code.append("    return").append(NL);
            code.append(".end method").append(NL);
        }

        // Generate other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {
            if (method.isConstructMethod()) {
                continue;
            }
            code.append(apply(method));
        }
        
        return code.toString();
    }


    private String generateField(Field field) {
        StringBuilder code = new StringBuilder();
        String fieldName = field.getFieldName();
        String fieldType = types.convertType(field.getFieldType());
        
        // Debug prints to identify the issue
        System.out.println("DEBUG - Field name: '" + fieldName + "'");
        System.out.println("DEBUG - Field type: '" + fieldType + "'");
        System.out.println("DEBUG - Original field type: " + field.getFieldType());
        
        // Validate that we have both name and type
        if (fieldName == null || fieldName.trim().isEmpty()) {
            System.err.println("ERROR - Field name is null or empty");
            return "";
        }
        
        if (fieldType == null || fieldType.trim().isEmpty()) {
            System.err.println("ERROR - Field type is null or empty");
            return "";
        }
        
        // Generate the field declaration
        code.append(".field public ").append(fieldName).append(" ").append(fieldType).append(NL);
        
        System.out.println("DEBUG - Generated field: " + code.toString().trim());
        
        return code.toString();
    }

    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;

        var code = new StringBuilder();

        // calculate modifier
        //var stackSimulator = new StackSimulator(method);
        var modifier = types.getModifier(method.getMethodAccessModifier());
        if (method.isStaticMethod()) {
            modifier += "static ";
        }

        var methodName = method.getMethodName();

        String jasminReturnType = types.getReturnType(method);
        System.out.println("jasminReturnType: " + jasminReturnType);

        String jasminParamTypes = types.getParamType(method);
        System.out.println("jasminParamType: " + jasminParamTypes);


        // TODO: Hardcoded param types and return type, needs to be expanded
        /*var params = "I";
        var returnType = "I";

        code.append("\n.method ").append(modifier)
                .append(methodName)
                .append("(" + params + ")" + returnType).append(NL);
        */
        code.append("\n.method ").append(types.getModifier(method.getMethodAccessModifier()));
        if (method.isStaticMethod()) {
            code.append("static ");
        }
        code.append(method.getMethodName())
                .append("(").append(types.getParamType(method)).append(")")
                .append(types.getReturnType(method)).append(NL);

        var stackSimulator = new StackSimulator(method, types);
        code.append(TAB).append(".limit stack ").append(stackSimulator.getMaxStackSize()).append(NL);
        code.append(TAB).append(".limit locals ").append(stackSimulator.getMaxLocals()).append(NL);

        for (var inst : method.getInstructions()) {
            for (Map.Entry<String, Instruction> entry : method.getLabels().entrySet()) {
                if (entry.getValue() == inst) {
                    code.append(entry.getKey()).append(":").append(NL);
                    break;
                }
            }

            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            code.append(instCode);
        }

        code.append(".end method\n");
        currentMethod = null;
        return code.toString();
    }

    //esta funçao ta kinda caotica secalhar dá para melhorar !!!!!!!!!!!!!!!!!!!!!!
    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();
        
        if (assign.getDest() instanceof ArrayOperand) {
            ArrayOperand arrayOp = (ArrayOperand) assign.getDest();
                        
            String arrayName = arrayOp.getName();
            
            Descriptor descriptor = currentMethod.getVarTable().get(arrayName);
            
            int reg = descriptor.getVirtualReg();
            
            String loadInstr = types.getOptimizedLoad("[I", reg);
            code.append(loadInstr).append(NL);
            
            for (Element index : arrayOp.getIndexOperands()) {
                code.append(apply(index));
            }
            
            code.append(apply(assign.getRhs()));
            
            code.append("iastore").append(NL);
            
            return code.toString();
        }
    

        // Original code for optimization cases
        if (assign.getRhs() instanceof BinaryOpInstruction) {
            var binaryOp = (BinaryOpInstruction) assign.getRhs();
            if (binaryOp.getOperation().getOpType() == ADD) {
                var lhs = assign.getDest();
                if (lhs instanceof Operand) {
                    var destOperand = (Operand) lhs;
                    var destReg = currentMethod.getVarTable().get(destOperand.getName());

                    if (binaryOp.getLeftOperand() instanceof Operand) {
                        var leftOperand = (Operand) binaryOp.getLeftOperand();
                        var leftReg = currentMethod.getVarTable().get(leftOperand.getName());

                        if (binaryOp.getRightOperand() instanceof LiteralElement &&
                                destReg != null && leftReg != null &&
                                destReg.getVirtualReg() == leftReg.getVirtualReg()) {

                            var literal = (LiteralElement) binaryOp.getRightOperand();
                            try {
                                int increment = Integer.parseInt(literal.getLiteral());
                                if (increment >= -128 && increment <= 127) {
                                    code.append("iinc ").append(destReg.getVirtualReg()).append(" ").append(increment).append(NL);
                                    return code.toString();
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    }

                    if (binaryOp.getRightOperand() instanceof Operand) {
                        var rightOperand = (Operand) binaryOp.getRightOperand();
                        var rightReg = currentMethod.getVarTable().get(rightOperand.getName());

                        if (binaryOp.getLeftOperand() instanceof LiteralElement &&
                                destReg != null && rightReg != null &&
                                destReg.getVirtualReg() == rightReg.getVirtualReg()) {

                            var literal = (LiteralElement) binaryOp.getLeftOperand();
                            try {
                                int increment = Integer.parseInt(literal.getLiteral());
                                if (increment >= -128 && increment <= 127) {
                                    code.append("iinc ").append(destReg.getVirtualReg()).append(" ").append(increment).append(NL);
                                    return code.toString();
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
            }
        }

        if (assign.getRhs() instanceof SingleOpInstruction) {
            var singleOp = (SingleOpInstruction) assign.getRhs();
            if (singleOp.getSingleOperand() instanceof Operand) {
                var tempOperand = (Operand) singleOp.getSingleOperand();
                var lhs = assign.getDest();

                if (lhs instanceof Operand) {
                    var destOperand = (Operand) lhs;
                    var destReg = currentMethod.getVarTable().get(destOperand.getName());

                    for (var inst : currentMethod.getInstructions()) {
                        if (inst instanceof AssignInstruction) {
                            var tempAssign = (AssignInstruction) inst;
                            if (tempAssign.getDest() instanceof Operand) {
                                var tempDest = (Operand) tempAssign.getDest();
                                if (tempDest.getName().equals(tempOperand.getName()) &&
                                        tempAssign.getRhs() instanceof BinaryOpInstruction) {

                                    var binaryOp = (BinaryOpInstruction) tempAssign.getRhs();
                                    if (binaryOp.getOperation().getOpType() == ADD) {

                                        if (binaryOp.getLeftOperand() instanceof Operand) {
                                            var leftOperand = (Operand) binaryOp.getLeftOperand();
                                            if (leftOperand.getName().equals(destOperand.getName()) &&
                                                    binaryOp.getRightOperand() instanceof LiteralElement) {

                                                var literal = (LiteralElement) binaryOp.getRightOperand();
                                                try {
                                                    int increment = Integer.parseInt(literal.getLiteral());
                                                    if (increment >= -128 && increment <= 127) {
                                                        code.append("iinc ").append(destReg.getVirtualReg()).append(" ").append(increment).append(NL);
                                                        return code.toString();
                                                    }
                                                } catch (NumberFormatException e) {
                                                }
                                            }
                                        }

                                        if (binaryOp.getRightOperand() instanceof Operand) {
                                            var rightOperand = (Operand) binaryOp.getRightOperand();
                                            if (rightOperand.getName().equals(destOperand.getName()) &&
                                                    binaryOp.getLeftOperand() instanceof LiteralElement) {

                                                var literal = (LiteralElement) binaryOp.getLeftOperand();
                                                try {
                                                    int increment = Integer.parseInt(literal.getLiteral());
                                                    if (increment >= -128 && increment <= 127) {
                                                        code.append("iinc ").append(destReg.getVirtualReg()).append(" ").append(increment).append(NL);
                                                        return code.toString();
                                                    }
                                                } catch (NumberFormatException e) {
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        code.append(apply(assign.getRhs()));

        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        var reg = currentMethod.getVarTable().get(operand.getName());

        String jasminType = types.convertType(assign.getTypeOfAssign());
        System.out.println("jasminType: " + jasminType);

        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA " + assign.getRhs().getInstType().toString());
        System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB " + assign.getRhs().toString());

        // TODO: Hardcoded for int type, needs to be expanded
        code.append(types.getOptimizedStore(jasminType, reg.getVirtualReg())).append(NL);

        return code.toString();
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        return types.getOptimizedConstant(literal.getLiteral()) + NL;
    }

    private String generateOperand(Operand operand) {
        var operandName = operand.getName();
        var reg = currentMethod.getVarTable().get(operandName);

        if (reg == null) {
            var baseName = operandName.contains(".") ? operandName.substring(0, operandName.indexOf(".")) : operandName;
            reg = currentMethod.getVarTable().get(baseName);
        }

        if (reg == null) {
            throw new RuntimeException("Variable not found in symbol table: " + operandName +
                    " (tried both full name and base name). Available variables: " +
                    currentMethod.getVarTable().keySet());
        }

        String jasminType = types.convertType(operand.getType());
        return types.getOptimizedLoad(jasminType, reg.getVirtualReg()) + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        var opType = binaryOp.getOperation().getOpType();

        if (opType == LTH || opType == GTH || opType == LTE || opType == GTE || opType == EQ || opType == NEQ) {
            // Replace timestamp with incrementing counter
            var currentLabel = labelCounter++;
            var label1 = "label_" + currentLabel + "_true";
            var label2 = "label_" + currentLabel + "_end";

            String jumpInstruction = switch (opType) {
                case LTH -> "if_icmplt";
                case GTH -> "if_icmpgt";
                case LTE -> "if_icmple";
                case GTE -> "if_icmpge";
                case EQ -> "if_icmpeq";
                case NEQ -> "if_icmpne";
                default -> throw new NotImplementedException(opType);
            };

            code.append(jumpInstruction).append(" ").append(label1).append(NL);
            code.append("iconst_0").append(NL);
            code.append("goto ").append(label2).append(NL);
            code.append(label1).append(":").append(NL);
            code.append("iconst_1").append(NL);
            code.append(label2).append(":").append(NL);

            return code.toString();
        }

        var typePrefix = types.getTypePrefix(binaryOp.getLeftOperand().getType());

        var op = switch (opType) {
            case ADD -> "add";
            case SUB -> "sub";
            case MUL -> "mul";
            case DIV -> "div";
            case AND -> "and";
            case OR -> "or";
            case XOR -> "xor";
            case SHL -> "shl";
            case SHR -> "shr";
            case REM -> "rem";
            default -> throw new NotImplementedException(opType);
        };
        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();
    
        if (returnInst.hasReturnValue()) {
            code.append(apply(returnInst.getOperand().get()));
        } else {
            // If there's no explicit return value but we're in a non-void method,
            // we need to load and return the 'a' variable
            if ("I".equals(types.getReturnType(currentMethod))) {
                code.append("iload_1").append(NL);
            }
        }
    
        code.append(types.getReturnInstruction(currentMethod.getReturnType())).append(NL);
        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putField) {
        var code = new StringBuilder();
    
        System.out.println("DEBUG - PutField instruction: " + putField);
        System.out.println("DEBUG - Operands: " + putField.getOperands());
        System.out.println("DEBUG - Field: " + putField.getField());
        System.out.println("DEBUG - Field type: " + putField.getField().getType());
    
        code.append(apply(putField.getOperands().get(0)));
    
        if (putField.getOperands().size() > 2) {
            System.out.println("DEBUG - Using operand value: " + putField.getOperands().get(2));
            code.append(apply(putField.getOperands().get(2)));
        } else {
            code.append("bipush 10").append(NL);
        }
    
        var fieldName = putField.getField().getName();
        var fieldType = types.convertType(putField.getField().getType());
        var className = ollirResult.getOllirClass().getClassName();
    
        System.out.println("DEBUG - Generated putfield instruction: putfield " + className + "/" + fieldName + " " + fieldType);
    
        code.append("putfield ").append(className).append("/").append(fieldName)
                .append(" ").append(fieldType).append(NL);
    
        return code.toString();
    }

    private String generateGetField(GetFieldInstruction getField) {
        var code = new StringBuilder();

        code.append(apply(getField.getOperands().get(0)));

        var fieldName = getField.getField().getName();
        var fieldType = types.convertType(getField.getField().getType());
        var className = ollirResult.getOllirClass().getClassName();

        code.append("getfield ").append(className).append("/").append(fieldName)
                .append(" ").append(fieldType).append(NL);

        return code.toString();
    }

    private String generateNew(NewInstruction newInst) {
        var code = new StringBuilder();

        if (newInst.getReturnType() instanceof ArrayType) {
            var arrayType = (ArrayType) newInst.getReturnType();

            boolean foundSize = false;
            for (Element operand : newInst.getOperands()) {
                if (operand instanceof Operand && ((Operand) operand).getName().equals("array")) {
                    continue;
                }
                code.append(apply(operand));
                foundSize = true;
                break;
            }

            if (!foundSize) {
                throw new RuntimeException("Could not find array size in new instruction operands");
            }

            var elementType = arrayType.getElementType();
            String jasminElementType = types.convertType(elementType);

            if ("I".equals(jasminElementType)) {
                code.append("newarray int").append(NL);
            } else if ("Z".equals(jasminElementType)) {
                code.append("newarray boolean").append(NL);
            } else if ("C".equals(jasminElementType)) {
                code.append("newarray char").append(NL);
            } else if ("F".equals(jasminElementType)) {
                code.append("newarray float").append(NL);
            } else if ("D".equals(jasminElementType)) {
                code.append("newarray double").append(NL);
            } else if ("J".equals(jasminElementType)) {
                code.append("newarray long").append(NL);
            } else if ("B".equals(jasminElementType)) {
                code.append("newarray byte").append(NL);
            } else if ("S".equals(jasminElementType)) {
                code.append("newarray short").append(NL);
            } else {
                code.append("anewarray ").append(jasminElementType.replace("L", "").replace(";", "")).append(NL);
            }
        } else if (newInst.getReturnType() instanceof ClassType) {
            var classType = (ClassType) newInst.getReturnType();
            var className = classType.getName().replace(".", "/");

            code.append("new ").append(className).append(NL);
            code.append("dup").append(NL);
        } else {
            throw new RuntimeException("Unsupported type for new instruction: " + newInst.getReturnType().getClass());
        }

        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        if (invokeSpecial.getArguments().isEmpty()) {
            if (!invokeSpecial.getOperands().isEmpty()) {
                code.append(apply(invokeSpecial.getOperands().getFirst()));
            }

            String className = "java/lang/Object";
            
            if (invokeSpecial.getOperands().size() > 0) {
                Element firstOperand = invokeSpecial.getOperands().getFirst();
                if (firstOperand instanceof Operand) {
                    Operand operand = (Operand) firstOperand;
                    if (!operand.getName().equals("this")) {
                        Descriptor descriptor = currentMethod.getVarTable().get(operand.getName());
                        if (descriptor != null && descriptor.getVarType() instanceof ClassType) {
                            className = ((ClassType)descriptor.getVarType()).getName().replace(".", "/");
                        }
                        else if (operand.getType() instanceof ClassType) {
                            className = ((ClassType)operand.getType()).getName().replace(".", "/");
                        }
                    }
                }
            }

            code.append("invokespecial ").append(className).append("/<init>()V").append(NL);
            return code.toString();
        }

        code.append(apply(invokeSpecial.getArguments().getFirst()));

        for (Element arg : invokeSpecial.getOperands()) {
            code.append(apply(arg));
        }

        var methodName = ((LiteralElement) invokeSpecial.getArguments().get(1)).getLiteral().replace("\"", "");
        var className = invokeSpecial.getArguments().getFirst().getType();
        var jasminClassName = types.convertType(className).replace("L", "").replace(";", "");

        var paramTypes = new StringBuilder();
        for (Element operand : invokeSpecial.getOperands()) {
            paramTypes.append(types.convertType(operand.getType()));
        }

        var returnType = types.convertType(invokeSpecial.getReturnType());

        code.append("invokespecial ").append(jasminClassName).append("/").append(methodName)
                .append("(").append(paramTypes).append(")").append(returnType).append(NL);

        return code.toString();
    }

    private String generateOpCond(OpCondInstruction opCond) {
        var code = new StringBuilder();

        code.append(apply(opCond.getOperands().getFirst()));
        code.append(apply(opCond.getOperands().getLast()));

        var op = opCond.getCondition().getOperation().getOpType();
        String jumpInstruction = switch (op) {
            case LTH -> "if_icmplt";
            case GTH -> "if_icmpgt";
            case LTE -> "if_icmple";
            case GTE -> "if_icmpge";
            case EQ -> "if_icmpeq";
            case NEQ -> "if_icmpne";
            default -> throw new NotImplementedException(op);
        };

        var labels = opCond.getLabel();
        code.append(jumpInstruction).append(" ").append(labels).append(NL);

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInst) {
        return "goto " + gotoInst.getLabel() + NL;
    }


    private String generateInvokeStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        for (Element argument : invokeStatic.getArguments()) {
            if (!(argument instanceof LiteralElement && (argument.equals(invokeStatic.getCaller()) || argument.equals(invokeStatic.getMethodName())))) {
                code.append(apply(argument));
            }
        }

        String className;
        if (invokeStatic.getCaller() instanceof LiteralElement) {
            String baseClassName = ((LiteralElement) invokeStatic.getCaller()).getLiteral().replace("\"", "");
            className = resolveFullClassName(baseClassName);
        } else if (invokeStatic.getCaller() instanceof Operand) {
            String baseClassName = ((Operand) invokeStatic.getCaller()).getName();
            if (baseClassName.contains(".")) {
                baseClassName = baseClassName.substring(0, baseClassName.lastIndexOf("."));
            }
            className = resolveFullClassName(baseClassName);
        } else {
            className = invokeStatic.getCaller().toString();
            if (!className.contains("/")) {
                className = resolveFullClassName(className);
            }
        }

        var methodName = ((LiteralElement) invokeStatic.getMethodName()).getLiteral().replace("\"", "");

        var paramTypes = new StringBuilder();
        for (Element argument : invokeStatic.getArguments()) {
            paramTypes.append(types.convertType(argument.getType()));
        }

        var returnType = types.convertType(invokeStatic.getReturnType());

        code.append("invokestatic ").append(className).append("/").append(methodName)
                .append("(").append(paramTypes).append(")").append(returnType).append(NL);

        return code.toString();
    }

    private String generateSingleOpCond(SingleOpCondInstruction singleOpCond) {
        var code = new StringBuilder();

        var firstOperand = singleOpCond.getOperands().getFirst();

        if (firstOperand instanceof Operand) {
            var operand = (Operand) firstOperand;
            for (var inst : currentMethod.getInstructions()) {
                if (inst instanceof AssignInstruction) {
                    var assign = (AssignInstruction) inst;
                    if (assign.getDest() instanceof Operand) {
                        var dest = (Operand) assign.getDest();
                        if (dest.getName().equals(operand.getName()) &&
                                assign.getRhs() instanceof BinaryOpInstruction) {
                            var binaryOp = (BinaryOpInstruction) assign.getRhs();
                            var opType = binaryOp.getOperation().getOpType();

                            if ((opType == LTH || opType == GTH || opType == LTE ||
                                    opType == GTE || opType == EQ || opType == NEQ)) {

                                boolean isComparingWithZero = false;
                                Element nonZeroOperand = null;

                                if (binaryOp.getRightOperand() instanceof LiteralElement) {
                                    var literal = (LiteralElement) binaryOp.getRightOperand();
                                    if ("0".equals(literal.getLiteral())) {
                                        isComparingWithZero = true;
                                        nonZeroOperand = binaryOp.getLeftOperand();
                                    }
                                } else if (binaryOp.getLeftOperand() instanceof LiteralElement) {
                                    var literal = (LiteralElement) binaryOp.getLeftOperand();
                                    if ("0".equals(literal.getLiteral())) {
                                        isComparingWithZero = true;
                                        nonZeroOperand = binaryOp.getRightOperand();
                                        opType = switch (opType) {
                                            case LTH -> GTH;
                                            case GTH -> LTH;
                                            case LTE -> GTE;
                                            case GTE -> LTE;
                                            default -> opType;
                                        };
                                    }
                                }

                                if (isComparingWithZero && nonZeroOperand != null) {
                                    code.append(apply(nonZeroOperand));
                                    String jumpInstruction = switch (opType) {
                                        case LTH -> "iflt";
                                        case GTH -> "ifgt";
                                        case LTE -> "ifle";
                                        case GTE -> "ifge";
                                        case EQ -> "ifeq";
                                        case NEQ -> "ifne";
                                        default -> throw new NotImplementedException(opType);
                                    };

                                    code.append(jumpInstruction).append(" ").append(singleOpCond.getLabel()).append(NL);
                                    return code.toString();
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        code.append(apply(singleOpCond.getOperands().getFirst()));
        code.append("ifne ").append(singleOpCond.getLabel()).append(NL);

        return code.toString();
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLength) {
        var code = new StringBuilder();

        code.append(apply(arrayLength.getOperands().getFirst()));

        code.append("arraylength").append(NL);

        return code.toString();
    }

    private String generateInvokeVirtual(InvokeVirtualInstruction invokeVirtual) {
        var code = new StringBuilder();

        code.append(apply(invokeVirtual.getCaller()));

        for (Element arg : invokeVirtual.getArguments()) {
            code.append(apply(arg));
        }

        var className = types.convertType(invokeVirtual.getCaller().getType())
                .replace("L", "")
                .replace(";", "");

        var methodName = ((LiteralElement) invokeVirtual.getMethodName()).getLiteral().replace("\"", "");

        var paramTypes = new StringBuilder();
        for (Element arg : invokeVirtual.getArguments()) {
            paramTypes.append(types.convertType(arg.getType()));
        }

        var returnType = types.convertType(invokeVirtual.getReturnType());

        code.append("invokevirtual ").append(className).append("/").append(methodName)
                .append("(").append(paramTypes).append(")").append(returnType).append(NL);

        if (!returnType.equals("V")) {
            boolean isValueUsed = false;
            
            for (Instruction inst : currentMethod.getInstructions()) {
                if (inst instanceof AssignInstruction) {
                    AssignInstruction assign = (AssignInstruction) inst;
                    if (assign.getRhs() == invokeVirtual) {
                        isValueUsed = true;
                        break;
                    }
                }
            }
            
            if (!isValueUsed) {
                code.append("pop").append(NL);
            }
        }

        return code.toString();
    }
    private String generateUnaryOp(UnaryOpInstruction unaryOp) {
        var code = new StringBuilder();

        code.append(apply(unaryOp.getOperand()));

        var opType = unaryOp.getOperation().getOpType();

        switch (opType) {
            case NOT:
            case NOTB:
                code.append("iconst_1").append(NL);
                code.append("ixor").append(NL);
                break;
            default:
                throw new NotImplementedException("Unary operation not yet implemented: " + opType);
        }

        return code.toString();
    }

    private String generateArrayOperand(ArrayOperand arrayOp) {
        var code = new StringBuilder();
        
        String arrayName = arrayOp.getName();
        
        Descriptor descriptor = currentMethod.getVarTable().get(arrayName);
        
        int reg = descriptor.getVirtualReg();
        
        String arrayType = types.convertType(arrayOp.getType());
        
        String loadInstr = types.getOptimizedLoad("[" + arrayType, reg);
        code.append(loadInstr).append(NL);
        
        for (Element index : arrayOp.getIndexOperands()) {
            code.append(apply(index));
        }
        
        code.append("iaload").append(NL);
        
        return code.toString();
    }

    private String resolveFullClassName(String baseClassName) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        
        for (String importStr : classUnit.getImports()) {
            if (importStr.endsWith("." + baseClassName) || importStr.equals(baseClassName)) {
                return importStr.replace(".", "/");
            }
        }
        
        return baseClassName;
    }
}