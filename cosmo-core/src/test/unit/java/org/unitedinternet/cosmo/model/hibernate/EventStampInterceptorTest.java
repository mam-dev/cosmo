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
package org.unitedinternet.cosmo.model.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.unitedinternet.cosmo.calendar.ICalendarUtils.createBaseCalendar;

import org.junit.jupiter.api.Test;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;

/**
 * Test EventStampHandler
 */
public class EventStampInterceptorTest {
   
    EventStampInterceptor interceptor = new EventStampInterceptor();
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    
    /**
     * Tests event stamp handler.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampHandler() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        VEvent vEvent = new VEvent(); 
        Calendar calendar = createBaseCalendar(vEvent);
        vEvent.getProperties().add(new DtStart(new DateTime("20070212T074500")));
        vEvent.getProperties().add(new DtEnd(new DateTime("20070212T094500")));
        eventStamp.setEventCalendar(calendar);
        master.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventStamp);
        
        assertEquals("20070212T074500", index.getStartDate());
        assertEquals("20070212T094500", index.getEndDate());
        assertTrue(index.getIsFloating().booleanValue());
        
        TimeZone ctz = registry.getTimeZone("America/Chicago");
        DateTime start = new DateTime("20070212T074500", ctz);
        vEvent.getStartDate().setDate(start);
        DateTime end = new DateTime("20070212T094500", ctz);
        vEvent.getEndDate().setDate(end);
        
        vEvent.getProperties().add(new RRule("FREQ=DAILY;"));
        eventStamp.setEventCalendar(calendar);
        
        
        index = interceptor.calculateEventStampIndexes(eventStamp);
        
        assertEquals("20070212T134500Z", index.getStartDate());
        assertEquals(HibEventStamp.TIME_INFINITY, index.getEndDate());
        assertFalse(index.getIsFloating().booleanValue());
    }
    
    /**
     * Tests event stamp handler all day.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampHandlerAllDay() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        VEvent vEvent = new VEvent(); 
        Calendar calendar = createBaseCalendar(vEvent);
        vEvent.getProperties().add(new DtStart(new Date("20070212")));
        vEvent.getProperties().add(new DtEnd(new Date("20070213")));
        eventStamp.setEventCalendar(calendar);
        master.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventStamp);
        
        assertEquals("20070212", index.getStartDate());
        assertEquals("20070213", index.getEndDate());
        assertTrue(index.getIsFloating().booleanValue());
      
        vEvent.getProperties().add(new RRule("FREQ=DAILY;"));
        eventStamp.setEventCalendar(calendar);
        
        index = interceptor.calculateEventStampIndexes(eventStamp);
        
        assertEquals("20070212", index.getStartDate());
        assertEquals(HibEventStamp.TIME_INFINITY, index.getEndDate());
        assertTrue(index.getIsFloating().booleanValue());
    }
    
    /**
     * Tests events stamp handler mods.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampHandlerMods() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        VEvent vEvent = new VEvent(); 
        Calendar calendar = createBaseCalendar(vEvent);
        vEvent.getProperties().add(new DtStart(new DateTime("20070212T074500")));
        vEvent.getProperties().add(new DtEnd(new DateTime("20070212T094500")));
        eventStamp.setEventCalendar(calendar);
        master.addStamp(eventStamp);
        
        HibNoteItem mod = new HibNoteItem();
        mod.setModifies(master);
        HibEventExceptionStamp eventExceptionStamp = new HibEventExceptionStamp(mod);
        
        VEvent vEventCopy = new VEvent(); 
        Calendar calendarCopy = createBaseCalendar(vEventCopy);
        vEventCopy.getProperties().add(new DtStart(new DateTime("20070213T084500")));
        eventExceptionStamp.setEventCalendar(calendarCopy);
        
        mod.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventExceptionStamp);
        
        assertEquals("20070213T084500", index.getStartDate());
        assertEquals("20070213T104500", index.getEndDate());
        assertTrue(index.getIsFloating().booleanValue());
        
        // handle case where master isn't an event anymore
        master.removeStamp(eventStamp);
        index = interceptor.calculateEventStampIndexes(eventExceptionStamp);
        
        assertEquals("20070213T084500", index.getStartDate());
        assertEquals("20070213T084500", index.getEndDate());
    }
    
}
