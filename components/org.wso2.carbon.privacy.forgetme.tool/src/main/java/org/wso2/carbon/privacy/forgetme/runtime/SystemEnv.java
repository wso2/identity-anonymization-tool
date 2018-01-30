package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

public class SystemEnv implements Environment {

    @Override
    public String getProperty(String name) {
        return System.getenv(name);
    }
}
