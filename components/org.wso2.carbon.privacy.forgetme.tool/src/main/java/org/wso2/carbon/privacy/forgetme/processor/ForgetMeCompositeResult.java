package org.wso2.carbon.privacy.forgetme.processor;

import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite result which carries other result within nested result tree.
 *
 */
public class ForgetMeCompositeResult extends ForgetMeResult {

    private List<ForgetMeResult> parts = new ArrayList<>();

    /**
     * Adds an entry to the composite result.
     *
     * @param entry the entry to be added.
     */
    public void addEntry(ForgetMeResult entry) {
        parts.add(entry);
    }
}
