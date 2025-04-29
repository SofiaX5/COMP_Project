package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Arrays;
import java.util.Map;

public class ConstFoldVisitor extends PostorderJmmVisitor<Map<String, String>, Boolean> {

    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(Arrays.asList("BinaryOp"), this::visitBinaryOp);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean defaultVisit(JmmNode node, Map<String, String> context) {
        for (var child : node.getChildren()) {
            visit(child, context);
        }
        return changed;
    }

    private Boolean visitBinaryOp(JmmNode binOp, Map<String, String> context) {
        JmmNode left = binOp.getChild(0);
        JmmNode right = binOp.getChild(1);

        visit(left, context);
        visit(right, context);

        if (left.getKind().equals("IntegerLiteral") && right.getKind().equals("IntegerLiteral")) {
            int leftVal = Integer.parseInt(left.get("value"));
            int rightVal = Integer.parseInt(right.get("value"));
            int result;

            String op = binOp.get("op");

            switch (op) {
                case "+" -> result = leftVal + rightVal;
                case "-" -> result = leftVal - rightVal;
                case "*" -> result = leftVal * rightVal;
                case "/" -> {
                    if (rightVal == 0) return changed; // Avoid division by zero
                    result = leftVal / rightVal;
                }
                default -> {
                    return changed; // Unsupported operation
                }
            }

            // Replace BinaryOp node with a constant literal
            binOp.put("kind", "IntegerLiteral");
            binOp.put("value", String.valueOf(result));
            binOp.getChildren().clear(); // Remove old children
            System.out.println("Folding constant expression: " + leftVal + " " + op + " " + rightVal + " -> " + result);
            changed = true;
        }

        return changed;
    }
}
