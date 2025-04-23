package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";


    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table, this.ollirTypes);
    }


    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);          // Professores
        addVisit(IMPORT_DECL, this::visitImportDecl);
        addVisit(CLASS_DECL, this::visitClass);          // Professores
        // Var_decl
        // Type
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(PARAM, this::visitParam);

        addVisit(BLOCK_STMT, this::visitBlockStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(EXPR_STMT, this::visitExprStmt);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);

        // Expr

        //setDefaultVisit(this::defaultVisit);
    }


    private String visitProgram(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        node.getChildren().stream()
                .map(this::visit)
                .forEach(code::append);

        return code.toString();
    }


    private String visitImportDecl(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder("import ");

        List<String> imports = node.getObjectAsList("name", String.class);
        String importString = String.join(".", imports);
        code.append(importString);

        code.append(END_STMT);
        return code.toString();
    }


    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());

        String superClass = node.hasAttribute("superClass") ? node.get("superClass") : null;
        if (superClass != null) code.append(" extends ").append(superClass);

        code.append(L_BRACKET);
        code.append(NL);
        code.append(NL);

        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        code.append(R_BRACKET);

        return code.toString();
    }


    private String visitMethodDecl(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = node.getBoolean("isPublic", false);
        if (isPublic) code.append("public ");

        var name = node.get("name");

        if (Objects.equals(name, "main")) { // Muito hardcode?
            code.append("static main(args.array.String).V");

        } else {
            code.append(name);
            // Params
            var params = node.getChild(1);
            code.append(visit(params));
            /*
            List<JmmNode> typeParams = params.getChildren(Kind.TYPE);
            List<String> nameParams = params.getObjectAsList("name", String.class);

            System.out.println("Method parameters: " + nameParams);

            code.append("(");
            if (!typeParams.isEmpty()) {
                for (int i = 0; i < typeParams.size() - 1; i++) {
                    var paramCode = visit(params.getChild(i));
                    code.append(paramCode).append(", ");
                }
                var paramCode = visit(params.getChild(typeParams.size() - 1));
                code.append(paramCode);
            }
            code.append(")");
             */


            // TODO: Hardcoded for int, needs to be expanded -> DONEEE
            // Type
            var retType = OptUtils.toOllirType(node.getChild(0));
            System.out.println("Return type: " + retType);
            code.append(retType);
        }

        // Rest of its children stmts
        code.append(L_BRACKET);

        StringBuilder stmtsCode = new StringBuilder();
        for (var stmt : node.getChildren(STMT)) {
            var codeSnippet = visit(stmt);
            stmtsCode.append(codeSnippet);
        }

        code.append(stmtsCode);

        // Return
        if (Objects.equals(name, "main")) {
            code.append("ret.V;");
        } else {
            List<JmmNode> exprs = node.getChildren(EXPR);
            JmmNode exprNode = exprs.getFirst();

            Type retType = types.getExprType(exprNode, table);

            var expr = exprVisitor.visit(exprNode);
            code.append("   ");
            code.append(expr.getComputation());
            code.append("ret");
            code.append(ollirTypes.toOllirType(retType));
            code.append(SPACE);

            code.append(expr.getCode());

            code.append(END_STMT);
        }

        code.append(R_BRACKET);
        code.append(NL);

        System.out.println("Generated method code: " + code.toString());
        return code.toString();
    }


    private String visitParam(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        List<JmmNode> typeParams = node.getChildren(Kind.TYPE);
        List<String> nameParams = node.getObjectAsList("name", String.class);

        code.append("(");
        if (!typeParams.isEmpty()) {
            for (int i = 0; i < typeParams.size() - 1; i++) {
                var typeCode = ollirTypes.toOllirType(typeParams.get(i));
                var name = nameParams.get(0);
                code.append(name).append(typeCode).append(", ");
            }
            var typeCode = ollirTypes.toOllirType(typeParams.get(typeParams.size() - 1));
            var name = nameParams.get(typeParams.size() - 1);
            code.append(name).append(typeCode);
        }
        code.append(")");
        return code.toString();
    }


    private String visitBlockStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        for (JmmNode child : node.getChildren()) {
            String stmtCode = visit(child);
            code.append(stmtCode);
        }
        return code.toString();
    }


    private String visitIfStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        OllirExprResult ifExpr = exprVisitor.visit(node.getChild(0));
        String thenStmt = visit(node.getChild(1));
        String elseStmt = visit(node.getChild(2));
        String ifNum = "0";

        code.append(ifExpr.getComputation());

        code.append("if (").append(ifExpr.getCode()).append(") goto then").append(ifNum).append(END_STMT);
        code.append(elseStmt);
        code.append("goto endif").append(ifNum).append(END_STMT);
        code.append(NL);

        code.append("then").append(ifNum).append(":\n");
        code.append(thenStmt);
        code.append("endif").append(ifNum).append(":\n");

        return code.toString();
    }


    private String visitExprStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        OllirExprResult exprOllir = exprVisitor.visit(node.getChild(0));
        code.append(exprOllir.getComputation());
        code.append(exprOllir.getCode()).append(";\n");
        return code.toString();
    }


    private String visitAssignStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        JmmNode lhs = node.getChild(0);
        JmmNode rhs = node.getChild(1);

        OllirExprResult rhsExpr = exprVisitor.visit(rhs);

        Type leftType = TypeUtils.getExprType(lhs, table);
        String ollirType = ollirTypes.toOllirType(leftType);

        code.append(rhsExpr.getComputation());

        if (lhs.getKind().equals(ARRAY_ELEM_EXPR)) {
            JmmNode arrayName = lhs.getChild(0);
            JmmNode indexExpr = lhs.getChild(1);

            OllirExprResult indexResult = exprVisitor.visit(indexExpr);
            code.append(indexResult.getComputation());

            OllirExprResult arrayResult = exprVisitor.visit(arrayName);
            code.append(arrayResult.getComputation());

            code.append(arrayResult.getCode())
                    .append("[").append(indexResult.getCode()).append("]")
                    .append(ollirType).append(SPACE)
                    .append(ASSIGN).append(ollirType).append(SPACE)
                    .append(rhsExpr.getCode()).append(END_STMT);
        } else {
            String varName;
            if (lhs.getKind().equals(VAR_REF_EXPR)) {
                varName = lhs.get("name");
            } else {
                OllirExprResult lhsExpr = exprVisitor.visit(lhs);
                code.append(lhsExpr.getComputation());
                code.append(lhsExpr.getCode()).append(SPACE)
                        .append(ASSIGN).append(ollirType).append(SPACE)
                        .append(rhsExpr.getCode()).append(END_STMT);
                return code.toString();
            }

            String tmp = ollirTypes.nextTemp() + ollirType;
            code.append(tmp).append(SPACE).append(ASSIGN).append(ollirType).append(SPACE)
                    .append(rhsExpr.getCode()).append(END_STMT);

            code.append(varName).append(ollirType).append(SPACE)
                    .append(ASSIGN).append(ollirType).append(SPACE)
                    .append(tmp).append(END_STMT);
        }

        return code.toString();
    }

    private String visitWhileStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        OllirExprResult condExpr = exprVisitor.visit(node.getChild(0));

        String loopBody = visit(node.getChild(1));

        String whileLabel = "while0";
        String endLabel = "endwhile0";

        code.append(whileLabel).append(":\n");
        code.append(condExpr.getComputation());
        code.append("if (").append(condExpr.getCode()).append(") goto body").append(whileLabel).append(";\n");
        code.append("goto ").append(endLabel).append(";\n");
        code.append("body").append(whileLabel).append(":\n");
        code.append(loopBody);
        code.append("goto ").append(whileLabel).append(";\n");
        code.append(endLabel).append(":\n");

        return code.toString();
    }

    private String visitArrayAssignStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        JmmNode array = node.getChild(0);
        JmmNode index = node.getChild(1);
        JmmNode value = node.getChild(2);

        OllirExprResult arrayExpr = exprVisitor.visit(array);
        OllirExprResult indexExpr = exprVisitor.visit(index);
        OllirExprResult valueExpr = exprVisitor.visit(value);

        Type elementType = TypeUtils.getExprType(value, table);
        String ollirType = ollirTypes.toOllirType(elementType);

        code.append(arrayExpr.getComputation());
        code.append(indexExpr.getComputation());
        code.append(valueExpr.getComputation());

        code.append(arrayExpr.getCode())
                .append("[").append(indexExpr.getCode()).append("]")
                .append(ollirType).append(SPACE)
                .append(ASSIGN).append(ollirType).append(SPACE)
                .append(valueExpr.getCode()).append(END_STMT);

        return code.toString();
    }


    private String buildConstructor() {

        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }


    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
