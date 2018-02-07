package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

import java.util.Collections;
import java.util.Map;

/**
 * System Environment supplied by the Runtime.
 *
 */
public class SystemEnv implements Environment {

    @Override
    public String getProperty(String name) {

        return System.getenv(name);
    }

    @Override
    public Map<String, String> asMap() {

        return Collections.unmodifiableMap(System.getenv());
    }
}
