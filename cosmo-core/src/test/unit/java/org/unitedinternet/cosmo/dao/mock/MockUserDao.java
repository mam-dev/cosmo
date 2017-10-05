/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.mock;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockAuditableObject;
import org.unitedinternet.cosmo.model.mock.MockUser;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockUserDao implements UserDao {
    static int idseq = 0;

    @SuppressWarnings("rawtypes")
    private HashMap usernameIdx;
    @SuppressWarnings("rawtypes")
    private HashMap emailIdx;
    @SuppressWarnings("rawtypes")
    private HashMap uidIdx;
    @SuppressWarnings("rawtypes")
    private HashMap activationIdIdx;
    private HashMap<String, PasswordRecovery> passwordRecoveryIdx;

    private MockDaoStorage storage = null;
    
    private VersionFourGenerator idGenerator = new VersionFourGenerator();

    /**
     * Constructor.
     * @param storage The mock dao storage.
     */
    @SuppressWarnings("rawtypes")
    public MockUserDao(MockDaoStorage storage) {
        this.storage = storage;
        usernameIdx = new HashMap();
        emailIdx = new HashMap();
        uidIdx = new HashMap();
        activationIdIdx = new HashMap();
        passwordRecoveryIdx = new HashMap<String, PasswordRecovery>();

        // add overlord user
        MockUser overlord = new MockUser();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(true);
        overlord.setCreationDate(new Date());
        overlord.setModifiedDate(new Date());
        createUser(overlord);
    }

    // UserDao methods

    /**
     * Gets user.
     * {@inheritDoc}
     * @param username The username.
     * @return The user.
     */
    public User getUser(String username) {
        if (username == null) {
            return null;
        }
        return (User) usernameIdx.get(username);
    }

    /**
     * Gets user by uid.
     * {@inheritDoc}
     * @param uid The uid.
     * @return The user.
     */
    public User getUserByUid(String uid) {
        if (uid == null) { 
            return null;
        }
        return (User) uidIdx.get(uid);
    }

    /**
     * Gets user by activation id.
     * {@inheritDoc}
     * @param id The id.
     * @return The user.
     */
    public User getUserByActivationId(String id) {
        if (id == null) {
            return null;
        }
        return (User) activationIdIdx.get(id);
    }

    /**
     * Gets user by email.
     * {@inheritDoc}
     * @param email The email.
     * @return The user.
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return (User) emailIdx.get(email);
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @param user The user.
     * @return The user.
     */
    @SuppressWarnings("unchecked")
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        user.setUid(idGenerator.nextStringIdentifier());
        
        // Set create/modified date, etag for User and associated subscriptions
        // and perferences.
        ((MockAuditableObject) user).setModifiedDate(new Date());
        ((MockAuditableObject) user).setCreationDate(new Date());
        ((MockAuditableObject) user).setEntityTag(((MockAuditableObject) user)
                .calculateEntityTag());
               
        
        for(Preference p: user.getPreferences()) {
            ((MockAuditableObject) p).setEntityTag(((MockAuditableObject) p)
                    .calculateEntityTag());
            ((MockAuditableObject) p).setModifiedDate(new Date());
            ((MockAuditableObject) p).setCreationDate(new Date());
        }
            
        ((MockUser) user).validate();
        if (usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        
        usernameIdx.put(user.getUsername(), user);
        emailIdx.put(user.getEmail(), user);
        uidIdx.put(user.getUid(), user);
        activationIdIdx.put(user.getActivationId(), user);
        return user;
    }

    /**
     * Updates user.
     * {@inheritDoc}
     * @param user The user.
     * @return The user.
     */
    @SuppressWarnings("unchecked")
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }
        
        // Update modified date, etag for User and associated subscriptions
        // and preferences.
        ((MockAuditableObject) user).setModifiedDate(new Date());
        ((MockAuditableObject) user).setEntityTag(((MockAuditableObject) user)
                .calculateEntityTag());
                
        
        for(Preference p: user.getPreferences()) {
            ((MockAuditableObject) p).setEntityTag(((MockAuditableObject) p)
                    .calculateEntityTag());
            ((MockAuditableObject) p).setModifiedDate(new Date());
            if (p.getCreationDate()==null) {
                ((MockAuditableObject) p).setCreationDate(new Date());
            }
        }
        
        ((MockUser) user).validate();
        String key = user.isUsernameChanged() ?
            user.getOldUsername() :
            user.getUsername();
        if (! usernameIdx.containsKey(key)) {
            throw new IllegalArgumentException("user not found");
        }
        if (user.isUsernameChanged() &&
            usernameIdx.containsKey(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        if (user.isEmailChanged() && emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }
        usernameIdx.put(user.getUsername(), user);
        if (user.isUsernameChanged()) {
            usernameIdx.remove(user.getOldUsername());
            storage.setRootUid(user.getUsername(), storage.getRootUid(user.getOldUsername()));
        }
        emailIdx.put(user.getEmail(), user);
        if (user.isEmailChanged()) {
            emailIdx.remove(user.getOldEmail());
        }
        return user;
    }

    /**
     * Removes user.
     * {@inheritDoc}
     * @param username The username.
     */
    public void removeUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (usernameIdx.containsKey(username)) {
            User user = (User) usernameIdx.get(username);
            usernameIdx.remove(username);
            emailIdx.remove(user.getEmail());
        }
    }

    /**
     * Removes user.
     * {@inheritDoc}
     * @param user The user.
     */
    public void removeUser(User user) {
        if (user == null) {
            return;
        }
        usernameIdx.remove(user.getUsername());
        emailIdx.remove(user.getEmail());
    }

    // Dao methods

    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    /**
     * Creates password recovery.
     * {@inheritDoc}
     * @param passwordRecovery The password recovery.
     */
    public void createPasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.put(passwordRecovery.getKey(), passwordRecovery);
        
    }

    /**
     * Deletes passwordRecovery.
     * {@inheritDoc}
     * @param passwordRecovery Password Recovery.
     */
    public void deletePasswordRecovery(PasswordRecovery passwordRecovery) {
        passwordRecoveryIdx.remove(passwordRecovery.getKey());
        
    }

    /**
     * Gets the password Recovery.
     * {@inheritDoc}
     * @param key The key.
     * @re passwordRecovery The passwordRecovery.
     */
    public PasswordRecovery getPasswordRecovery(String key) {
        return passwordRecoveryIdx.get(key);
    }

    /**
     * Finds users by preference.
     * {@inheritDoc}
     * @param key The key.
     * @param value The value.
     * @return The users.
     */
    @SuppressWarnings("unchecked")
    public Set<User> findUsersByPreference(String key, String value) {
        HashSet<User> results = new HashSet<User>();
        for (User user : (Collection<User>) usernameIdx.values()) {
            for (Preference pref: user.getPreferences()) {
                if (pref.getKey().equals(key) && pref.getValue().equals(value)) {
                    results.add(user);
                }
            }
        }
        
        return results;
    }

}
