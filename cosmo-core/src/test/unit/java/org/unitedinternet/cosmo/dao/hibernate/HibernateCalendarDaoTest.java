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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.IsNotDefinedFilter;
import org.unitedinternet.cosmo.calendar.query.PropertyFilter;
import org.unitedinternet.cosmo.calendar.query.TextMatchFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

/**
 * Test CalendarDaoImpl
 */
public class HibernateCalendarDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    protected CalendarDaoImpl calendarDao;

    @Autowired
    protected ContentDaoImpl contentDao;

    @Autowired
    protected UserDaoImpl userDao;

    /**
     * Constructor.
     */
    public HibernateCalendarDaoTest() {
        super();
    }

    /**
     * Tests calendar dao basic.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarDaoBasic() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());

        CalendarCollectionStamp ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);

        Assert.assertNotNull(queryItem);
        Assert.assertEquals("test", queryItem.getName());
        Assert.assertEquals("en", ccs.getLanguage());
        Assert.assertEquals("test description", ccs.getDescription());

        // test update
        queryItem.setName("test2");
        ccs.setLanguage("es");
        ccs.setDescription("test description2");

        contentDao.updateCollection(queryItem);
        Assert.assertNotNull(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);

        Assert.assertEquals("test2", queryItem.getName());
        Assert.assertEquals("es", ccs.getLanguage());
        Assert.assertEquals("test description2", ccs.getDescription());

        // test add event
        ContentItem event = generateEvent("test.ics", "cal1.ics", "testuser");

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ContentItem newEvent = contentDao.createContent(calendar, event);

        clearSession();

        // test query event
        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        EventStamp evs = (EventStamp) queryEvent.getStamp(EventStamp.class);

        Assert.assertEquals("test.ics", queryEvent.getName());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());

        // test update event
        queryEvent.setName("test2.ics");
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes("cal2.ics")));

        queryEvent = contentDao.updateContent(queryEvent);

        Calendar cal = evs.getEventCalendar();

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        evs = (EventStamp) queryEvent.getStamp(EventStamp.class);

        Assert.assertEquals("test2.ics", queryEvent.getName());
        Assert.assertEquals(evs.getEventCalendar().toString(), cal.toString());

        // test delete
        contentDao.removeContent(queryEvent);

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        Assert.assertNull(queryEvent);

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Tests long property value.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @Test
    public void testLongPropertyValue() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        ContentItem event = generateEvent("big.ics", "big.ics", "testuser");

        event = contentDao.createContent(calendar, event);

        clearSession();

        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(event.getUid());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());
    }

    /**
     * Tests find by Ical uid.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @Test
    public void testFindByEventIcalUid() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        NoteItem event = generateEvent("test.ics", "cal1.ics", "testuser");

        event = (NoteItem) contentDao.createContent(calendar, event);

        clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        String uid = "68ADA955-67FF-4D49-BBAC-AF182C620CF6";
        ContentItem queryEvent = calendarDao.findEventByIcalUid(uid, calendar);
        Assert.assertNotNull(queryEvent);
        Assert.assertEquals(event.getUid(), queryEvent.getUid());
    }

    /**
     * Gets calendar querying.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testCalendarQuerying() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        for (int i = 1; i <= 5; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "cal" + i + ".ics", "testuser");
            contentDao.createContent(calendar, event);
        }

        CalendarFilter filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        ComponentFilter eventFilter = new ComponentFilter("VEVENT");
        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        eventFilter.getPropFilters().add(propFilter);

        clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());

        // Should match ics.1
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        ContentItem nextItem = queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // Should match all
        eventFilter.setPropFilters(new ArrayList());
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        // should match three
        PropertyFilter propFilter2 = new PropertyFilter("SUMMARY");
        propFilter2.setTextMatchFilter(new TextMatchFilter("Physical"));
        eventFilter.getPropFilters().add(propFilter2);
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(3, queryEvents.size());

        // should match everything except #1...so that means 4
        eventFilter.getPropFilters().remove(propFilter2);
        eventFilter.getPropFilters().add(propFilter);
        propFilter.getTextMatchFilter().setNegateCondition(true);
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(4, queryEvents.size());

        // should match ics.1 again
        propFilter.getTextMatchFilter().setNegateCondition(false);
        propFilter.getTextMatchFilter().setValue("vISiBlE");
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // should match all 5 (none have rrules)
        propFilter.setTextMatchFilter(null);
        propFilter.setName("RRULE");
        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        // time range test
        eventFilter.setPropFilters(new ArrayList());
        DateTime start = new DateTime("20050817T115000Z");
        DateTime end = new DateTime("20050818T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRange = new TimeRangeFilter(period);
        eventFilter.setTimeRangeFilter(timeRange);

        // should match ics.1
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        // 10 year period
        start.setTime(new GregorianCalendar(1996, 1, 22).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);
        timeRange.setPeriod(period);

        // should match all now
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(5, queryEvents.size());

        propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        eventFilter.getPropFilters().add(propFilter);

        // should match ics.1
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(1, queryEvents.size());
        nextItem = (ContentItem) queryEvents.iterator().next();
        Assert.assertEquals("test1.ics", nextItem.getName());

        start.setTime(new GregorianCalendar(2006, 8, 6).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        period = new Period(start, end);

        timeRange.setPeriod(period);

        // should match none
        queryEvents = calendarDao.findCalendarItems(calendar, filter);
        Assert.assertEquals(0, queryEvents.size());
    }

    /**
     * Gets calendar querying.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarQueryingTimeRangeDaily() throws Exception {

    }

    /**
     * Gets user.
     * 
     * @param userDao
     *            The userDao.
     * @param username
     *            The username.
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    /**
     * Generates calendar.
     * 
     * @param name
     *            The name.
     * @param owner
     *            The owner.
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
     * 
     * @param name
     *            The name.
     * @param file
     *            The file.
     * @param owner
     *            The owner.
     * @return The note item.
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    private NoteItem generateEvent(String name, String file, String owner) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setOwner(getUser(userDao, owner));

        EventStamp evs = new HibEventStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
        event.setIcalUid(evs.getIcalUid());
        if (evs.getEvent().getDescription() != null) {
            event.setBody(evs.getEvent().getDescription().getValue());
        }
        if (evs.getEvent().getSummary() != null) {
            event.setDisplayName(evs.getEvent().getSummary().getValue());
        }

        return event;
    }

    /**
     * Gets calendar.
     * 
     * @param item
     *            The content item.
     * @return The calendar.
     */
    private Calendar getCalendar(ContentItem item) {
        return new EntityConverter(null).convertContent(item);
    }

}
