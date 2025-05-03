package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ConstFoldVisitor extends PostorderJmmVisitor<Map<String, JmmNode>, Boolean> {

    @Override
    protected void buildVisitor() {
        addVisit(Kind.BINARY_EXPR, this::visitBinaryOp);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean changed = false;
        for (var child : node.getChildren()) {
            changed |= visit(child, context);
        }
        return changed;
    }

    private Boolean visitBinaryOp(JmmNode binOp, Map<String, JmmNode> context) {
        JmmNode left = binOp.getChild(0);
        JmmNode right = binOp.getChild(1);

        visit(left, context);
        visit(right, context);

        JmmNode newNode;

        if (left.getKind().equals("IntegerLiteral") && right.getKind().equals("IntegerLiteral")) {
            String op = binOp.get("op");

            if (Objects.equals(op, "+") || Objects.equals(op, "-") || Objects.equals(op, "*") || Objects.equals(op, "/")) {
                int leftVal = Integer.parseInt(left.get("value"));
                int rightVal = Integer.parseInt(right.get("value"));
                int result;

                switch (op) {
                    case "+" -> result = leftVal + rightVal;
                    case "-" -> result = leftVal - rightVal;
                    case "*" -> result = leftVal * rightVal;
                    case "/" -> {
                        if (rightVal == 0) return false; // Avoid division by zero
                        result = leftVal / rightVal;
                    }
                    default -> {
                        return false; // Unsupported operation
                    }
                }
                newNode = new JmmNodeImpl(Collections.singletonList(Kind.INTEGER_LITERAL.toString()));
                newNode.putObject("value", String.valueOf(result));
            }
            else if (Objects.equals(op, "<")) {
                int leftVal = Integer.parseInt(left.get("value"));
                int rightVal = Integer.parseInt(right.get("value"));

                boolean result = leftVal < rightVal;
                newNode = new JmmNodeImpl(Collections.singletonList(Kind.BOOLEAN_LITERAL.toString()));
                newNode.putObject("value", String.valueOf(result));
            }
            else if (Objects.equals(op, "&&")) {
                boolean leftVal = Boolean.parseBoolean(left.get("value"));
                boolean rightVal = Boolean.parseBoolean(right.get("value"));

                boolean result = leftVal && rightVal;
                newNode = new JmmNodeImpl(Collections.singletonList(Kind.BOOLEAN_LITERAL.toString()));
                newNode.putObject("value", String.valueOf(result));
            }
            else {
                return false;
            }

            // Replace BinaryOp node with a constant literal
            binOp.replace(newNode);
            //System.out.println("Folding constant expression: " + leftVal + " " + op + " " + rightVal + " -> " + result);
           return true;
        }

        return false;
    }
}
