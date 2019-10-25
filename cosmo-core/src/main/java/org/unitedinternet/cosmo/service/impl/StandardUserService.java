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
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserBase;
import org.unitedinternet.cosmo.service.OverlordDeletionException;
import org.unitedinternet.cosmo.service.ServiceEvent;
import org.unitedinternet.cosmo.service.ServiceListener;
import org.unitedinternet.cosmo.service.UserService;

/**
 * Standard implementation of {@link UserService}.
 */
@Service
public class StandardUserService extends BaseService implements UserService {

    private static final Log LOG = LogFactory.getLog(StandardUserService.class);

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
    @Override
    public User getUser(String username) {
        if (LOG.isDebugEnabled()) {
            // Fix Log Forging - fortify
            // Writing unvalidated user input to log files can allow an attacker to forge log entries
            // or inject malicious content into the logs.
            LOG.debug("getting user " + username);
        }
        return userDao.getUser(username);
    }

    @Override
    public Group getGroup(String name) {
        LOG.debug("getting group " + name);
        return  userDao.getGroup(name);
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
    @Override
    public User getUserByEmail(String email) {
        if (LOG.isDebugEnabled()) {
            // Fix Log Forging - fortify
            // Writing unvalidated user input to log files can allow an attacker to forge log entries
            // or inject malicious content into the logs.
            LOG.debug("getting user with email address " + email);
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
    @Override
    public User createUser(User user) {
        return createUser(user, new ServiceListener[] {});
    }

    @Override
    public Group createGroup(Group group) {
        return createGroup(group, new ServiceListener[] {});
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
    @Override
    public User createUser(User user, ServiceListener[] listeners) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating user " + user.getUsername());
        }

        user.validateRawPassword();

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

    @Override
    public Group createGroup(Group group, ServiceListener[] listeners) {
        LOG.debug("creating group: " + group.getUsername());

        fireBeforeEvent(new ServiceEvent("CREATE_GROUP", group), listeners);
        try {
            userDao.createGroup(group);
            LOG.info("created new group: " + group.getUsername());
        } catch (DataIntegrityViolationException e) {
            if (userDao.getUser(group.getUsername()) != null) {
                throw new DuplicateUsernameException(group.getUsername());
            }
            throw e;
        }
        Group newGroup = userDao.getGroup(group.getUsername());

        HomeCollectionItem home = contentDao.createRootItem(newGroup);
        fireAfterEvent(new ServiceEvent("CREATE_GROUP", group, home), listeners);

        return newGroup;
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
    @Override
    public User updateUser(User user) {
        boolean isUsernameChanged = user.isUsernameChanged();
        if (LOG.isDebugEnabled()) {
            // Fix Log Forging - fortify
            // Writing unvalidated user input to log files can allow an attacker to forge
            // log entries or inject malicious content into the logs.
            LOG.debug("updating user " + user.getOldUsername());
            if (isUsernameChanged) {
                // Fix Log Forging - fortify
                // Writing unvalidated user input to log files can allow an attacker to forge log entries
                // or inject malicious content into the logs.
                LOG.debug("... changing username to " + user.getUsername());
            }
        }

        if (user.getPassword().length() < 32) {
            user.validateRawPassword();
            user.setPassword(digestPassword(user.getPassword()));
        }

        userDao.updateUser(user);

        User newUser = userDao.getUser(user.getUsername());

        if (isUsernameChanged) {
            if (LOG.isDebugEnabled()) {
                // Fix Log Forging - fortify
                // Writing unvalidated user input to log files can allow an attacker to forge log entries
                // or inject malicious content into the logs.
                LOG.debug("renaming root item for user " + newUser.getUsername());
            }
            HomeCollectionItem rootCollection = contentDao.getRootItem(newUser);
            rootCollection.setName(newUser.getUsername());
            contentDao.updateCollection(rootCollection);
        }

        return newUser;
    }

    @Override
    public Group updateGroup (Group group) {
        boolean isNameChanged = group.isUsernameChanged();
        LOG.debug("updating group " +  group.getOldUsername());

        if (isNameChanged) {
            LOG.debug(group.getOldUsername() + ":  changing group name to"  + group.getUsername());
        }
        userDao.updateGroup(group);

        Group newGroup = userDao.getGroup(group.getUsername());
        if (isNameChanged) {
            LOG.debug("renaming root item for group: " + newGroup.getUsername());
            HomeCollectionItem rootCollection = contentDao.getRootItem(newGroup);
            rootCollection.setName(newGroup.getUsername());
            contentDao.updateCollection(rootCollection);
        }
        return newGroup;
    }

    /**
     * Removes the user account identified by the given username from the repository.
     *
     * @param username
     *            the username of the account to return
     */
    @Override
    public void removeUser(String username) {
        if (LOG.isDebugEnabled()) {
            // Fix Log Forging - fortify
            // Writing unvalidated user input to log files can allow an attacker to
            // forge log entries or inject malicious content into the logs.
            LOG.debug("removing user " + username);
        }
        User user = userDao.getUser(username);
        removeUserAndItems(user);
    }

    @Override
    public void removeGroup(String name) {
        LOG.debug("removing group: " + name);
        Group group = userDao.getGroup(name);
        removeUserAndItems(group);
    }


    /**
     * Removes a user account from the repository.
     *
     * @param user
     *            the account to remove
     */
    @Override
    public void removeUser(User user) {
        if (LOG.isDebugEnabled()) {
            // Fix Log Forging - fortify
            // Writing unvalidated user input to log files can allow an attacker to forge log entries
            // or inject malicious content into the logs.
            LOG.debug("removing user " + user.getUsername());
        }
        removeUserAndItems(user);
    }

    @Override
    public void removeGroup(Group group) {
        LOG.debug("removing group " + group.getUsername());
        removeUserAndItems(group);
    }

    /**
     * Removes a set of user accounts from the repository. Will not remove the overlord.
     * 
     * @param users
     */
    @Override
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
                // Fix Log Forging - fortify
                // Writing unvalidated user input to log files can allow an attacker to forge log entries
                // or inject malicious content into the logs.
                LOG.debug("removing user " + user.getUsername());
            }
        }

    }

    @Override
    public void removeGroups(Set<Group> groups) {
        for (Group group: groups) {
            removeUserAndItems(group);
            LOG.debug("removing group: "+ group.getUsername());
        }
    }

    @Override
    public  void removeGroupsByName(Set<String> names) {
        removeGroups(names.stream().map(name -> userDao.getGroup(name))
                .collect(Collectors.toSet()));
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
                // Fix Log Forging - fortify
                // Writing unvalidated user input to log files can allow an attacker
                // to forge log entries or inject malicious content into the logs.
                LOG.debug("removing user " + username);
            }
        }
    }

    /**
     * Generates a random password in a format suitable for presentation as an authentication credential.
     */
    public String generatePassword() {
        String password = passwordGenerator.allocateToken("").getKey();
        return password.length() <= User.PASSWORD_LEN_MAX ? password : password.substring(0, User.PASSWORD_LEN_MAX - 1);
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

    /**
     * Remove all Items associated to User. This is done by removing the HomeCollectionItem, which is the root
     * collection of all the user's items.
     * @param user
     */
    private void removeUserAndItems(UserBase user) {
        if (user == null) {
            return;
        }
        HomeCollectionItem home = contentDao.getRootItem(user);
        // remove collections/subcollections
        contentDao.removeCollection(home);
        // remove dangling items
        // (items that only exist in other user's collections)
        contentDao.removeUserContent(user);

        if (user instanceof User) {

            userDao.removeUser((User) user);
            LOG.debug("removed user and all items: " + user);
        }
        else if (user instanceof Group) {
            userDao.removeGroup((Group) user);
            LOG.debug("removed group and all items: " + user);
        } else {
            throw new CosmoException();
        }
    }
}
