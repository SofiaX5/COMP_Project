package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Constant Propagation Visitor.
 * Propagates known constant values across assignments and expressions.
 */
public class ConstPropVisitor extends AJmmVisitor<Map<String, JmmNode>, Boolean> {

    private final Map<String, JmmNode> constTable = new HashMap<>();
    private final Map<String, Boolean> constUsed = new HashMap<>();

    @Override
    protected void buildVisitor() {
        //addVisit(METHOD_DECL, this::visitMethod);
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        setDefaultVisit(this::defaultVisit);
    }

    public Boolean visit(JmmNode root) {
        System.out.println(root.toTree());
        boolean a = visit(root, new HashMap<>());
        System.out.println("final" + root.toTree());
        return a;
    }

    /*
    private Boolean visitMethod(JmmNode method, Map<String, JmmNode> context) {
        System.out.println("METHOD");
        boolean changed = false;
        for (JmmNode stmt : method.getChildren(ASSIGN_STMT)) {
            changed |= visit(stmt, context);
        }

        for (JmmNode expr : method.getChildren(EXPR)) {
            changed |= visit(expr, context);
        }

        System.out.println("Context: " + context);

        return changed;
    }
     */

    private Boolean visitAssignment(JmmNode assignment, Map<String, JmmNode> context) {
        System.out.println("ASSIGN");
        JmmNode lhs = assignment.getChild(0);
        JmmNode rhs = assignment.getChild(1);
        System.out.println("SOCORRO" + lhs.getKind() + "   -   " + rhs.getKind());

        //visit(rhs, context);
        if (!lhs.getKind().equals("VarRefExpr") || !lhs.getAttributes().contains("name")) {
            return false;
        }

        String varName = lhs.get("name");
        if (rhs.getKind().equals(INTEGER_LITERAL.toString()) || rhs.getKind().equals(BOOLEAN_LITERAL.toString())) {
            JmmNode newNode = new JmmNodeImpl(Collections.singletonList(rhs.getKind()));
            newNode.put("value", String.valueOf(rhs.get("value")));
            System.out.println("ODEIO COMPILADORES" + newNode);
            context.put(varName, newNode);

            System.out.println("Propagating constant: " + varName + " = " + rhs.get("value"));
            return true;
        } /*else {
            context.remove(varName);
        }*/

        return false;
    }

    private Boolean visitVarRef(JmmNode varRef, Map<String, JmmNode> context) {
        System.out.println("VAR_REF");
        String varName = varRef.get("name");

        if (context.containsKey(varName)) {
            JmmNode newNode = context.get(varName);
            varRef.replace(newNode);
            System.out.println("VAR: Propagating constant: " + varName + " = " + newNode);
            //constUsed.put(varName, true);

            return true;
        }

        return false;
    }

    private Boolean visitIfStmt(JmmNode varRef, Map<String, JmmNode> context) {
        return false;
    }

    private Boolean visitWhileStmt(JmmNode varRef, Map<String, JmmNode> context) {
        return false;
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean changed = false;
        for (var child : node.getChildren()) {
            changed |= visit(child, context);
        }
        return changed;
    }
}
