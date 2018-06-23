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

package org.wso2.carbon.privacy.forgetme.userstore.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.privacy.forgetme.userstore.exception.UserStoreModuleException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 *
 * Reads the secondary user store configurations and build the realm configurations.
 *
 */
public class SecondaryUserStoreReader {

    /**
     *
     * Builds a realms configuration based on the given user store configuration path and primary realm.
     *
     * @param userStoreConfigFilePath The path of the user store configuration.
     * @param primaryRealm Primary realm.
     * @return The realm configuration of the secondary user store manager.
     */
    public RealmConfiguration read(Path userStoreConfigFilePath, RealmConfiguration primaryRealm)
            throws UserStoreModuleException {

        RealmConfiguration realmConfigurationForSecondaryUserStore = new RealmConfiguration();

        realmConfigurationForSecondaryUserStore.setPrimary(false);
        realmConfigurationForSecondaryUserStore.setRealmClassName(primaryRealm.getRealmClassName());
        realmConfigurationForSecondaryUserStore.setRealmProperties(primaryRealm.getRealmProperties());

        try {
            StAXOMBuilder builder = new StAXOMBuilder(new FileInputStream(userStoreConfigFilePath.toFile()));
            OMElement userStoreElement = builder.getDocumentElement();

            String userStoreClassName = userStoreElement.getAttributeValue(
                    new QName(UserCoreConstants.RealmConfig.ATTR_NAME_CLASS));
            realmConfigurationForSecondaryUserStore.setUserStoreClass(userStoreClassName);
            realmConfigurationForSecondaryUserStore.setUserStoreProperties(getUserStoreProperties(userStoreElement));
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new UserStoreModuleException("An error occurred while reading the secondary user store.", e);
        }

        return realmConfigurationForSecondaryUserStore;
    }

    private Map<String, String> getUserStoreProperties(OMElement userStoreElement) {

        Map<String, String> map = new HashMap<String, String>();
        Iterator<?> ite = userStoreElement.getChildrenWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propElem = (OMElement) ite.next();
            String propName = propElem.getAttributeValue(new QName(UserCoreConstants.RealmConfig.ATTR_NAME_PROP_NAME));
            String propValue = propElem.getText();

            if (propName != null && propValue != null) {
                map.put(propName.trim(), propValue.trim());
            }
        }
        return map;
    }

}
