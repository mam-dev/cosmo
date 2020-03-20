/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.filter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.model.BaseEventStamp;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;
import net.fortuna.ical4j.util.TimeZones;

/**
 * Adds EventStamp specific criteria to StampFilter.
 * Matches only EventStamp instances.
 * <p>
 * The following filter properties are supported:
 * <ul>
 * <li>PROPERTY_INCLUDE_MASTER_ITEMS - If set to "false", and there
 *     is a timeRange specified in the filter, then master items
 *     that match the timeRange will not be included in the results.
 *     This is used to only return occurrences and modifications of
 *     a recurring event, but not the master event item.
 * </ul>
 */
public class EventStampFilter extends StampFilter {
    
    public static final String PROPERTY_INCLUDE_MASTER_ITEMS = 
        "cosmo.filter.eventStamp.timeRange.includeMasterItems";
    public static final String PROPERTY_DO_TIMERANGE_SECOND_PASS = 
        "cosmo.filter.eventStamp.timeRange.doSecondPass";
    private static final Logger LOG = LoggerFactory.getLogger(EventStampFilter.class);
    private Period period = null;
    private Boolean isRecurring = null;
    private DateTime dstart;
    private DateTime dend;
    private Date fstart;
    private Date fend;
    private TimeZone timezone = null;
    private boolean expandRecurringEvents = false;
    
    public Period getPeriod() {
        return period;
    }

    /**
     * Matches events that occur in a given time-range.
     * @param period time-range
     */
    public void setPeriod(Period period) {
        this.period = period;
        // Get fixed start/end time
        dstart = period.getStart();
        dend = period.getEnd();
        
        // set timezone on floating times
        updateFloatingTimes();
    }
    
    /**
     * Matches events that occur in a given time-range.
     * @param start range start
     * @param end range end
     */
    public void setTimeRange(Date start, Date end) {
        dstart = utc(start);
        dend = utc(end);
        
        fstart = start;
        fend = end;
        
        updateFloatingTimes();
        period = new Period(dstart, dend);
    }

    public String getUTCStart() {
        return dstart.toString();
    }

    public String getUTCStart(String dateTimeFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        sdf.setTimeZone(TimeZones.getUtcTimeZone());
        return sdf.format(dstart);
    }
    
    public String getUTCEnd() {
        return dend.toString();
    }
    
    public String getUTCEnd(String dateTimeFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        sdf.setTimeZone(TimeZones.getUtcTimeZone());
        return sdf.format(dend);
    }

    public String getFloatStart(String dateTimeFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        return sdf.format(fstart);
    }
    
    public String getFloatStart() {
        return fstart.toString();
    }
    

    public String getFloatEnd() {
        return fend.toString();
    }
    
    public String getFloatEnd(String dateTimeFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        return sdf.format(fend);
    }
    
    public boolean isExpandRecurringEvents() {
        return expandRecurringEvents;
    }

    /**
     * If a time-range query is specified, the filter can be configured
     * to expand recurring events within the time range.  This results
     * in a <code>NoteOccurrence</code> item returned for each occurrence
     * of a matching recurring event during the specified time range.
     * @param expandRecurringEvents if true and a time-range is specified,
     *        return a NoteOccurrence for each occurence of a recurring event
     *        during the time-range.
     */
    public void setExpandRecurringEvents(boolean expandRecurringEvents) {
        this.expandRecurringEvents = expandRecurringEvents;
    }

    public EventStampFilter() {
        setStampClass(BaseEventStamp.class);
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Used in time-range filtering.  If set, and there is a time-range
     * to filter on, the timezone will be used in comparing floating
     * times.  If null, the server time-zone will be used in determining
     * if floating time values match.
     * @param timezone timezone to use in comparing floating times
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
        updateFloatingTimes();
    }
    
    

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    /**
     * Match only recurring events.
     * @param isRecurring if set, return recurring events only, or only
     *                    non recurring events
     */
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    private void updateFloatingTimes() {
        if(dstart!=null) {
            Value v = Value.DATE_TIME;
            fstart = fstart == null ? (DateTime) Dates.getInstance(dstart, v) : fstart;
            if(fstart instanceof DateTime){
                ((DateTime)fstart).setUtc(false);
                // if the timezone is null then default system timezone is used
                ((DateTime)fstart).setTimeZone((timezone != null) ? timezone : null);
            }
        }
        if(dend!=null) {
            Value v = Value.DATE_TIME;
            fend = fend == null ? (DateTime) Dates.getInstance(dend, v) : fend;
            if(fend instanceof DateTime){
                ((DateTime)fend).setUtc(false);
                // if the timezone is null then default system timezone is used
                ((DateTime)fend).setTimeZone((timezone != null) ? timezone : null);
            }
        }
        
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        TimeZone tz = getTimezone();
        if(tz != null){
            //for some reason ical4j's TimeZone for a valid ID (i.e. Europe/Bucharest) has raw offset 0
            df.setTimeZone(java.util.TimeZone.getTimeZone(tz.getID()));
        }
        
        if(fstart != null && fstart.getClass() == net.fortuna.ical4j.model.Date.class){
            try {
                dstart = new DateTime(df.parse(fstart.toString()).getTime());
                dstart.setUtc(true);
            } catch (ParseException e) {
                LOG.error("Error occured while parsing fstart [" + fstart.toString() + "]", e);
            }
        }
        
        if(fend != null && fend.getClass() == net.fortuna.ical4j.model.Date.class){
            try {
                dend = new DateTime(df.parse(fend.toString()).getTime());
                dend.setUtc(true);
            } catch (ParseException e) {
                LOG.error("Error occured while parsing fend [" + fend.toString() + "]", e);
            }
        }
    }
    
    private static DateTime utc(Date date) {
        DateTime dt = new DateTime(date);
        dt.setUtc(true);
        return dt;
    }
 
}
