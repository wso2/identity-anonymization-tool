package org.wso2.carbon.privacy.forgetme.config;

import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents System Configuration.
 *
 */
public class SystemConfig {

    private Map<Path, InstructionReader> directoryToInstructionReaderMap = new HashMap<>();
    private Map<String, ProcessorConfig> processorConfigMap = new HashMap<>();
    private List<String> processors = new ArrayList<>();

    public void addInstructionReader(Path dir, InstructionReader instructionReader) {
        directoryToInstructionReaderMap.put(dir, instructionReader);
    }

    public Map<Path, InstructionReader> getDirectoryToInstructionReaderMap() {
        return Collections.unmodifiableMap(directoryToInstructionReaderMap);
    }

    public void addProcessorConfig(String processorName, ProcessorConfig processorConfig) {
        processorConfigMap.put(processorName, processorConfig);
    }

    public Map<String, ProcessorConfig> getProcessorConfigMap() {
        return Collections.unmodifiableMap(processorConfigMap);
    }

    public void addProcessor(String processor) {
        processors.add(processor);
    }

    public List<String> getProcessors() {
        return Collections.unmodifiableList(processors);
    }
}
