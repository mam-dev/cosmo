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
package org.unitedinternet.cosmo.model.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;
import org.unitedinternet.cosmo.model.mock.MockEventExceptionStamp;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Test EventStamp
 */
public class ThisAndFutureHelperTest {
   
    
    protected String baseDir = "src/test/unit/resources/testdata/thisandfuture/";
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    /**
     * Test break floating series.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBreakFloatingSeries() throws Exception {
        Calendar cal1 = getCalendar("thisandfuturetest_floating.ics");
        Calendar cal2 = getCalendar("thisandfuturetest_floating_changed.ics");
        
        NoteItem oldSeries = createEvent("oldmaster", cal1);
        NoteItem newSeries = createEvent("newmaster", cal2);
        
        Date lastRecurrenceId = new DateTime("20070808T081500");
        
        ThisAndFutureHelper helper = new ThisAndFutureHelper();
        
        Set<NoteItem> results = 
            helper.breakRecurringEvent(oldSeries, newSeries, lastRecurrenceId);
        
        EventStamp eventStamp = StampUtils.getEventStamp(oldSeries);
        Recur recur = eventStamp.getRecurrenceRules().get(0);
        
        Assert.assertEquals(new DateTime("20070807T235959Z"), recur.getUntil());
        
        Assert.assertEquals(8, results.size());
        
        assertContains("oldmaster:20070808T081500", results, false);
        assertContains("oldmaster:20070809T081500", results, false);
        assertContains("oldmaster:20070810T081500", results, false);
        assertContains("oldmaster:20070811T081500", results, false);
        assertContains("newmaster:20070808T081500", results, true);
        assertContains("newmaster:20070809T081500", results, true);
        assertContains("newmaster:20070810T081500", results, true);
        assertContains("newmaster:20070811T081500", results, true);
    }
    
    /**
     * Tests break floating series with time shift.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBreakFloatingSeriesWithTimeShift() throws Exception {
        Calendar cal1 = getCalendar("thisandfuturetest_floating.ics");
        Calendar cal2 = getCalendar("thisandfuturetest_floating_changed_timeshift.ics");
        
        NoteItem oldSeries = createEvent("oldmaster", cal1);
        NoteItem newSeries = createEvent("newmaster", cal2);
        
        Date lastRecurrenceId = new DateTime("20070808T081500");
        
        ThisAndFutureHelper helper = new ThisAndFutureHelper();
        
        Set<NoteItem> results = 
            helper.breakRecurringEvent(oldSeries, newSeries, lastRecurrenceId);
        
        EventStamp eventStamp = StampUtils.getEventStamp(oldSeries);
        Recur recur = eventStamp.getRecurrenceRules().get(0);
        
        Assert.assertEquals(new DateTime("20070807T235959Z"), recur.getUntil());
        
        Assert.assertEquals(8, results.size());
        
        assertContains("oldmaster:20070808T081500", results, false);
        assertContains("oldmaster:20070809T081500", results, false);
        assertContains("oldmaster:20070810T081500", results, false);
        assertContains("oldmaster:20070811T081500", results, false);
        assertContains("newmaster:20070808T101500", results, true);
        assertContains("newmaster:20070809T101500", results, true);
        assertContains("newmaster:20070810T101500", results, true);
        assertContains("newmaster:20070811T101500", results, true);
        
        // verify that start date was also changed for mod where
        // recurrenceId==dtstart
        NoteItem mod = getByUid("newmaster:20070811T101500", results);
        Assert.assertNotNull(mod);
        
        EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
        Assert.assertNotNull(ees);
        
        Assert.assertTrue(ees.getStartDate().equals(ees.getRecurrenceId()));
    }
    
    /**
     * Tests break timezone series.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBreakTimeZoneSeries() throws Exception {
        Calendar cal1 = getCalendar("thisandfuturetest_timezone.ics");
        Calendar cal2 = getCalendar("thisandfuturetest_timezone_changed.ics");
        
        NoteItem oldSeries = createEvent("oldmaster", cal1);
        NoteItem newSeries = createEvent("newmaster", cal2);
        
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
       
        Date lastRecurrenceId = new DateTime("20070809T084500", tz);
        
        ThisAndFutureHelper helper = new ThisAndFutureHelper();
        
        Set<NoteItem> results = 
            helper.breakRecurringEvent(oldSeries, newSeries, lastRecurrenceId);
        
        EventStamp eventStamp = StampUtils.getEventStamp(oldSeries);
        Recur recur = eventStamp.getRecurrenceRules().get(0);
        
        Assert.assertEquals(new DateTime("20070808T235959Z", tz), recur.getUntil());
        
        Assert.assertEquals(4, results.size());
        
        assertContains("oldmaster:20070809T134500Z", results, false);
        assertContains("oldmaster:20070816T134500Z", results, false);
       
        assertContains("newmaster:20070809T134500Z", results, true);
        assertContains("newmaster:20070816T134500Z", results, true);
        
    }
    
    /**
     * Tests break all day series.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBreakAllDaySeries() throws Exception {
        Calendar cal1 = getCalendar("thisandfuturetest_allday.ics");
        Calendar cal2 = getCalendar("thisandfuturetest_allday_changed.ics");
        
        NoteItem oldSeries = createEvent("oldmaster", cal1);
        NoteItem newSeries = createEvent("newmaster", cal2);
        
        Date lastRecurrenceId = new Date("20070813");
        
        ThisAndFutureHelper helper = new ThisAndFutureHelper();
        
        Set<NoteItem> results = 
            helper.breakRecurringEvent(oldSeries, newSeries, lastRecurrenceId);
        
        EventStamp eventStamp = StampUtils.getEventStamp(oldSeries);
        Recur recur = eventStamp.getRecurrenceRules().get(0);
        
        Assert.assertEquals(new Date("20070812"), recur.getUntil());
        
        Assert.assertEquals(4, results.size());
        
        assertContains("oldmaster:20070820", results, false);
        assertContains("oldmaster:20070827", results, false);
        
        assertContains("newmaster:20070824", results, true);
        assertContains("newmaster:20070831", results, true);
    }
    
    /**
     * Asserts contains.
     * @param uid The uid.
     * @param notes The notes.
     * @param isActive The boolean for isActive.
     */
    protected void assertContains(String uid, Collection<NoteItem> notes, boolean isActive) {
        for(NoteItem note: notes) {
            if(note.getUid().equals(uid) && note.getIsActive()==isActive) {
                    return;
            }
        }
            
        Assert.fail(uid + " not in collection");
    }
    
