package org.wso2.carbon.identity.instructions;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JdbcForgetMeInstructionReader implements InstructionReader {

    @Override
    public String getType() {
        return "rdbms";
    }

    @Override
    public List<ForgetMeInstruction> read(Path path, Properties properties, Environment environment) {

        List<ForgetMeInstruction> result = new ArrayList<>();

        File contendDirectory = path.toFile();
        File[] subDirs = contendDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        for (int i = 0; i < subDirs.length; i++) {
            Path subPath = subDirs[i].toPath();
            RdbmsForgetMeInstruction forgetMeInstruction = new RdbmsForgetMeInstruction(subPath);
            result.add(forgetMeInstruction);
        }

        return result;
    }
}
