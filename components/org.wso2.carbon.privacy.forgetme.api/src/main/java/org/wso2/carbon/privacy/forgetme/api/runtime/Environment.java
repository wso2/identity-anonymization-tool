package org.wso2.carbon.privacy.forgetme.api.runtime;

import java.util.Map;

/**
 * Definition for runtime environment variables set.
 */
public interface Environment {

    String getProperty(String name);

    /**
     * Returns a map view of the Environment variables.
     *
     * @return
     */
    Map<String, String> asMap();
}
