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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
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
        lockManager = new SingleVMLockManager();
        service = new StandardContentService(contentDao, lockManager, new StandardTriageStatusQueryProcessor());
        
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
        User user1 = testHelper.makeDummyUser("user1", "password");
        User user2 = testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);
        
        // login as user1
        initiateContext(user1);
        
        // should work fine
        proxyService.findItemByUid("1");
        //Assert.assertTrue(sa.getSecured());
        
        // now set security context to user2
        initiateContext(user2);
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
        roTicket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        collection.getTickets().add(roTicket);
        Ticket rwTicket = testHelper.makeDummyTicket();
        rwTicket.setKey("T2");
        rwTicket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
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
