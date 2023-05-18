/*
 * Copyright 2008 Open Source Applications Foundation
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

import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;
import org.unitedinternet.cosmo.model.StampUtils;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;

/**
 * Utility functions for NoteItems
 */
public class NoteUtils {

    public static Date getStartDate(NoteItem note) {
        // start date of occurence is occurence date
        if (note instanceof NoteOccurrence) {
            return ((NoteOccurrence) note).getOccurrenceDate();
        }

        // otherwise get start date from event stamp
        BaseEventStamp es = StampUtils.getBaseEventStamp(note);
        if (es == null) {
            return null;
        }

        return es.getStartDate();
    }

    public static DateTime getNormalizedDate(Date date, TimeZone tz) {
        return ICalendarUtils.pinFloatingTime(date, tz);
    }

    public static boolean isEvent(NoteItem note) {
        return StampUtils.getBaseEventStamp(note) != null;
    }

    public static boolean isTask(NoteItem note) {
        return StampUtils.getTaskStamp(note) != null;
    }
}
