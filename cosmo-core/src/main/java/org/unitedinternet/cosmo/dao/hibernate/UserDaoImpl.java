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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibUser;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(UserDaoImpl.class);

    private VersionFourGenerator idGenerator;

    public User createUser(User user) {

        try {
            if (user == null) {
                throw new IllegalArgumentException("user is required");
            }

            if (getBaseModelObject(user).getId() != -1) {
                throw new IllegalArgumentException("new user is required");
            }

            if (findUserByUsernameIgnoreCase(user.getUsername()) != null) {
                throw new DuplicateUsernameException(user.getUsername());
            }

            if (findUserByEmailIgnoreCase(user.getEmail()) != null) {
                throw new DuplicateEmailException(user.getEmail());
            }

            if (user.getUid() == null || "".equals(user.getUid())) {
                user.setUid(getIdGenerator().nextStringIdentifier());
            }

            getSession().save(user);
            getSession().flush();
            return user;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }

    }

    public User getUser(String username) {
        try {
            return findUserByUsername(username);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("uid required");
        }

        try {
            return findUserByUid(uid);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByActivationId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        try {
            return findUserByActivationId(id);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email required");
        }
        try {
            return findUserByEmail(email);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> findUsersByPreference(String key, String value) {
        try {
            Query<User> query = getSession().createNamedQuery("users.byPreference", User.class);
            query.setParameter("key", key).setParameter("value", value);
            List<User> results = query.getResultList();

            Set<User> users = new HashSet<>();

            // TODO figure out how to load all properties using HQL
            for (User user : results) {
                Hibernate.initialize(user);
                users.add(user);
            }

            return users;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null) {
                removeUser(user);
            }
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(User user) {
        try {
            // TODO: should probably let db take care of this with
            // cacade constaint
            deleteAllPasswordRecoveries(user);

            getSession().delete(user);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User updateUser(User user) {
        try {
            // prevent auto flushing when querying for existing users
            getSession().setHibernateFlushMode(FlushMode.MANUAL);

            User findUser = findUserByUsernameOrEmailIgnoreCaseAndId(getBaseModelObject(user).getId(),
                    user.getUsername(), user.getEmail());

            if (findUser != null) {
                if (findUser.getEmail().equals(user.getEmail())) {
                    throw new DuplicateEmailException(user.getEmail());
                } else {
                    throw new DuplicateUsernameException(user.getUsername());
                }
            }

            user.updateTimestamp();
            getSession().update(user);
            getSession().flush();

            return user;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void createPasswordRecovery(PasswordRecovery passwordRecovery) {
        try {
            getSession().save(passwordRecovery);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public PasswordRecovery getPasswordRecovery(String key) {
        try {
            Query<PasswordRecovery> query = getSession()
                    .createNamedQuery("passwordRecovery.byKey", PasswordRecovery.class).setParameter("key", key);
            query.setCacheable(true);
            List<PasswordRecovery> recoveryList = query.getResultList();
            return recoveryList.size() > 0 ? recoveryList.get(0) : null;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void deletePasswordRecovery(PasswordRecovery passwordRecovery) {
        try {
            getSession().delete(passwordRecovery);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

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
        return (User) getSession().byNaturalId(HibUser.class).using("username", username).load();
    }

    private User findUserByUsernameIgnoreCase(String username) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byUsername.ignorecase", User.class).setParameter("username",
                username);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        return this.getUserFromQuery(query);
    }

    private User findUserByUsernameOrEmailIgnoreCaseAndId(Long userId, String username, String email) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byUsernameOrEmail.ignorecase.ingoreId", User.class)
                .setParameter("username", username).setParameter("email", email).setParameter("userid", userId);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        return this.getUserFromQuery(query);
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byEmail", User.class).setParameter("email", email);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        return this.getUserFromQuery(query);
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byEmail.ignorecase", User.class).setParameter("email",
                email);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        return this.getUserFromQuery(query);
    }

    private User findUserByUid(String uid) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byUid", User.class).setParameter("uid", uid);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        return this.getUserFromQuery(query);
    }


    private User findUserByActivationId(String id) {
        Session session = getSession();
        Query<User> query = session.createNamedQuery("user.byActivationId", User.class).setParameter("activationId", id);
        query.setCacheable(true);
        return this.getUserFromQuery(query);
    }
    
    private User getUserFromQuery(Query<User> query) {
        List<User> users = query.getResultList();
        return users.size() > 0 ? users.get(0) : null;
    }
    
    private void deleteAllPasswordRecoveries(User user) {
        Session session = getSession();
        session.getNamedQuery("passwordRecovery.delete.byUser").setParameter("user", user).executeUpdate();
    }
    
    private BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

}
