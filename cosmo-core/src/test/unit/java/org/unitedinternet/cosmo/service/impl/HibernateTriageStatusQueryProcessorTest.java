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
package org.unitedinternet.cosmo.service.impl;

import java.util.Set;

import net.fortuna.ical4j.model.DateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.dao.hibernate.AbstractSpringDaoTestCase;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.dao.hibernate.UserDaoImpl;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test StandardTriageStatusQueryProcessor using hibernate
 * implementations.
 */
public class HibernateTriageStatusQueryProcessorTest extends AbstractSpringDaoTestCase {

    @Autowired
    private ContentDaoImpl contentDao;
    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private StandardTriageStatusQueryProcessor queryProcessor;
    
    protected static final String CALENDAR_UID = "calendaruid";
    protected static final String NOTE_UID = "note";

    /**
     * Constructor.
     */
    public HibernateTriageStatusQueryProcessorTest() {
        super();
    }
    
    /**
     * On set up in transaction
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void onSetUpInTransaction() throws Exception {
        queryProcessor = new StandardTriageStatusQueryProcessor();
        queryProcessor.setContentDao(contentDao);
        
        CollectionItem calendar = generateCalendar("testcalendar", "testuser");
        
        calendar.setUid(CALENDAR_UID);
        
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));
        
        contentDao.createCollection(root, calendar);
       
        NoteItem note = generateNote("testlaternote", "testuser");
        note.setUid(NOTE_UID + "later");
        note.getTriageStatus().setCode(TriageStatus.CODE_LATER);
        contentDao.createContent(calendar, note);
        
        note = generateNote("testdonenote", "testuser");
        note.setUid(NOTE_UID + "done");
        note.getTriageStatus().setCode(TriageStatus.CODE_DONE);
        note = (NoteItem) contentDao.createContent(calendar, note);
        
        NoteItem noteMod = generateNote("testnotemod", "testuser");
        noteMod.setUid(NOTE_UID + "mod");
        noteMod.setModifies(note);
        noteMod.getTriageStatus().setCode(TriageStatus.CODE_NOW);
        noteMod = (NoteItem) contentDao.createContent(calendar, noteMod);
        
        for (int i = 1; i <= 3; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "eventwithtimezone"
                    + i + ".ics", "testuser");
            event.setUid("calendar2_" + i);
            contentDao.createContent(calendar, event);
        }
    }

    /**
     * Tests get all collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetAllCollection() throws Exception {
        CollectionItem calendar = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID);
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(null, new DateTime("20070601T000000Z"), null);
        Set<NoteItem> done = queryProcessor.processTriageStatusQuery(calendar, context);
        Assert.assertEquals(10, done.size());
        verifyItemInSet(done,NOTE_UID + "later");
        verifyItemInSet(done,NOTE_UID + "done");
        verifyItemInSet(done,NOTE_UID + "mod");
        verifyItemInSet(done,"calendar2_1:20070529T101500Z");
        verifyItemInSet(done,"calendar2_3:20070531T081500Z");
        verifyItemInSet(done,"calendar2_1");
        verifyItemInSet(done,"calendar2_2");
        verifyItemInSet(done,"calendar2_3");
    }

    /**
     * Tests get done collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetDoneCollection() throws Exception {
        CollectionItem calendar = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID);
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_DONE, new DateTime("20070601T000000Z"), null);
        Set<NoteItem> done = queryProcessor.processTriageStatusQuery(calendar, context);
        Assert.assertEquals(5, done.size());
        verifyItemInSet(done,NOTE_UID + "done");
        verifyItemInSet(done,"calendar2_1:20070529T101500Z");
        verifyItemInSet(done,"calendar2_3:20070531T081500Z");
        verifyItemInSet(done,"calendar2_1");
        verifyItemInSet(done,"calendar2_3");
    }
    
    /**
     * Tests get done item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetDoneItem() throws Exception {
        NoteItem done = (NoteItem) contentDao.findItemByUid("calendar2_1");
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_DONE, new DateTime("20070601T000000Z"), null);
        Set<NoteItem> results = queryProcessor.processTriageStatusQuery(done, context);
        Assert.assertEquals(2, results.size());
        
        verifyItemInSet(results,"calendar2_1:20070529T101500Z");
        verifyItemInSet(results,"calendar2_1");
    }
    
    /**
     * Tests get later collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetLaterCollection() throws Exception {
        CollectionItem calendar = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID);
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_LATER, new DateTime("20070601T000000Z"), null);
        Set<NoteItem> later = queryProcessor.processTriageStatusQuery(calendar, context);
        Assert.assertEquals(5, later.size());
        verifyItemInSet(later,NOTE_UID + "later");
        verifyItemInSet(later,"calendar2_1:20070605T101500Z");
        verifyItemInSet(later,"calendar2_3:20070601T081500Z");
        verifyItemInSet(later,"calendar2_1");
        verifyItemInSet(later,"calendar2_3");
    }
    
    /**
     * Tests get later item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetLaterItem() throws Exception {
        NoteItem later = (NoteItem) contentDao.findItemByUid("calendar2_1");
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_LATER, new DateTime("20070601T000000Z"), null);
        Set<NoteItem> results = queryProcessor.processTriageStatusQuery(later, context);
        Assert.assertEquals(2, results.size());
        verifyItemInSet(results,"calendar2_1:20070605T101500Z");
        verifyItemInSet(results,"calendar2_1");
    }
    
    /**
     * Tests get now collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetNowCollection() throws Exception {
        CollectionItem calendar = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID);
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_NOW, new DateTime("20070601T083000Z"), null);
        Set<NoteItem> now = queryProcessor.processTriageStatusQuery(calendar, context);
        Assert.assertEquals(5, now.size());
        
        // should be included because triage status is NOW
        verifyItemInSet(now,NOTE_UID + "mod");
        // should be included because its the parent of a modification included
        verifyItemInSet(now,NOTE_UID + "done");
        // should be included because triage status is null
        verifyItemInSet(now, "calendar2_2");
        // should be included because occurence overlaps instant in time
        verifyItemInSet(now,"calendar2_3:20070601T081500Z");
        // should be included because occurrence is included
        verifyItemInSet(now, "calendar2_3");
    }
    
    /**
     * Tests get now item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetNowItem() throws Exception {
        NoteItem now = (NoteItem) contentDao.findItemByUid("calendar2_3");
        TriageStatusQueryContext context =
            new TriageStatusQueryContext(TriageStatus.LABEL_NOW, new DateTime("20070601T083000Z"), null);
        Set<NoteItem> results = queryProcessor.processTriageStatusQuery(now, context);
        Assert.assertEquals(2, results.size());
        
        // should be included because occurence overlaps instant in time
        verifyItemInSet(results,"calendar2_3:20070601T081500Z");
        // should be included because occurrence is included
        verifyItemInSet(results, "calendar2_3");
    }
    
    /**
     * Gets user.
     * @param userDao The userDao.
     * @param username The username.
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    /**
     * Generates calendar.
     * @param name The name.
     * @param owner The owner.
     * @return The collection item.
     */
    private CollectionItem generateCalendar(String name, String owner) {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setName(name);
        calendar.setOwner(getUser(userDao, owner));
        
        CalendarCollectionStamp ccs = new HibCalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");
        
        return calendar;
    }

    /**
     * Generates event.
     * @param name The name.
     * @param file The file.
     * @param owner The owner.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private NoteItem generateEvent(String name, String file, String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        EventStamp evs = new HibEventStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
       
        return event;
    }
    
    /**
     * Generates note.
     * @param name The name.
     * @param owner The owner.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private NoteItem generateNote(String name,
            String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(getUser(userDao, owner));
       
        return event;
    }
    
    /**
     * Verify item in set
     * @param items The set with items.
     * @param uid The uid.
     */
    private void verifyItemInSet(Set<NoteItem> items, String uid) {
        for(Item item: items) {
            if(item.getUid().equals(uid)) {
                return;
            }
        }
        
        Assert.fail("item " + uid + " not in set");   
    }

}
