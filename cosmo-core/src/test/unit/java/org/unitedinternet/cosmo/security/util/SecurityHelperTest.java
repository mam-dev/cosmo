/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockTicketPrincipal;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;

/**
 * Test Case for <code>SecurityHelper/code> which uses mock
 * model objects.
 */
public class SecurityHelperTest {
    
    private TestHelper testHelper;
 
    private SecurityHelper securityHelper;
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private MockUserDao userDao;
    
    /**
     * Sets up.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        securityHelper = new SecurityHelper(contentDao, userDao);
    }

    /**
     * Tests collection user access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCollectionUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        CollectionItem col = testHelper.makeDummyCalendarCollection(user1);
        CosmoSecurityContext context = getSecurityContext(user1);
        
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
    }
    
    /**
     * Tests collection ticket access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCollectionTicketAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        
        CollectionItem col = testHelper.makeDummyCalendarCollection(user1);
        
        Ticket roTicket = testHelper.makeDummyTicket();
        roTicket.setKey("1");
        roTicket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        col.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        
        CosmoSecurityContext context = getSecurityContext(roTicket);
        
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(rwTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(rwBogus);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
    }
    
    /**
     * Tests content user access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User user3 = testHelper.makeDummyUser("user3","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user2);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        CosmoSecurityContext context = getSecurityContext(user1);
        
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user2);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user3);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        
        // remove note from col2, so user2 doesn't have access
        note.removeParent(col2);
        
        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
    }
    
    /**
     * Tests content ticket access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentTicketAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user1);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        Ticket roTicket = testHelper.makeDummyTicket();
        roTicket.setKey("1");
        roTicket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        col2.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col2.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        Ticket rwItemTicket = testHelper.makeDummyTicket();
        rwItemTicket.setKey("4");
        rwItemTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        note.getTickets().add(rwItemTicket);
        
        CosmoSecurityContext context = getSecurityContext(roTicket);
        
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(rwTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(rwBogus);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        // remove note from col2, so rwTicket doesn't have access
        note.removeParent(col2);
        
        context = getSecurityContext(rwTicket);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        // check item ticket
        context = getSecurityContext(rwItemTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
    }
    
    /**
     * Tests content user with tickets access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentUserWithTicketsAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user1);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        col2.getTickets().add(rwTicket);
        Ticket rwBogus = testHelper.makeDummyTicket();
        rwBogus.setKey("3");
        rwBogus.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        Ticket rwItemTicket = testHelper.makeDummyTicket();
        rwItemTicket.setKey("4");
        rwItemTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        note.getTickets().add(rwItemTicket);
        
        Set<Ticket> tickets = new HashSet<Ticket>();
        tickets.add(rwTicket);
        
        CosmoSecurityContext context = getSecurityContextWithTickets(user2, tickets);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        
        tickets.clear();
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
        tickets.add(rwItemTicket);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
    }
    
    @Test
    public void shouldFindReadAccessForSubscription() {
        User sharer = testHelper.makeDummyUser("sharer", "passwd1");
        this.userDao.createUser(sharer);
        User sharee = testHelper.makeDummyUser("sharee", "passwd2");
        this.userDao.createUser(sharee);
        
        MockCollectionItem collectionItem = new MockCollectionItem();
        collectionItem.setOwner(sharer);
        collectionItem.setUid("col1");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(sharer);
        note.addParent(collectionItem);
        collectionItem.addChild(note);
        
        Ticket ticket  =  testHelper.makeDummyTicket(sharer);
        ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        ticket.setItem(collectionItem);
        collectionItem.addTicket(ticket);
        
        CollectionSubscription subscription =testHelper.makeDummySubscription(collectionItem, ticket);
        subscription.setOwner(sharee);
        subscription.setTargetCollection(collectionItem);
        subscription.setTicket(ticket);
        sharee.getSubscriptions().add(subscription);
        
        CosmoSecurityContext context = this.getSecurityContext(sharee);
        Assert.assertTrue(this.securityHelper.hasReadAccess(context, collectionItem));        
    }
    
    @Test
    public void shouldFindWriteAccessForSubscription() {
        User sharer = testHelper.makeDummyUser("sharer", "passwd1");
        this.userDao.createUser(sharer);
        User sharee = testHelper.makeDummyUser("sharee", "passwd2");
        this.userDao.createUser(sharee);
        
        MockCollectionItem collectionItem = new MockCollectionItem();
        collectionItem.setOwner(sharer);
        collectionItem.setUid("col1");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(sharer);
        note.addParent(collectionItem);
        collectionItem.addChild(note);
        
        Ticket ticket  =  testHelper.makeDummyTicket(sharer);
        ticket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        ticket.setItem(collectionItem);
        collectionItem.addTicket(ticket);
        
        CollectionSubscription subscription =testHelper.makeDummySubscription(collectionItem, ticket);
        subscription.setOwner(sharee);
        subscription.setTargetCollection(collectionItem);
        subscription.setTicket(ticket);
        sharee.getSubscriptions().add(subscription);
        
        CosmoSecurityContext context = this.getSecurityContext(sharee);
        Assert.assertTrue(this.securityHelper.hasWriteAccess(context, collectionItem));
        
    }
    
    
    /**
     * Gets security context.
     * @param user The user.
     * @return The cosmo security context.
     */
    private CosmoSecurityContext getSecurityContext(User user) {
        return new MockSecurityContext(new MockUserPrincipal(user));
    }
    
    /**
     * Gets security context with tickets.
     * @param user The user.
     * @param tickets The tickets.
     * @return The cosmo security context.
     */
    private CosmoSecurityContext getSecurityContextWithTickets(User user, Set<Ticket> tickets) {
        return new MockSecurityContext(new MockUserPrincipal(user), tickets);
    }
    
    /**
     * Gets security context.
     * @param ticket The ticket.
     * @return The security context.
     */
    private CosmoSecurityContext getSecurityContext(Ticket ticket) {
        return new MockSecurityContext(new MockTicketPrincipal(ticket));
    } 
}
