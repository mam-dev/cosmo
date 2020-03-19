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
package org.unitedinternet.cosmo.service.impl;

import java.io.FileInputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ModificationUid;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockEventExceptionStamp;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;

/**
 * Test Case for <code>StandardContentService</code> which uses mock
 * data access objects.
 *
 * @see StandardContentService
 * @see MockContentDao
 */
public class StandardContentServiceTest {

    private StandardContentService service;
    
    private MockContentDao contentDao;
    private MockDaoStorage storage;
    private SingleVMLockManager lockManager;
    private TestHelper testHelper;
    
    protected String baseDir = "src/test/unit/resources/testdata/";

    /**
     * Set up.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();        
        contentDao = new MockContentDao(storage);
        lockManager = new SingleVMLockManager();
        service = new StandardContentService(contentDao, lockManager, new StandardTriageStatusQueryProcessor());                
    }

    /**
     * Tests find item by path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFindItemByPath() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        Assert.assertNotNull(item);
        Assert.assertEquals(dummyContent, item);

        contentDao.removeContent(dummyContent);
    }
    
    /**
     * Tests invalid mod uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testInvalidModUid() throws Exception {
        
        Item item = service.findItemByUid("uid" + ModificationUid.RECURRENCEID_DELIMITER + "bogus");
        
        // bogus mod uid should result in no item found, not a ModelValidationException
        Assert.assertNull(item);
    }

    /**
     * Tests find non existent item by path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFindNonExistentItemByPath() throws Exception {
        String path = "/foo/bar/baz";
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        Assert.assertNull(item);
    }

    /**
     * Tests remove item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveItem() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        service.removeItem(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        Assert.assertNull(item);
        
        // cannot remove HomeCollection
        try {
            service.removeItem(rootCollection);
            Assert.fail("able to remove root!");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests create content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        ContentItem content = new MockNoteItem();
        content.getAttributeValue("");
        content.setName("foo");
        content.setOwner(user);
        content = service.createContent(rootCollection, content);

        Assert.assertNotNull(content);
        Assert.assertEquals("foo", content.getName());
        Assert.assertEquals(user, content.getOwner());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateContentThrowsExceptionForInvalidDates() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        NoteItem noteItem = new MockNoteItem();
        noteItem.getAttributeValue("");
        noteItem.setName("foo");
        noteItem.setOwner(user);
        
        Calendar c = new Calendar();
        VEvent e = new VEvent();
        e.getProperties().add(new DtStart("20131010T101010Z"));
        e.getProperties().add(new DtEnd("20131010T091010Z"));
        
        c.getComponents().add(e);
        MockEventStamp mockEventStamp = new MockEventStamp();
        mockEventStamp.setEventCalendar(c);
        noteItem.addStamp(mockEventStamp);
        
        service.createContent(rootCollection, noteItem);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUpdateCollectionFailsForEventsWithInvalidDates() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);

        NoteItem noteItem = new MockNoteItem();
        noteItem.getAttributeValue("");
        noteItem.setName("foo");
        noteItem.setOwner(user);
        
        Calendar c = new Calendar();
        VEvent e = new VEvent();
        e.getProperties().add(new DtStart("20131010T101010Z"));
        e.getProperties().add(new DtEnd("20131010T091010Z"));
        
        c.getComponents().add(e);
        MockEventStamp mockEventStamp = new MockEventStamp();
        mockEventStamp.setEventCalendar(c);
        noteItem.addStamp(mockEventStamp);
        
        service.updateCollection(rootCollection, Collections.singleton((Item)noteItem));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateContentItemsForEventsWithInvalidDates() throws Exception {
        NoteItem masterEvent = new MockNoteItem();
        
        MockEventStamp mockEventStamp = new MockEventStamp();
        mockEventStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T101010Z"));
        
        masterEvent.addStamp(mockEventStamp);
        mockEventStamp.setItem(masterEvent);
        
        NoteItem overridenComponent = new MockNoteItem();
        MockEventExceptionStamp overridenComponentStamp = new MockEventExceptionStamp();
        overridenComponentStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T091010Z"));
        overridenComponent.addStamp(overridenComponentStamp);
        overridenComponentStamp.setItem(overridenComponent);
        
        masterEvent.addModification(overridenComponent);
        
        service.createContentItems(createParent(), Collections.<ContentItem>singleton(masterEvent));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUpdateContentItemsForEventsWithInvalidDates() throws Exception {
        NoteItem masterEvent = new MockNoteItem();
        
        MockEventStamp mockEventStamp = new MockEventStamp();
        mockEventStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T101010Z"));
        
        masterEvent.addStamp(mockEventStamp);
        mockEventStamp.setItem(masterEvent);
        
        NoteItem overridenComponent = new MockNoteItem();
        MockEventExceptionStamp overridenComponentStamp = new MockEventExceptionStamp();
        overridenComponentStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T091010Z"));
        overridenComponent.addStamp(overridenComponentStamp);
        overridenComponentStamp.setItem(overridenComponent);
        
        masterEvent.addModification(overridenComponent);
        
        service.updateContentItems(createParent(), Collections.<ContentItem>singleton(masterEvent));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUpdateContentForEventsWithInvalidDates() throws Exception {
        NoteItem masterEvent = new MockNoteItem();
        
        MockEventStamp mockEventStamp = new MockEventStamp();
        mockEventStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T101010Z"));
        
        masterEvent.addStamp(mockEventStamp);
        mockEventStamp.setItem(masterEvent);
        
        NoteItem overridenComponent = new MockNoteItem();
        MockEventExceptionStamp overridenComponentStamp = new MockEventExceptionStamp();
        overridenComponentStamp.setEventCalendar(createEventCalendarWithDates("20131010T101010Z", "20131010T091010Z"));
        overridenComponent.addStamp(overridenComponentStamp);
        overridenComponentStamp.setItem(overridenComponent);
        
        masterEvent.addModification(overridenComponent);
        
        service.updateContent(masterEvent);
    }
    private CollectionItem createParent(){
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        return rootCollection;
    }
    
    private Calendar createEventCalendarWithDates(String start, String end) throws ParseException{
        Calendar c = new Calendar();
        VEvent e = new VEvent();
        e.getProperties().add(new DtStart(start));
        e.getProperties().add(new DtEnd(end));
        
        c.getComponents().add(e);
        
        return c;
    }
    
    
    
    

    /**
     * Tests remove content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveContent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user);
        dummyContent = contentDao.createContent(rootCollection, dummyContent);

        service.removeContent(dummyContent);

        String path = "/" + user.getUsername() + "/" + dummyContent.getName();
        Item item = service.findItemByPath(path);

        // XXX service should throw exception rather than return null
        Assert.assertNull(item);
    }
    
    /**
     * Tests create collection with children.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        NoteItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar");
        dummyContent.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        Assert.assertNotNull(dummyCollection);
        Assert.assertEquals(1, dummyCollection.getChildren().size());
        Assert.assertEquals("bar", 
                dummyCollection.getChildren().iterator().next().getName());
    }
    
    /**
     * Tests update collection with children.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUpdateCollectionWithChildren() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent1 = new MockNoteItem();
        dummyContent1.setName("bar1");
        dummyContent1.setOwner(user);
        
        ContentItem dummyContent2 = new MockNoteItem();
        dummyContent2.setName("bar2");
        dummyContent2.setOwner(user);
        
        HashSet<Item> children = new HashSet<Item>();
        children.add(dummyContent1);
        children.add(dummyContent2);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection, children);
        
        Assert.assertEquals(2, dummyCollection.getChildren().size());
        
        ContentItem bar1 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        ContentItem bar2 = 
            getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        Assert.assertNotNull(bar1);
        Assert.assertNotNull(bar2);
        
        bar1.setIsActive(false);
       
        ContentItem bar3 = new MockNoteItem();
        bar3.setName("bar3");
        bar3.setOwner(user);
        
        children.clear();
        children.add(bar1);
        children.add(bar2);
        children.add(bar3);
        
        dummyCollection = service.updateCollection(dummyCollection, children);
          
        Assert.assertEquals(2, dummyCollection.getChildren().size());
        
        bar1 = getContentItemFromSet(dummyCollection.getChildren(), "bar1");
        bar2 = getContentItemFromSet(dummyCollection.getChildren(), "bar2");
        bar3 = getContentItemFromSet(dummyCollection.getChildren(), "bar3");
        
        Assert.assertNull(bar1);
        Assert.assertNotNull(bar2);
        Assert.assertNotNull(bar3);
    }
    /**
     * Tests collection hash gets updated.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCollectionHashGetsUpdated() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        
        CollectionItem dummyCollection = new MockCollectionItem();
        dummyCollection.setName("foo");
        dummyCollection.setOwner(user);
        
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("bar1");
        dummyContent.setOwner(user);
        
        dummyCollection = 
            service.createCollection(rootCollection, dummyCollection);
        
        dummyContent = 
            service.createContent(dummyCollection, dummyContent);
        
        Assert.assertEquals(1, dummyCollection.generateHash());
        
        dummyContent = service.updateContent(dummyContent);
           
        Assert.assertEquals(2, dummyCollection.generateHash());
        
        dummyContent = service.updateContent(dummyContent);
        Assert.assertEquals(3, dummyCollection.generateHash());
    }
    
    /**
     * Tests update.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUpdateEvent() throws Exception {
        User user = testHelper.makeDummyUser();
        CollectionItem rootCollection = contentDao.createRootItem(user);
        NoteItem masterNote = new MockNoteItem();
        masterNote.setName("foo");
        masterNote.setOwner(user);
        
        Calendar calendar = getCalendar("event_with_exceptions1.ics"); 
        
        EventStamp eventStamp = new MockEventStamp(masterNote);
        masterNote.addStamp(eventStamp);
        contentDao.createContent(rootCollection, masterNote);
        
        EntityConverter converter = new EntityConverter(testHelper.getEntityFactory());
        Set<ContentItem> toUpdate = new HashSet<ContentItem>();
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents().iterator().next(), toUpdate);
        
        Calendar masterCal = eventStamp.getEventCalendar();
        VEvent masterEvent = eventStamp.getMasterEvent();
        
        Assert.assertEquals(1, masterCal.getComponents().getComponents(Component.VEVENT).size());
        Assert.assertNull(eventStamp.getMasterEvent().getRecurrenceId());
        
        Assert.assertEquals(masterNote.getModifications().size(), 4);
        for(NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp eventException = StampUtils.getEventExceptionStamp(mod);
            VEvent exceptionEvent = eventException.getExceptionEvent();
            Assert.assertEquals(mod.getModifies(), masterNote);
            Assert.assertEquals(masterEvent.getUid().getValue(), exceptionEvent.getUid().getValue());
        }
        
        Calendar fullCal = converter.convertNote(masterNote);
      
        Assert.assertNotNull(getEvent("20060104T140000", fullCal));
        Assert.assertNotNull(getEvent("20060105T140000", fullCal));
        Assert.assertNotNull(getEvent("20060106T140000", fullCal));
        Assert.assertNotNull(getEvent("20060107T140000", fullCal));
        
        Assert.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060107T140000", masterNote.getModifications()));
        
        Assert.assertEquals(fullCal.getComponents().getComponents(Component.VEVENT).size(), 5);
        
        // now update
        calendar = getCalendar("event_with_exceptions2.ics"); 
        toUpdate.addAll(converter.convertEventCalendar(masterNote, calendar));
        service.updateContentItems(masterNote.getParents().iterator().next(), toUpdate);
        
        fullCal = converter.convertNote(masterNote);
        
        // should have removed 1, added 2 so that makes 4-1+2=5
        Assert.assertEquals(masterNote.getModifications().size(), 5);
        Assert.assertNotNull(getEventException("20060104T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060105T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060106T140000", masterNote.getModifications()));
        Assert.assertNull(getEventException("20060107T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060108T140000", masterNote.getModifications()));
        Assert.assertNotNull(getEventException("20060109T140000", masterNote.getModifications()));
        
        Assert.assertNotNull(getEvent("20060104T140000", fullCal));
        Assert.assertNotNull(getEvent("20060105T140000", fullCal));
        Assert.assertNotNull(getEvent("20060106T140000", fullCal));
        Assert.assertNull(getEvent("20060107T140000", fullCal));
        Assert.assertNotNull(getEvent("20060108T140000", fullCal));
        Assert.assertNotNull(getEvent("20060109T140000", fullCal));
    }
    
    /**
     * Gets content item from set.
     * @param items The items.
     * @param name The name.
     * @return The content item.
     */
    private ContentItem getContentItemFromSet(Set<Item> items, String name) {
        for(Item item : items) {
            if(item.getName().equals(name)) {
                return (ContentItem) item;
            }
        }   
        return null;
    }
    /**
     * Gets event exception.
     * @param recurrenceId The recurrence id.
     * @param items The items.
     * @return The event exceptions stamp.
     */
    private EventExceptionStamp getEventException(String recurrenceId, Set<NoteItem> items) {
        for(NoteItem mod : items) {
            EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
            if (ees.getRecurrenceId().toString().equals(recurrenceId)) {
                return ees;
            }
        }
        return null;
    }
    
    /**
     * Gets calendar.
     * @param filename The file name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private Calendar getCalendar(String filename) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + filename);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
    /**
     * Gets event.
     * @param recurrenceId The recurrence id.
     * @param calendar The calendar.
     * @return The event.
     */
    private VEvent getEvent(String recurrenceId, Calendar calendar) {
        ComponentList<VEvent> events = calendar.getComponents().getComponents(Component.VEVENT);
        for(VEvent event : events) {            
            if(event.getRecurrenceId()!=null && event.getRecurrenceId().getDate().toString().equals(recurrenceId))
                return event;
        }
        return null;
    }
}
