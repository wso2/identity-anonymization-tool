package org.wso2.carbon.privacy.forgetme.runtime;

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

import java.util.Properties;

public class DefaultEnv implements Environment {

    private Properties properties = new Properties();

    @Override
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }
}
