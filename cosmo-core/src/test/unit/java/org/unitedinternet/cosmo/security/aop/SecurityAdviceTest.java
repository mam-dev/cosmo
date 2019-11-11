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
package org.unitedinternet.cosmo.security.aop;

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.mock.MockSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockTicketPrincipal;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.impl.StandardContentService;
import org.unitedinternet.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

/**
 * Test Case for <code>SecurityAdvice/code>
 * This test doesn't check secured flag. See SecurityAdviceConcurrencyTest for that test.
 */

public class SecurityAdviceTest {

    
    private StandardContentService service;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    private ContentService proxyService;
    private MockSecurityManager securityManager;
    
    private SecurityAdvice sa;

    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp()  {       
        testHelper = new TestHelper();
        securityManager = new MockSecurityManager();
        storage = new MockDaoStorage();        
        contentDao = new MockContentDao(storage);
        userDao = new MockUserDao(storage);
        this.sa = new SecurityAdvice(securityManager, contentDao, userDao);
        service = new StandardContentService();
        lockManager = new SingleVMLockManager();
        service.setContentDao(contentDao);
        service.setLockManager(lockManager);
        service.setTriageStatusQueryProcessor(new StandardTriageStatusQueryProcessor());
        service.init();
        
        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service); 
        
        factory.addAspect(sa);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }

    /**
     * Tests secured api with user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSecuredApiWithUser() throws Exception {
        User user1 = testHelper.makeDummyUser("user1", "password"); // User1 is owner: can read & write item 1
        User user2 = testHelper.makeDummyUser("user2", "password"); // User2 is nothing: can't read & write item 1,
        User user3 = testHelper.makeDummyUser("user3", "password"); // User3 is given an unprotected ACE: can read, can't write item 1
        User user4 = testHelper.makeDummyUser("user4", "password"); // User4 does not own item 2 but should be able to edit it and create new items in user2 collection due to unprotected ACE


        CollectionItem rootCollection = contentDao.createRootItem(user1);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);
        //User3 can only read
        Ace aceContent1 = testHelper.makeDummyAce();
        aceContent1.setUser(user3);
        aceContent1.getPermissions().add(Permission.READ);
        List<Ace> acesContent1 = new ArrayList<>();
        acesContent1.add(aceContent1);
        service.alterAcl(dummyContent, acesContent1);


        //Create root collection for user2
        CollectionItem rootCollection2 = contentDao.createRootItem(user2);
        //Deny all for user3
        Ace aceCollection2 = testHelper.makeDummyAce();
        aceCollection2.setIsDeny(true);
        aceCollection2.setUser(user3);
        aceCollection2.getPermissions().add(Permission.READ);
        aceCollection2.getPermissions().add(Permission.WRITE);
        aceCollection2.getPermissions().add(Permission.FREEBUSY);

        Ace aceCollection2_second = testHelper.makeDummyAce();
        aceCollection2_second.setType(Ace.Type.AUTHENTICATED);
        aceCollection2_second.getPermissions().add(Permission.READ);
        aceCollection2_second.getPermissions().add(Permission.WRITE);

        List<Ace> aces = new ArrayList<>();
        aces.add(aceCollection2);
        aces.add(aceCollection2_second);
        service.alterAcl(rootCollection2, aces);


        // login as user1
        initiateContext(user1);
        
        // should work fine
        proxyService.findItemByUid("1");
        //Assert.assertTrue(sa.getSecured());

        //user1 can create item in collection of user2 so this should be fine
        ContentItem dummyContentByUser1 = new MockNoteItem();
        dummyContentByUser1.setName("user1content");
        dummyContentByUser1.setOwner(user1);
        dummyContentByUser1.setUid("2");
        dummyContentByUser1 = proxyService.createContent(rootCollection2, dummyContentByUser1);




        // now set security context to user2
        initiateContext(user2);
        //Assert.assertFalse(sa.getSecured());
        // should fail
        try {
            proxyService.findItemByUid("1");
            Assert.fail("able to view item by user2");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.READ, e.getPermission());
        }

        //User2 should be able to read and write content in its own collection
        ContentItem item = (ContentItem)proxyService.findItemByUid("2");
        Assert.assertNotNull(item);

        proxyService.updateContent(item);


        
        // try to update item
        // should fail
        try {
            proxyService.updateContent(dummyContent);
            Assert.fail("able to update item by user2");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }


        //login as user3
        initiateContext(user3);

        //read fine
        proxyService.findItemByUid("1");

        //update fail
        try {
            proxyService.updateContent(dummyContent);
            Assert.fail("able to update item with uid 1 by user3");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }

        //Should fail reading item with uid 2
        try {
            proxyService.findItemByUid("2");
            Assert.fail("able to read item with uid 2 by user3");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("2", e.getItem().getUid());
            Assert.assertEquals(Permission.READ, e.getPermission());
        }

        //Should fail writing item with uid 2
        try {
            proxyService.updateContent(dummyContentByUser1);
            Assert.fail("able to update item with uid 2 by user3");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("2", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }


        initiateContext(user4);

        // Find item with id 2 - should be ok
        proxyService.findItemByUid("2");
        // updating content with id 2 is also ok
        proxyService.updateContent(dummyContentByUser1);

        //create new item in user2 collection
        ContentItem dummyContentByUser4 = new MockNoteItem();

        dummyContentByUser4.setOwner(user4);
        dummyContentByUser4.setUid("3");
        dummyContentByUser4.setName("foo2content4");
        dummyContentByUser4 = proxyService.createContent(rootCollection2, dummyContentByUser4);


        // login as user1
        initiateContext(user1);

        //Can obtain item with uid 3
        proxyService.findItemByUid("3");

        //And update as well
        proxyService.updateContent(dummyContentByUser4);


        // And update content in its own collection
        proxyService.updateContent(dummyContent);
    }

    @Test
    public void testSecuredApiWithGroup() throws Exception {
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        User user3 = testHelper.makeDummyUser("user3", "password");

        Group group = testHelper.makeDummyGroup("group");
        user1.addGroup(group);
        user2.addGroup(group);

        CollectionItem rootCollection = contentDao.createRootItem(group);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        // login as user1
        initiateContext(user1);

        // should work fine
        proxyService.findItemByUid("1");

        // login as user1
        initiateContext(user2);

        // should work fine
        proxyService.findItemByUid("1");
        //Assert.assertTrue(sa.getSecured());

        // now set security context to user3
        initiateContext(user3);
        //Assert.assertFalse(sa.getSecured());
        // should fail
        try {
            proxyService.findItemByUid("1");
            Assert.fail("able to view item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.READ, e.getPermission());
        }

        // try to update item
        // should fail
        try {
            proxyService.updateContent(dummyContent);
            Assert.fail("able to update item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }

        // login as user1
        initiateContext(user1);

        // should succeed
        proxyService.updateContent(dummyContent);
    }



    /**
     * Tests secured api with ticket.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSecuredApiWithTicket() throws Exception {
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        CollectionItem collection = testHelper.makeDummyCollection(user1);
        collection.setUid("col");
        
        // create RO and RW tickets on collection
        Ticket roTicket = testHelper.makeDummyTicket();
        roTicket.setKey("T1");
        roTicket.getPermissions().add(Permission.READ);
        collection.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("T2");
        rwTicket.getPermissions().add(Permission.WRITE);
        collection.getTickets().add(rwTicket);
        
        collection = contentDao.createCollection(rootCollection, collection);
        
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(collection, dummyContent);
        
        
        // login as RO ticket
        initiateContext(roTicket);
        
        // view is fine
        proxyService.findItemByUid("1");
        
        // update should fail
        try {
            proxyService.updateContent(dummyContent);
            Assert.fail("able to update item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.WRITE, e.getPermission());
        }
        
        // login as RW ticket
        initiateContext(rwTicket);
        
        // view and update should work
        proxyService.findItemByUid("1");
        proxyService.updateContent(dummyContent);
        
        // login as use2 including rw ticket
        HashSet<Ticket> tickets = new HashSet<Ticket>();
        tickets.add(rwTicket);
        initiateContextWithTickets(user2, tickets);
        
        // view and update should work
        proxyService.findItemByUid("1");
        proxyService.updateContent(dummyContent);
        
        // remove tickets
        tickets.clear();
        
        // view should fail
        try {
            proxyService.findItemByUid("1");
            Assert.fail("able to view item");
        } catch (ItemSecurityException e) {
            Assert.assertEquals("1", e.getItem().getUid());
            Assert.assertEquals(Permission.READ, e.getPermission());
        }
    }
    
    /**
     * Initiates context.
     * @param user The user.
     */
    private void initiateContext(User user) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user)));
    }
    
    /**
     * Initiates context with tickets.
     * @param user The user.
     * @param tickets The tickets.
     */
    private void initiateContextWithTickets(User user, Set<Ticket> tickets) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user), tickets));
    }
    
    /**
     * Initiates context.
     * @param ticket The ticket.
     */
    private void initiateContext(Ticket ticket) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockTicketPrincipal(ticket)));
    }
    
}
