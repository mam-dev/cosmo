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
package org.unitedinternet.cosmo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides utility methods for working with dates and times.
 */
public class DateUtil {
    private static final String RFC3339_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

    /**
     * Parses a datetime+timezone string in one of the following
     * formats, returning a calendar in the given timezone.
     * 
     * 2002-10-10T00:00:00+05:00
     * 2002-10-09T19:00:00Z
     * 2002-10-10T00:00:00GMT+05:00
     * 
     * @param date string
     */
    public static Calendar parseRfc3339Calendar(String value) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(RFC3339_DATE_FORMAT);
        TimeZone timezone = null;
        Date date = null;
        if(value.charAt(value.length()-1)=='Z') {
            value = value.replace("Z", "GMT-00:00");
            timezone = TimeZone.getTimeZone("GMT-00:00");
            date = sdf.parse(value);
        }
        else if(value.indexOf("GMT")==-1 && 
                (value.charAt(value.length()-6) == '+' ||
                 value.charAt(value.length()-6) == '-')) {
            String tzId = "GMT" + value.substring(value.length()-6);
            value = value.substring(0, value.length()-6) + tzId;
            timezone = TimeZone.getTimeZone(tzId);
            date = sdf.parse(value);
        } else {
            String tzId = value.substring(value.length()-9);
            timezone = TimeZone.getTimeZone(tzId);
            date = sdf.parse(value);
        }
            
        GregorianCalendar cal = new GregorianCalendar(timezone);
        cal.setTime(date);

        return cal;
    }

    /**
     * Parses a datetime+timezone string in one of the following
     * formats, returning a UTC date:
     * 
     * 2002-10-10T00:00:00+05:00
     * 2002-10-10T00:00:00GMT+05:00
     * 
     * @param date string
     * @throws ParseException
     */
    public static Date parseRfc3339Date(String date) throws ParseException {
        return parseDate(date, RFC3339_DATE_FORMAT);
    }
    
    /**
     * Parses a datetime+timezone string in the given format,
     * returning a UTC date.
     * 
     * @param date string
     * @throws ParseException
     */
    public static Date parseDate(String date, String format)
            throws ParseException {
        return parseDate(date, format, null);
    }

    /**
     * Parses a datetime+timezone string in the given format and
     * timezone, returning a UTC date.
     * 
     * @param date string
     * @throws ParseException
     */
    public static Date parseDate(String date, String format, TimeZone tz)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        if (tz != null) {
            formatter.setTimeZone(tz);
        }
        return formatter.parse(date);
    }

    /** */
    public static String formatRfc3339Calendar(Calendar cal) {
        return formatRfc3339Date(cal.getTime(), cal.getTimeZone());
    }

    /** */
    public static String formatRfc3339Date(Date date) {
        return formatRfc3339Date(date, null);
    }

    /** */
    public static String formatRfc3339Date(Date date,
                                           TimeZone tz) {
        String raw = formatDate(RFC3339_DATE_FORMAT, date, tz);
        return raw.replaceFirst("GMT", "");
    }

    /** */
    public static String formatDate(String pattern,
                                    Date date) {
        return formatDate(pattern, date, null);
    }

    /** */
    public static String formatDate(String pattern, Date date, TimeZone tz) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        if (tz != null) {
            formatter.setTimeZone(tz);
        }
        return formatter.format(date);
    }
}