    /**
     * Gets by uid.
     * @param uid The uid.
     * @param notes The notes.
     * @return The note item.
     */
    protected NoteItem getByUid(String uid, Collection<NoteItem> notes) {
        for (NoteItem note: notes) {
            if (note.getUid().equals(uid)) {
                    return note;
            }
        }
        
        return null;
    }
    
    /**
     * Gets calendar.
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
    /**
     * Creates event.
     * @param uid The uid.
     * @param calendar The calendar.
     * @return The note item.
     */
    protected NoteItem createEvent(String uid, Calendar calendar) {
        NoteItem master = new MockNoteItem();
        master.setUid(uid);
        EventStamp es = new MockEventStamp(master);
        master.addStamp(es);
        
        ComponentList<VEvent> vevents = calendar.getComponents().getComponents(Component.VEVENT);
        List<VEvent> exceptions = new ArrayList<VEvent>();        
        // get list of exceptions (VEVENT with RECURRENCEID)        
        for (VEvent event : vevents) {            
            if (event.getRecurrenceId() != null) {
                exceptions.add(event);
                NoteItem mod = new MockNoteItem();
                mod.setUid(new ModificationUidImpl(master,event.getRecurrenceId().getDate()).toString());
                mod.setModifies(master);
                master.addModification(mod);
                EventExceptionStamp ees = new MockEventExceptionStamp(mod);
                mod.addStamp(ees);
                ees.createCalendar();
                ees.setRecurrenceId(event.getRecurrenceId().getDate());
                ees.setStartDate(event.getStartDate().getDate());
                ees.setAnyTime(null);
            } 
        }
        
        
        for (VEvent ex: exceptions) {
            calendar.getComponents().remove(ex);
        }
        
        es.setEventCalendar(calendar);
        
        return master;
    }
    
    
}
