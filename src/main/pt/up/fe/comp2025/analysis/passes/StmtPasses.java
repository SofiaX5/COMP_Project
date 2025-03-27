package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;

import java.util.List;
import java.util.Objects;

public class StmtPasses extends AnalysisVisitor {
    public StmtPasses(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_DECL, this::checkExpr);
    }

    private Void checkExpr(JmmNode method, SymbolTable table) {
        String currentMethod = method.get("name");
        List<Symbol> locals = table.getLocalVariables(currentMethod);

        List<JmmNode> list_stmt = method.getChildren(Kind.STMT);
        for (JmmNode stmt : list_stmt) {
            List<JmmNode> babies_expr = stmt.getChildren(Kind.EXPR);
            if (babies_expr.size() >= 2) {
                JmmNode baby1 = babies_expr.getFirst();
                JmmNode baby2 = babies_expr.get(1);

                if (!Objects.equals(TypeUtils.getExprType(baby1, table),TypeUtils.getExprType(baby2, table)) ) {
                    var message = "Assignment types not matching:" + TypeUtils.getExprType(baby1, table) + " and " + TypeUtils.getExprType(baby2, table);
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

        return null;
    }

}

