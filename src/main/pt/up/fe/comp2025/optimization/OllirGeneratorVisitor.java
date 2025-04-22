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
        exprVisitor = new OllirExprGeneratorVisitor(table);
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
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        // Expr

        addVisit(RETURN_STMT, this::visitReturn);

        // setDefaultVisit(this::defaultVisit);
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
        code.append(name);

        if (!Objects.equals(name, "main")) { // Muito hardcode?
            // TODO: Hardcoded for a single parameter, needs to be expanded  -> DONEEE?????
            // Params
            var params = node.getChild(1);
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


            // TODO: Hardcoded for int, needs to be expanded -> DONEEE
            // Type
            var retType = OptUtils.toOllirType(node.getChild(0));
            System.out.println("Return type: " + retType);
            code.append(retType);
        }

        // Rest of its children stmts
        code.append(L_BRACKET);

        var stmtsCode = node.getChildren(STMT).stream()
                .map(this::visit)
                .collect(Collectors.joining("\n   ", "   ", ""));
        code.append(stmtsCode);

        code.append(R_BRACKET);
        code.append(NL);

        System.out.println("Generated method code: " + code.toString());
        return code.toString();
    }


    private String visitParam(JmmNode node, Void unused) {

        var typeCode = ollirTypes.toOllirType(node.getChild(0));
        var id = node.get("name");

        String code = id + typeCode;

        return code;
    }


    private String visitAssignStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();

        // code to compute self
        // statement has type of lhs
        JmmNode lhs = node.getChild(0);
        Type leftType = TypeUtils.getExprType(lhs, table);
        String ollirType = ollirTypes.toOllirType(leftType);
        var varCode = lhs.get("name") + ollirType;

        // code to compute the children
        var rhsExpr = exprVisitor.visit(node.getChild(1));
        code.append(rhsExpr.getComputation());


        code.append(varCode);
        code.append(SPACE);

        code.append(ASSIGN);
        code.append(ollirType);
        code.append(SPACE);

        code.append(rhsExpr.getCode());

        code.append(END_STMT);

        return code.toString();
    }


    private String visitReturn(JmmNode node, Void unused) {
        // TODO: Hardcoded for int type, needs to be expanded
        //Type retType = TypeUtils.newIntType();
        Type retType = types.getExprType(node.getChild(0), table);

        StringBuilder code = new StringBuilder();


        var expr = node.getNumChildren() > 0 ? exprVisitor.visit(node.getChild(0)) : OllirExprResult.EMPTY;


        code.append(expr.getComputation());
        code.append("ret");
        code.append(ollirTypes.toOllirType(retType));
        code.append(SPACE);

        code.append(expr.getCode());

        code.append(END_STMT);

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
