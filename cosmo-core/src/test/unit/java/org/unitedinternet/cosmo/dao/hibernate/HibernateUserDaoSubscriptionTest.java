/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.util.Set;


import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscription;
import org.unitedinternet.cosmo.model.hibernate.HibTicket;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test HibernateUserDaoSubscription.
 *
 */
public class HibernateUserDaoSubscriptionTest extends AbstractHibernateDaoTestCase {
    @Autowired
    protected ContentDaoImpl contentDao;
    @Autowired
    protected UserDaoImpl userDao;
    
     /**
     * Constructor.
     */
    public HibernateUserDaoSubscriptionTest() {
        super();
    }
    
    /**
     * Test subscribe.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSubscribe() throws Exception {
       //TODO Update subscription test.
    }

    /**
     * Gets user.
     * @param userDao UserDao.
     * @param username Username
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    /**
     * Gets collection.
     * @param parent The parent.
     * @param name The name.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private CollectionItem getCollection(CollectionItem parent,
                                         String name)
        throws Exception {
        for (Item child : (Set<Item>) parent.getChildren()) {
            if (child.getName().equals(name)) {
                return (CollectionItem) child;
            }
        }
        CollectionItem collection = new HibCollectionItem();
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(parent.getOwner());
        return contentDao.createCollection(parent, collection);
    }

    /**
     * Generates ticket.
     * @param item The item.
     * @param owner The owner.
     * @return The ticket.
     */
    private Ticket generateTicket(Item item,
                                  User owner) {
        Ticket ticket = new HibTicket();
        ticket.setOwner(owner);
        ticket.setTimeout(Ticket.TIMEOUT_INFINITE);
        contentDao.createTicket(item, ticket);
        return ticket;
    }
    
}
