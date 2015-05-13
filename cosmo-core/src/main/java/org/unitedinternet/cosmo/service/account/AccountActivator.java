/*
 * Copyright 2005-2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.service.account;

import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.Service;

public interface AccountActivator extends Service {

    /**
     * Return a new activation token.
     *
     * @return a new activation token
     */
    String generateActivationToken();

    /**
     * Performs whatever action this service provides for account activation.
     *
     * @param user
     * @param locale TODO
     */
    void sendActivationMessage(User user, ActivationContext activationContext);

    /**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    User getUserFromToken(String activationToken);

    /**
     * Determines whether or not activation is required.
     */
    boolean isRequired();
}
