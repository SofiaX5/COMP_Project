package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp2025.ast.Kind.TYPE;
import static pt.up.fe.comp2025.ast.TypeUtils.convertType;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {


    private final AccumulatorMap<String> temporaries;

    private final TypeUtils types;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
    }


    public String nextTemp() {

        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {

        // Subtract 1 because the base is 1
        var nextTempNum = temporaries.add(prefix) - 1;

        return prefix + nextTempNum;
    }


    public static String toOllirType(JmmNode typeNode) {
        TYPE.checkOrThrow(typeNode);
        Type type = convertType(typeNode);
        return toOllirType(type.getName(), type.isArray());
    }

    public String toOllirType(Type type) {
        return toOllirType(type.getName(), type.isArray());
    }

    private static String toOllirType(String typeName, boolean isArray) {
        String baseType = switch (typeName) {
            case "boolean" -> "bool";
            case "int"     -> "i32";
            case "void"    -> "V";
            case "String"  -> "String";
            default        -> typeName;
        };
        return (isArray ? ".array" : "") + "." + baseType;
    }
}
