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
package org.unitedinternet.cosmo.model;

import java.io.FileInputStream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;

/**
 * Test EventStamp
 */
public class EventStampTest {
   
    protected String baseDir = "src/test/unit/resources/testdata/";
    @SuppressWarnings("unused")
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    /**
     * Tests ex dates.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExDates() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        EventStamp eventStamp = new MockEventStamp(master);
        
        eventStamp.setEventCalendar(getCalendar("recurring_with_exdates.ics"));
        
        DateList exdates = eventStamp.getExceptionDates();
        
        Assert.assertNotNull(exdates);
        Assert.assertTrue(2==exdates.size());
        Assert.assertNotNull(exdates.getTimeZone());
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
}
