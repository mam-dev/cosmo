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
package org.unitedinternet.cosmo;

import java.security.SecureRandom;

import org.junit.After;
import org.junit.Before;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.calendar.query.impl.StandardCalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.mock.MockCalendarDao;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.model.UserIdentitySupplierDefault;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockTicketPrincipal;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.unitedinternet.cosmo.server.ServiceLocatorFactory;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.service.impl.StandardContentService;
import org.unitedinternet.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.unitedinternet.cosmo.service.impl.StandardUserService;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

/**
 */
public class MockHelper extends TestHelper {
    private MockEntityFactory entityFactory;
    private MockSecurityManager securityManager;
    private ServiceLocatorFactory serviceLocatorFactory;
    private SingleVMLockManager lockManager; 
    private StandardContentService contentService;
    private StandardUserService userService;
    private ICalendarClientFilterManager clientFilterManager;
    private StandardCalendarQueryProcessor calendarQueryProcessor;
    private User user;
    private HomeCollectionItem homeCollection;
    private UserIdentitySupplier userIdentitySupplier = new UserIdentitySupplierDefault();
    
    /**
     * Constructor.
     */
    public MockHelper() {
        super();

        securityManager = new MockSecurityManager();

        serviceLocatorFactory = new ServiceLocatorFactory();
        serviceLocatorFactory.setCmpPrefix("/cmp");
        serviceLocatorFactory.setDavPrefix("/dav");
        serviceLocatorFactory.setSecurityManager(securityManager);
        serviceLocatorFactory.init();

        MockDaoStorage storage = new MockDaoStorage();
        MockCalendarDao calendarDao = new MockCalendarDao(storage);
        MockContentDao contentDao = new MockContentDao(storage);
        MockUserDao userDao = new MockUserDao(storage);
        lockManager = new SingleVMLockManager();
        
        entityFactory = new MockEntityFactory();
        
        contentService = new StandardContentService(contentDao, lockManager, new StandardTriageStatusQueryProcessor());        

        clientFilterManager = new ICalendarClientFilterManager();
        
        calendarQueryProcessor = new StandardCalendarQueryProcessor(new EntityConverter(entityFactory), contentDao, calendarDao);
        calendarQueryProcessor.setCalendarDao(calendarDao);
        
        userService = new StandardUserService();
        userService.setContentDao(contentDao);
        userService.setUserDao(userDao);
        KeyBasedPersistenceTokenService keyBasedPersistenceTokenService = new KeyBasedPersistenceTokenService();
        keyBasedPersistenceTokenService.setServerSecret("cosmossecret");
        keyBasedPersistenceTokenService.setServerInteger(123);
        keyBasedPersistenceTokenService.setSecureRandom(new SecureRandom());
        userService.setPasswordGenerator(keyBasedPersistenceTokenService);
        userService.init();

        user = userService.getUser("test");
        if (user == null) {
            user = makeDummyUser("test", "password");
            userService.createUser(user);
        }
        homeCollection = contentService.getRootItem(user);
    }
    
    /**
     * Setup.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {}
    
    /**
     * TearDown method.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * Log in.
     */
    public void logIn() {
        logInUser(user);
    }

