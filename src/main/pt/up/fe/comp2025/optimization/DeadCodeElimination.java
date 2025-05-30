package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2025.ast.Kind; // Assuming you have an 'Kind' enum for node types

import java.util.*;

import static pt.up.fe.comp2025.ast.Kind.*; // Import your AST Kind enum


public class DeadCodeElimination extends PostorderJmmVisitor<Map<String, Boolean>, Boolean> {

    private boolean changed = false;
    private SymbolTable symbolTable;

    public DeadCodeElimination(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        addVisit(METHOD_DECL, this::visitMethod);
        addVisit(ASSIGN_STMT, this::visitAssignment);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignment);
        addVisit(EXPR_STMT, this::visitExprStatement);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(BLOCK_STMT, this::visitBlockStmt);
        setDefaultVisit(this::defaultVisit);
    }

    @Override
    public Boolean visit(JmmNode root) {
        changed = false;
        visit(root, new HashMap<>());
        return changed;
    }

    private Boolean visitMethod(JmmNode method, Map<String, Boolean> liveVariables) {
        Map<String, Boolean> methodLiveVariables = new HashMap<>();

        boolean methodChanged = false;
        for (JmmNode child : method.getChildren()) {
            if (child != null && visit(child, methodLiveVariables)) {
                methodChanged = true;
            }
        }
        return methodChanged;
    }

    private Boolean visitAssignment(JmmNode assignment, Map<String, Boolean> liveVariables) {
        JmmNode lhs = assignment.getChild(0);
        JmmNode rhs = assignment.getChild(1);

        collectUsedVariables(rhs, liveVariables);

        boolean nodeChanged = false;

        if (lhs != null && lhs.getKind().equals(VAR_REF_EXPR.toString())) {
            String varName = lhs.get("name");

            if (!liveVariables.containsKey(varName) || !liveVariables.get(varName)) {
                if (!hasSideEffects(rhs)) {
                    assignment.replace(new JmmNodeImpl(Collections.singletonList(Kind.NOOP_STMT.toString())));
                    changed = true;
                    nodeChanged = true;
                    System.out.println("Eliminated dead assignment for variable: " + varName);
                } else {
                    liveVariables.remove(varName);
                }
            } else {
                liveVariables.remove(varName); // Mark as defined, no longer live for previous defs.
            }
        }
        return nodeChanged;
    }

    private Boolean visitArrayAssignment(JmmNode assignment, Map<String, Boolean> liveVariables) {
        JmmNode arrayRef = assignment.getChild(0);
        JmmNode indexExpr = assignment.getChild(1);
        JmmNode valueExpr = assignment.getChild(2);

        collectUsedVariables(arrayRef, liveVariables);
        collectUsedVariables(indexExpr, liveVariables);
        collectUsedVariables(valueExpr, liveVariables);

        if (arrayRef != null && arrayRef.getKind().equals(VAR_REF_EXPR.toString())) {
            liveVariables.put(arrayRef.get("name"), true);
        }

        return false;
    }

    private Boolean visitExprStatement(JmmNode exprStmt, Map<String, Boolean> liveVariables) {
        JmmNode expr = exprStmt.getChild(0);
        collectUsedVariables(expr, liveVariables);
        return false;
    }

    private Boolean visitWhileStmt(JmmNode whileNode, Map<String, Boolean> liveVariables) {
        JmmNode condition = whileNode.getChild(0);
        JmmNode body = whileNode.getChild(1);

        boolean changedInLoop = false;

        Map<String, Boolean> loopLiveVariables = new HashMap<>(liveVariables);

        if (visit(body, loopLiveVariables)) changedInLoop = true;
        if (visit(condition, loopLiveVariables)) changedInLoop = true;

        loopLiveVariables.forEach((var, isLive) -> liveVariables.merge(var, isLive, (oldVal, newVal) -> oldVal || newVal));

        return changedInLoop;
    }

    private Boolean visitIfStmt(JmmNode ifNode, Map<String, Boolean> liveVariables) {
        JmmNode condition = ifNode.getChild(0);
        JmmNode thenBlock = ifNode.getChild(1);
        JmmNode elseBlock = ifNode.getNumChildren() > 2 ? ifNode.getChild(2) : null;

        boolean changedInIf = false;

        Map<String, Boolean> thenLiveVariables = new HashMap<>(liveVariables);
        Map<String, Boolean> elseLiveVariables = new HashMap<>(liveVariables);

        if (visit(thenBlock, thenLiveVariables)) changedInIf = true;
        if (elseBlock != null && visit(elseBlock, elseLiveVariables)) changedInIf = true;

        thenLiveVariables.forEach((var, isLive) -> liveVariables.merge(var, isLive, (oldVal, newVal) -> oldVal || newVal));
        elseLiveVariables.forEach((var, isLive) -> liveVariables.merge(var, isLive, (oldVal, newVal) -> oldVal || newVal));

        if (visit(condition, liveVariables)) changedInIf = true;

        return changedInIf;
    }

    private Boolean visitBlockStmt(JmmNode block, Map<String, Boolean> liveVariables) {
        boolean blockChanged = false;
        for (JmmNode child : block.getChildren()) {
            if (child != null && visit(child, liveVariables)) {
                blockChanged = true;
            }
        }
        return blockChanged;
    }


    private void collectUsedVariables(JmmNode node, Map<String, Boolean> liveVariables) {
        if (node == null) return;

        if (node.getKind().equals(VAR_REF_EXPR.toString())) {
            liveVariables.put(node.get("name"), true); // Mark as live (used)
        }
        for (JmmNode child : node.getChildren()) {
            collectUsedVariables(child, liveVariables);
        }
    }

    private boolean hasSideEffects(JmmNode expr) {
        if (expr == null) return false;

        if (expr.getKind().equals(METHOD_CALL_EXPR.toString()) ||
                expr.getKind().equals(NEW_OBJECT_EXPR.toString()) ||
                expr.getKind().equals(NEW_ARRAY_EXPR.toString())) {
            return true;
        }

        for (JmmNode child : expr.getChildren()) {
            if (hasSideEffects(child)) {
                return true;
            }
        }
        return false;
    }

    private Boolean defaultVisit(JmmNode node, Map<String, Boolean> liveVariables) {
        boolean nodeChanged = false;
        for (int i = 0; i < node.getNumChildren(); i++) {
            JmmNode child = node.getChild(i);
            if (child != null && visit(child, liveVariables)) {
                nodeChanged = true;
            }
        }
        return nodeChanged;
    }
}