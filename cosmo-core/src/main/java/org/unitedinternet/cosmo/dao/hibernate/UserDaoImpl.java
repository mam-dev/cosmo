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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.*;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibGroup;
import org.unitedinternet.cosmo.model.hibernate.HibUser;
import org.unitedinternet.cosmo.model.hibernate.HibUserBase;
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

        if (findUserOrGroupByName(user.getUsername()) != null) {
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
    public Group createGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group is required");
        }

        if (getBaseModelObject(group).getId() != -1) {
            throw new IllegalArgumentException("new group is required");
        }
        /** TODO: Maybe split users and groups onto different tables? **/
        if (findUserOrGroupByName(group.getUsername()) != null) {
            throw new DuplicateUsernameException(group.getUsername());
        }


        if (group.getUid() == null || "".equals(group.getUid())) {
            group.setUid(getIdGenerator().nextStringIdentifier());
        }

        this.em.persist(group);
        this.em.flush();
        return group;
    }

    @Override
    public User getUser(String username) {
        return findUserByUsername(username);
    }

    @Override
    public Group getGroup(String name) {
        return findGroupByName(name);
    }

    @Override
    public UserIterator users() {
        Stream<HibUser> stream = getUserStream();
        Iterator<HibUser> iterator = stream.iterator();
        return new UserIterator() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public User next() {
                return iterator.next();
            }
        };
    }

    @Override
    public GroupIterator groups() {
        Iterator<HibGroup> iterator = getGroupStream().iterator();
        return new GroupIterator() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Group next() {
                return iterator.next();
            }
        };
    }

    private Stream<HibUser> getUserStream() {
        TypedQuery<HibUser> query = this.em.createNamedQuery("user.all", HibUser.class);
        Stream<HibUser> stream = query.getResultStream();
        return stream;
    }
    
    private Stream<HibGroup> getGroupStream() {
        TypedQuery<HibGroup> query = this.em.createNamedQuery("group.all", HibGroup.class);
        Stream<HibGroup> stream = query.getResultStream();
        return stream;    
    }

    @Override
    public User getUserByUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("uid required");
        }
        return findUserByUid(uid);
    }

    @Override
    public Group getGroupByUid(String uid) {
        return null;
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

    @Override
    public void removeGroup(String name) {
        try {
            Group group = findGroupByName(name);
            if (group != null) {
                removeGroup(group);;
            }
        } catch (HibernateException e) {
            this.em.clear();;
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(User user) {
        for (Group g : user.getGroups()) {
            g.removeUser(user);
        }
        this.em.remove(user);
        this.em.flush();

    }

    @Override
    public void removeGroup(Group group) {
        this.em.remove(group);
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

    @Override
    public Group updateGroup(Group group) {
        // Prevent auto flushing when querying for existing users
        this.em.setFlushMode(FlushModeType.COMMIT);

        UserBase findUser = findUserOrGroupByName(group.getUsername());

        if (findUser != null) {
                throw new DuplicateUsernameException(group.getUsername());
        }
        group.updateTimestamp();
        this.em.merge(group);
        this.em.flush();

        return group;
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
        List<HibUser> usersList = this.em.createNamedQuery("user.byUsername", HibUser.class)
                .setParameter("username", username).getResultList();
        if (!usersList.isEmpty()) {
            return usersList.get(0);
        }
        return null;
    }

    private Group findGroupByName(String name) {
        List<HibGroup> groupsList = this.em.createNamedQuery("group.byUsername", HibGroup.class)
                .setParameter("username", name).getResultList();
        if (!groupsList.isEmpty()) {
            return groupsList.get(0);
        }
        return null;
    }

    private UserBase findUserOrGroupByName(String name) {
        List<HibUserBase> usersList = this.em.createNamedQuery("userOrGroup.byUsername", HibUserBase.class)
                .setParameter("username", name).getResultList();
        if (!usersList.isEmpty()) {
            return usersList.get(0);
        }
        return null;
    }


    private User findUserByUsernameOrEmail(Long userId, String username, String email) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byUsernameOrEmail", User.class)
                .setParameter("username", username).setParameter("email", email).setParameter("userid", userId);
        return this.getFirstFromQuery(query);
    }

    private User findUserByEmail(String email) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byEmail", User.class).setParameter("email", email);
        return this.getFirstFromQuery(query);
    }

    private User findUserByUid(String uid) {
        TypedQuery<User> query = this.em.createNamedQuery("user.byUid", User.class).setParameter("uid", uid);
        return this.getFirstFromQuery(query);
    }

    private Group findGroupByUid(String uid) {
        TypedQuery<Group> query = this.em.createNamedQuery("group.byUid", Group.class).setParameter("uid", uid);
        return this.getFirstFromQuery(query);
    }

    private UserBase findUserOrGroupByUid(String uid) {
        TypedQuery<UserBase> query = this.em.createNamedQuery("userOrGroup.byUid", UserBase.class).setParameter("uid", uid);
        return this.getFirstFromQuery(query);
    }



    private <T> T getFirstFromQuery(TypedQuery<T> query) {
        List<T> items = query.getResultList();
        return items.size() > 0 ? items.get(0) : null;
    }

    private BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

}
