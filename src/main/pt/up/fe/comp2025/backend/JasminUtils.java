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
}
