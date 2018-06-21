/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.privacy.forgetme.userstore.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreModuleException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Performs user store operations against an LDAP user store.
 */
public class LDAPUserStoreHandler extends UserStoreHandler {

    private static final Log log = LogFactory.getLog(LDAPUserStoreHandler.class);

    @Override
    public String getName() {

        return "read-write-ldap-handler";
    }

    /**
     * Renames the user in the underlying LDAP user store.
     *
     * @param currentName The existing username.
     * @param newName     The new username.
     * @throws UserStoreModuleException
     */
    @Override
    public void renameUser(String currentName, String newName) throws UserStoreModuleException {

        try {

            String userSearchBase = getRealmConfiguration().getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
            String userNameAttribute = getRealmConfiguration().getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

            String currentDN = String.format("%s=%s,%s", userNameAttribute, currentName, userSearchBase);
            String newDN = String.format("%s=%s,%s", userNameAttribute, newName, userSearchBase);

            LDAPConnectionContext ldapConnectionContext = new LDAPConnectionContext(getRealmConfiguration());
            DirContext context = ldapConnectionContext.getContext();

            if (log.isDebugEnabled()) {
                log.debug(String.format("Renaming the user. Search base : '%s', Username attribute : '%s'",
                        userSearchBase, userNameAttribute));
            }
            context.rename(currentDN, newDN);

        } catch (UserStoreException | NamingException e) {
            throw new UserStoreModuleException("An error occurred while renaming a user in an LDAP user store.", e);
        }

    }
}
