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

        return switch (kind) {
            case "ArrayType", "VarargType" ->
                //var baseType = typeNode.get("name");
                    new Type("int", true);
            case "BoolType" -> new Type("boolean", isArray);
            case "IntType" -> new Type("int", isArray);
            case "StringType" -> new Type("String", isArray);
            case "NameType" -> {
                var name = typeNode.get("name");
                yield new Type(name, isArray);
            }
            default -> new Type("undefined", isArray);
        };
    }


    /**
     * Gets the {@link Type} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    //VISTO????????????? temos de ver isto melhor provavelmente, adicionei a symbomtable como param
    public static Type getExprType(JmmNode expr, SymbolTable symbolTable) {
        var kind = expr.getKind();
        switch (kind) {
            case "NotExpr":
            case "BooleanLiteral":
                return new Type("boolean", false);

            case "BinaryExpr":
                var op = expr.get("op");
                if (op.equals("&&") || op.equals("<")) { return new Type("boolean", false); }
                else if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) { return new Type("int", false); }
                break;

            case "ArrayElemExpr":
                JmmNode arrayExpr = expr.getChild(0);
                Type arrayType = getExprType(arrayExpr, symbolTable);
                if (arrayType.isArray()) {
                    return new Type(arrayType.getName(), false);
                }
                return arrayType;

            case "IntegerLiteral":
            case "LengthExpr":
                return new Type("int", false);

            case "MethodCallExpr":
                JmmNode objectExpr = expr.getChild(0);
                String methodName = expr.get("name");
                Type objectType = getExprType(objectExpr, symbolTable);

                if (symbolTable.getMethods().contains(methodName)) {
                    return symbolTable.getReturnType(methodName);
                }
                return new Type("int", false); // Default fallback

            case "ParenthesizesExpr":
                return getExprType(expr.getChild(0), symbolTable);

            case "ArrayExpr":
                if (expr.getNumChildren() > 0) {
                    Type elemType = getExprType(expr.getChild(0), symbolTable);
                    return new Type(elemType.getName(), true);
                }
                return new Type("int", true);

            case "VarRefExpr":
                String varName = expr.get("name");

                JmmNode current = expr;
                String currentMethod = null;
                while (current.getParent() != null) {
                    current = current.getParent();
                    if (current.getKind().equals("MethodDecl") || current.getKind().equals(Kind.METHOD_DECL.toString())) {
                        currentMethod = current.get("name");
                        break;
                    }
                }

                if (currentMethod != null) {
                    for (var variable : symbolTable.getLocalVariables(currentMethod)) {
                        if (variable.getName().equals(varName)) {
                            return variable.getType();
                        }
                    }

                    for (var param : symbolTable.getParameters(currentMethod)) {
                        if (param.getName().equals(varName)) {
                            JmmNode ancestor = expr;
                            boolean isVararg = false;
                            while (ancestor != null) {
                                if (ancestor.getKind().equals("VarargType")) {
                                    isVararg = true;
                                    break;
                                }
                                ancestor = ancestor.getParent();
                            }

                            if (isVararg) {
                                return new Type(param.getType().getName(), true);
                            }
                            return param.getType();
                        }
                    }
                }
                for (var field : symbolTable.getFields()) {
                    if (field.getName().equals(varName)) {
                        return field.getType();
                    }
                }

                return new Type("int", false);

            case "ThisExpr":
                return new Type(symbolTable.getClassName(), false);

            case "NewArrayExpr":
                return new Type("int", true);

            case "NewObjectExpr":
                return new Type(expr.get("name"), false);
        }
        return new Type("int", false);
    }


}
