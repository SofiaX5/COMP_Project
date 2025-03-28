package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;

import java.util.List;
import java.util.Objects;

public class VarargPasses extends AnalysisVisitor {
    public VarargPasses(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.PARAM, this::visitParam);
    }

    private Void visitParam(JmmNode param, SymbolTable table) {
        if (param.getChildren().isEmpty()) {
            return null;
        }

        JmmNode paramType = param.getChild(0);

        Boolean isEllipsis = Objects.requireNonNullElse(paramType.getObject("isEllipsis", Boolean.class), false);
        if (!isEllipsis) {
            return null;
        }

        String methodName = param.getParent().get("name");
        List<Symbol> parameters = table.getParameters(methodName);

        if (parameters == null || parameters.isEmpty()) {
            return null;
        }

        if (parameters.size() > 1) {
            String lastParamName = parameters.get(parameters.size() - 1).getName();
            if (!Objects.equals(lastParamName, param.get("name"))) {
                addReport(Report.newError(Stage.SEMANTIC, param.getLine(), param.getColumn(),
                        "Vararg must be the last parameter of the method", null));
            }
        }

        return null;
    }

}
