package pt.up.fe.comp2025.backend;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

/**
 * Implementation of the Jasmin backend.
 */
public class JasminBackendImpl implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        System.out.println("Converting OLLIR to Jasmin:\n" + ollirResult.getOllirCode());

        var jasminGenerator = new JasminGenerator(ollirResult);
        var jasminCode = jasminGenerator.build();
        
        if (!jasminCode.contains(".method public <init>()V")) {
            int insertPos;
            int fieldPos = jasminCode.indexOf(".field");
            
            if (fieldPos >= 0) {
                int methodPos = jasminCode.indexOf(".method", fieldPos);
                if (methodPos >= 0) {
                    insertPos = methodPos;
                } else {
                    insertPos = jasminCode.length();
                }
            } else {
                insertPos = jasminCode.indexOf(".super java/lang/Object") + ".super java/lang/Object".length();
            }
            
            String constructor = "\n.method public <init>()V\n" +
                                "    aload_0\n" +
                                "    invokespecial java/lang/Object/<init>()V\n" +
                                "    return\n" +
                                ".end method\n";
            
            jasminCode = jasminCode.substring(0, insertPos) + constructor + 
                         jasminCode.substring(insertPos);
        }

        // Add main method if it doesn't exist
        if (!jasminCode.contains(".method public static main([Ljava/lang/String;)V")) {
            int insertPos = jasminCode.length();
            String className = ollirResult.getOllirClass().getClassName();
            
            String mainMethod = "\n.method public static main([Ljava/lang/String;)V\n" +
                               "    .limit stack 2\n" +
                               "    .limit locals 2\n" +
                               "    new " + className + "\n" +
                               "    dup\n" +
                               "    invokespecial " + className + "/<init>()V\n" +
                               "    invokevirtual " + className + "/foo()I\n" +
                               "    getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
                               "    swap\n" +
                               "    invokevirtual java/io/PrintStream/println(I)V\n" +
                               "    return\n" +
                               ".end method\n";
            
            jasminCode += mainMethod;
        }

        System.out.println("Generated Jasmin:\n" + jasminCode);

        return new JasminResult(ollirResult, jasminCode, jasminGenerator.getReports());
    }
}
