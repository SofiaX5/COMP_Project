package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // generate class name
        var className = classUnit.getClassName();
        code.append(".class public ").append(className).append(NL);        System.out.println("CCCCCCCCCCCCCCCCC");
        // TODO: When you support 'extends', this must be updated
        var fullSuperClass = "java/lang/Object";

        if (classUnit.getSuperClass() != null) {
            fullSuperClass = classUnit.getSuperClass().replace(".", "/");
        }

        code.append(".super ").append(fullSuperClass).append(NL).append(NL);

        for (var field : classUnit.getFields()) {
            code.append(generateField(field));
        }

        boolean hasConstructor = classUnit.getMethods().stream()
                .anyMatch(Method::isConstructMethod);

        // generate a single constructor method
        if (!hasConstructor) {
            var defaultConstructor = """
                    ;default constructor
                    .method public <init>()V
                        aload_0
                        invokespecial %s/<init>()V
                        return
                    .end method
                    """.formatted(fullSuperClass);
            code.append(defaultConstructor);
            }


        // generate code for all other methods
        for (var method : ollirResult.getOllirClass().getMethods()) {

            // Ignore constructor, since there is always one constructor
            // that receives no arguments, and has been already added
            // previously
            if (method.isConstructMethod()) {
                continue;
            }

            code.append(apply(method));
        }
        return code.toString();
    }


    private String generateField(Field field) {
        var code = new StringBuilder();
        var modifier = types.getModifier(field.getFieldAccessModifier());
        var fieldType = types.convertType(field.getFieldType());

        code.append(".field ").append(modifier)
                .append(field.getFieldName()).append(" ")
                .append(fieldType).append(NL);

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
        code.append("\n.method ").append(modifier)
                .append(methodName)
                .append("(" + jasminParamTypes + ")" + jasminReturnType).append(NL);

        var stackSimulator = new StackSimulator(method, types);
        code.append(TAB).append(".limit stack ").append(stackSimulator.getMaxStackSize()).append(NL);
        code.append(TAB).append(".limit locals ").append(stackSimulator.getMaxLocals()).append(NL);

        for (var inst : method.getInstructions()) {
            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            code.append(instCode);
        }

        code.append(".end method\n");

        // unset method
        currentMethod = null;
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        var code = new StringBuilder();

        code.append(apply(assign.getRhs()));

        var lhs = assign.getDest();

        if (!(lhs instanceof Operand)) {
            throw new NotImplementedException(lhs.getClass());
        }

        var operand = (Operand) lhs;

        var reg = currentMethod.getVarTable().get(operand.getName());


        String jasminType = types.convertType(assign.getTypeOfAssign());        System.out.println("jasminType: " + jasminType);

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
        var reg = currentMethod.getVarTable().get(operand.getName());
        String jasminType = types.convertType(operand.getType());

        return types.getOptimizedLoad(jasminType, reg.getVirtualReg()) + NL;
    }

    private String generateBinaryOp(BinaryOpInstruction binaryOp) {
        var code = new StringBuilder();

        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        // TODO: Hardcoded for int type, needs to be expanded
        var typePrefix = types.getTypePrefix(binaryOp.getLeftOperand().getType());

        var op = switch (binaryOp.getOperation().getOpType()) {
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
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };
        code.append(typePrefix + op).append(NL);

        return code.toString();
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        if (returnInst.hasReturnValue()) {
            code.append(apply(returnInst.getOperand().get()));
        }
        // TODO: Hardcoded for int type, needs to be expanded

        code.append(types.getReturnInstruction(currentMethod.getReturnType())).append(NL);
        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putField) {
        var code = new StringBuilder();

        code.append(apply(putField.getOperands().get(0)));

        code.append(apply(putField.getOperands().get(1)));

        var field = (Operand) putField.getOperands().get(0);
        var fieldName = putField.getField().getName();
        var fieldType = types.convertType(putField.getField().getType());
        var className = ollirResult.getOllirClass().getClassName();

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

        var classType = (ClassType) newInst.getReturnType();
        var className = classType.getName().replace(".", "/");

        code.append("new ").append(className).append(NL);
        code.append("dup").append(NL);

        return code.toString();
    }
    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        if (invokeSpecial.getArguments().isEmpty()) {
            if (!invokeSpecial.getOperands().isEmpty()) {
                code.append(apply(invokeSpecial.getOperands().getFirst()));
            }

            var className = "java/lang/Object";
            if (invokeSpecial.getReturnType() instanceof org.specs.comp.ollir.type.ClassType) {
                var classType = (org.specs.comp.ollir.type.ClassType) invokeSpecial.getReturnType();
                className = classType.getName().replace(".", "/");
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
}