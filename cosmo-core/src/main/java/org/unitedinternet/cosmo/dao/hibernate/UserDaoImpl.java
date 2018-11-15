/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.hibernate;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibUser;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * 
 */
@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private VersionFourGenerator idGenerator;

    @PersistenceContext
    private EntityManager em;

    @Override
    public User createUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }

        if (getBaseModelObject(user).getId() != -1) {
            throw new IllegalArgumentException("new user is required");
        }

        if (findUserByUsername(user.getUsername()) != null) {
            throw new DuplicateUsernameException(user.getUsername());
        }

        if (findUserByEmail(user.getEmail()) != null) {
            throw new DuplicateEmailException(user.getEmail());
        }

        if (user.getUid() == null || "".equals(user.getUid())) {
            user.setUid(getIdGenerator().nextStringIdentifier());
        }

        this.em.persist(user);
        this.em.flush();
        return user;

    }

    @Override
    public User getUser(String username) {
        return findUserByUsername(username);
    }

    @Override
    public User getUserByUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("uid required");
        }
        return findUserByUid(uid);
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email required");
        }
        return findUserByEmail(email);

    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null) {
                removeUser(user);
            }
        } catch (HibernateException e) {
            this.em.clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(User user) {
        // TODO: Should probably let DB take care of this with cascade constaint
        this.em.remove(user);
        this.em.flush();

    }

    public User updateUser(User user) {
        // Prevent auto flushing when querying for existing users
        this.em.setFlushMode(FlushModeType.COMMIT);

        User findUser = findUserByUsernameOrEmail(getBaseModelObject(user).getId(), user.getUsername(),
                user.getEmail());

        if (findUser != null) {
            if (findUser.getEmail().equals(user.getEmail())) {
                throw new DuplicateEmailException(user.getEmail());
            } else {
                throw new DuplicateUsernameException(user.getUsername());
            }
        }
        user.updateTimestamp();
        this.em.merge(user);
        this.em.flush();

        return user;
    }

    @PostConstruct
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }
    }

    public VersionFourGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(VersionFourGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private User findUserByUsername(String username) {
        List<HibUser> usersList = this.em.createQuery(" FROM HibUser u WHERE u.username= :username", HibUser.class)
                .setParameter("username", username).getResultList();
        if (!usersList.isEmpty()) {
            return usersList.get(0);
        }
        return null;
    }

    private User findUserByUsernameOrEmail(Long userId, String username, String email) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byUsernameOrEmail", User.class)
                .setParameter("username", username).setParameter("email", email).setParameter("userid", userId);
        return this.getUserFromQuery(query);
    }

    private User findUserByEmail(String email) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byEmail", User.class).setParameter("email", email);
        return this.getUserFromQuery(query);
    }

    private User findUserByUid(String uid) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byUid", User.class).setParameter("uid", uid);
        return this.getUserFromQuery(query);
    }

    private User getUserFromQuery(TypedQuery<User> query) {
        List<User> users = query.getResultList();
        return users.size() > 0 ? users.get(0) : null;
    }

    private BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

}
