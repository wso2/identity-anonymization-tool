package org.wso2.carbon.identity.instructions;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JdbcForgetMeInstructionReader implements InstructionReader {

    @Override
    public String getType() {
        return "rdbms";
    }

    @Override
    public ForgetMeInstruction read(File contendDirectory, Environment environment) {
        System.out.println("Reading JdbcForgetMeInstructionReader");

        Path sqlDir = Paths.get(contendDirectory.getAbsolutePath(), "sql");
        Path dataSourceConfigDir = Paths.get(contendDirectory.getAbsolutePath(), "datasources");
        RdbmsForgetMeInstruction forgetMeInstruction = new RdbmsForgetMeInstruction(sqlDir, dataSourceConfigDir,
                "WSO2_CARBON_DB");
        return forgetMeInstruction;
    }
}
