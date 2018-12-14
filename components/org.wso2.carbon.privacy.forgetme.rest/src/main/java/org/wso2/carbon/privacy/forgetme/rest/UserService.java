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

package org.wso2.carbon.privacy.forgetme.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.privacy.forgetme.rest.domain.User;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @deprecated This service is made redundant and the service API and implementation is moved to identity-governance.
 * Replaced with service API: /t/{tenant-domain}/api/identity/user/v1.0/update-username
 *
 * API implementation for user store operations.
 */
@Path("/user")
public class UserService {

    private static final Log log = LogFactory.getLog(UserService.class);

    @PUT
    @Path("/{tenantId}/{tenantDomain}/{userStoreDomain}/{username}")
    public void renameUser(@PathParam("tenantId") Integer tenantId,
                           @PathParam("tenantDomain") String tenantDomain,
                           @PathParam("userStoreDomain") String userStoreDomain,
                           @PathParam("username") String username,
                           User user) {

       log.warn("Invoked deprecated /forgetme/v1.0/user API.");
    }

}
