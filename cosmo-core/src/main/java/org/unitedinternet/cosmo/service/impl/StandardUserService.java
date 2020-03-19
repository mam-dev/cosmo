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
package org.unitedinternet.cosmo.service.impl;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.OverlordDeletionException;
import org.unitedinternet.cosmo.service.ServiceEvent;
import org.unitedinternet.cosmo.service.ServiceListener;
import org.unitedinternet.cosmo.service.UserService;

/**
 * Standard implementation of {@link UserService}.
 */
@Service
public class StandardUserService extends BaseService implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(StandardUserService.class);

    private static final int PASSWORD_DEFAULT_LEN_MIN = 5;
    private static final int PASSWORD_DEFAULT_LEN_MAX = 25;

    /**
     * The service uses MD5 if no digest algorithm is explicitly set.
     */
    public static final String DEFAULT_DIGEST_ALGORITHM = "MD5";

    private String digestAlgorithm = DEFAULT_DIGEST_ALGORITHM;

    @Autowired
    private TokenService passwordGenerator;

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private UserDao userDao;

    @Value("${cosmo.user.password.min.length:5}")
    private int passwordLengthMin;

    @Value("${cosmo.user.password.max.length:25}")
    private int passwordLengthMax;

    // UserService methods

    /**
     * Returns the user account identified by the given username.
     *
     * @param username
     *            the username of the account to return
     *
     * @throws DataRetrievalFailureException
     *             if the account does not exist
     */
    public User getUser(String username) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting user {}", username);
        }
        return userDao.getUser(username);
    }

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email
     *            the email address of the account to return
     *
     * @throws DataRetrievalFailureException
     *             if the account does not exist
     */
    public User getUserByEmail(String email) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting user with email address {}", email);
        }
        return userDao.getUserByEmail(email);
    }

    /**
     * Creates a user account in the repository. Digests the raw password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code> after saving the original one.
     *
     * @param user
     *            the account to create
     *
     * @throws DataIntegrityViolationException
     *             if the username or email address is already in use
     */
    public User createUser(User user) {
        return createUser(user, new ServiceListener[] {});
    }

    /**
     * Creates a user account in the repository as per {@link #createUser(User)}. Sends the <code>CREATE_USER</code>
     * event to each provided listener, providing the newly created user and home collection as state.
     *
     * @param user
     *            the account to create
     * @param listeners
     *            an array of listeners to notify
     * @throws DataIntegrityViolationException
     *             if the username or email address is already in use
     */
    public User createUser(User user, ServiceListener[] listeners) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating user {}", user.getUsername());
        }

        validateRawPassword(user);
        user.setPassword(digestPassword(user.getPassword()));

        fireBeforeEvent(new ServiceEvent("CREATE_USER", user), listeners);

        try {
            userDao.createUser(user);
            LOG.info("Created new user: " + user.getUid());
        } catch (DataIntegrityViolationException e) {
            if (userDao.getUser(user.getUsername()) != null) {
                throw new DuplicateUsernameException(user.getUsername());
            }
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                throw new DuplicateEmailException(user.getEmail());
            }
            throw e;
        }

        User newUser = userDao.getUser(user.getUsername());

        HomeCollectionItem home = null;
        if (!newUser.isOverlord()) {
            home = contentDao.createRootItem(newUser);
        }

        fireAfterEvent(new ServiceEvent("CREATE_USER", user, home), listeners);

        return newUser;
    }

    /**
     * Updates a user account that exists in the repository. If the password has been changed, digests the raw new
     * password and uses the result to replace the stored password. Returns a new instance of <code>User</code> after
     * saving the original one.
     *
     * @param user
     *            the account to update
     *
     * @throws DataRetrievalFailureException
     *             if the account does not exist
     * @throws DataIntegrityViolationException
     *             if the username or email address is already in use
     */
    public User updateUser(User user) {
        boolean isUsernameChanged = user.isUsernameChanged();
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating user {}", user.getOldUsername());
            if (isUsernameChanged) {
                LOG.debug("... changing username to {}", user.getUsername());
            }
        }

        // validate and compute password hash
        validateRawPassword(user);
        user.setPassword(digestPassword(user.getPassword()));

        userDao.updateUser(user);

        User newUser = userDao.getUser(user.getUsername());
        if (isUsernameChanged) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("renaming root item for user {}", newUser.getUsername());
            }
            HomeCollectionItem rootCollection = contentDao.getRootItem(newUser);
            rootCollection.setName(newUser.getUsername());
            contentDao.updateCollection(rootCollection);
        }

        return newUser;
    }

    /**
     * Removes the user account identified by the given username from the repository.
     *
     * @param username
     *            the username of the account to return
     */
    public void removeUser(String username) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing user {}", username);
        }
        User user = userDao.getUser(username);
        removeUserAndItems(user);
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user
     *            the account to remove
     */
    public void removeUser(User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing user {}", user.getUsername());
        }
        removeUserAndItems(user);
    }

    /**
     * Removes a set of user accounts from the repository. Will not remove the overlord.
     * 
     * @param users
     */
    public void removeUsers(Set<User> users) throws OverlordDeletionException {
        for (User user : users) {
            if (user.isOverlord()) {
                throw new OverlordDeletionException();
            }
            removeUserAndItems(user);
        }
        // Only log if all removes were successful
        if (LOG.isDebugEnabled()) {
            for (User user : users) {
               LOG.debug("removing user {}", user.getUsername());
            }
        }

    }

    /**
     * Removes the user accounts identified by the given usernames from the repository. Will not remove overlord.
     * 
     * @param usernames
     */
    public void removeUsersByName(Set<String> usernames) throws OverlordDeletionException {
        for (String username : usernames) {
            if (username.equals(User.USERNAME_OVERLORD)) {
                throw new OverlordDeletionException();
            }
            User user = userDao.getUser(username);
            removeUserAndItems(user);
        }
        // Only log if all removes were successful
        if (LOG.isDebugEnabled()) {
            for (String username : usernames) {
                LOG.debug("removing user {}", username);
            }
        }
    }

    /**
     * Generates a random password in a format suitable for presentation as an authentication credential.
     */
    public String generatePassword() {
        String password = passwordGenerator.allocateToken("").getKey();
        return (password.length() <= this.passwordLengthMax) ? password : password.substring(0, this.passwordLengthMax - 1);
    }

    /**
     * Validates raw password before user creation. Raw password should not be null and should have a length between
     * {@link #passwordLengthMin} and {@link #passwordLengthMax}.
     */
    public void validateRawPassword(User user) {
        String rawPassword = user.getPassword();
        if (rawPassword == null) {
            throw new ModelValidationException("UserName" + user.getUsername() + " UID" + user.getUid(),
                "Password not specified");
        }
        if (rawPassword.length() < this.passwordLengthMin || rawPassword.length() > this.passwordLengthMax) {

            throw new ModelValidationException("UserName" + user.getUsername() + " UID" + user.getUid(),
                "Password must be " + this.passwordLengthMin + " to " + this.passwordLengthMax + " characters in length");
        }
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties and defaulting optional properties.
     */
    public void init() {
        if (contentDao == null) {
            throw new IllegalStateException("contentDao is required");
        }
        if (userDao == null) {
            throw new IllegalStateException("userDao is required");
        }
        if (passwordGenerator == null) {
            throw new IllegalStateException("passwordGenerator is required");
        }
        if (digestAlgorithm == null) {
            digestAlgorithm = DEFAULT_DIGEST_ALGORITHM;
        }
        if (passwordLengthMin == 0) {
            this.passwordLengthMin = PASSWORD_DEFAULT_LEN_MIN;
        }
        if (passwordLengthMax == 0) {
            this.passwordLengthMax = PASSWORD_DEFAULT_LEN_MAX;
        }
    }

    /**
     * Readies the service for garbage collection, shutting down any resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     * Digests the given password using the set message digest and hex encodes it.
     */
    protected String digestPassword(String password) {
        if (password == null) {
            return password;
        }
        try {
            return new String(Hex.encodeHex(
                    MessageDigest.getInstance(digestAlgorithm).digest(password.getBytes(Charset.forName("UTF-8")))));
        } catch (Exception e) {
            throw new CosmoException("cannot get digest for algorithm " + digestAlgorithm, e);
        }
    }

    /**
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    /**
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     */
    public TokenService getPasswordGenerator() {
        return this.passwordGenerator;
    }

    /**
     */
    public void setPasswordGenerator(TokenService generator) {
        this.passwordGenerator = generator;
    }

    /**
     */
    public ContentDao getContentDao() {
        return this.contentDao;
    }

    /**
     */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     */
    public UserDao getUserDao() {
        return this.userDao;
    }

    /**
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


    public int getPasswordLengthMin() {
        return this.passwordLengthMin;
    }

    public void setPasswordLengthMin(int passwordLengthMin) {
        this.passwordLengthMin = passwordLengthMin;
    }

    public int getPasswordLengthMax() {
        return this.passwordLengthMax;
    }

    public void setPasswordLengthMax(int passwordLengthMax) {
        this.passwordLengthMax = passwordLengthMax;
    }

    /**
     * Remove all Items associated to User. This is done by removing the HomeCollectionItem, which is the root
     * collection of all the user's items.
     */
    private void removeUserAndItems(User user) {
        if (user == null) {
            return;
        }
        HomeCollectionItem home = contentDao.getRootItem(user);
        // remove collections/subcollections
        contentDao.removeCollection(home);
        // remove dangling items
        // (items that only exist in other user's collections)
        contentDao.removeUserContent(user);
        userDao.removeUser(user);
    }


}
