package pt.up.fe.comp2025.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.Instruction;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JasminUtils {

    private final OllirResult ollirResult;
    private final HashMap<String, String> typeMap = new HashMap<>();

    public JasminUtils(OllirResult ollirResult) {
        // Can be useful to have if you expand this class with more methods
        this.ollirResult = ollirResult;
        this.typeMap.put("int32", "I");
        this.typeMap.put("void", "V");
        this.typeMap.put("boolean", "Z");
        this.typeMap.put("char", "C");
        //this.typeMap.put("[", "[?????");
    }

    public String getReturnType(Method method) {
        String type = convertType(method.getReturnType());
        System.out.println("Method Return Type: "+ type);
        return type;
    }
    public String getParamType(Method method) {
        StringBuilder types = new StringBuilder();
        for (Element param : method.getParams()) {
            String type = convertType(param.getType());
            types.append(type);
            System.out.println("Param Type: "+ type);
        }
        return types.toString();
    }

    public String getAssignType(AssignInstruction instruction) {
        String type = convertType(instruction.getTypeOfAssign());
        System.out.println("Instruction Type: "+type);
        return type;
    }

    //V-Void
    //B-Byte
    //C-char
    //D-double
    //F-float
    //I-int
    //J-long
    //L ClassName ;-reference (instance da class)
    //S-short
    //Z-boolean
    //[-reference (array)

    public String convertType(Type type) {
        if (type instanceof ArrayType) {
            return "[" + convertType(((ArrayType) type).getElementType());
        } else if (type instanceof ClassType) {
            return "L" + ((ClassType) type).getName().replace(".", "/") + ";";
        } else {
            String typeStr = type.toString().toLowerCase();
            return typeMap.getOrDefault(typeStr, "Ljava/lang/Object;");
        }
    }


    public String getModifier(AccessModifier accessModifier) {
        return accessModifier != AccessModifier.DEFAULT ?
                accessModifier.name().toLowerCase() + " " :
                "";
    }

    //jasmin optim.

    public String getOptimizedLoad(String type, int register) {
        if ("I".equals(type) || "Z".equals(type)) {
            return switch (register) {
                case 0 -> "iload_0";
                case 1 -> "iload_1";
                case 2 -> "iload_2";
                case 3 -> "iload_3";
                default -> "iload " + register;
            };
        } else if (type.startsWith("L") || type.startsWith("[")) {
            return switch (register) {
                case 0 -> "aload_0";
                case 1 -> "aload_1";
                case 2 -> "aload_2";
                case 3 -> "aload_3";
                default -> "aload " + register;
            };
        }
        return "iload " + register;
    }

    public String getOptimizedStore(String type, int register) {
        if ("I".equals(type) || "Z".equals(type)) {
            return switch (register) {
                case 0 -> "istore_0";
                case 1 -> "istore_1";
                case 2 -> "istore_2";
                case 3 -> "istore_3";
                default -> "istore " + register;
            };
        } else if (type.startsWith("L") || type.startsWith("[")) {
            return switch (register) {
                case 0 -> "astore_0";
                case 1 -> "astore_1";
                case 2 -> "astore_2";
                case 3 -> "astore_3";
                default -> "astore " + register;
            };
        }
        return "istore " + register;
    }

    public String getOptimizedConstant(String literal) {
        try {
            int value = Integer.parseInt(literal);
            return switch (value) {
                case -1 -> "iconst_m1";
                case 0 -> "iconst_0";
                case 1 -> "iconst_1";
                case 2 -> "iconst_2";
                case 3 -> "iconst_3";
                case 4 -> "iconst_4";
                case 5 -> "iconst_5";
                default -> {
                    if (value >= -128 && value <= 127) {
                        yield "bipush " + value;
                    } else if (value >= -32768 && value <= 32767) {
                        yield "sipush " + value;
                    } else {
                        yield "ldc " + value;
                    }
                }
            };
        } catch (NumberFormatException e) {
            return "ldc " + literal;
        }
    }


    public String getTypePrefix(Type type) {
        String jasminType = convertType(type);
        if ("I".equals(jasminType) || "Z".equals(jasminType)) {
            return "i";
        } else if ("F".equals(jasminType)) {
            return "f";
        } else if ("D".equals(jasminType)) {
            return "d";
        } else if ("J".equals(jasminType)) {
            return "l";
        } else if (jasminType.startsWith("L") || jasminType.startsWith("[")) {
            return "a";
        }
        return "i";
    }

    public String getReturnInstruction(Type returnType) {
        String jasminType = convertType(returnType);
        if ("V".equals(jasminType)) {
            return "return";
        } else if ("I".equals(jasminType) || "Z".equals(jasminType)) {
            return "ireturn";
        } else if ("F".equals(jasminType)) {
            return "freturn";
        } else if ("D".equals(jasminType)) {
            return "dreturn";
        } else if ("J".equals(jasminType)) {
            return "lreturn";
        } else if (jasminType.startsWith("L") || jasminType.startsWith("[")) {
            return "areturn";
        }
        return "return";
    }


}
