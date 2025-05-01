package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.ASSIGN_STMT;
import static pt.up.fe.comp2025.ast.Kind.VAR_REF_EXPR;

/**
 * Constant Propagation Visitor.
 * Propagates known constant values across assignments and expressions.
 */
public class ConstPropVisitor extends PostorderJmmVisitor<Map<String, JmmNode>, Boolean> {

    private final Map<String, JmmNode> constTable = new HashMap<>();

    @Override
    protected void buildVisitor() {
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        setDefaultVisit(this::defaultVisit);
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean changed = false;
        for (var child : node.getChildren()) {
            changed |= visit(child, context);
        }
        return changed;
    }

    private Boolean visitAssignment(JmmNode assignment, Map<String, JmmNode> context) {
        JmmNode lhs = assignment.getChild(0);
        JmmNode rhs = assignment.getChild(1);

        if (!lhs.getKind().equals("VarRefExpr") || !lhs.getAttributes().contains("name")) {
            visit(rhs, context);
            return false;
        }

        String varName = lhs.get("name");
        visit(rhs, context);

        System.out.println("SOCORRO" + lhs.getKind() + "   -   " + rhs.getKind());

        if (rhs.getKind().equals("IntegerLiteral") || rhs.getKind().equals("BooleanLiteral")) {
            context.put(varName, rhs);
            System.out.println("Propagating constant: " + varName + " = " + rhs.get("value"));
            return true;
        } else {
            context.remove(varName);
        }

        return false;
    }

    private Boolean visitVarRef(JmmNode varRef, Map<String, JmmNode> context) {
        String varName = varRef.get("name");

        if (context.containsKey(varName)) {
            JmmNode constantNode = context.get(varName);
            varRef.replace(constantNode);
            return true;
        }

        return false;
    }

    public Boolean visit(JmmNode root) {
        return visit(root, new HashMap<>());
    }
}