    /**
     * Log in user.
     * @param u - the user.
     */
    public void logInUser(User u) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(u));
    }

    /**
     * Log in ticket.
     * @param t - The ticket.
     */
    public void logInTicket(Ticket t) {
        securityManager.setUpMockSecurityContext(new MockTicketPrincipal(t));
    }

    /**
     * Returns the security manager.
     * @return The security manager.
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Gets service locator factory.
     * @return The service locator factory.
     */
    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    /**
     * Gets content service.
     * @return The content service.
     */
    public ContentService getContentService() {
        return contentService;
    }
    
    /**
     * Gets entity factory.
     * @return EntityFactory.
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    /**
     * Gets calendar query processor.
     * @return The calendar query processor.
     */
    public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }

    /**
     * Gets users service.
     * @return The users service.
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Gets the user.
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets home collection.
     * @return Home collection.
     */
    public HomeCollectionItem getHomeCollection() {
        return homeCollection;
    }
    /**
     * Makes and stores dummy collection.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCollection()
        throws Exception {
        return makeAndStoreDummyCollection(homeCollection);
    }

    /**
     * Makes and stores dummy collection.
     * @param parent The collection item parent.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCollection(CollectionItem parent)
        throws Exception {
        CollectionItem c = makeDummyCollection(user);
        return contentService.createCollection(parent, c);
    }
    
    /**
     * Makes and stores dummy calendar collection.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCalendarCollection()
        throws Exception {
        return makeAndStoreDummyCalendarCollection(null);
    }
    
    /**
     * Makes and stores dummy calendar collection.
     * @param name The name.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCalendarCollection(String name)
            throws Exception {
        CollectionItem c = makeDummyCalendarCollection(user, name);
        return contentService.createCollection(homeCollection, c);
    }

    /**
     * Locks collection.
     * @param collection The collection.
     */
    public void lockCollection(CollectionItem collection) {
        lockManager.lockCollection(collection);
    }

    /**
     * Makes and stores dummy content.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public ContentItem makeAndStoreDummyContent()
        throws Exception {
        return makeAndStoreDummyContent(homeCollection);
    }

    /**
     * Makes and stores dummy content.
     * @param parent The collection item parent.
     * @return The content item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public ContentItem makeAndStoreDummyContent(CollectionItem parent)
        throws Exception {
        ContentItem c = makeDummyContent(user);
        return contentService.createContent(parent, c);
    }

    /**
     * Makes and stores dummy item.
     * @return The note item
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public NoteItem makeAndStoreDummyItem()
        throws Exception {
        return makeAndStoreDummyItem(homeCollection);
    }

    /**
     * Makes and store dummy item.
     * @param parent The collection item parent.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public NoteItem makeAndStoreDummyItem(CollectionItem parent)
        throws Exception {
        return makeAndStoreDummyItem(parent, null);
    }

    /**
     * Makes and store dummy item.
     * @param parent The collection item parent.
     * @param name The name.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public NoteItem makeAndStoreDummyItem(CollectionItem parent,
                                          String name)
        throws Exception {
        NoteItem i = makeDummyItem(user, name);
        return (NoteItem) contentService.createContent(parent, i);
    }

    /**
     * Makes and store dummy ticket.
     * @param collection The collection item.
     * @return The ticket.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public Ticket makeAndStoreDummyTicket(CollectionItem collection)
        throws Exception {
        Ticket ticket = makeDummyTicket(user);
        contentService.createTicket(collection, ticket);
        return ticket;
    }

    /**
     * Makes and store dummy subscription.
     * @return The collection subscription.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionSubscription makeAndStoreDummySubscription()
        throws Exception {
        CollectionItem collection = makeAndStoreDummyCollection();
        Ticket ticket = makeAndStoreDummyTicket(collection);
        return makeAndStoreDummySubscription(collection, ticket);
    }

    /**
     * Makes and stores dummy subscription.
     * @param collection The collection item.
     * @param ticket The ticket.
     * @return The collection subscription.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionSubscription makeAndStoreDummySubscription(CollectionItem collection, Ticket ticket)
                                                                 throws Exception {
        CollectionSubscription sub = makeDummySubscription(collection, ticket);
        userService.updateUser(user);
        return sub;
    }

    /**
     * Makes and stores dummy preferences.
     * @return The preference.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public Preference makeAndStoreDummyPreference()
        throws Exception {
        Preference pref = makeDummyPreference();
        user.addPreference(pref);
        userService.updateUser(user);
        return pref;
    }

    /**
     * Finds item.
     * @param uid The ui.
     * @return The note item.
     */
    public NoteItem findItem(String uid) {
        return (NoteItem) contentService.findItemByUid(uid);
    }

    /**
     * Finds collection.
     * @param uid The uid.
     * @return The collection item.
     */
    public CollectionItem findCollection(String uid) {
        return (CollectionItem) contentService.findItemByUid(uid);
    }    

    public ICalendarClientFilterManager getClientFilterManager() {
        return clientFilterManager;
    }
    
    public UserIdentitySupplier getUserIdentitySupplier(){
    	return userIdentitySupplier;
    }
}
