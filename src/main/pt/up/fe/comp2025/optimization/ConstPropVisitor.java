package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Constant Propagation Visitor.
 * Propagates known constant values across assignments and expressions.
 */
public class ConstPropVisitor extends PostorderJmmVisitor<Map<String, String>, Boolean> {

    private final Map<String, String> constTable = new HashMap<>();
    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit("Assignment", this::visitAssignment);
        addVisit("VarUse", this::visitVarUse);
        addVisit("BinaryExpr", this::visitBinaryExpr);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean defaultVisit(JmmNode node, Map<String, String> context) {
        for (var child : node.getChildren()) {
            visit(child, context);
        }
        return changed;
    }

    private Boolean visitAssignment(JmmNode assignment, Map<String, String> context) {
        String varName = assignment.get("var");
        JmmNode rhs = assignment.getChild(0);

        visit(rhs, context);

        // Check if the right-hand side is now a constant
        if (rhs.getKind().equals("IntegerLiteral") || rhs.getKind().equals("BooleanLiteral")) {
            context.put(varName, rhs.get("value")); // Update context with constant value
            System.out.println("Propagating constant: " + varName + " = " + rhs.get("value"));
        } else {
            context.remove(varName); // Variable now has unknown value
        }

        return changed;
    }

    private Boolean visitVarUse(JmmNode varUse, Map<String, String> context) {
        String varName = varUse.get("name");

        if (context.containsKey(varName)) {
            // Replace VarUse with IntegerLiteral or BooleanLiteral
            String value = context.get(varName);

            boolean isBoolean = value.equals("1") || value.equals("0");

            varUse.put("kind", isBoolean ? "BooleanLiteral" : "IntegerLiteral");
            varUse.put("value", value);
            System.out.println("Replacing variable use: " + varName + " -> " + value);
            changed = true;
        }

        return changed;
    }

    private Boolean visitBinaryExpr(JmmNode binaryExpr, Map<String, String> context) {
        JmmNode left = binaryExpr.getChild(0);
        JmmNode right = binaryExpr.getChild(1);

        visit(left, context);
        visit(right, context);

        // Se os dois lados são IntegerLiteral, podemos calcular já o valor
        if (left.getKind().equals("IntegerLiteral") && right.getKind().equals("IntegerLiteral")) {
            int leftVal = Integer.parseInt(left.get("value"));
            int rightVal = Integer.parseInt(right.get("value"));
            String op = binaryExpr.get("op");

            int result;
            switch (op) {
                case "+":
                    result = leftVal + rightVal;
                    break;
                case "-":
                    result = leftVal - rightVal;
                    break;
                case "*":
                    result = leftVal * rightVal;
                    break;
                case "/":
                    if (rightVal != 0) result = leftVal / rightVal;
                    else return changed; // Division by zero -> don't optimize
                    break;
                default:
                    return changed; // Unknown operator
            }

            // Replace BinaryExpr node by a literal
            binaryExpr.put("kind", "IntegerLiteral");
            binaryExpr.put("value", String.valueOf(result));
            binaryExpr.getChildren().clear(); // Remove old children
            changed = true;
        }

        return changed;
    }

    public Boolean visit(JmmNode root) {
        changed = false;
        visit(root, new HashMap<>());
        return changed;
    }
}
