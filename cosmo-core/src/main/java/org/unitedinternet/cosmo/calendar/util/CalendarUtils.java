/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.calendar.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.validate.ValidationException;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

/**
 * Utility methods for working with icalendar data.
 */
public class CalendarUtils implements ICalendarConstants {

    private static String[] SUPPORTED_COMPONENT_TYPES = { Component.VEVENT, Component.VTODO, Component.VJOURNAL,
            Component.VFREEBUSY, COMPONENT_VAVAILABLITY };

    private static String[] SUPPORTED_COLLATIONS = { "i;ascii-casemap", "i;octet" };

    /**
     * Convert Calendar object to String.
     * 
     * @param calendar
     * @return string representation of calendar
     * @throws ValidationException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static String outputCalendar(Calendar calendar) throws ValidationException, IOException {
	if (calendar == null) {
	    return null;
	}
	CalendarOutputter outputter = new CalendarOutputter();
	StringWriter sw = new StringWriter();
	outputter.output(calendar, sw);
	return sw.toString();
    }

    /**
     * Parse icalendar string into Calendar object.
     * 
     * @param calendar
     *            icalendar string
     * @return Calendar object
     * @throws ParserException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static Calendar parseCalendar(String calendar) throws ParserException, IOException {
	if (calendar == null) {
	    return null;
	}
	CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
	clearTZRegistry(builder);

	StringReader sr = new StringReader(calendar);
	return conformToRfc5545(builder.build(sr));
    }

    /**
     * Parse icalendar string into calendar component
     * 
     * @param calendar
     *            icalendar string
     * @return Component object
     * @throws ParserException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static Component parseComponent(String component) throws ParserException, IOException {
	if (component == null) {
	    return null;
	}
	/*
	 * Don't use dispenser as this method may be called from within a build() as in the case of the custom timezone
	 * registry parsing a timezone
	 */
	CalendarBuilder builder = new CalendarBuilder();
	StringReader sr = new StringReader("BEGIN:VCALENDAR\n" + component + "END:VCALENDAR");

	return (Component) conformToRfc5545(builder.build(sr)).getComponents().get(0);
    }

    /**
     * Parse icalendar data from Reader into Calendar object.
     * 
     * @param reader
     *            icalendar data reader
     * @return Calendar object
     * @throws ParserException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static Calendar parseCalendar(Reader reader) throws ParserException, IOException {
	if (reader == null) {
	    return null;
	}
	CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
	clearTZRegistry(builder);
	return conformToRfc5545(builder.build(reader));
    }

    /**
     * Parse icalendar data from byte[] into Calendar object.
     * 
     * @param content
     *            icalendar data
     * @return Calendar object
     * @throws ParserException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static Calendar parseCalendar(byte[] content) throws ParserException, IOException {
	CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
	clearTZRegistry(builder);
	return conformToRfc5545(builder.build(new ByteArrayInputStream(content)));
    }

    /**
     * Parse icalendar data from InputStream
     * 
     * @param is
     *            icalendar data inputstream
     * @return Calendar object
     * @throws ParserException
     *             - if something is wrong this exception is thrown.
     * @throws IOException
     *             - if something is wrong this exception is thrown.
     */
    public static Calendar parseCalendar(InputStream is) throws ParserException, IOException {
	CalendarBuilder builder = CalendarBuilderDispenser.getCalendarBuilder();
	clearTZRegistry(builder);
	return conformToRfc5545(builder.build(is));
    }

    private static Calendar conformToRfc5545(Calendar calendar) throws IOException {
	try {
	    calendar.conformToRfc5545();
	    return calendar;
	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	    throw new IOException(e);
	}
    }

    public static Calendar copyCalendar(Calendar calendar) {
	if (calendar == null) {
	    return null;
	}
	try {
	    return new Calendar(calendar);
	} catch (Exception e) {
	    throw new CosmoException("error copying calendar: " + calendar, e);
	}
    }

    public static Component copyComponent(Component comp) {
	try {
	    return comp.copy();
	} catch (Exception e) {
	    throw new CosmoException("error copying component: " + comp, e);
	}
    }

    public static boolean isSupportedComponent(String type) {
	for (String s : SUPPORTED_COMPONENT_TYPES) {
	    if (s.equalsIgnoreCase(type)) {
		return true;
	    }
	}
	return false;
    }

    public static boolean isSupportedCollation(String collation) {
	for (String s : SUPPORTED_COLLATIONS) {
	    if (s.equalsIgnoreCase(collation)) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasMultipleComponentTypes(Calendar calendar) {
	String found = null;
	for (Object component : calendar.getComponents()) {
	    if (component instanceof VTimeZone) {
		continue;
	    }
	    if (found == null) {
		found = ((CalendarComponent) component).getName();
		continue;
	    }
	    if (!found.equals(((CalendarComponent) component).getName())) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasSupportedComponent(Calendar calendar) {
	for (Object component : calendar.getComponents()) {
	    if (isSupportedComponent(((CalendarComponent) component).getName())) {
		return true;
	    }
	}
	return false;
    }

    private static void clearTZRegistry(CalendarBuilder cb) {
	// Clear timezone registry if present
	TimeZoneRegistry tzr = cb.getRegistry();
	if (tzr != null) {
	    tzr.clear();
	}
    }
}
