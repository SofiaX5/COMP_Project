package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import java.util.*;

public class AstDeadCodeElimination extends AJmmVisitor<Map<String, Object>, Boolean> {

    private boolean changed = false;
    private Set<String> currentLiveVariables = new HashSet<>();

    public AstDeadCodeElimination() {
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("Block", this::visitBlock);
        addVisit("AssignmentStatement", this::visitAssignmentStatement);
        addVisit("IfStatement", this::visitIfStatement);
        addVisit("ReturnStatement", this::visitReturnStatement);
        addVisit("BinaryExpression", this::visitBinaryExpression);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("MethodCall", this::visitMethodCall);
        setDefaultVisit(this::defaultVisit);
    }

    public Boolean visitMethodDeclaration(JmmNode method, Map<String, Object> context) {
        currentLiveVariables = new HashSet<>();
        changed = false;
        for (JmmNode child : new ArrayList<>(method.getChildren())) {
            visit(child, context);
        }
        JmmNode methodBody = getMethodBody(method);

        if (methodBody != null) {
            List<JmmNode> statements = new ArrayList<>(methodBody.getChildren());
            Collections.reverse(statements);

            for (JmmNode statement : statements) {
                visit(statement, context);
            }
        }

        return changed;
    }

    public Boolean visitBlock(JmmNode block, Map<String, Object> context) {
        Set<String> liveBeforeBlock = new HashSet<>(currentLiveVariables);

        List<JmmNode> statements = new ArrayList<>(block.getChildren());
        Collections.reverse(statements);

        boolean blockChanged = false;
        for (JmmNode statement : statements) {
            if (visit(statement, context)) {
                blockChanged = true;
                changed = true;
            }
        }
        currentLiveVariables.addAll(liveBeforeBlock);

        return blockChanged;
    }

    public Boolean visitAssignmentStatement(JmmNode assignment, Map<String, Object> context) {
        String assignedVar = getAssignedVariable(assignment);
        if (assignedVar == null) {
            return false;
        }

        if (assignmentHasSideEffects(assignment.getChild(1))) {
            visit(assignment.getChild(1), context);
            currentLiveVariables.add(assignedVar);
            return false;
        }

        Set<String> rhsUses = new HashSet<>();
        collectUsedVariables(assignment.getChild(1), rhsUses);
        currentLiveVariables.addAll(rhsUses);

        if (!currentLiveVariables.contains(assignedVar)) {
            assignment.getParent().removeChild(assignment);
            changed = true;
            return true;
        } else {
            currentLiveVariables.remove(assignedVar);
            return false;
        }
    }

    public Boolean visitIfStatement(JmmNode ifStmt, Map<String, Object> context) {
        boolean ifChanged = false;

        Set<String> liveAfterIf = new HashSet<>(currentLiveVariables);

        Set<String> liveInThenBranch = new HashSet<>(liveAfterIf);
        currentLiveVariables = liveInThenBranch;
        JmmNode thenBlock = ifStmt.getNumChildren() > 1 ? ifStmt.getChild(1) : null;
        if (thenBlock != null) {
            if (visit(thenBlock, context)) {
                ifChanged = true;
            }
        }
        liveInThenBranch = new HashSet<>(currentLiveVariables);

        Set<String> liveInElseBranch = new HashSet<>(liveAfterIf);
        currentLiveVariables = liveInElseBranch;
        JmmNode elseBlock = ifStmt.getNumChildren() > 2 ? ifStmt.getChild(2) : null;
        if (elseBlock != null) {
            if (visit(elseBlock, context)) {
                ifChanged = true;
            }
        }
        liveInElseBranch = new HashSet<>(currentLiveVariables);


        JmmNode condition = ifStmt.getChild(0);
        String conditionKind = condition.getKind();

        boolean thenBranchIsReachable = true;
        boolean elseBranchIsReachable = true;

        if (conditionKind.equals("BooleanLiteral")) {
            boolean conditionValue = Boolean.parseBoolean(condition.get("value"));
            if (conditionValue) {
                elseBranchIsReachable = false;
            } else {
                thenBranchIsReachable = false;
            }
        }

        Set<String> mergedLiveForBranches = new HashSet<>();
        if (thenBranchIsReachable) {
            mergedLiveForBranches.addAll(liveInThenBranch);
        }
        if (elseBranchIsReachable) {
            mergedLiveForBranches.addAll(liveInElseBranch);
        }

        currentLiveVariables.clear();
        currentLiveVariables.addAll(liveAfterIf);
        currentLiveVariables.addAll(mergedLiveForBranches);

        Set<String> conditionUsedVars = new HashSet<>();
        collectUsedVariables(condition, conditionUsedVars);
        currentLiveVariables.addAll(conditionUsedVars);

        if (!thenBranchIsReachable && thenBlock != null) {
            ifStmt.removeChild(thenBlock);
            changed = true;
            ifChanged = true;
        }
        if (!elseBranchIsReachable && elseBlock != null) {
            ifStmt.removeChild(elseBlock);
            changed = true;
            ifChanged = true;
        }
        return ifChanged;
    }

