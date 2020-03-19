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
package org.unitedinternet.cosmo.icalendar;

import java.io.IOException;
import java.io.OutputStream;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * A class that writes Cosmo calendar model objects to output streams formatted according to the iCalendar specification
 * (RFC 2445).
 */
public class ICalendarOutputter {

    /**
     * Writes an iCalendar string representing the calendar items contained within the given calendar collection to the
     * given output stream.
     *
     * Since the calendar content stored with each calendar items is parsed and validated when the item is created,
     * these errors should not reoccur when the calendar is being outputted.
     *
     * @param collection the <code>CollectionItem</code> to format
     *
     * @throws IllegalArgumentException if the collection is not stamped as a calendar collection
     * @throws IOException
     */
    public static void output(Calendar calendar, OutputStream out) throws IOException {

        CalendarOutputter outputter = new CalendarOutputter();
        outputter.setValidating(false);
        try {
            outputter.output(calendar, out);
        } catch (ValidationException e) {
            throw new IllegalStateException("unable to validate collection calendar", e);
        }
    }
}
