package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp2025.ast.Kind.*;


public class ConstPropVisitor extends AJmmVisitor<Map<String, JmmNode>, Boolean> {

    private final Map<String, JmmNode> constTable = new HashMap<>();

    @Override
    protected void buildVisitor() {
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(METHOD_DECL, this::visitMethod);
        setDefaultVisit(this::defaultVisit);
    }


    public Boolean visit(JmmNode root) {
        constTable.clear();

        collectConstants(root);

        System.out.println("Collected constants: " + constTable);

        boolean changed = visit(root, new HashMap<>());

        return changed;
    }

    private void collectConstants(JmmNode node) {
        if (node.getKind().equals(ASSIGN_STMT.toString())) {
            JmmNode lhs = node.getChild(0);
            JmmNode rhs = node.getChild(1);

            if (lhs.getKind().equals(VAR_REF_EXPR.toString()) &&
                    (rhs.getKind().equals(INTEGER_LITERAL.toString()) || rhs.getKind().equals(BOOLEAN_LITERAL.toString()))) {

                String varName = lhs.get("name");
                JmmNode constNode = new JmmNodeImpl(Collections.singletonList(rhs.getKind()));
                constNode.put("value", rhs.get("value"));
                constTable.put(varName, constNode);
                System.out.println("Found constant: " + varName + " = " + rhs.get("value"));
            }
        }

        for (int i = 0; i < node.getNumChildren(); i++) {
            collectConstants(node.getChild(i));
        }
    }

    private Boolean visitMethod(JmmNode method, Map<String, JmmNode> context) {
        boolean changed = false;

        for (int i = 0; i < method.getNumChildren(); i++) {
            JmmNode child = method.getChild(i);
            changed |= visit(child, context);
        }

        for (int i = 0; i < method.getNumChildren(); i++) {
            JmmNode child = method.getChild(i);

            if (child.getKind().equals(VAR_REF_EXPR.toString()) &&
                    i > 0 && (method.getChild(i-1).getKind().equals("ReturnToken") ||
                    child.getAttributes().contains("isReturn"))) {

                String varName = child.get("name");
                if (constTable.containsKey(varName)) {
                    JmmNode constNode = constTable.get(varName);
                    JmmNode newNode = new JmmNodeImpl(Collections.singletonList(constNode.getKind()));
                    newNode.put("value", constNode.get("value"));

                    child.replace(newNode);
                    System.out.println("Propagated constant in return: " + varName + " = " + newNode.get("value"));
                    changed = true;
                }
            }
        }

        return changed;
    }

    private Boolean visitAssignment(JmmNode assignment, Map<String, JmmNode> context) {
        JmmNode lhs = assignment.getChild(0);
        JmmNode rhs = assignment.getChild(1);

        boolean changed = false;

        changed |= visit(rhs, context);

        if (rhs.getKind().equals(VAR_REF_EXPR.toString()) && rhs.getAttributes().contains("name")) {
            String rhsVarName = rhs.get("name");
            if (constTable.containsKey(rhsVarName)) {
                JmmNode constNode = constTable.get(rhsVarName);
                JmmNode newRhs = new JmmNodeImpl(Collections.singletonList(constNode.getKind()));
                newRhs.put("value", constNode.get("value"));
                rhs.replace(newRhs);
                System.out.println("Propagated constant in assignment: " + rhsVarName + " = " + newRhs.get("value"));
                changed = true;
            }
        }

        return changed;
    }

    private Boolean visitVarRef(JmmNode varRef, Map<String, JmmNode> context) {
        if (!varRef.getAttributes().contains("name")) {
            return false;
        }

        String varName = varRef.get("name");

        if (constTable.containsKey(varName)) {
            JmmNode constNode = constTable.get(varName);
            JmmNode newNode = new JmmNodeImpl(Collections.singletonList(constNode.getKind()));
            newNode.put("value", constNode.get("value"));

            varRef.replace(newNode);
            System.out.println("Propagated constant variable reference: " + varName + " = " + newNode.get("value"));

            return true;
        }

        return false;
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean changed = false;

        for (int i = 0; i < node.getNumChildren(); i++) {
            changed |= visit(node.getChild(i), context);
        }

        return changed;
    }
}