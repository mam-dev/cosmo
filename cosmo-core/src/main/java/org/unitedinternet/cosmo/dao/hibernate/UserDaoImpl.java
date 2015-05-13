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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.ArrayPagedList;
import org.unitedinternet.cosmo.model.PagedList;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.PageCriteria;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibUser;
import org.unitedinternet.cosmo.util.VersionFourGenerator;
import org.springframework.orm.hibernate4.SessionFactoryUtils;


/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(UserDaoImpl.class);

    private VersionFourGenerator idGenerator;

    private static final QueryCriteriaBuilder<User.SortType> QUERY_CRITERIA_BUILDER =
            new UserQueryCriteriaBuilder<User.SortType>();

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

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<User>();
            Iterator it = getSession().getNamedQuery("user.all").iterate();
            while (it.hasNext()) {
                users.add((User) it.next());
            }

            return users;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public PagedList getUsers(PageCriteria<User.SortType> pageCriteria) {
        try {
            Criteria crit = QUERY_CRITERIA_BUILDER.buildQueryCriteria(
                    getSession(), pageCriteria);
            List<User> results = crit.list();

            // Need the total
            Long size = (Long) getSession().getNamedQuery("user.count")
                    .uniqueResult();

            return new ArrayPagedList<User, User.SortType>(pageCriteria, results, size.intValue());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    public Set<User> findUsersByPreference(String key, String value) {
        try {
            Query hibQuery = getSession().getNamedQuery("users.byPreference");
            hibQuery.setParameter("key", key).setParameter("value", value);
            List<User> results = hibQuery.list();

            Set<User> users = new HashSet<User>();

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
            getSession().setFlushMode(FlushMode.MANUAL);

            User findUser = findUserByUsernameOrEmailIgnoreCaseAndId(getBaseModelObject(user)
                    .getId(), user.getUsername(), user.getEmail());

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
            Query hibQuery = getSession().getNamedQuery("passwordRecovery.byKey")
                    .setParameter("key", key);
            hibQuery.setCacheable(true);
            return (PasswordRecovery) hibQuery.uniqueResult();
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
        Query hibQuery = session.getNamedQuery("user.byUsername.ignorecase").setParameter(
                "username", username);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByUsernameOrEmailIgnoreCaseAndId(Long userId,
                                                          String username, String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery(
                "user.byUsernameOrEmail.ignorecase.ingoreId").setParameter(
                "username", username).setParameter("email", email)
                .setParameter("userid", userId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail.ignorecase").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByUid(String uid) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUid").setParameter(
                "uid", uid);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        return (User) hibQuery.uniqueResult();
    }

    private void deleteAllPasswordRecoveries(User user) {
        Session session = getSession();
        session.getNamedQuery("passwordRecovery.delete.byUser").setParameter(
                "user", user).executeUpdate();
    }

    private User findUserByActivationId(String id) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byActivationId").setParameter(
                "activationId", id);
        hibQuery.setCacheable(true);
        return (User) hibQuery.uniqueResult();
    }

    private static class UserQueryCriteriaBuilder<SortType extends User.SortType> extends
            StandardQueryCriteriaBuilder<SortType> {

        public UserQueryCriteriaBuilder() {
            super(User.class);
        }

        protected List<Order> buildOrders(PageCriteria<SortType> pageCriteria) {
            List<Order> orders = new ArrayList<Order>();

            User.SortType sort = pageCriteria.getSortType();
            if (sort == null) {
                sort = User.SortType.USERNAME;
            }

            if (sort.equals(User.SortType.NAME)) {
                orders.add(createOrder(pageCriteria, "lastName"));
                orders.add(createOrder(pageCriteria, "firstName"));
            } else if (sort.equals(User.SortType.ADMIN)) {
                orders.add(createOrder(pageCriteria, "admin"));
            } else if (sort.equals(User.SortType.EMAIL)) {
                orders.add(createOrder(pageCriteria, "email"));
            } else if (sort.equals(User.SortType.CREATED)) {
                orders.add(createOrder(pageCriteria, "CreatedDate"));
            } else if (sort.equals(User.SortType.LAST_MODIFIED)) {
                orders.add(createOrder(pageCriteria, "ModifiedDate"));
            } else if (sort.equals(User.SortType.ACTIVATED)) {
                orders.add(createOrder(pageCriteria, "activationId"));
            } else {
                orders.add(createOrder(pageCriteria, "username"));
            }

            return orders;
        }

        private Order createOrder(PageCriteria pageCriteria, String property) {
            return pageCriteria.isSortAscending() ?
                    Order.asc(property) :
                    Order.desc(property);
        }
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

}
