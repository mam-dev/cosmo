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

import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.User;

public abstract class AbstractCosmoAccountActivator
    implements AccountActivator {

    private UserDao userDao;
    private boolean required;

    // AccountActivator methods

/**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public User getUserFromToken(String activationToken){
        return this.userDao.getUserByActivationId(activationToken);
    }

    /**
     * Determines whether or not activation is required.
     */
    public boolean isRequired() {
        return required;
    }

    // our methods

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao){
        this.userDao = userDao;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
