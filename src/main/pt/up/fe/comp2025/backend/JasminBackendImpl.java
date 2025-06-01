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
            String className = ollirResult.getOllirClass().getClassName();
            String constructor = "\n.method public <init>()V\n" +
                                 "    aload_0\n" +
                                 "    invokespecial java/lang/Object/<init>()V\n" +
                                 "    return\n" +
                                 ".end method\n";
            jasminCode = jasminCode.replace(".super java/lang/Object", 
                                          ".super java/lang/Object" + constructor);
        }

        System.out.println("Generated Jasmin:\n" + jasminCode);

        return new JasminResult(ollirResult, jasminCode, jasminGenerator.getReports());
    }

}
