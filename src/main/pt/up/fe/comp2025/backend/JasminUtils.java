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
        String type = (method.getReturnType()).toString().toLowerCase();
        System.out.println("Method Return Type: "+type);
        return this.typeMap.get(type);
    }
    public String getParamType(Method method) {
        ArrayList params = method.getParams();
        String types = "";
        for (int i = 0; i < params.size(); i++) {
            String type = params.get(i).toString().toLowerCase();
            if (type.contains(".")) {
                type = type.substring(type.lastIndexOf('.') + 1);
            }
            System.out.println("Method Param Type: "+type);
            types += this.typeMap.get(type);
        }
        return types;
    }

    public String getAssignType(AssignInstruction instruction) {
        String type = (instruction.getTypeOfAssign()).toString().toLowerCase();
        System.out.println("Instruction Type: "+type);
        return this.typeMap.get(type).toLowerCase();
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



    public String getModifier(AccessModifier accessModifier) {
        return accessModifier != AccessModifier.DEFAULT ?
                accessModifier.name().toLowerCase() + " " :
                "";
    }
}
