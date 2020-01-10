/*
 * EventOperationExtensionsAdviceTest.java Jul 6, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.event.aop;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibTaskStamp;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.impl.StandardContentService;
import org.unitedinternet.cosmo.service.impl.StandardTriageStatusQueryProcessor;
import org.unitedinternet.cosmo.service.interceptors.EventAddHandler;
import org.unitedinternet.cosmo.service.interceptors.EventRemoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventUpdateHandler;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * Tests for ContextServiceExtensionsAdvice.
 * @author iulia zidaru
 *
 */
public class ContextServiceExtensionsAdviceTest {

    private ContextServiceExtensionsAdvice eventOperationExtensionsAdvice;
        
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    private ContentService proxyService;
    private StandardContentService service;
    
    
    private static final String ATTENDEE_1 = "testuser1@test.com";

    private static final String ORGANIZER = "testorganizer@test.com";
    
    private NoCallExpected noCallExpectedHandler = new NoCallExpected();
    private SimpleCheckCallExpected simpleCheckCallExpectedHandler = new SimpleCheckCallExpected();
    private CheckManyAttendeesCallExpected checkManyAttendeesCallExpected = new CheckManyAttendeesCallExpected();
    
    
    private class NoCallExpected implements EventAddHandler, EventUpdateHandler, EventRemoveHandler{
        
        @Override
        public void beforeUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            Assert.fail("Unexpected call of update handler!");          
            
        }

