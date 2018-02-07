package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default environment which can be used as a property set.
 *
 */
public class DefaultEnvironment implements Environment {

    private Map<String, String> properties = new HashMap<>();

    @Override
    public String getProperty(String name) {

        return properties.get(name);
    }

    @Override
    public Map<String, String> asMap() {

        return Collections.unmodifiableMap(properties);
    }

    public void setProperty(String name, String value) {

        properties.put(name, value);
    }
}
