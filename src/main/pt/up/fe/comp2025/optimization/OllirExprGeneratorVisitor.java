package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;
import java.util.Objects;

import static pt.up.fe.comp2025.ast.Kind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 */
public class OllirExprGeneratorVisitor extends PreorderJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";

    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    public OllirExprGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
    }


    @Override
    protected void buildVisitor() {
        // parenthesis
        addVisit(NEW_ARRAY_EXPR, this::visitNewArray);
        addVisit(NEW_OBJECT_EXPR, this::visitNewObject);
        addVisit(ARRAY_ELEM_EXPR, this::visitArrayElem);
        addVisit(LENGTH_EXPR, this::visitLength);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCall);
        addVisit(NOT_EXPR, this::visitNot);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(ARRAY_EXPR, this::visitArray);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(THIS_EXPR, this::visitThis);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        // setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitNewArray(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        OllirExprResult exprOllir = visit(node.getChild(0));
        code.append("new(array,").append(exprOllir.getCode()).append(").array.i32");
        return new OllirExprResult(code.toString());
    }

    private OllirExprResult visitNewObject(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitArrayElem(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitLength(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        code.append("invokestatic(");

        JmmNode importExpr = node.getChild(0);
        String funcName = node.get("name");
        code.append(importExpr.get("name")).append(", \"").append(funcName).append("\"");

        if (node.getNumChildren() > 1) {
            List<JmmNode> params = node.getChildren(EXPR);
            for (int i = 1; i < node.getNumChildren(); i++) {
                JmmNode param = params.get(i);
                OllirExprResult paramOllir = visit(param);
                code.append(", ").append(paramOllir.getCode());
            }
        }
        code.append(").V;\n");
        return new OllirExprResult(code.toString());
    }

    private OllirExprResult visitNot(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        // code to compute the children
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        // code to compute self
        Type resType = types.getExprType(node,table);
        String resOllirType = ollirTypes.toOllirType(resType);
        String code = ollirTypes.nextTemp() + resOllirType;

        computation.append(code).append(SPACE)
                .append(ASSIGN).append(resOllirType).append(SPACE)
                .append(lhs.getCode()).append(SPACE);

        Type type = types.getExprType(node,table);
        computation.append(node.get("op")).append(ollirTypes.toOllirType(type)).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(code, computation);
    }

    private OllirExprResult visitArray(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        var intType = TypeUtils.newIntType();
        String ollirIntType = ollirTypes.toOllirType(intType);

        String code = node.get("value") + ollirIntType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        var boolType = TypeUtils.newBoolType();
        String ollirBoolType = ollirTypes.toOllirType(boolType);

        String value;
        if (Objects.equals(node.get("value"), "true")) value = "1";
        else value = "0";

        String code = value + ollirBoolType;
        return new OllirExprResult(code);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        String code = "";
        return new OllirExprResult(code);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {

        var id = node.get("name");
        Type type = types.getExprType(node,table);
        String ollirType = ollirTypes.toOllirType(type);

        String code = id + ollirType;

        return new OllirExprResult(code);
    }

    /**
     * Default visitor. Visits every child node and return an empty result.
     *
     * @param node
     * @param unused
     * @return
     */
    private OllirExprResult defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return OllirExprResult.EMPTY;
    }

}
