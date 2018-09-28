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
package org.unitedinternet.cosmo.acegisecurity.userdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.User;

/**
 * Implements Acegi Security's <code>UserDetailsService</code>
 * interface by retrieving user details with a <code>UserDao</code>.
 * 
 * @see UserDetailsService
 * @see UserDao
 */
@Component
public class CosmoUserDetailsService implements UserDetailsService {

	@Autowired
    private UserDao userDao;

    /**
     * Locates the user with the given username by retrieving it
     * with this service's <code>UserDao</code> and returns a
     * <code>UserDetails</code> representing the user.
     *
     * @param username the username to look up
     * @return a fully populated <code>UserDetails</code> (never
     * <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be
     * found
     * @see UserDetails
     * @throws DataAccessException - if something is wrong this exception is thrown.
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException {
        User user = userDao.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("user " + username + " not found");
        }
        return new CosmoUserDetails(userDao.getUser(username));
    }

    /**
     * Gets user dao.
     * @return The user.
     */
    public UserDao getUserDao() {
        return userDao;
    }

    /**
     * Sets user dao.
     * @param userDao The user dao.
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
