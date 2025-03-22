package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.List;
import java.util.Objects;

/**
 * Checks if the type of the expression in a return statement is compatible with the method return type.
 *
 * @author JBispo
 */
public class UndeclaredVariable extends AnalysisVisitor {

    private String currentMethod;

    public UndeclaredVariable(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);
        addVisit(Kind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");

        List<JmmNode> list_stmt = method.getChildren(Kind.STMT);

        for (JmmNode stmt : list_stmt) {
            var kind = stmt.getKind();
            List<JmmNode> exprs = stmt.getChildren(Kind.EXPR);
            if (Objects.equals(kind, "IfStmt")) {
                var expr = exprs.getFirst();
                if (!Objects.equals(TypeUtils.getExprType(expr, table), new Type("Boolean", false))) {
                    var message = "If condition is not of type Boolean.";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            message,
                            null)
                    );
                }
            }
        }

        /* ISTO ESTÁ A DAR UM ERRO E EU NÃO PERCEBO PORQUÊ, FICA AQUI CASO POSSA SER ÚTIL :)
        while (!list_stmt.isEmpty()) {

            JmmNode stmt = list_stmt.getFirst();
            list_stmt.removeFirst();
            System.out.println("sdsdsds" + list_stmt);

            List<JmmNode> list_stmt_temp = stmt.getChildren(Kind.STMT);
            list_stmt.addAll(list_stmt_temp);

            List<JmmNode> list_expr_temp = stmt.getChildren(Kind.EXPR);
            for (JmmNode expr : list_expr_temp) {
                if (expr.hasAttribute("op")) {
                    List<JmmNode> operands = stmt.getChildren(Kind.EXPR);
                    var op1 = operands.getFirst();
                    var op2 = operands.get(1);
                    if (TypeUtils.getExprType(op1) != TypeUtils.getExprType(op2)) {
                        var message = "Operation variables don't have the same type.";
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                method.getLine(),
                                method.getColumn(),
                                message,
                                null)
                        );
                    }
                }
            }
        }
         */

        List<JmmNode> list_expr = method.getChildren(Kind.EXPR);
        for (JmmNode expr : list_expr) {
            List<JmmNode> babies_expr = expr.getChildren(Kind.EXPR);
            if (expr.hasAttribute("op")) {
                var op = expr.get("op");
                var op1 = babies_expr.getFirst();
                var op2 = babies_expr.get(1);
                if (op.equals("+")||op.equals(">")) {
                    if ((Objects.equals(TypeUtils.getExprType(op1,  table), new Type("String", false)) &&
                            Objects.equals(TypeUtils.getExprType(op2, table), new Type("String", false))) ||
                            Objects.equals(TypeUtils.getExprType(op1,table), new Type("Int", false)) &&
                            Objects.equals(TypeUtils.getExprType(op2,table), new Type("Int", false))) {
                        return null;
                    } else {
                        var message = String.format("Operands type are not adequate for the operation %s.", op);
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                method.getLine(),
                                method.getColumn(),
                                message,
                                null)
                        );
                    }

                } else if (op.equals("-")||op.equals("*")||op.equals("/")) {
                    if (Objects.equals(TypeUtils.getExprType(op1, table), new Type("Int", false)) &&
                        Objects.equals(TypeUtils.getExprType(op2, table), new Type("Int", false))) {
                        return null;
                    } else {
                        var message = String.format("Operands type are not adequate for the operation %s.", op);
                        addReport(Report.newError(
                                Stage.SEMANTIC,
                                method.getLine(),
                                method.getColumn(),
                                message,
                                null)
                        );
                    }
                }
            }
        }

        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        // Check if exists a parameter or variable declaration with the same name as the variable reference
        var varRefName = varRefExpr.get("name");

        // Var is a field, return
        if (table.getFields().stream()
                .anyMatch(field -> field.getName().equals(varRefName))) {
            return null;
        }

        // Var is a parameter, return
        if (table.getParameters(currentMethod).stream()
                .anyMatch(param -> param.getName().equals(varRefName))) {
            return null;
        }

        // Var is a declared variable, return
        if (table.getLocalVariables(currentMethod).stream()
                .anyMatch(varDecl -> varDecl.getName().equals(varRefName))) {
            return null;
        }

        // Import is a declared variable, return
        if (table.getImports().stream()
                .anyMatch(import_ -> import_.equals(varRefName))) {
            return null;
        }

        // Create error report
        var message = String.format("Variable '%s' does not exist.", varRefName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                varRefExpr.getLine(),
                varRefExpr.getColumn(),
                message,
                null)
        );

        return null;
    }

    // Tests ObjectAssignmentFail and ObjectAssignmentPassImports  are not compatible
    // ObjectAssignmentPassImports has a comment that we can assigned 2 different objects

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        List<JmmNode> exprs = assignStmt.getChildren(Kind.EXPR);
        JmmNode leftExpr = exprs.get(0);
        JmmNode rightExpr = exprs.get(1);

        Type leftType = TypeUtils.getExprType(leftExpr, table);
        Type rightType = TypeUtils.getExprType(rightExpr, table);

        if (leftType != null && rightType != null && leftType.getName().equals("boolean") && rightType.getName().equals("int")) {
            var message = String.format("Cannot assign an Int value to a Boolean variable '%s'.", leftExpr.get("name"));
            addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));
            return null;
        }

        if (leftType != null && rightType != null &&
                !isPrimitiveType(leftType) && !isPrimitiveType(rightType) &&
                !leftType.getName().equals(rightType.getName())) {

            boolean isAssigningToCurrentClass = leftType.getName().equals(table.getClassName());

            boolean isExtending = table.getSuper() != null && rightType.getName().equals(table.getClassName()) &&
                    leftType.getName().equals(table.getSuper());

            boolean leftTypeImported = isTypeImported(leftType.getName(), table);
            boolean rightTypeImported = isTypeImported(rightType.getName(), table);

            boolean compatibleByImport = (leftTypeImported && rightTypeImported);

            if (isAssigningToCurrentClass && !isExtending && !compatibleByImport) {
                var message = String.format("Cannot assign object of type '%s' to variable of type '%s'.",
                        rightType.getName(), leftType.getName());
                addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));
            }
            else if (!isExtending && !compatibleByImport) {
                var message = String.format("Cannot assign object of type '%s' to variable of type '%s'.",
                        rightType.getName(), leftType.getName());
                addReport(Report.newError(Stage.SEMANTIC, assignStmt.getLine(), assignStmt.getColumn(), message, null));
            }
        }

        return null;
    }

    private boolean isTypeImported(String typeName, SymbolTable table) {
        for (String importName : table.getImports()) {
            if (importName.endsWith("." + typeName) || importName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPrimitiveType(Type type) {
        return type.isArray() ||
                type.getName().equals("int") ||
                type.getName().equals("boolean") ||
                type.getName().equals("void");
    }

}
