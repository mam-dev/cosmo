/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

package org.unitedinternet.cosmo.dav.caldav;

import java.io.IOException;
import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.validate.ValidationException;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.unitedinternet.cosmo.calendar.util.CalendarBuilderDispenser;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;

/**
 * Helper class for extracting a <code>VTimeZone</code> from an iCalendar
 * object.
 */
public class TimeZoneExtractor {

    /**
     * Creates an instance of <code>VTimeZone</code> from a
     * DAV property value.
     *
     * The property value must be an iCalendar string. The further
     * requirements for a valid time zone are as per 
     * {@link #extractTimeZone(String)}.
     *
     * @param prop the <code>WebDavProperty</code> containing the
     * timezone
     * @return a <code>Calendar</code> containing a single
     * <code>VTimeZone</code> representing the extracted  timezone, or
     * <code>null</code> if the property or its value is <code>null</code>
     * @throws CosmoDavException if the property value cannot be parsed or is not
     * a valid iCalendar object containing a single VTIMEZONE component
     */
    public static Calendar extract(WebDavProperty prop)
        throws CosmoDavException {
        if (prop == null) {
            return null;
        }
        if (prop.getValue() == null) {
            return null;
        }

        return extractInCalendar(prop.getValueText());
    }

    /**
     * Creates an instance of <code>VTimeZone</code> from an
     * iCalendar string.
     *
     * The iCalendar string must include an enclosing VCALENDAR object
     * and exactly one enclosed VTIMEZONE component. All components,
     * properties and parameters are validated according to RFC 2445.
     *
     * @param ical the iCalendar string to parse
     * @return the  <code>VTimeZone</code> representing the extracted
     * timezone, or <code>null</code> if the iCalendar string is
     *  <code>null</code>
     * @throws CosmoDavException if the iCalendar string cannot be parsed or is
     * not a valid iCalendar object containing a single VTIMEZONE component
     */
    public static VTimeZone extract(String ical)
        throws CosmoDavException {
        Calendar calendar = extractInCalendar(ical);
        if (calendar == null) {
            return null;
        }
        return (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
    }

    /**
     * Creates an instance of <code>VTimeZone</code> from an
     * iCalendar string.
     *
     * The iCalendar string must include an enclosing VCALENDAR object
     * and exactly one enclosed VTIMEZONE component. All components,
     * properties and parameters are validated according to RFC 2445.
     *
     * @param ical the iCalendar string to parse
     * @return a <code>Calendar</code> containing a single
     *  <code>VTimeZone</code> representing the extracted timezone, or 
     * code>null</code> if the iCalendar string is <code>null</code>
     * @throws CosmoDavException if the iCalendar string cannot be parsed or is
     * not a valid iCalendar object containing a single VTIMEZONE component
     */
    public static Calendar extractInCalendar(String ical)
        throws CosmoDavException {
        if (ical == null) {
            return null;
        }

        Calendar calendar = null;
        try {
            CalendarBuilder builder =
                CalendarBuilderDispenser.getCalendarBuilder();
            calendar = builder.build(new StringReader(ical));
            CalendarClientsAdapter.adaptTimezoneCalendarComponent(calendar);
            calendar.validate(true);
        } catch (IOException e) {
            throw new CosmoDavException(e);
        } catch (ParserException e) {
            throw new InvalidCalendarDataException("Calendar object not parseable: " + e.getMessage());
        } catch (ValidationException e) {
            throw new InvalidCalendarDataException("Invalid calendar object: " + e.getMessage());
        }

        if (calendar.getComponents().size() > 1) {
            throw new InvalidCalendarDataException("Calendar object contains more than one VTIMEZONE component");
        }

        VTimeZone vtz = (VTimeZone)
            calendar.getComponent(Component.VTIMEZONE);
        if (vtz == null) {
            throw new InvalidCalendarDataException("Calendar object must contain a VTIMEZONE component");
        }

        return calendar;
    }
}
