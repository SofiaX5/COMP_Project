package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp2025.symboltable.JmmSymbolTable;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        addVisit(Kind.NEW_OBJECT_EXPR, this::visitNewObjectExpr);
        addVisit(Kind.CLASS_DECL, this::visitClassDecl);
        addVisit(Kind.NOT_EXPR, this::visitUnaryExpr);
        addVisit(Kind.BINARY_EXPR, this::visitBinaryExpr);

    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable table) {
        if (!binaryExpr.hasAttribute("op") || binaryExpr.getChildren(Kind.EXPR).size() < 2) {
            return null;
        }

        String op = binaryExpr.get("op");
        List<JmmNode> operands = binaryExpr.getChildren(Kind.EXPR);

        if (op.matches("[+\\-*/]")) {
            Type op1Type = TypeUtils.getExprType(operands.get(0), table);
            Type op2Type = TypeUtils.getExprType(operands.get(1), table);

            if (op1Type == null || op2Type == null ||
                    !op1Type.getName().equals("int") ||
                    !op2Type.getName().equals("int")) {
                var message = String.format("Arithmetic operation '%s' requires integer operands.", op);
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        binaryExpr.getLine(),
                        binaryExpr.getColumn(),
                        message,
                        null)
                );
            }
        }
        else if (op.equals("<")) {
            Type op1Type = TypeUtils.getExprType(operands.get(0), table);
            Type op2Type = TypeUtils.getExprType(operands.get(1), table);

            if (op1Type == null || op2Type == null ||
                    !op1Type.getName().equals("int") ||
                    !op2Type.getName().equals("int")) {
                var message = "Comparison operation '<' requires integer operands.";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        binaryExpr.getLine(),
                        binaryExpr.getColumn(),
                        message,
                        null)
                );
            }
        }
        else if (op.equals("&&")) {
            Type op1Type = TypeUtils.getExprType(operands.get(0), table);
            Type op2Type = TypeUtils.getExprType(operands.get(1), table);

            if (op1Type == null || op2Type == null ||
                    !op1Type.getName().equals("boolean") ||
                    !op2Type.getName().equals("boolean")) {
                var message = "Logical AND operation '&&' requires boolean operands.";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        binaryExpr.getLine(),
                        binaryExpr.getColumn(),
                        message,
                        null)
                );
            }
        }

        return null;
    }

    private Void visitUnaryExpr(JmmNode unaryExpr, SymbolTable table) {
        if (!unaryExpr.getKind().equals("NotExpr")) {
            return null;
        }

        List<JmmNode> children = unaryExpr.getChildren();
        if (children.size() != 1) {
            return null;
        }

        JmmNode operand = children.get(0);
        Type operandType = TypeUtils.getExprType(operand, table);

        if (operandType == null || !operandType.getName().equals("boolean") || operandType.isArray()) {
            String message;

            if (operandType == null) {
                message = "Negation operator '!' requires boolean operand but found unknown type.";
            } else if (operandType.getName().equals("int")) {
                message = "Cannot apply negation operator '!' to int.";
            } else if (operandType.isArray()) {
                message = "Cannot apply negation operator '!' to an array type.";
            } else {
                message = "Negation operator '!' requires boolean operand.";
            }

            addReport(Report.newError(
                    Stage.SEMANTIC,
                    unaryExpr.getLine(),
                    unaryExpr.getColumn(),
                    message,
                    null)
            );
        }

        return null;
    }


    private Void visitClassDecl(JmmNode classDecl, SymbolTable table) {
        Set<String> fieldNames = new HashSet<>();
        Set<String> importedClassNames = new HashSet<>();
        Set<String> methodNames = new HashSet<>();
        Set<String> uniqueImports = new HashSet<>();

        for (Symbol field : table.getFields()) {
            if (!fieldNames.add(field.getName())) {
                addReport(Report.newError(Stage.SEMANTIC, classDecl.getLine(), classDecl.getColumn(),
                        "Duplicated field declaration: " + field.getName(), null));
            }
            if (Objects.equals(field.getType().getName(), "vararg")) {
                addReport(Report.newError(Stage.SEMANTIC, classDecl.getLine(), classDecl.getColumn(),
                        "Vararg variable is not a parameter.", null));
            }
        }

        for (String importName : table.getImports()) {
            uniqueImports.add(importName.trim());
        }

        for (String importName : uniqueImports) {
            String[] importList = importName.split(",");

            for (String importItem : importList) {
                String trimmedImport = importItem.trim();
                String className = trimmedImport.contains(".")
                        ? trimmedImport.substring(trimmedImport.lastIndexOf('.') + 1)
                        : trimmedImport;

                if (!importedClassNames.add(className)) {
                    addReport(Report.newError(Stage.SEMANTIC, classDecl.getLine(), classDecl.getColumn(),
                            "Duplicated imported class: " + className, null));
                }
            }
        }


        for (String methodName : table.getMethods()) {
            if (!methodNames.add(methodName)) {
                addReport(Report.newError(Stage.SEMANTIC, classDecl.getLine(), classDecl.getColumn(),
                        "Duplicate method declaration: " + methodName, null));
            }
        }

        return null;
    }

    private Void visitMethodDecl(JmmNode method, SymbolTable table) {
        currentMethod = method.get("name");

        Set<String> paramNames = new HashSet<>();
        Set<String> localVarNames = new HashSet<>();

        if (table.getReturnType(currentMethod) == null) {
            for (Symbol field : table.getFields()) {
                if (method.getDescendants("VarAccess").stream()
                        .anyMatch(var -> var.get("name").equals(field.getName()))) {
                    addReport(Report.newError(Stage.SEMANTIC, method.getLine(), method.getColumn(),
                            "Cannot access instance field '" + field.getName() + "' in a static method.", null));
                }
            }
        }

        for (Symbol param : table.getParameters(currentMethod)) {
            if (!paramNames.add(param.getName())) {
                addReport(Report.newError(Stage.SEMANTIC, method.getLine(), method.getColumn(),
                        "Duplicated parameter: " + param.getName(), null));
            }
        }

        for (Symbol localVar : table.getLocalVariables(currentMethod)) {
            if (!localVarNames.add(localVar.getName())) {
                addReport(Report.newError(Stage.SEMANTIC, method.getLine(), method.getColumn(),
                        "Duplicated local variable: " + localVar.getName(), null));
            }
            if (Objects.equals(localVar.getType().getName(), "vararg")) {
                addReport(Report.newError(Stage.SEMANTIC, method.getLine(), method.getColumn(),
                        "Vararg variable is not a parameter.", null));
            }
        }

        List<JmmNode> list_stmt = method.getChildren(Kind.STMT);
        List<JmmNode> list_stmt_expr  = new java.util.ArrayList<>(List.of());
        for (JmmNode stmt : list_stmt) {
            var kind = stmt.getKind();
            List<JmmNode> exprs = stmt.getChildren(Kind.EXPR);
            list_stmt_expr.addAll(exprs);
            if (Objects.equals(kind, "IfStmt")) {
                var expr = exprs.getFirst();
                if (!Objects.equals(TypeUtils.getExprType(expr, table), new Type("boolean", false))) {
                    var message = "If condition is not of type Boolean.";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            message,
                            null)
                    );
                }
            } else if (Objects.equals(kind, "ExprStmt")) {

                var exprStmt = exprs.getFirst();
                if (!Objects.equals(exprStmt.getKind(), "MethodCallExpr")) {
                    var message = "ExprStmt is not of type MethodCallExpr.";
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


        List<JmmNode> list_expr = new java.util.ArrayList<>(List.of());
        List<JmmNode> list_method_expr = method.getChildren(Kind.EXPR);
        list_expr.addAll(list_stmt_expr);
        list_expr.addAll(list_method_expr);
        for (JmmNode expr : list_expr) {
            List<JmmNode> babies_expr = expr.getChildren(Kind.EXPR);
            if (expr.hasAttribute("op")) {
                var op = expr.get("op");
                var op1 = babies_expr.getFirst();
                var op2 = babies_expr.get(1);
                if (op.equals("+")||op.equals("<")||op.equals("-")||op.equals("*")||op.equals("/")) {
                    if (Objects.equals(TypeUtils.getExprType(op1,table), new Type("int", false)) &&
                            Objects.equals(TypeUtils.getExprType(op2,table), new Type("int", false))) {
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

            if (Objects.equals(expr.getKind(), "MethodCallExpr")) {
                List<Symbol> params = table.getParameters(expr.get("name"));
                if (!(params == null) && !params.isEmpty() && !Objects.equals(params.getFirst().getType(), new Type("vararg", true))) {
                    for (int i = 0; i < params.size(); i++) {
                        if (!Objects.equals(TypeUtils.getExprType(babies_expr.get(i + 1), table), params.get(i).getType())) {
                            var message = "Parameter type don't match.";
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
            } else if (Objects.equals(expr.getKind(), "LengthExpr")) {
                JmmNode length_string = babies_expr.getFirst();
                JmmNode length = babies_expr.get(1);


                if ((!Objects.equals(TypeUtils.getExprType(length_string, table), new Type("String", false))
                && !Objects.equals(TypeUtils.getExprType(length_string, table), new Type("int", true)))
                || !Objects.equals(length.get("name"),  "length")) {
                    var message = "Invalid length expression.";
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            message,
                            null)
                    );
                }
            }

            if (Objects.equals(currentMethod, "main") && Objects.equals(expr.getKind(), "ThisExpr")) {
                var message = "THIS expression is not allowed in static methods.";
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        method.getLine(),
                        method.getColumn(),
                        message,
                        null)
                );
            }
        }

        if (!Objects.equals(currentMethod, "main")) {
            JmmNode return_expr = method.getChildren(Kind.EXPR).getFirst();
            Type return_type = TypeUtils.getExprType(return_expr, table);
            Type function_type = TypeUtils.convertType(method.getChildren(Kind.TYPE).getFirst());

            if (!Objects.equals(return_type, function_type)) {
                if (!Objects.equals(return_type, new Type("vararg", false))) {
                    var message = "Return function type is " + function_type + ", not " + return_type;
                    addReport(Report.newError(
                            Stage.SEMANTIC,
                            method.getLine(),
                            method.getColumn(),
                            message,
                            null)
                    );
                } else {
                    if (!Objects.equals(function_type, new Type("int", false))) {
                        var message = "Return function type is " + function_type + ", not " + return_type;
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

        if (Objects.equals(currentMethod, "main")) {
            if (table.getFields().stream()
                    .anyMatch(field -> field.getName().equals(varRefName))) {
                var message = String.format("Cannot access field '%s' in a static method.", varRefName);
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        varRefExpr.getLine(),
                        varRefExpr.getColumn(),
                        message,
                        null)
                );
                return null;
            }
        }

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

        if (Objects.equals(varRefName, "length")) {
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

    private Void visitNewObjectExpr(JmmNode newObjExpr, SymbolTable table) {
        SpecsCheck.checkNotNull(currentMethod, () -> "Expected current method to be set");

        var newObjExprName = newObjExpr.get("name");

        // Import is a declared variable, return
        if (table.getImports().stream()
                .anyMatch(import_ -> import_.equals(newObjExprName))) {
            return null;
        }

        if (Objects.equals(table.getClassName(), newObjExpr.get("name"))) {
            return null;
        }

        // Create error report
        var message = String.format("Variable '%s' does not exist.", newObjExprName);
        addReport(Report.newError(
                Stage.SEMANTIC,
                newObjExpr.getLine(),
                newObjExpr.getColumn(),
                message,
                null)
        );

        return null;
    }


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

    public static boolean isTypeImported(String typeName, SymbolTable table) {
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
