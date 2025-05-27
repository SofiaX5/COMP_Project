package pt.up.fe.comp2025.optimization;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.ArrayList;
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


    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes;;
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
        addVisit(PARENTHESIZES_EXPR, this::visitParenthesizesExpr);

        // setDefaultVisit(this::defaultVisit);
    }

    private OllirExprResult visitNewArray(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        OllirExprResult exprOllir = visit(node.getChild(0));
        code.append("new(array,").append(exprOllir.getCode()).append(").array.i32");
        return new OllirExprResult(code.toString());
    }

    private OllirExprResult visitNewObject(JmmNode node, Void unused) {
        String className = node.get("name");
        String tempVar = ollirTypes.nextTemp();

        StringBuilder computation = new StringBuilder();
        StringBuilder code = new StringBuilder();

        computation.append(tempVar).append(".").append(className)
                .append(" :=.").append(className)
                .append(" new(").append(className).append(").").append(className)
                .append(END_STMT);

        computation.append("invokespecial(").append(tempVar).append(".").append(className)
                .append(", \"<init>\").V").append(END_STMT);

        code.append(tempVar).append(".").append(className);

        return new OllirExprResult(code.toString(), computation.toString());
    }

    private OllirExprResult visitArrayElem(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();
        StringBuilder code = new StringBuilder();

        JmmNode arrayName = node.getChild(0);
        JmmNode arrayIndex = node.getChild(1);

        OllirExprResult arrayResult = visit(arrayName);
        OllirExprResult indexResult = visit(arrayIndex);

        computation.append(arrayResult.getComputation());
        computation.append(indexResult.getComputation());

        String indexCode = indexResult.getCode();
        if (indexCode.contains("invokestatic") || indexCode.contains("invokevirtual") || indexCode.contains("[")) {
            String tempVar = ollirTypes.nextTemp();
            computation.append(tempVar).append(".i32 :=.i32 ").append(indexCode).append(";\n");
            indexCode = tempVar + ".i32";
        }

        code.append(arrayResult.getCode())
                .append("[").append(indexCode).append("]")
                .append(".i32");

        return new OllirExprResult(code.toString(), computation.toString());
    }


    private OllirExprResult visitLength(JmmNode node, Void unused) {
        JmmNode arrayExpr = node.getChild(0);
        OllirExprResult arrayResult = visit(arrayExpr);

        String tempVar = ollirTypes.nextTemp();

        StringBuilder computation = new StringBuilder();
        computation.append(arrayResult.getComputation());

        computation.append(tempVar).append(".i32 :=.i32 ")
                .append("arraylength(")
                .append(arrayResult.getCode())
                .append(").i32").append(END_STMT);

        return new OllirExprResult(tempVar + ".i32", computation.toString());
    }

    private OllirExprResult visitMethodCall(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        StringBuilder computation = new StringBuilder();

        JmmNode target = node.getChild(0);
        String methodName = node.get("name");
        List<JmmNode> paramNodes = node.getChildren().subList(1, node.getNumChildren());

        String targetName;
        boolean isStaticCall = false;

        if (target.getKind().equals("ThisExpr")) {
            targetName = "this";
        } else {
            targetName = target.get("name");

            for (String importStr : table.getImports()) {
                // REVER
                String simpleImport = importStr.contains(".") ?
                        importStr.substring(importStr.lastIndexOf('.') + 1) : importStr;
                if (simpleImport.equals(targetName)) {
                    isStaticCall = true;
                    break;
                }
            }
        }

        /* QUESTIONÁVEL REVER (E APAGAR O QUE ESTÁ EM BAIXO
        boolean isVarargsCall = false;
        if (!isStaticCall && (targetName.equals("this") || table.getClassName().equals(targetName))) {
            if (table.getMethods().contains(methodName)) {
                List<Symbol> paramsMethod = table.getParameters(methodName);

                if (!paramNodes.isEmpty()) {
                    Type lastParam = paramsMethod.get(paramsMethod.size() - 1).getType();

                    if (lastParam.isArray()) { // Como obtenho o jmmnode me vez do type :(
                    EM PROCESSO
                    if (numArgs < params.size() && !Objects.equals(params.getFirst().getType(), new Type("vararg", true))) {

                        if (paramNodes.size() >= paramTypes.size()) {
                            isVarargsCall = true;
                        }
                    }
                }
            }
        }
         */

        boolean isVarargsCall = false;
        if (!isStaticCall && !targetName.equals("this")) {

        } else if (table.getMethods().contains(methodName)) {
            isVarargsCall = table.getParameters(methodName).size() > 0 &&
                    paramNodes.size() > table.getParameters(methodName).size();
        }



        List<String> paramCodes = new ArrayList<>();

        Type returnType = types.getExprType(node, table);
        String ollirRetType = ollirTypes.toOllirType(returnType);

        if (isVarargsCall) {
            String arrayTempVar = ollirTypes.nextTemp();
            computation.append(arrayTempVar).append(".array.i32 :=.array.i32 ");
            computation.append("new(array, ").append(paramNodes.size()).append(".i32).array.i32").append(END_STMT);

            for (int i = 0; i < paramNodes.size(); i++) {
                OllirExprResult paramResult = visit(paramNodes.get(i));
                computation.append(paramResult.getComputation());

                computation.append(arrayTempVar).append("[").append(i).append(".i32].i32 :=.i32 ");
                computation.append(paramResult.getCode()).append(END_STMT);
            }

            paramCodes.add(arrayTempVar + ".array.i32");

        } else {
            for (JmmNode param : paramNodes) {
                OllirExprResult paramResult = visit(param);
                computation.append(paramResult.getComputation());

                String paramCode = paramResult.getCode();
                if (paramCode.contains("invoke") || paramCode.contains("[") || paramCode.contains("/")) {
                    String tempParam = ollirTypes.nextTemp() + ".i32";
                    computation.append(tempParam).append(" :=.i32 ").append(paramCode).append(";\n");
                    paramCode = tempParam;
                }

                paramCodes.add(paramCode);
            }
        }

        if (isStaticCall) {
            code.append("invokestatic(")
                    .append(targetName).append(", \"").append(methodName).append("\"");

            for (String paramCode : paramCodes) {
                code.append(", ").append(paramCode);
            }

            code.append(")").append(ollirRetType);
        } else {
            OllirExprResult targetResult;

            if (targetName.equals("this")) {
                targetResult = new OllirExprResult("this."+table.getClassName(), "");
            } else {
                targetResult = visit(target);
                computation.append(targetResult.getComputation());
            }

            code.append("invokevirtual(")
                    .append(targetResult.getCode())
                    .append(", \"").append(methodName).append("\"");

            for (String paramCode : paramCodes) {
                code.append(", ").append(paramCode);
            }

            JmmNode smtm = node.getParent();
            returnType = types.getExprType(smtm.getChildren().getFirst(), table);
            ollirRetType = ollirTypes.toOllirType(returnType);

            code.append(")").append(ollirRetType);
        }

        return new OllirExprResult(code.toString(), computation.toString());
    }


    private OllirExprResult visitNot(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        StringBuilder computation = new StringBuilder();

        String tempVar = ollirTypes.nextTemp();
        var boolType = TypeUtils.newBoolType();
        String ollirType = ollirTypes.toOllirType(boolType);

        JmmNode expr = node.getChild(0);
        OllirExprResult exprResult = visit(expr);
        code.append(tempVar).append(ollirType);

        computation.append(tempVar).append(ollirType)
                .append(" :=").append(ollirType).append(" !").append(ollirType)
                .append(SPACE).append(exprResult.getCode()).append(END_STMT);
        return new OllirExprResult(code.toString(), computation.toString());
    }


    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        String op = node.get("op");

        if (op.equals("&&")) {
            return handleLogicalAnd(node);
        }

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

    private OllirExprResult handleLogicalAnd(JmmNode node) {
        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        StringBuilder computation = new StringBuilder();

        computation.append(lhs.getComputation());

        String labelId = ollirTypes.nextTemp().substring(4);
        String andEndLabel = "and_end_" + labelId;

        String resultVar = ollirTypes.nextTemp() + ".bool";

        computation.append("if (").append(lhs.getCode()).append(") goto ").append(andEndLabel).append("_check_rhs;\n");

        computation.append(resultVar).append(" :=.bool 0.bool;\n");
        computation.append("goto ").append(andEndLabel).append(";\n");

        computation.append(andEndLabel).append("_check_rhs:\n");
        computation.append(rhs.getComputation());

        computation.append(resultVar).append(" :=.bool ").append(rhs.getCode()).append(";\n");

        computation.append(andEndLabel).append(":\n");

        return new OllirExprResult(resultVar, computation.toString());
    }

    private OllirExprResult visitArray(JmmNode node, Void unused) {
        StringBuilder computation = new StringBuilder();
        String arrayName = "a";

        int index = 0;
        for (JmmNode child : node.getChildren()) {
            OllirExprResult value = visit(child);
            String indexLiteral = index + ".i32";

            computation.append(arrayName).append("[").append(indexLiteral).append("]")
                    .append(".i32 :=.i32 ").append(value.getCode()).append(";\n");

            computation.append("invokestatic(ioPlus, \"printResult\", ")
                    .append(arrayName).append("[").append(indexLiteral).append("].i32").append(").V;\n");

            index++;
        }

        return new OllirExprResult("", computation.toString());
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
        return new OllirExprResult("this." + table.getClassName());
    }


    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        StringBuilder computation = new StringBuilder();

        var name = node.get("name");
        final var refName = name;
        Type type = types.getExprType(node,table);
        String ollirType = ollirTypes.toOllirType(type);

        JmmNode parent = node.getParent();
        if (parent.getKind().equals(Kind.METHOD_DECL.toString())) {
            String methodName = parent.get("name");
            boolean isField = table.getLocalVariables(methodName).stream().map(Symbol::getName).noneMatch(varName -> varName.equals(refName))
                                && table.getParameters(methodName).stream().map(Symbol::getName).noneMatch(varName -> varName.equals(refName));
            if (isField) {
                name = ollirTypes.nextTemp();

                computation.append(name).append(ollirType)
                        .append(" :=").append(ollirType)
                        .append(" getfield(this,").append(name).append(ollirType).append(")").append(ollirType)
                        .append(END_STMT);
            }
        }
        code.append(name).append(ollirType);
        return new OllirExprResult(code.toString(), computation.toString());
    }

    private OllirExprResult visitParenthesizesExpr(JmmNode node, Void unused) {
        OllirExprResult innerExpr = visit(node.getChild(0));

        return innerExpr;
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
