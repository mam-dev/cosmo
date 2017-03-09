/*
 * CalendarClientsAdapterTest.java Nov 14, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;


public class CalendarClientsAdapterTest {
    
    /**
     * MKCALENDAR in icalIOS7 provides a timezone without prodid
     * @throws IOException
     * @throws ParserException
     * @throws ValidationException
     */
    @Test
    public void icalIOS7_missingTimezoneProductIdIsAdded() throws IOException, ParserException, ValidationException{
        Calendar calendar = new CalendarBuilder().build(new ByteArrayInputStream(geticalIOS7Calendar()));
        CalendarClientsAdapter.adaptTimezoneCalendarComponent(calendar);
        calendar.validate(true);//must not throw exceptions
    }

    private byte[] geticalIOS7Calendar() {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("CALSCALE:GREGORIAN\n");
        sb.append("BEGIN:VTIMEZONE\n");
        sb.append("TZID:Europe/Bucharest\n");
        sb.append("BEGIN:DAYLIGHT\n");
        sb.append("TZOFFSETFROM:+0200\n");
        sb.append("TZNAME:GMT+3\n");
        sb.append("TZOFFSETTO:+0300\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n");
        sb.append("DTSTART:19970330T030000\n");
        sb.append("END:DAYLIGHT\n");
        sb.append("BEGIN:STANDARD\n");
        sb.append("TZOFFSETFROM:+0300\n");
        sb.append("TZNAME:GMT+2\n");
        sb.append("TZOFFSETTO:+0200\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n");
        sb.append("DTSTART:19971026T040000\n");
        sb.append("END:STANDARD\n");
        sb.append("END:VTIMEZONE\n");
        sb.append("END:VCALENDAR\n");
        return sb.toString().getBytes();
    }

}
