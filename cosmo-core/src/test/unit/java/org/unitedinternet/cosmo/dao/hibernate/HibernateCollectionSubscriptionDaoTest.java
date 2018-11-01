package org.unitedinternet.cosmo.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.CollectionSubscriptionDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.dao.subscription.UuidSubscriptionGenerator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscription;
import org.unitedinternet.cosmo.model.hibernate.HibTicket;

public class HibernateCollectionSubscriptionDaoTest extends AbstractSpringDaoTestCase {

    @Autowired
    private CollectionSubscriptionDao subscriptionDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ContentDaoImpl contentDao;

    private HibernateTestHelper testHelper = new HibernateTestHelper();

    private User sharer;
    private User shareeOne;
    private User shareeTwo;

    private HomeCollectionItem homeCollection;

    @Before
    public void setUp() {
        assertNotNull(this.subscriptionDao);
        this.sharer = this.testHelper.createDummyUser(userDao, 0);
        this.homeCollection = this.contentDao.createRootItem(sharer);
        this.shareeOne = this.testHelper.createDummyUser(userDao, 1);
        this.shareeTwo = this.testHelper.createDummyUser(userDao, 2);
    }

    @Test
    public void shouldNotFindAnythingWhenSearchingForNullUid() {
        List<CollectionSubscription> subscriptions = this.subscriptionDao.findByTargetCollectionUid(null);
        assertTrue(subscriptions.isEmpty());
    }

    @Test
    public void shouldNotFindAnythingWhenSearchingForRandomUid() {
        List<CollectionSubscription> subscriptions = this.subscriptionDao
                .findByTargetCollectionUid(UUID.randomUUID().toString());
        assertTrue(subscriptions.isEmpty());
    }

    @Test
    public void shouldFindSubscriptionsByTargetCollectionUid() {
        CollectionItem targetCollection = new HibCollectionItem();
        targetCollection.setOwner(sharer);
        targetCollection = this.contentDao.createCollection(homeCollection, targetCollection);
        assertNotNull(targetCollection.getUid());

        List<CollectionSubscription> subscriptions = this.subscriptionDao
                .findByTargetCollectionUid(targetCollection.getUid());
        assertTrue(subscriptions.isEmpty());

        HibCollectionSubscription subscriptionCollection = new HibCollectionSubscription();
        subscriptionCollection.setOwner(shareeOne);
        subscriptionCollection.setTargetCollection(targetCollection);

        this.subscriptionDao.addOrUpdate(subscriptionCollection);
        assertNotNull(subscriptionCollection.getId());

        subscriptionCollection = new HibCollectionSubscription();
        subscriptionCollection.setOwner(shareeTwo);
        subscriptionCollection.setTargetCollection(targetCollection);
        this.subscriptionDao.addOrUpdate(subscriptionCollection);
        assertNotNull(subscriptionCollection.getId());

        subscriptions = this.subscriptionDao.findByTargetCollectionUid(targetCollection.getUid());
        assertFalse(subscriptions.isEmpty());
        assertEquals(2, subscriptions.size());
    }
    
    @Test
    public void shouldNotFindAnythingWhenSearchingForNullTicketKey () {
        CollectionSubscription sub = this.subscriptionDao.findByTicket(null);
        assertNull(sub);
    }
    
    @Test
    public void shouldNotFindAnythingWhenSearchingForRandomTicketKey () {
        CollectionSubscription sub = this.subscriptionDao.findByTicket(UUID.randomUUID().toString());
        assertNull(sub);
    }
    
    @Test
    public void shouldFindSubscriptionByTicketKey() {
        CollectionItem targetCollection = new HibCollectionItem();
        targetCollection.setOwner(sharer);
        targetCollection = this.contentDao.createCollection(homeCollection, targetCollection);
        assertNotNull(targetCollection.getUid());
        
        Ticket ticket = new HibTicket(TicketType.FREE_BUSY);
        ticket.setItem(targetCollection);
        ticket.setKey(UuidSubscriptionGenerator.get().getNext());
        ticket.setOwner(sharer);
        ticket.setTimeout(Ticket.TIMEOUT_INFINITE);
        this.contentDao.createTicket(targetCollection, ticket);


        HibCollectionSubscription subscriptionCollection = new HibCollectionSubscription();
        subscriptionCollection.setOwner(shareeOne);
        subscriptionCollection.setTargetCollection(targetCollection);
        subscriptionCollection.setTicket(ticket);

        this.subscriptionDao.addOrUpdate(subscriptionCollection);
        assertNotNull(subscriptionCollection.getId());
        
        CollectionSubscription sub = this.subscriptionDao.findByTicket(ticket.getKey());
        assertNotNull(sub);
        assertEquals(shareeOne, sub.getOwner());
        
        this.subscriptionDao.delete(sub);
        sub = this.subscriptionDao.findByTicket(ticket.getKey());
        assertNull(sub);
    }
}
