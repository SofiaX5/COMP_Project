package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.*;

import static pt.up.fe.comp2025.ast.Kind.*;

public class ConstPropVisitor extends AJmmVisitor<Map<String, JmmNode>, Boolean> {

    private boolean changed = false;

    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(METHOD_DECL, this::visitMethod);
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        setDefaultVisit(this::defaultVisit);
    }

    public Boolean visit(JmmNode root) {
        changed = false;
        visit(root, new HashMap<>());
        return changed;
    }

    private Boolean visitProgram(JmmNode program, Map<String, JmmNode> context) {
        boolean programChanged = false;
        for (JmmNode child : program.getChildren()) {
            if (child != null && visit(child, context)) {
                programChanged = true;
            }
        }
        return programChanged;
    }

    private Boolean visitMethod(JmmNode method, Map<String, JmmNode> context) {
        Map<String, JmmNode> methodConstTable = new HashMap<>();
        boolean methodChanged = false;

        for (int i = 0; i < method.getNumChildren(); i++) {
            JmmNode child = method.getChild(i);
            if (child != null && visit(child, methodConstTable)) {
                methodChanged = true;
            }
        }
        return methodChanged;
    }


    private Boolean visitAssignment(JmmNode assignment, Map<String, JmmNode> context) {
        JmmNode lhs = assignment.getChild(0);
        JmmNode rhs = assignment.getChild(1);

        boolean rhsChanged = visit(rhs, context);

        if (lhs != null && lhs.getKind().equals(VAR_REF_EXPR.toString())) {
            String varName = lhs.get("name");

            if (rhs != null && (rhs.getKind().equals(INTEGER_LITERAL.toString()) || rhs.getKind().equals(BOOLEAN_LITERAL.toString()))) {
                if (context.containsKey(varName)) {
                    System.out.println("Variable " + varName + " reassigned; removing from constants for strict test.");
                    context.remove(varName);
                    changed = true;
                } else {
                    JmmNode constNode = new JmmNodeImpl(Collections.singletonList(rhs.getKind()));
                    constNode.put("value", rhs.get("value"));
                    context.put(varName, constNode);
                    System.out.println("Assigned initial constant: " + varName + " = " + rhs.get("value"));
                    changed = true;
                }
            }
            else if (rhs != null && rhs.getKind().equals(VAR_REF_EXPR.toString()) && context.containsKey(rhs.get("name"))) {
                if (context.containsKey(varName)) {
                    System.out.println("Variable " + varName + " reassigned via constant variable; removing from constants for strict test.");
                    context.remove(varName);
                    changed = true;
                } else {
                    JmmNode constNode = context.get(rhs.get("name"));
                    context.put(varName, constNode);
                    System.out.println("Propagating constant from " + rhs.get("name") + " to " + varName);
                    changed = true;
                }
            }
            else {
                if (context.containsKey(varName)) {
                    System.out.println("Variable " + varName + " is no longer a constant (reassignment or non-literal RHS).");
                    context.remove(varName);
                    changed = true;
                }
            }
        }
        return rhsChanged;
    }

    private Boolean visitVarRef(JmmNode varRef, Map<String, JmmNode> context) {
        if (!varRef.getAttributes().contains("name")) {
            return false;
        }

        String varName = varRef.get("name");

        if (context.containsKey(varName)) {
            JmmNode constNode = context.get(varName);
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
        boolean whileChanged = false;

        Set<String> assignedInLoop = new HashSet<>();
        if (whileNode.getNumChildren() > 1 && whileNode.getChild(1) != null) {
            collectAssignedVarsInSubtree(whileNode.getChild(1), assignedInLoop);
        }

        Map<String, JmmNode> conditionContext = new HashMap<>(context);
        for (String varName : assignedInLoop) {
            if (conditionContext.containsKey(varName)) {
                conditionContext.remove(varName);
                System.out.println("Variable " + varName + " removed from condition context (modified in loop).");
            }
        }

        if (whileNode.getNumChildren() > 0 && whileNode.getChild(0) != null) {
            if (visit(whileNode.getChild(0), conditionContext)) {
                whileChanged = true;
            }
        }

        Map<String, JmmNode> loopBodyContext = new HashMap<>(context);
        for (String varName : assignedInLoop) {
            if (loopBodyContext.containsKey(varName)) {
                loopBodyContext.remove(varName);
                System.out.println("Variable " + varName + " removed from loop body context (modified in loop).");
            }
        }

        if (whileNode.getNumChildren() > 1 && whileNode.getChild(1) != null) {
            if (visit(whileNode.getChild(1), loopBodyContext)) {
                whileChanged = true;
            }
        }

        for (String varName : assignedInLoop) {
            if (context.containsKey(varName)) {
                context.remove(varName);
                changed = true;
                System.out.println("Variable " + varName + " removed from outer context (after loop).");
            }
        }

        return whileChanged;
    }

    private void collectAssignedVarsInSubtree(JmmNode node, Set<String> assignedVars) {
        if (node == null) return;

        if (node.getKind().equals(ASSIGN_STMT.toString())) {
            JmmNode lhs = node.getChild(0);
            if (lhs != null && lhs.getKind().equals(VAR_REF_EXPR.toString())) {
                assignedVars.add(lhs.get("name"));
            }
        } else if (node.getKind().equals(ARRAY_ASSIGN_STMT.toString())) {
            JmmNode arrayName = node.getChild(0);
            if (arrayName != null && arrayName.getKind().equals(VAR_REF_EXPR.toString())) {
                assignedVars.add(arrayName.get("name"));
            }
        }

        for (JmmNode child : node.getChildren()) {
            collectAssignedVarsInSubtree(child, assignedVars);
        }
    }


    private Boolean visitIfStmt(JmmNode ifNode, Map<String, JmmNode> context) {
        boolean ifChanged = false;

        if (ifNode.getNumChildren() > 0 && ifNode.getChild(0) != null) {
            if (visit(ifNode.getChild(0), context)) {
                ifChanged = true;
            }
        }

        Map<String, JmmNode> thenContext = new HashMap<>(context);
        Map<String, JmmNode> elseContext = new HashMap<>(context);

        if (ifNode.getNumChildren() > 1 && ifNode.getChild(1) != null) {
            if (visit(ifNode.getChild(1), thenContext)) {
                ifChanged = true;
            }
        }

        if (ifNode.getNumChildren() > 2 && ifNode.getChild(2) != null) {
            if (visit(ifNode.getChild(2), elseContext)) {
                ifChanged = true;
            }
        } else {
            elseContext = new HashMap<>(context);
        }


        Set<String> allVarsInBranches = new HashSet<>();
        allVarsInBranches.addAll(thenContext.keySet());
        allVarsInBranches.addAll(elseContext.keySet());

        for (String varName : allVarsInBranches) {
            boolean inThen = thenContext.containsKey(varName);
            boolean inElse = elseContext.containsKey(varName);

            if (inThen && inElse) {
                JmmNode thenValue = thenContext.get(varName);
                JmmNode elseValue = elseContext.get(varName);

                if (thenValue.getKind().equals(elseValue.getKind()) && thenValue.get("value").equals(elseValue.get("value"))) {
                    context.put(varName, thenValue);
                } else {
                    if (context.containsKey(varName)) {
                        context.remove(varName);
                        changed = true;
                        System.out.println("Variable " + varName + " removed from constants (different values in if/else branches).");
                    }
                }
            } else {
                if (context.containsKey(varName)) {
                    context.remove(varName);
                    changed = true;
                    System.out.println("Variable " + varName + " removed from constants (constant only in one if/else branch).");
                }
            }
        }

        return ifChanged;
    }

    private Boolean defaultVisit(JmmNode node, Map<String, JmmNode> context) {
        boolean nodeChanged = false;

        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChild(i);
            if (child != null && visit(child, context)) {
                nodeChanged = true;
            }
        }
        return nodeChanged;
    }
}