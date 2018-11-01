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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Set;

import org.junit.Before;
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
public class HibernateCalendarDaoTest extends AbstractSpringDaoTestCase {
	
    @Autowired
    protected CalendarDaoImpl calendarDao;

    @Autowired
    protected ContentDaoImpl contentDao;

    @Autowired
    protected UserDaoImpl userDao;

    private CollectionItem calendar;
    private CalendarFilter filter;
    private ComponentFilter eventFilter;

    @Before
    public void setUpCalendar() throws Exception {
        calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        calendar = contentDao.createCollection(root, calendar);

        for (int i = 1; i <= 5; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "cal" + i + ".ics", "testuser");
            contentDao.createContent(calendar, event);
        }
        this.clearSession();
        this.calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
    }

    // Query-filters BEGIN

    @Before
    public void setUpFilters() {
        this.filter = new CalendarFilter();
        ComponentFilter compFilter = new ComponentFilter("VCALENDAR");
        this.eventFilter = new ComponentFilter("VEVENT");
        this.filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);
    }

    @Test
    public void shouldMatchFirstEvent() {
        // Should match ics.1
        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        this.eventFilter.getPropFilters().add(propFilter);

        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(1, queryEvents.size());
        ContentItem nextItem = queryEvents.iterator().next();
        assertEquals("test1.ics", nextItem.getName());
    }

    @Test
    public void shouldMatchAllWhenNoPropertyFilterIsSet() {
        // Should match all when no f
        eventFilter.getPropFilters().clear();
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(5, queryEvents.size());
    }

    @Test
    public void shouldMatchTreeEventsForTwoPropertyFilters() {
        // should match three

        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("Visible"));
        this.eventFilter.getPropFilters().add(propFilter);

        PropertyFilter propFilter2 = new PropertyFilter("SUMMARY");
        propFilter2.setTextMatchFilter(new TextMatchFilter("Physical"));
        eventFilter.getPropFilters().add(propFilter2);
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(3, queryEvents.size());
    }

    @Test
    public void shouldMatchEverythingExceptNegating() {
        // should match everything except #1...so that means 4
        eventFilter.getPropFilters().clear();
        PropertyFilter propFilter1 = new PropertyFilter("SUMMARY");
        propFilter1.setTextMatchFilter(new TextMatchFilter("Visible"));
        propFilter1.getTextMatchFilter().setNegateCondition(true);
        this.eventFilter.getPropFilters().add(propFilter1);
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(4, queryEvents.size());
    }

    @Test
    public void shouldMatchEventOneWithIgnoreCase() {
        // should match ics.1 again
        PropertyFilter propFilter = new PropertyFilter("SUMMARY");
        propFilter.setTextMatchFilter(new TextMatchFilter("vISiBlE"));
        propFilter.getTextMatchFilter().setNegateCondition(false);
        this.eventFilter.getPropFilters().add(propFilter);
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(1, queryEvents.size());
        ContentItem nextItem = (ContentItem) queryEvents.iterator().next();
        assertEquals("test1.ics", nextItem.getName());
    }

    @Test
    public void shouldMatchAllWhenLookingForNonExistentProperty() {
        // should match all 5 (none have rrules)
        PropertyFilter propFilter = new PropertyFilter("RRULE");
        propFilter.setTextMatchFilter(null);
        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        this.eventFilter.getPropFilters().add(propFilter);

        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(5, queryEvents.size());
    }

    @Test
    public void shouldMatchEventOneWithTimeRangeFilter() throws ParseException {
        // Time range test
        eventFilter.getPropFilters().clear();
        DateTime start = new DateTime("20050817T115000Z");
        DateTime end = new DateTime("20050818T115000Z");

        Period period = new Period(start, end);
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        eventFilter.setTimeRangeFilter(timeRangeFilter);

        // should match ics.1
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(1, queryEvents.size());
        ContentItem nextItem = (ContentItem) queryEvents.iterator().next();
        assertEquals("test1.ics", nextItem.getName());
    }

    @Test
    public void shouldMatchAllEventsInTenYearsPeriod() {
        eventFilter.getPropFilters().clear();
        // 10 year period
        DateTime start = new DateTime();
        DateTime end = new DateTime();
        start.setTime(new GregorianCalendar(1996, 1, 22).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(new Period(start, end));
        eventFilter.setTimeRangeFilter(timeRangeFilter);

        // should match all now
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(5, queryEvents.size());
    }

    @Test
    public void shouldMatchNoneInTimeRange() {
        eventFilter.getPropFilters().clear();
        // 10 year period
        DateTime start = new DateTime();
        DateTime end = new DateTime();
        start.setTime(new GregorianCalendar(2006, 8, 6).getTimeInMillis());
        end.setTime(System.currentTimeMillis());
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(new Period(start, end));
        eventFilter.setTimeRangeFilter(timeRangeFilter);
        // should match none
        Set<ICalendarItem> queryEvents = calendarDao.findCalendarItems(calendar, filter);
        assertEquals(0, queryEvents.size());
    }

    // Query-filters END

    @Test
    public void shouldCorrectlyCreateUpdateAndFind() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());

        CalendarCollectionStamp ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);

        assertNotNull(queryItem);
        assertEquals("test", queryItem.getName());
        assertEquals("en", ccs.getLanguage());
        assertEquals("test description", ccs.getDescription());

        // test update
        queryItem.setName("test2");
        ccs.setLanguage("es");
        ccs.setDescription("test description2");

        contentDao.updateCollection(queryItem);
        assertNotNull(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);

        assertEquals("test2", queryItem.getName());
        assertEquals("es", ccs.getLanguage());
        assertEquals("test description2", ccs.getDescription());

        // test add event
        ContentItem event = generateEvent("test.ics", "cal1.ics", "testuser");

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ContentItem newEvent = contentDao.createContent(calendar, event);

        clearSession();

        // test query event
        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        EventStamp evs = (EventStamp) queryEvent.getStamp(EventStamp.class);

        assertEquals("test.ics", queryEvent.getName());
        assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());

        // test update event
        queryEvent.setName("test2.ics");
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes("cal2.ics")));

        queryEvent = contentDao.updateContent(queryEvent);

        Calendar cal = evs.getEventCalendar();

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        evs = (EventStamp) queryEvent.getStamp(EventStamp.class);

        assertEquals("test2.ics", queryEvent.getName());
        assertEquals(evs.getEventCalendar().toString(), cal.toString());

        // test delete
        contentDao.removeContent(queryEvent);

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        assertNull(queryEvent);

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        assertNull(queryItem);
    }

    @Test
    public void shouldCorrectlyCreateBigContent() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        ContentItem event = generateEvent("big.ics", "big.ics", "testuser");

        event = contentDao.createContent(calendar, event);

        clearSession();

        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(event.getUid());
        assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());
    }

    @Test
    public void shouldCorrectlyFindByICalUid() throws Exception {
        CollectionItem calendar = generateCalendar("test", "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(getUser(userDao, "testuser"));

        contentDao.createCollection(root, calendar);

        NoteItem event = generateEvent("test.ics", "cal1.ics", "testuser");

        event = (NoteItem) contentDao.createContent(calendar, event);

        this.clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        String uid = "68ADA955-67FF-4D49-BBAC-AF182C620CF6";
        ContentItem queryEvent = calendarDao.findEventByIcalUid(uid, calendar);
        assertNotNull(queryEvent);
        assertEquals(event.getUid(), queryEvent.getUid());
    }

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

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private Calendar getCalendar(ContentItem item) {
        return new EntityConverter(null).convertContent(item);
    }
}
