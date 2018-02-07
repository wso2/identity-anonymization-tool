package org.wso2.carbon.privacy.forgetme.runtime;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;

/**
 * Utility to resolve String in the form
 * "Hello ${user}, You need to eat ${food.name}".
 *
 */
public class VariableResolver {

    private Environment environment;
    private StrSubstitutor substitutor;

    public VariableResolver(Environment environment) {

        this.environment = environment;
        substitutor = new StrSubstitutor(environment.asMap());
    }

    public String resolve(String string) {

        return substitutor.replace(string);
    }
}
