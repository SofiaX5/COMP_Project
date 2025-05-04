package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.*;

import static pt.up.fe.comp2025.ast.Kind.*;


public class ConstPropVisitor extends AJmmVisitor<Map<String, JmmNode>, Boolean> {

    private final Map<String, JmmNode> constTable = new HashMap<>();
    private final Set<String> loopModifiedVars = new HashSet<>();
    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(METHOD_DECL, this::visitMethod);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        setDefaultVisit(this::defaultVisit);
    }


    public Boolean visit(JmmNode root) {
        constTable.clear();
        loopModifiedVars.clear();
        changed = false;

        identifyLoopModifiedVars(root);

        collectConstants(root);

        System.out.println("Collected constants: " + constTable);

        visit(root, new HashMap<>());

        return changed;
    }

    private void identifyLoopModifiedVars(JmmNode node) {
        if (node.getKind().equals(WHILE_STMT.toString())) {
            findModifiedVarsInLoop(node);
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            identifyLoopModifiedVars(node.getChild(i));
        }
    }

    private void findModifiedVarsInLoop(JmmNode loopNode) {
        JmmNode body = loopNode.getChild(1);
        findAssignmentsInNode(body);
    }

    private void findAssignmentsInNode(JmmNode node) {
        if (node.getKind().equals(ASSIGN_STMT.toString())) {
            JmmNode lhs = node.getChild(0);
            if (lhs.getKind().equals(VAR_REF_EXPR.toString())) {
                String varName = lhs.get("name");
                loopModifiedVars.add(varName);
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            findAssignmentsInNode(node.getChild(i));
        }
    }

    private void collectConstants(JmmNode node) {
        if (node.getKind().equals(ASSIGN_STMT.toString())) {
            JmmNode lhs = node.getChild(0);
            JmmNode rhs = node.getChild(1);

            if (lhs.getKind().equals(VAR_REF_EXPR.toString()) &&
                    (rhs.getKind().equals(INTEGER_LITERAL.toString()) || rhs.getKind().equals(BOOLEAN_LITERAL.toString()))) {

                String varName = lhs.get("name");

                if (!loopModifiedVars.contains(varName)) {
                    JmmNode constNode = new JmmNodeImpl(Collections.singletonList(rhs.getKind()));
                    constNode.put("value", rhs.get("value"));
                    constTable.put(varName, constNode);
                    System.out.println("Found constant: " + varName + " = " + rhs.get("value"));
                }
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            collectConstants(node.getChild(i));
        }
    }

    private Boolean visitMethod(JmmNode method, Map<String, JmmNode> context) {
        boolean methodChanged = false;

        for (int i = 0; i < method.getNumChildren(); i++) {
            JmmNode child = method.getChild(i);
            if (visit(child, context)) {
                methodChanged = true;
            }
        }

        return methodChanged;
    }

    private Boolean visitAssignment(JmmNode assignment, Map<String, JmmNode> context) {
        JmmNode rhs = assignment.getChild(1);

        boolean rhsChanged = visit(rhs, context);

        if (rhs.getKind().equals(VAR_REF_EXPR.toString()) && rhs.getAttributes().contains("name")) {
            String rhsVarName = rhs.get("name");
            if (constTable.containsKey(rhsVarName)) {
                JmmNode constNode = constTable.get(rhsVarName);
                JmmNode newRhs = new JmmNodeImpl(Collections.singletonList(constNode.getKind()));
                newRhs.put("value", constNode.get("value"));
                rhs.replace(newRhs);
                System.out.println("Propagated constant in assignment: " + rhsVarName + " = " + newRhs.get("value"));
                rhsChanged = true;
                changed = true;
            }
        }

        return rhsChanged;
    }

    private Boolean visitVarRef(JmmNode varRef, Map<String, JmmNode> context) {
        if (!varRef.getAttributes().contains("name")) {
            return false;
        }

        String varName = varRef.get("name");

        if (loopModifiedVars.contains(varName)) {
            return false;
        }

        if (constTable.containsKey(varName)) {
            JmmNode constNode = constTable.get(varName);
            JmmNode newNode = new JmmNodeImpl(Collections.singletonList(constNode.getKind()));
            newNode.put("value", constNode.get("value"));

            varRef.replace(newNode);
            System.out.println("Propagated constant variable reference: " + varName + " = " + newNode.get("value"));
            changed = true;
            return true;
        }

        return false;
    }

    private Boolean visitWhileStmt(JmmNode whileNode, Map<String, JmmNode> context) {
        JmmNode condition = whileNode.getChild(0);
        boolean conditionChanged = visit(condition, context);

        JmmNode body = whileNode.getChild(1);
        boolean bodyChanged = visit(body, context);

        return conditionChanged || bodyChanged;
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean nodeChanged = false;

        for (int i = 0; i < node.getNumChildren(); i++) {
            if (visit(node.getChild(i), context)) {
                nodeChanged = true;
            }
        }

        return nodeChanged;
    }
}