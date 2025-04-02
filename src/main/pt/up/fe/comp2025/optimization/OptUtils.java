package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import static pt.up.fe.comp2025.ast.Kind.TYPE;

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
        String typeName = typeNode.get("name");
        TYPE.checkOrThrow(typeNode);

        return toOllirType(typeNode);
    }

    public String toOllirType(Type type) {
        return toOllirType(type.getName());
    }

    private static String toOllirType(String typeName) {
        boolean isArray = typeName.endsWith("[]");

        String baseType = switch (isArray ? typeName.substring(0, typeName.length() - 2) : typeName) {
            case "boolean" -> "bool";
            case "int" -> "i32";
            case "void" -> "V";
            case "String" -> "String";
            default -> typeName;
        };

        return (isArray ? ".array" : "") + "." + baseType;
    }


}
