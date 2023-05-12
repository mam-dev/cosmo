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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.DateList;

/**
 * Test EventStamp
 */
public class EventStampTest {

    /**
     * Tests ex dates.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExDates() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        EventStamp eventStamp = new MockEventStamp(master);

        eventStamp.setEventCalendar(
                new CalendarBuilder().build(this.getClass().getResourceAsStream("recurring_with_exdates.ics")));

        DateList exdates = eventStamp.getExceptionDates();

        assertNotNull(exdates);
        assertTrue(2 == exdates.size());
        assertNotNull(exdates.getTimeZone());
    }

}
