package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

public class NestedEnv implements Environment {

    private Environment parentEnv;
    private Environment childEnv;

    public NestedEnv(Environment parentEnv, Environment childEnv) {
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
}
