package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NestedEnvironment implements Environment {

    private Environment parentEnv;
    private Environment childEnv;

    public NestedEnvironment(Environment parentEnv, Environment childEnv) {

        this.parentEnv = parentEnv;
        this.childEnv = childEnv;
    }

    @Override
    public String getProperty(String name) {

        String value = childEnv.getProperty(name);
        if (value == null) {
            value = parentEnv.getProperty(name);
        }
        return value;
    }

    @Override
    public Map<String, String> asMap() {

        Map<String, String> result = new HashMap<>();
        parentEnv.asMap().forEach((key, value) -> result.put(key, value));
        childEnv.asMap().forEach((key, value) -> result.put(key, value));
        return Collections.unmodifiableMap(result);
    }
}
