package org.wso2.carbon.privacy.forgetme.runtime;

import static org.testng.Assert.assertEquals;

/**
 * Test for VariableResolver.
 */
public class VariableResolverTest {

    @org.testng.annotations.Test
    public void testResolve() throws Exception {

        DefaultEnvironment env = new DefaultEnvironment();
        env.setProperty("Var1", "Val1");
        env.setProperty("Var2", "Val2");

        VariableResolver variableResolver = new VariableResolver(env);

        assertEquals(variableResolver.resolve("${Var1} == Val1"), "Val1 == Val1");
    }
}