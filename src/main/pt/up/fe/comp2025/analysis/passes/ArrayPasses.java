package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;
import java.util.Objects;

public class ArrayPasses extends AnalysisVisitor {
    private String currentMethod;

    public ArrayPasses(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.ARRAY_EXPR, this::visitArrayExpr);
        addVisit(Kind.ASSIGN_STMT, this::visitArrayAssignment);
        addVisit(Kind.NEW_ARRAY_EXPR, this::visitNewArrayExpr);

        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);

        addVisit(Kind.ARRAY_ELEM_EXPR, this::visitArrayElementExpr);
        addVisit(Kind.WHILE_STMT, this::visitWhileStmt);
        addVisit(Kind.IF_STMT, this::visitIfStmt);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
    }


    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    private Void visitArrayExpr(JmmNode node, SymbolTable table) {
        List<JmmNode> elements = node.getChildren();

        if (elements.isEmpty()) {
            return null;
        }

        Type firstElementType = TypeUtils.getExprType(elements.getFirst(), table);

        for (int i = 1; i < elements.size(); i++) {
            Type currentType = TypeUtils.getExprType(elements.get(i), table);

            if (!Objects.equals(firstElementType, currentType)) {
                var message = "Array elements must have the same type. Found: " +
                        firstElementType.getName() + " and " + currentType.getName();

                addReport(Report.newError(
                        Stage.SEMANTIC,
                        node.getLine(),
                        node.getColumn(),
                        message,
                        null
                ));
                break;
            }
        }

        return null;
    }

    private Void visitArrayAssignment(JmmNode node, SymbolTable table) {
        List<JmmNode> children = node.getChildren();

        if (children.size() == 2) {
            JmmNode leftSide = children.get(0);
            JmmNode rightSide = children.get(1);

            if (rightSide.getKind().equals(Kind.ARRAY_EXPR.toString())) {
                Type leftType = TypeUtils.getExprType(leftSide,table);

                if (!leftType.isArray()) {
                    var message = "Cannot assign array to non-array variable";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            node.getLine(),
                            node.getColumn(),
                            message,
                            null
                    ));
                } else {
                    Type expectedElementType = new Type(leftType.getName(), false);
                    for (JmmNode element : rightSide.getChildren()) {
                        Type elementType = TypeUtils.getExprType(element,table);
                        if (!Objects.equals(expectedElementType, elementType)) {
                            var message = "Invalid array element type. Expected: " +
                                    expectedElementType.getName() + ", Found: " + elementType.getName();
                            addReport(Report.newError(
                                    Stage.SEMANTIC,
                                    element.getLine(),
                                    element.getColumn(),
                                    message,
                                    null
                            ));
                            break;
                        }
                    }
                }
            }
        }

        return null;
    }

    private Void visitNewArrayExpr(JmmNode node, SymbolTable table) {
        JmmNode sizeExpr = node.getChildren().getFirst();
        Type sizeType = TypeUtils.getExprType(sizeExpr,table);

        if ((!sizeType.getName().equals("Int") && !sizeType.getName().equals("int")) || sizeType.isArray()) {
            var message = "Array size must be an integer expression";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    sizeExpr.getLine(),
                    sizeExpr.getColumn(),
                    message,
                    null
            ));
        }

        return null;
    }

    private Void visitArrayElementExpr(JmmNode node, SymbolTable table) {
        List<JmmNode> children = node.getChildren();

        if (children.size() == 2) {
            JmmNode arrayExpr = children.get(0);
            JmmNode indexExpr = children.get(1);

            Type arrayType = TypeUtils.getExprType(arrayExpr,table);
            if (!arrayType.isArray()) {
                var message = "Cannot perform array access on non-array type: " + arrayType.getName();
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        arrayExpr.getLine(),
                        arrayExpr.getColumn(),
                        message,
                        null
                ));
            }

            Type indexType = TypeUtils.getExprType(indexExpr,table);
            if ((!Objects.equals(indexType.getName(), "int") && !Objects.equals(indexType.getName(), "Int")) || indexType.isArray()) {
                var message = "Array index must be an integer expression";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        indexExpr.getLine(),
                        indexExpr.getColumn(),
                        message,
                        null
                ));
            }
        }

        return null;
    }

    private Void visitWhileStmt(JmmNode node, SymbolTable table) {
        List<JmmNode> children = node.getChildren();

        if (!children.isEmpty()) {
            JmmNode conditionExpr = children.getFirst();
            Type conditionType = TypeUtils.getExprType(conditionExpr,table);

            if (conditionType.isArray() || !Objects.equals(conditionType.getName(), "boolean")) {
                var message = "While condition must be a boolean expression, not " +
                        conditionType.getName() + (conditionType.isArray() ? "[]" : "");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        conditionExpr.getLine(),
                        conditionExpr.getColumn(),
                        message,
                        null
                ));
            }
        }

        return null;
    }

    private Void visitIfStmt(JmmNode node, SymbolTable table) {
        List<JmmNode> children = node.getChildren();

        if (!children.isEmpty()) {
            JmmNode conditionExpr = children.getFirst();
            Type conditionType = TypeUtils.getExprType(conditionExpr, table);

            if (conditionType.isArray() || !Objects.equals(conditionType.getName(), "boolean")) {
                var message = "If condition must be a boolean expression, not " +
                        conditionType.getName() + (conditionType.isArray() ? "[]" : "");
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        conditionExpr.getLine(),
                        conditionExpr.getColumn(),
                        message,
                        null
                ));
            }
        }

        return null;
    }


    private Void visitVarRefExpr(JmmNode node, SymbolTable table) {
        // can be used for additional checks maybeee
        return null;
    }

}
