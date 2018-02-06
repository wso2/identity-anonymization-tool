package org.wso2.carbon.privacy.forgetme.config;

import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Represents System Configuration.
 *
 */
public class SystemConfig {

    private Map<Path, InstructionReaderConfig> directoryToInstructionReaderMap = new HashMap<>();
    private Map<String, ProcessorConfig> processorConfigMap = new HashMap<>();
    private List<String> processors = new ArrayList<>();
    private Path workDir;

    /**
     * Adds an instruction reader with the given path.
     *
     * @param dir
     * @param instructionReader
     */
    public void addInstructionReader(Path dir, InstructionReader instructionReader, Properties properties) {
        directoryToInstructionReaderMap.put(dir, new InstructionReaderConfig(instructionReader, properties));
    }

    public Map<Path, InstructionReaderConfig> getDirectoryToInstructionReaderMap() {
        return Collections.unmodifiableMap(directoryToInstructionReaderMap);
    }

    /**
     * Adds a processor configuration agianst the given processor name.
     *
     * @param processorName
     * @param processorConfig
     */
    public void addProcessorConfig(String processorName, ProcessorConfig processorConfig) {
        processorConfigMap.put(processorName, processorConfig);
    }

    public Map<String, ProcessorConfig> getProcessorConfigMap() {
        return Collections.unmodifiableMap(processorConfigMap);
    }

    /**
     * Add a processor name, effectively enables the processor.
     *
     * @param processor
     */
    public void addProcessor(String processor) {
        processors.add(processor);
    }

    /**
     * Returns the active list of processor names.
     * @return
     */
    public List<String> getProcessors() {
        return Collections.unmodifiableList(processors);
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }
}
