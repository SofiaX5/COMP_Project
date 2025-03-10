package pt.up.fe.comp2025.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static Type newIntType() {
        return new Type("int", false);
    }

//VISTO ?
    public static Type convertType(JmmNode typeNode) {
        var kind = typeNode.getKind();
        boolean isArray = Boolean.parseBoolean(typeNode.getOptional("isArray").orElse("false"));

        switch (kind) {
            case "ArrayType":
            case "VarargType":
                var baseType = typeNode.get("name");
                return new Type(baseType, true);

            case "BoolType":
                return new Type("boolean", isArray);

            case "IntType":
                return new Type("int", isArray);

            case "StringType":
                return new Type("String", isArray);

            case "NameType":
                var name = typeNode.get("name");
                return new Type(name, isArray);
        }
        return new Type("undefined", isArray);
    }


    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    //VISTO?
    public Type getExprType(JmmNode expr) {
        var kind = expr.getKind();
        switch (kind) {
            case "NotExpr" :
            case "BooleanLiteral" :
                return new Type("boolean", false);

            case "BinaryExpr":
                var op = expr.get("op");
                if (op.equals("&&") || op.equals("<")) { return new Type("boolean", false);}
                else if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {return new Type("int", false);}

            case "ArrayElemExpr" :
                return getExprType (expr.getChild(0));

            case "IntegerLiteral":
            case "LengthExpr" :
                return new Type("int", false);

            case "MethodCallExpr" :

            case "ParenthesisEpr" :
                return getExprType (expr.getChild(0));

            case "ArrayExpr" :
                return new Type("int", true);


            case "VarRefExpr" :
                /////////////////////////////????????????
            case "ThisExpr" :
                /// /////////////////////////////////////?



        }
        // TODO: Update when there are new types
        return new Type("int", false);
    }


}