        @Override
        public void afterUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            Assert.fail("Unexpected call of update handler!");          
            
        }

        @Override
        public void beforeRemove(CollectionItem collection, Set<Item> items) {
              Assert.fail("Unexpected call of remove handler!");    
            
        }

        @Override
        public void afterRemove(CollectionItem collection, Set<Item> items) {
              Assert.fail("Unexpected call of remove handler!");    
            
        }

        @Override
        public void beforeAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
             //Assert.fail("Unexpected call of add handler!");          
            
        }

        @Override
        public void afterAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
             //Assert.fail("Unexpected call of add handler!");          
            
        }
    }

    private class SimpleCheckCallExpected implements EventAddHandler, EventUpdateHandler, EventRemoveHandler{
        private CollectionItem rootCollection;
        private Set<ContentItem> contentItems;

        @SuppressWarnings("unused")
        private void checkEventData(List<VEvent> events) {
            VEvent event = events.get(0);
            //organizer
            String organizer = getOrganizerFrom(event);
            Assert.assertTrue("Check expected Organizer",organizer.equals(ORGANIZER));
            
            //attendees
            List<String> addresses = getAttendeesFrom(event);
            Assert.assertTrue("Check 1 expected Attendee",addresses.size() == 1);
            Assert.assertTrue("Check expected Attendee", addresses.get(0).equals(ATTENDEE_1));
        }
        
        private List<String> getAttendeesFrom(VEvent event) {
            List<String> addresses = new ArrayList<String>();
            for(int i = 0; i < event.getProperties().size(); i++){
                if(event.getProperties().get(i) instanceof Attendee){
                    Attendee attendee = (Attendee)event.getProperties().get(i);
                    if(attendee.getCalAddress() != null){
                        String mailto[] = attendee.getCalAddress().toString().split(":");
                        addresses.add(mailto[mailto.length - 1]);
                    }
                }
            }
            return addresses;
        }

        private String getOrganizerFrom(VEvent event) {
            if(event.getOrganizer() != null){
                String organizer = event.getOrganizer().getValue();
                if(organizer.length() > 0 && organizer.contains(":")){
                    String parameters[] = organizer.split(":");
                    return parameters[1];
                }
            }
            return null;
        }
        @Override
        public void beforeUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            checkMethoodParameters(parent, contentItems);
            
        }

        private void checkMethoodParameters(CollectionItem parent,
                Set<ContentItem> contentItems) {
            Assert.assertEquals("parent collection was sent", rootCollection, parent);
            Assert.assertTrue("contentItems sent to handler", contentItems.equals(this.contentItems));
        }

        @Override
        public void afterUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            checkMethoodParameters(parent, contentItems);
            
        }

        public void setRootCollection(CollectionItem rootCollection) {
            this.rootCollection = rootCollection;            
        }

        public void setContentItems(Set<ContentItem> contentItems) {
            this.contentItems = contentItems;
            
        }

        @Override
        public void beforeRemove(CollectionItem collection, Set<Item> items) {
            
            
        }

        @Override
        public void afterRemove(CollectionItem collection, Set<Item> items) {
                        
        }

        @Override
        public void beforeAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
            
        }

        @Override
        public void afterAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
            
        }
    }
   
    private class CheckManyAttendeesCallExpected implements EventAddHandler, EventUpdateHandler, EventRemoveHandler{
/*        private CollectionItem rootCollection; */
        @SuppressWarnings("unused")
        private Set<ContentItem> contentItems;
        
        @SuppressWarnings("unused")
        private void checkEventData(List<VEvent> events) {
            VEvent event = events.get(0);
            
            List<String> addresses = getAttendeesFrom(event);
            
            Assert.assertTrue("Check 3 expected Attendee", addresses.size() == 3);
            Assert.assertTrue("Check expected Attendee testuser1@test.de", addresses.get(0).equals("testuser1@test.de"));
            Assert.assertTrue("Check expected Attendee testuser2@test.de", addresses.get(1).equals("testuser2@test.de"));
            Assert.assertTrue("Check expected Attendee testuser3@test.de", addresses.get(2).equals("testuser3@test.de"));
        }
        
        private List<String> getAttendeesFrom(VEvent event) {
            List<String> addresses = new ArrayList<String>();
            for(int i = 0; i < event.getProperties().size(); i++){
                if(event.getProperties().get(i) instanceof Attendee){
                    Attendee attendee = (Attendee)event.getProperties().get(i);
                    if(attendee.getCalAddress() != null){
                        String mailto[] = attendee.getCalAddress().toString().split(":");
                        addresses.add(mailto[mailto.length - 1]);
                    }
                }
            }
            return addresses;
        }
        @Override
        public void beforeUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            //not used
            
        }

        

        @Override
        public void afterUpdate(CollectionItem parent,
                Set<ContentItem> contentItems) {
            //not used
            
        }


        @Override
        public void beforeAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
        }

        @Override
        public void afterAdd(CollectionItem parent,
                Set<ContentItem> contentItems) {
        }

        @Override
        public void beforeRemove(CollectionItem parent, Set<Item> items) {
            
        }

        @Override
        public void afterRemove(CollectionItem parent, Set<Item> items) {
            
        }


    }
    
    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        
        testHelper = new TestHelper();
        storage = new MockDaoStorage();        
        contentDao = new MockContentDao(storage);
        lockManager = new SingleVMLockManager();
        service = new StandardContentService(contentDao, lockManager, new StandardTriageStatusQueryProcessor());
        
        simpleCheckCallExpectedHandler = new SimpleCheckCallExpected();
        checkManyAttendeesCallExpected = new CheckManyAttendeesCallExpected();        
        //Each test creates the advice with expected asserts
    }
    
    /**
     * Create service with the advice. It is not expected to create eventData for handers because they 
     * won't execute.
     */
    private void createProxyServiceWithoutExpectedAdviceExecution() {
        eventOperationExtensionsAdvice = new ContextServiceExtensionsAdvice(){
        };
        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service); 

        factory.addAspect(eventOperationExtensionsAdvice);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }
    
    /**
     * Create service with the advice. The handlers wll execure with expected results.
     */
    private void createProxyServiceWithExpectedAdviceExecution() {
        eventOperationExtensionsAdvice = new ContextServiceExtensionsAdvice();
        
        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service); 

        factory.addAspect(eventOperationExtensionsAdvice);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }
    
    /**
     * If no handler is specified, the event extension data is not created
     * because is not used. the processing continues.
     * @throws IOException 
     * @throws ValidationException 
     */
    @Test
    public void createEventNotCreatedIfNoHandlers() throws IOException, ValidationException{
        createProxyServiceWithoutExpectedAdviceExecution();
        
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        //call service
        ContentItem contentItem = testHelper.makeDummyContent(user);
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(contentItem);
        //test(fails if getEventData method is called)
        proxyService.createContentItems(rootCollection, contentItems);       

        proxyService.updateContent(contentItem);
        
        proxyService.removeContent(contentItem);

      
    }
    
    
    /**
     * 
     * @throws IOException 
     * @throws ValidationException 
     * @throws URISyntaxException  
     */
    @Test
    public void testSimpleContentItems() throws IOException, ValidationException, URISyntaxException{
        createProxyServiceWithExpectedAdviceExecution();
        
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        ContentItem contentItem = createSimpleContentItem(user);
        
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(contentItem);
        addSimpleCheckCallExpectedOnHandlers(rootCollection, contentItems);
        
        
        
        
        proxyService.createContentItems(rootCollection, contentItems);
        
        proxyService.updateContent(contentItem);
        
        proxyService.removeContent(contentItem);

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleAddContent() throws IOException, ValidationException, URISyntaxException{
        createProxyServiceWithExpectedAdviceExecution();
        List<EventAddHandler> lst = new ArrayList<EventAddHandler>();
        EventAddHandler mockAddHandler = Mockito.mock(EventAddHandler.class);
        lst.add(mockAddHandler);
        eventOperationExtensionsAdvice.setAddHandlers(lst);
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        ContentItem contentItem = createSimpleContentItem(user);
        
             
        
        
        proxyService.createContent(rootCollection, contentItem);
        
        Mockito.verify(mockAddHandler).beforeAdd(Mockito.eq(rootCollection), Mockito.any(Set.class));
    }
    
    /**
     * 
     * @throws IOException 
     * @throws ValidationException 
     * @throws URISyntaxException  
     * @throws ParserException  
     */
    @Test
    public void testContentItemManyAttendees() throws IOException, ValidationException, URISyntaxException, ParserException{
        createProxyServiceWithExpectedAdviceExecution();
        addCheckManyAttendeedCallExpectedOnHandlers();
        
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        ContentItem contentItem = createContentItemManyAttendees(user);
        
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(contentItem);
        
        
        
        proxyService.createContentItems(rootCollection, contentItems);
        
        proxyService.updateContent(contentItem);
        
        proxyService.removeContent(contentItem);
    }

    /**
     * Creates a simple item which will be checked with simpleCheckCallExpectedHandler.
     *  
     * @param user
     * @return
     * @throws URISyntaxException
     */
    private ContentItem createSimpleContentItem(User user) throws URISyntaxException {
        //call service
        ContentItem contentItem = testHelper.makeDummyContent(user);


        HibEventStamp eventStamp = new HibEventStamp();
        VEvent vEvent = new VEvent();
        vEvent.getProperties().add(Method.REQUEST);
        vEvent.getProperties().add(Version.VERSION_2_0);
        
        Attendee dev1 = new Attendee(URI.create("MAILTO:" + ATTENDEE_1));
        dev1.getParameters().add(Role.REQ_PARTICIPANT);
        dev1.getParameters().add(PartStat.NEEDS_ACTION);
        dev1.getParameters().add(Rsvp.TRUE);
        vEvent.getProperties().add(dev1);
                
        Organizer  organizer = new Organizer("MAILTO:" + ORGANIZER);        
        vEvent.getProperties().add(organizer);
        vEvent.getProperties().add(Status.VEVENT_CONFIRMED);
        vEvent.getProperties().add(Transp.OPAQUE);
            
        Calendar calendar = new Calendar();
        calendar.getComponents().add(vEvent);
            
        eventStamp.setEventCalendar(calendar);        
        contentItem.addStamp(eventStamp);
        return contentItem;
    }
    
    
    /**
     * If the item has no stamps, the event extension data is not created
     * because is not used. the processing continues.
     * @throws IOException 
     * @throws ValidationException 
     */
    @Test
    public void createEventNotCreatedIfNoStamps() throws IOException, ValidationException{
        createProxyServiceWithoutExpectedAdviceExecution();
        addNoCallExpectedOnHandlers();
 
        //test data        
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        //call service
        ContentItem contentItem = testHelper.makeDummyContent(user);
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(contentItem);
        
        //test(fails if getEventData method is called)
        proxyService.createContentItems(rootCollection, contentItems);     
        
        proxyService.updateContent(contentItem);
        
        proxyService.removeContent(contentItem);
    }

    /**
     * Test handlers are not called
     */
    private void addNoCallExpectedOnHandlers() {
        List<EventAddHandler> addHandlers = new ArrayList<EventAddHandler>();
        addHandlers.add(noCallExpectedHandler);
        eventOperationExtensionsAdvice.setAddHandlers(addHandlers);
        
        List<EventUpdateHandler> updateHandlers = new ArrayList<EventUpdateHandler>();
        updateHandlers.add(noCallExpectedHandler);
        eventOperationExtensionsAdvice.setUpdateHandlers(updateHandlers);
        
        List<EventRemoveHandler> removeHandlers = new ArrayList<EventRemoveHandler>();
        removeHandlers.add(noCallExpectedHandler);
        eventOperationExtensionsAdvice.setRemoveHandlers(removeHandlers);
    }
    
    /**
     * 
     * @param rootCollection 
     * @param contentItems 
     */
    private void addSimpleCheckCallExpectedOnHandlers(CollectionItem rootCollection, Set<ContentItem>contentItems) {
        List<EventAddHandler> addHandlers = new ArrayList<EventAddHandler>();
        simpleCheckCallExpectedHandler.setRootCollection(rootCollection);
        simpleCheckCallExpectedHandler.setContentItems(contentItems);
        
        addHandlers.add(simpleCheckCallExpectedHandler);
        eventOperationExtensionsAdvice.setAddHandlers(addHandlers);
        
        List<EventUpdateHandler> updateHandlers = new ArrayList<EventUpdateHandler>();
        updateHandlers.add(simpleCheckCallExpectedHandler);
        eventOperationExtensionsAdvice.setUpdateHandlers(updateHandlers);
        
        List<EventRemoveHandler> removeHandlers = new ArrayList<EventRemoveHandler>();
        removeHandlers.add(simpleCheckCallExpectedHandler);
        eventOperationExtensionsAdvice.setRemoveHandlers(removeHandlers);
    }
    
    /**
     * 
     */
    private void addCheckManyAttendeedCallExpectedOnHandlers() {
        List<EventAddHandler> addHandlers = new ArrayList<EventAddHandler>();
        addHandlers.add(checkManyAttendeesCallExpected);
        eventOperationExtensionsAdvice.setAddHandlers(addHandlers);
        
        List<EventUpdateHandler> updateHandlers = new ArrayList<EventUpdateHandler>();
        updateHandlers.add(checkManyAttendeesCallExpected);
        eventOperationExtensionsAdvice.setUpdateHandlers(updateHandlers);
        
        List<EventRemoveHandler> removeHandlers = new ArrayList<EventRemoveHandler>();
        removeHandlers.add(checkManyAttendeesCallExpected);
        eventOperationExtensionsAdvice.setRemoveHandlers(removeHandlers);
    }
    
    /**
     * If the item has no event stamps, the event extension data is not created
     * because is not used. the processing continues.
     * @throws IOException 
     * @throws ValidationException 
     */
    @Test
    public void createEventNotCreatedIfNoEventStamps() throws IOException, ValidationException{
        //advice setup
        createProxyServiceWithExpectedAdviceExecution();      
        addNoCallExpectedOnHandlers();
 
        
        //test data        
        User user = testHelper.makeDummyUser("user1", "password");

        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        //call service
        ContentItem contentItem = testHelper.makeDummyContent(user);
        contentItem.addStamp(new HibTaskStamp());
        
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(contentItem);
        //test(fails if getEventData method is called)
        proxyService.createContentItems(rootCollection, contentItems);     
        
        proxyService.updateContent(contentItem);
        
        proxyService.removeContent(contentItem);
    }
    
    /**
     * 
     * @param user 
     * @return ContentItem 
     * @throws ParserException 
     * @throws IOException 
     */
    private ContentItem createContentItemManyAttendees(User user) throws ParserException, IOException{
        String calendarContent =
                "BEGIN:VCALENDAR\n" +
                    "CALSCALE:GREGORIAN\n" +
                    "PRODID:-//Example Inc.//Example Calendar//EN\n" +
                    "VERSION:2.0\n" +
                    "BEGIN:VTIMEZONE\n" +
                    "LAST-MODIFIED:20040110T032845Z\n" +
                    "TZID:US/Eastern\n" +
                    "BEGIN:DAYLIGHT\n" +
                    "DTSTART:20000404T020000\n" +
                    "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4\n" +
                    "TZNAME:EDT\n" +
                    "TZOFFSETFROM:-0500\n" +
                    "TZOFFSETTO:-0400\n" +
                    "END:DAYLIGHT\n" +
                    "BEGIN:STANDARD\n" +
                    "DTSTART:20001026T020000\n" +
                    "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\n" +
                    "TZNAME:EST\n" +
                    "TZOFFSETFROM:-0400\n" +
                    "TZOFFSETTO:-0500\n" +
                    "END:STANDARD\n" +
                    "END:VTIMEZONE\n" +
                    "BEGIN:VEVENT\n" +
                    "ATTENDEE;CN=Attendee Attendee;PARTSTAT=ACCEPTED:mailto:testuser1@test.de\n" +
                    "ATTENDEE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:testuser2@test.de\n" +
                    "ATTENDEE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:testuser3@test.de\n" +
                    "DTSTAMP:20051228T232640Z\n" +
                    "DTSTART;TZID=US/Eastern:20060109T111500\n" +
                    "DURATION:PT1H\n" +
                    "ORGANIZER;CN=\"Bernard Desruisseaux\":mailto:bernard@example.com\n" +
                    "SUMMARY:Meeting 1.3\n" +
                    "UID:04625B9DFE753561D3D4D882@ninevah.local\n" +
                    "END:VEVENT\n" +
                    "END:VCALENDAR";
        
        Calendar calendar = CalendarUtils.parseCalendar(calendarContent);
         
        
        //call service
        ContentItem contentItem = testHelper.makeDummyContent(user);


        HibEventStamp eventStamp = new HibEventStamp();
            
        eventStamp.setEventCalendar(calendar);        
        contentItem.addStamp(eventStamp);
        return contentItem;
    }
    
}
