package pt.up.fe.comp2025.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2025.ast.Kind;
import pt.up.fe.comp2025.ast.TypeUtils;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;

import static pt.up.fe.comp2025.ast.Kind.*;

public class JmmSymbolTableBuilder {

    // In case we want to already check for some semantic errors during symbol table building.
    private List<Report> reports;

    public List<Report> getReports() {
        return reports;
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                node.getLine(),
                node.getColumn(),
                message,
                null);
    }

    public JmmSymbolTable build(JmmNode root) {
        reports = new ArrayList<>();

        // TODO: After your grammar supports more things inside the program (e.g., imports) you will have to change this
        var classDecl = root.getChildren(CLASS_DECL).getFirst();
        SpecsCheck.checkArgument(Kind.CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);
        String className = classDecl.get("name");
        String superClass = classDecl.hasAttribute("superClass") ? classDecl.get("superClass") : null;

        var methods = buildMethods(classDecl);
        var returnTypes = buildReturnTypes(classDecl);
        var params = buildParams(classDecl);
        var locals = buildLocals(classDecl);
        var importsDecl = root.getChildren(IMPORT_DECL);
        var imports = buildImports(importsDecl);
        var fields = buildFields(classDecl);

        return new JmmSymbolTable(className, superClass, methods, returnTypes, params, locals, imports, fields);
    }


    private Map<String, Type> buildReturnTypes(JmmNode classDecl) {
        Map<String, Type> map = new HashMap<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            if (method.hasAttribute("name")) {
                var name = method.get("name");

                if (method.getNumChildren() > 0) {
                    var typeNode = method.getChild(0);
                    Type returnType = TypeUtils.convertType(typeNode);
                    map.put(name, returnType);
                } else {
                    map.put(name, new Type("void", false));
                }
            }
        }

        return map;
    }


    private Map<String, List<Symbol>> buildParams(JmmNode classDecl) {
        Map<String, List<Symbol>> map = new HashMap<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            var name = method.get("name");

            if (Objects.equals(name, "main")) {
                List<Symbol> params = new ArrayList<>();
                params.add(new Symbol(new Type("String", true), method.get("id")));
                map.put(name, params);
            } else {
                List<JmmNode> paramList = method.getChildren(PARAM);
                if (!paramList.isEmpty()) {
                    var param = method.getChildren(PARAM).getFirst();
                    List <String> names = param.getObjectAsList("name", String.class);
                    var typeList = param.getChildren(TYPE);

                    List<Symbol> params = new ArrayList<>();
                    for (var typeNode : typeList) {
                        var type = TypeUtils.convertType(typeNode);
                        params.add(new Symbol(type, names.getFirst()));
                        names.removeFirst();
                    }
                    map.put(name, params);
                }
            }
        }

        return map;
    }

    private Map<String, List<Symbol>> buildLocals(JmmNode classDecl) {
        var map = new HashMap<String, List<Symbol>>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            if (method.hasAttribute("name")) {
                var name = method.get("name");
                var locals = method.getChildren(VAR_DECL).stream()
                        // TODO: When you support new types, this code has to be update :) VISTO?
                        .map(varDecl -> new Symbol(TypeUtils.convertType(varDecl.getChild(0)), varDecl.get("name")))
                        .toList();
                map.put(name, locals);
            }
        }

        return map;
    }

    private List<String> buildMethods(JmmNode classDecl) {
        List<String> methods = new ArrayList<>();

        for (var method : classDecl.getChildren(METHOD_DECL)) {
            methods.add(method.get("name"));
        }

        return methods;
    }

    private List<String> buildImports(List <JmmNode> importsDecl) {
        List<String> imports = new ArrayList<>();

        for (var importDecl : importsDecl) {
            List <String> import_ = importDecl.getObjectAsList("name", String.class);
            System.out.println(import_.size());
            imports.add(String.join(",", import_));
        }

        return imports;
    }

    private List<Symbol> buildFields(JmmNode classDecl) {
        List<Symbol> fields = new ArrayList<>();

        for (JmmNode var : classDecl.getChildren(VAR_DECL)) {
            var typeNode = var.getChild(0);
            Type typeName = TypeUtils.convertType(typeNode);
            String name = var.get("name");
            fields.add(new Symbol(typeName, name));
        }

        return fields;
    }

}
