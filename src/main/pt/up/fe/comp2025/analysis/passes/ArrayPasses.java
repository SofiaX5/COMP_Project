package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
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
    }


    // Check that all elements in an array expression are of the same type
    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");
        return null;
    }

    // Check that all elements in an array expression are of the same type
    private Void visitArrayExpr(JmmNode node, SymbolTable table) {
        List<JmmNode> elements = node.getChildren();

        if (elements.isEmpty()) {
            return null;
        }

        // Get the type of the first element as a reference
        Type firstElementType = TypeUtils.getExprType(elements.get(0));

        // Check if all elements have the same type
        for (int i = 1; i < elements.size(); i++) {
            Type currentType = TypeUtils.getExprType(elements.get(i));

            // If types don't match, report an error
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

    // Check that array assignments are type-compatible
    private Void visitArrayAssignment(JmmNode node, SymbolTable table) {
        List<JmmNode> children = node.getChildren();

        // Check if this is an array assignment (target is variable and right side is array expression)
        if (children.size() == 2) {
            JmmNode leftSide = children.get(0);
            JmmNode rightSide = children.get(1);

            // Check if right side is an array expression
            if (rightSide.getKind().equals(Kind.ARRAY_EXPR.toString())) {
                Type leftType = TypeUtils.getExprType(leftSide);

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
                    // Check if array element types match the expected type
                    Type expectedElementType = new Type(leftType.getName(), false);
                    for (JmmNode element : rightSide.getChildren()) {
                        Type elementType = TypeUtils.getExprType(element);
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
        JmmNode sizeExpr = node.getChildren().get(0);
        Type sizeType = TypeUtils.getExprType(sizeExpr);

        if (!sizeType.getName().equals("int") || sizeType.isArray()) {
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

}