    public Boolean visitReturnStatement(JmmNode returnStmt, Map<String, Object> context) {
        if (returnStmt.getNumChildren() > 0) {
            collectUsedVariables(returnStmt.getChild(0), currentLiveVariables);
            visit(returnStmt.getChild(0), context);
        }
        return false;
    }

    public Boolean visitBinaryExpression(JmmNode binaryExpr, Map<String, Object> context) {
        boolean childChanged1 = visit(binaryExpr.getChild(1), context);
        boolean childChanged0 = visit(binaryExpr.getChild(0), context);

        collectUsedVariables(binaryExpr.getChild(0), currentLiveVariables);
        collectUsedVariables(binaryExpr.getChild(1), currentLiveVariables);

        return childChanged0 || childChanged1;
    }

    public Boolean visitIdentifier(JmmNode identifier, Map<String, Object> context) {
        if (!identifier.getParent().getKind().equals("AssignmentStatement") ||
                identifier.getParent().getChild(0) != identifier) {
            currentLiveVariables.add(identifier.get("name"));
        }
        return false;
    }

    public Boolean visitMethodCall(JmmNode methodCall, Map<String, Object> context) {
        if (hasSideEffects(methodCall)) {
            for (int i = 1; i < methodCall.getNumChildren(); i++) {
                collectUsedVariables(methodCall.getChild(i), currentLiveVariables);
                visit(methodCall.getChild(i), context);
            }
        }
        for (JmmNode child : new ArrayList<>(methodCall.getChildren())) {
            visit(child, context);
        }
        return false;
    }


    public Boolean defaultVisit(JmmNode node, Map<String, Object> context) {
        boolean nodeChanged = false;
        List<JmmNode> children = new ArrayList<>(node.getChildren());
        if (node.getKind().equals("MethodDeclaration") || node.getKind().equals("Block")) {
            Collections.reverse(children);
        }

        for (JmmNode child : children) {
            if (visit(child, context)) {
                nodeChanged = true;
            }
        }
        return nodeChanged;
    }

    private JmmNode getMethodBody(JmmNode methodDeclaration) {
        for (JmmNode child : methodDeclaration.getChildren()) {
            if (child.getKind().equals("Block")) {
                return child;
            }
        }
        return null;
    }

    private void collectUsedVariables(JmmNode node, Set<String> usedVars) {
        if (node == null) return;

        if (node.getKind().equals("Identifier")) {
            if (! (node.getParent() != null && node.getParent().getKind().equals("AssignmentStatement") && node.getParent().getChild(0) == node) ) {
                usedVars.add(node.get("name"));
            }
        } else if (node.getKind().equals("MethodCall")) {
            for (int i = 1; i < node.getNumChildren(); i++) {
                collectUsedVariables(node.getChild(i), usedVars);
            }
            return;
        } else if (node.getKind().equals("ArrayAccess")) {
            collectUsedVariables(node.getChild(0), usedVars);
            collectUsedVariables(node.getChild(1), usedVars);
            return;
        } else if (node.getKind().equals("ReturnStatement")) {
            if (node.getNumChildren() > 0) {
                collectUsedVariables(node.getChild(0), usedVars);
            }
            return;
        }

        for (JmmNode child : node.getChildren()) {
            collectUsedVariables(child, usedVars);
        }
    }

    private String getAssignedVariable(JmmNode assignment) {
        if (assignment.getNumChildren() > 0) {
            JmmNode target = assignment.getChild(0);
            if (target.getKind().equals("Identifier")) {
                return target.get("name");
            }
        }
        return null;
    }

    private boolean assignmentHasSideEffects(JmmNode rhs) {
        return hasSideEffects(rhs);
    }

    private boolean hasSideEffects(JmmNode node) {
        if (node == null) return false;

        if (node.getKind().equals("MethodCall") ||
                node.getKind().equals("NewExpression") ||
                node.getKind().equals("ArrayAssignmentStatement")) {
            return true;
        }

        for (JmmNode child : node.getChildren()) {
            if (hasSideEffects(child)) {
                return true;
            }
        }
        return false;
    }
}