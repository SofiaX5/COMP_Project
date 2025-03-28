package pt.up.fe.comp2025.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2025.analysis.AnalysisVisitor;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class UndeclaredPasses extends AnalysisVisitor {

    private TypeUtils typeUtils;

    public UndeclaredPasses(SymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    public void buildVisitor() {
        addVisit(Kind.METHOD_CALL_EXPR, this::visitMethodCall);
    }

    private Void visitMethodCall(JmmNode methodCall, SymbolTable table) {
        String methodName = methodCall.get("name");

        List<String> methods = table.getMethods();

        if (methods.contains(methodName)) {
            return null;
        }

        JmmNode expr = methodCall.getChild(0);

        Type type = TypeUtils.getExprType(expr, table);
        if (table.getImports().stream().anyMatch(i -> i.equals(type.getName()))) {
            return null;
        }

        String superClass = table.getSuper();
        if (type.getName().equals(table.getClassName()) && superClass != null && !superClass.isEmpty()) {
            return null;
        }

        addReport(Report.newError(Stage.SEMANTIC, methodCall.getLine(), methodCall.getColumn(),
                String.format("Method '%s' does not exist.", methodName), null));

        return null;
    }


}
