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
package org.unitedinternet.cosmo.calendar.query.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.dao.mock.MockCalendarDao;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;

/**
 * Test StandardCalendarQueryProcessorTest using mock implementations.
 *
 */
public class StandardCalendarQueryProcessorTest {

    private MockCalendarDao calendarDao;
    private MockContentDao contentDao;
    private MockEntityFactory factory;
    private MockDaoStorage storage;
    private TestHelper testHelper;
    private StandardCalendarQueryProcessor queryProcessor;

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    
    protected static final String CALENDAR_UID = "calendaruid";

    /**
     * Constructor.
     */
    public StandardCalendarQueryProcessorTest() {
        super();
    }
    
    /**
     * SetUp
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        factory = new MockEntityFactory();
        storage = new MockDaoStorage();
        contentDao = new MockContentDao(storage);
        calendarDao = new MockCalendarDao(storage);
        queryProcessor = new StandardCalendarQueryProcessor(new EntityConverter(factory), contentDao, calendarDao);
        queryProcessor.setCalendarDao(calendarDao);
     
        User user = testHelper.makeDummyUser();
        CollectionItem root = contentDao.createRootItem(user);
        
        CollectionItem calendar = generateCalendar("testcalendar", user);
        calendar.setUid(CALENDAR_UID);
        calendar.setName(calendar.getUid());
          
        contentDao.createCollection(root, calendar);
        
        for (int i = 1; i <= 3; i++) {
            ContentItem event = generateEvent("test" + i + ".ics", "eventwithtimezone"
                    + i + ".ics", user);
            event.setUid(CALENDAR_UID + i);
            contentDao.createContent(calendar, event);
        }
        
        FreeBusyItem fb = generateFreeBusy("test4.ics", "vfreebusy.ics", user);
        fb.setUid(CALENDAR_UID + "4");
        contentDao.createContent(calendar, fb);
    }
    
    /**
     * Test the add of busy periods recurring all day.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAddBusyPeriodsRecurringAllDay() throws Exception {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        // the range
        DateTime start = new DateTime("20070103T090000Z");
        DateTime end = new DateTime("20070117T090000Z");
        
        Period fbRange = new Period(start, end);
        
        Calendar calendar = CalendarUtils.parseCalendar(testHelper.getBytes("allday_weekly_recurring.ics"));
        
        // test several timezones
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070108T060000Z/20070109T060000Z,20070115T060000Z/20070116T060000Z", busyPeriods.toString());
        
        busyPeriods.clear();
        
        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070108T080000Z/20070109T080000Z,20070115T080000Z/20070116T080000Z", busyPeriods.toString());
        
        busyPeriods.clear();
        
        tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");
        queryProcessor.addBusyPeriods(calendar, tz, fbRange, busyPeriods, busyTentativePeriods, busyUnavailablePeriods);
        
        Assert.assertEquals("20070107T130000Z/20070108T130000Z,20070114T130000Z/20070115T130000Z", busyPeriods.toString());
    }

    /**
     * Tests free busy query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFreeBusyQuery() throws Exception {
        DateTime start = new DateTime("20070507T051500Z");
        DateTime end = new DateTime("200705016T051500Z");
        
        Period period = new Period(start, end);
        
        CollectionItem calendar = (CollectionItem) contentDao.findItemByUid(CALENDAR_UID);
        
        // verify we get resuts from VEVENTS in collection
        VFreeBusy vfb = queryProcessor.freeBusyQuery(calendar, period);
        
        verifyPeriods(vfb, FbType.BUSY, "20070508T081500Z/20070508T091500Z,"
               + "20070509T081500Z/20070509T091500Z,20070510T081500Z/20070510T091500Z,"
               + "20070511T081500Z/20070511T091500Z,20070512T081500Z/20070512T091500Z,"
               + "20070513T081500Z/20070513T091500Z,20070514T081500Z/20070514T091500Z,"
               + "20070515T081500Z/20070515T091500Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20070508T101500Z/20070508T111500Z,"
                + "20070515T101500Z/20070515T111500Z");
       
        // verify we get resuts from VFREEBUSY in collection
        start = new DateTime("20060101T051500Z");
        end = new DateTime("20060105T051500Z");
        
        period = new Period(start, end);
        
        vfb = queryProcessor.freeBusyQuery(calendar, period);
        
        verifyPeriods(vfb, FbType.BUSY, "20060103T100000Z/20060103T120000Z,20060104T100000Z/20060104T120000Z");
        verifyPeriods(vfb, FbType.BUSY_TENTATIVE, "20060102T100000Z/20060102T120000Z");
        verifyPeriods(vfb, FbType.BUSY_UNAVAILABLE, "20060105T010000Z/20060105T020000Z");
    }
    
    /**
     * Gets user.
     * @param userDao UserDao.
     * @param username Username.
     * @return The user.
     */
    @SuppressWarnings("unused")
	private User getUser(UserDao userDao, String username) {
        return testHelper.makeDummyUser(username, username);
    }

    /**
     * Generates calendar.
     * @param name The name.
     * @param owner The owner
     * @return The collection item.
     */
    private CollectionItem generateCalendar(String name, User owner) {
        CollectionItem calendar = factory.createCollection();
        calendar.setOwner(owner);
        
        CalendarCollectionStamp ccs =
            factory.createCalendarCollectionStamp(calendar);
        
        calendar.addStamp(ccs);
        return calendar;
    }

    /**
     * Generates events.
     * @param name The name.
     * @param file The file.
     * @param owner The owner.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private NoteItem generateEvent(String name, String file,
            User owner) throws Exception {
        NoteItem event = factory.createNote();
        event.setName(name);
        event.setDisplayName(name);
        event.setOwner(owner);
       
        EventStamp evs = factory.createEventStamp(event);
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(testHelper.getBytes(file)));
       
        return event;
    }
    
    /**
     * Generates free busy.
     * @param name The name. 
     * @param file The file.
     * @param owner The owner.
     * @return Free busy item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private FreeBusyItem generateFreeBusy(String name, String file,
            User owner) throws Exception {
        FreeBusyItem fb = factory.createFreeBusy();
        fb.setName(name);
        fb.setDisplayName(name);
        fb.setOwner(owner);
        fb.setFreeBusyCalendar(CalendarUtils.parseCalendar(testHelper.getBytes(file)));
        
        return fb;
    }
    
    /**
     * verify periods.
     * @param vfb VFreeBusy.
     * @param fbtype FbType.
     * @param periods The periods.
     */
    private void verifyPeriods(VFreeBusy vfb, FbType fbtype, String periods) {
        PropertyList<FreeBusy> props = vfb.getProperties(Property.FREEBUSY);
        FreeBusy fb = null;
        
        for(FreeBusy next : props) {            
            FbType type = (FbType) next.getParameter(Parameter.FBTYPE);
            if(type==null && fbtype==null) {
                fb = next;
            }
            else if(type != null && type.equals(fbtype)) {
                fb = next;
            }
        }
        
        if (fb == null) {
            Assert.fail("periods " + periods + " not in " + vfb.toString());
        }
        Assert.assertEquals(periods, fb.getPeriods().toString());
    }
}