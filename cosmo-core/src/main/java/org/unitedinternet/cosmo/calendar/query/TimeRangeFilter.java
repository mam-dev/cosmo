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
package org.unitedinternet.cosmo.calendar.query;

import java.text.ParseException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Represents the CALDAV:time-range element. From sec 9.8:
 * 
 * Name: time-range
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies a time range to limit the set of calendar components
 * returned by the server.
 * 
 * Definition:
 * 
 * <!ELEMENT time-range EMPTY>
 * 
 * <!ATTLIST time-range start CDATA #IMPLIED end CDATA #IMPLIED> 
 * start value: an iCalendar "date with UTC time" 
 * end value: an iCalendar "date with UTC time"
 */
public class TimeRangeFilter implements CaldavConstants {
    
    private static final Long TWO_YEARS_MILLIS = new Long(63072000000L);
    
    private Period period = null;

    private VTimeZone timezone = null;

    private DateTime dstart, dend;

    /**
     * Constructor.
     * @param period The period.
     */
    public TimeRangeFilter(Period period) {
        setPeriod(period);
    }
    
    /**
     * Construct a TimeRangeFilter object from a DOM Element
     * @param element The DOM Element.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public TimeRangeFilter(Element element, VTimeZone timezone) throws ParseException {        
        // Get start (must be present)
        String start =
            DomUtil.getAttribute(element, ATTR_CALDAV_START, null);
        if (start == null) {
            throw new ParseException("CALDAV:comp-filter time-range requires a start time", -1);
        }
        
        DateTime trstart = new DateTime(start);
        if (! trstart.isUtc()) {
            throw new ParseException("CALDAV:param-filter timerange start must be UTC", -1);
        }

        // Get end (must be present)
        String end =
            DomUtil.getAttribute(element, ATTR_CALDAV_END, null);        
        DateTime trend = end != null ? new DateTime(end) : getDefaultEndDate(trstart);
        
        if (! trend.isUtc()) {
            throw new ParseException("CALDAV:param-filter timerange end must be UTC", -1);
        }

        setPeriod(new Period(trstart, trend));
        setTimezone(timezone);
    }

    /**
     * Calculates a default end date relative to specified start date.
     * 
     * @param startDate
     * @return
     */
    private DateTime getDefaultEndDate(DateTime startDate) {
        DateTime endDate = new DateTime(startDate.getTime() + TWO_YEARS_MILLIS);
        endDate.setUtc(true);
        return endDate;
    }
    /**
     * 
     * @param dtStart The timerange start.
     * @param dtEnd The timerange end.
     */
    public TimeRangeFilter(DateTime dtStart, DateTime dtEnd) {
        if (!dtStart.isUtc()) {
            throw new IllegalArgumentException("timerange start must be UTC");
        }

        if (!dtEnd.isUtc()) {
            throw new IllegalArgumentException("timerange start must be UTC");
        }

        Period period = new Period(dtStart, dtEnd);
        setPeriod(period);
    }

    public TimeRangeFilter(java.util.Date start, java.util.Date end) {
        this(utc(start), utc(end));
    }

    private static DateTime utc(java.util.Date date) {
        DateTime dt = new DateTime(date);
        dt.setUtc(true);
        return dt;
    }

    public TimeRangeFilter(String start, String end)
        throws ParseException {
        this(new DateTime(start), new DateTime(end));
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
        // Get fixed start/end time
        dstart = period.getStart();
        dend = period.getEnd();
    }

    public String getUTCStart() {
        return dstart.toString();
    }

    public String getUTCEnd() {
        return dend.toString();
    }

    public VTimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(VTimeZone timezone) {
        this.timezone = timezone;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("dstart", dstart).
            append("dend", dend).
            toString();
    }
}
