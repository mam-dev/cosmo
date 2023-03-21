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
package org.unitedinternet.cosmo.calendar.data;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Summary;

/**
 * Test expand output filter
 */
public class ExpandRecurringEventsTest {
    protected String baseDir = "src/test/unit/resources/testdata/";
    
    /**
     * Tests expand event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExpandEvent() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_recurr_test1.ics");
        Calendar calendar = cb.build(fis);
        
        assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringBuilder buffer = new StringBuilder();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());
        
        Calendar filterCal = cb.build(sr);
        
        ComponentList<VEvent> comps = filterCal.getComponents().getComponents("VEVENT");
        
        // Should expand to 3 event components
        assertEquals(3, comps.size());
                
        Iterator<VEvent> it = comps.iterator();
        VEvent event = it.next();
        
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
        assertEquals("20060102T190000Z", event.getRecurrenceId().getDate().toString());
        
        event = it.next();
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060103T190000Z", event.getStartDate().getDate().toString());
        assertEquals("20060103T190000Z", event.getRecurrenceId().getDate().toString());
        
        event = it.next();
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060104T190000Z", event.getStartDate().getDate().toString());
        assertEquals("20060104T190000Z", event.getRecurrenceId().getDate().toString());
        
        verifyExpandedCalendar(filterCal);
    }
    
    /**
     * Tests expand event with overrides.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExpandEventWithOverrides() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_recurr_test2.ics");
        Calendar calendar = cb.build(fis);
        
        ComponentList<VEvent> comps = calendar.getComponents().getComponents("VEVENT");
        
        assertEquals(5, comps.size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringBuilder buffer = new StringBuilder();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());
        
        Calendar filterCal = cb.build(sr);
        
        comps = filterCal.getComponents().getComponents("VEVENT");
        
        // Should expand to 3 event components
        assertEquals(3, comps.size());
                
        Iterator<VEvent> it = comps.iterator();
        VEvent event = it.next();
        
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
        assertEquals("20060102T190000Z", event.getRecurrenceId().getDate().toString());
        
        event = it.next();
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060103T190000Z", event.getStartDate().getDate().toString());
        assertEquals("20060103T190000Z", event.getRecurrenceId().getDate().toString());
        
        event = it.next();
        assertEquals("event 6 changed", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060104T210000Z", event.getStartDate().getDate().toString());
        assertEquals("20060104T190000Z", event.getRecurrenceId().getDate().toString());
        
        verifyExpandedCalendar(filterCal);
    }
    
    /**
     * Removed test expand with non recurring event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void removedTestExpandNonRecurringEvent() throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + "expand_nonrecurr_test3.ics");
        Calendar calendar = cb.build(fis);
        
        assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
        TimeZone tz = new TimeZone(vtz);
        OutputFilter filter = new OutputFilter("test");
        DateTime start = new DateTime("20060102T140000", tz);
        DateTime end = new DateTime("20060105T140000", tz);
        start.setUtc(true);
        end.setUtc(true);
        
        Period period = new Period(start, end);
        filter.setExpand(period);
        filter.setAllSubComponents();
        filter.setAllProperties();
        
        StringBuilder buffer = new StringBuilder();
        filter.filter(calendar, buffer);
        StringReader sr = new StringReader(buffer.toString());
        
        Calendar filterCal = cb.build(sr);
        
        ComponentList<VEvent> comps = filterCal.getComponents().getComponents("VEVENT");
        
        // Should be the same component
        assertEquals(1, comps.size());
                
        Iterator<VEvent> it = comps.iterator();
        VEvent event = it.next();
        
        assertEquals("event 6", ((Summary) event.getProperties().getProperty(Property.SUMMARY)).getValue());
        assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
        assertNull(event.getRecurrenceId());
        
        verifyExpandedCalendar(filterCal);
    }
    
    /**
     * Verify expand calendar.
     * @param calendar The calendar.
     */
    private void verifyExpandedCalendar(Calendar calendar) {
        // timezone should be stripped
        assertNull(calendar.getComponents().getComponent("VTIMEZONE"));
        
        ComponentList<VEvent> comps = calendar.getComponents().getComponents("VEVENT");
        
        for(VEvent event : comps) {
            DateTime dt = (DateTime) event.getStartDate().getDate();
            
            // verify start dates are UTC
            assertNull(event.getStartDate().getParameters().getParameter(Parameter.TZID));
            assertTrue(dt.isUtc());
            
            // verify no recurrence rules
            assertNull(event.getProperties().getProperty(Property.RRULE));
        }
    }
    
}
