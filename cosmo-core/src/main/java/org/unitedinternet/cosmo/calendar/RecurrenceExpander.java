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
package org.unitedinternet.cosmo.calendar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.util.Dates;

/**
 * Utility class that contains apis that that involve
 * expanding recurring components.
 */
public class RecurrenceExpander {
    private static final Logger LOG = LoggerFactory.getLogger(RecurrenceExpander.class);
    private static Date maxExpandDate = null;
   
    static {
        // Expand out to 2030 for those recurrence rules
        // that have an end.  Recurring events with no end
        // will be indexed as infinite.
        java.util.Calendar c = java.util.Calendar.getInstance();
        
        c.set(java.util.Calendar.YEAR, 2030);
        c.set(java.util.Calendar.MONTH, 0);
        c.set(java.util.Calendar.DAY_OF_MONTH, 1);
        
        maxExpandDate = new Date(c.getTime());
    }
    
    /**
     * Constructor.
     */
    public RecurrenceExpander() {
        super();
    }
    
    /**
     * Return start and end Date that represent the start of the first 
     * occurrence of a recurring component and the end of the last
     * occurence.  If the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     * @param calendar Calendar containing master and modification components
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Calendar calendar) {
        try{
            ComponentList<VEvent> vevents = calendar.getComponents().getComponents(Component.VEVENT);
            
            List<Component> exceptions = new ArrayList<Component>();
            Component masterComp = null;
            
            // get list of exceptions (VEVENT with RECURRENCEID)
            for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
                VEvent event = i.next();
                if (event.getRecurrenceId() != null) {
                    exceptions.add(event);
                }
                else {
                    masterComp = event;
                }
                
            }
            
            return calculateRecurrenceRange(masterComp, exceptions);
        } catch (Exception e){
            LOG.error("ERROR in calendar: " + calendar, e);
            throw e;
        }
    }

    /**
     * Return a start and end Date that represents the start of the first
     * occurence of a recurring component and the end of the last occurence.  If
     * the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     * 
     * @param comp Component to analyze
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Component comp) {
        return calculateRecurrenceRange(comp, new ArrayList<Component>(0));
        
    }    
    /**
     * Return a start and end Date that represents the start of the first
     * occurence of a recurring component and the end of the last occurence.  If
     * the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     * 
     * @param comp Component to analyze
     * @param modifications modifications to component
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Component comp, List<Component> modifications) {

        Date[] dateRange = new Date[2];
        Date start = getStartDate(comp);
        
        // must have start date
        if (start == null) {
            return new Date[]{};
        }
        
        Dur duration = null;
        Date end = getEndDate(comp);
        if (end == null) {
            if (start instanceof DateTime) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        } else {
            if(end instanceof DateTime) {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if(end.before(start)) {
                    end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(start), start);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if(end.before(start)) {
                    end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(start), start);
                }
            }
            duration = new Dur(start, end);
        }
        
        // Always add master's occurence
        dateRange[0] = start;
        dateRange[1] = end;
        
        // Now tweak range based on RDATE, RRULE, and component modifications
        // For now, ignore EXDATE and EXRULE because RDATE and RRULE will 
        // give us the broader range.
        
        // recurrence dates..
        PropertyList<RDate> rDates = comp.getProperties().getProperties(Property.RDATE);
        for (RDate rdate : rDates) {
            // Both PERIOD and DATE/DATE-TIME values allowed
            if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                    Parameter.VALUE))) {
                for (Period period : rdate.getPeriods()) {
                    if (period.getStart().before(dateRange[0])) {
                        dateRange[0] = period.getStart();
                    }
                    if (period.getEnd().after(dateRange[1])) {
                        dateRange[1] = period.getEnd();
                    }
                    
                }
            } else {
                for (Date startDate : rdate.getDates()) {
                    Date endDate = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration
                            .getTime(startDate), startDate);
                    if (startDate.before(dateRange[0])) {
                        dateRange[0] = startDate;
                    }
                    if (endDate.after(dateRange[1])) {
                        dateRange[1] = endDate;
                    }
                }
            }
        }

        // recurrence rules..
        PropertyList<RRule> rRules = comp.getProperties().getProperties(Property.RRULE);
        for (RRule rrule : rRules) {
            Recur recur = rrule.getRecur();
            
            // If this is an infinite recurring event, we are done processing
            // the rules
            if(recur.getCount()==-1 && recur.getUntil()==null) {
                dateRange[1] = null;
                break;
            }
            
            // DateList startDates = rrule.getRecur().getDates(start.getDate(),
            // adjustedRangeStart, rangeEnd, (Value)
            // start.getParameters().getParameter(Parameter.VALUE));
            DateList startDates = rrule.getRecur().getDates(start, start,
                    maxExpandDate,
                    start instanceof DateTime ? Value.DATE_TIME : Value.DATE);
            
            // Dates are sorted, so get the last occurence, and calculate the end
            // date and update dateRange if necessary
            if(startDates.size()>0) {
                Date lastStart = (Date) startDates.get(startDates.size()-1);
                Date endDate = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(lastStart), start);
                
                if (endDate.after(dateRange[1])) {
                    dateRange[1] = endDate;
                }
            }
        }
        
        // event modifications....
        for(Component modComp : modifications) {
            Date startMod = getStartDate(modComp);
            Date endMod = getEndDate(modComp);
            if (startMod.before(dateRange[0])) {
                dateRange[0] = startMod;
            }
            if (dateRange[1] != null && endMod != null && endMod.after(dateRange[1])) {
                dateRange[1] = endMod;
            }
            
            // TODO: handle THISANDFUTURE/THISANDPRIOR edge cases
        }
        
        // make sure timezones are consistent with original timezone
        if(start instanceof DateTime) {
            
            boolean utc = ((DateTime) start).isUtc();
            TimeZone tz = ((DateTime) start).getTimeZone();
            
            if((dateRange[0] instanceof DateTime)){
                if(tz != null ){
                    ((DateTime) dateRange[0]).setTimeZone(tz);
                }else{
                    ((DateTime) dateRange[0]).setUtc(utc);
                }
            }
            if((dateRange[1] instanceof DateTime) &&
                dateRange[1]!=null && (dateRange[1] instanceof DateTime)) {
                    if(tz != null){
                        ((DateTime) dateRange[1]).setTimeZone(tz);
                    }else{
                        ((DateTime) dateRange[1]).setUtc(utc);
                    } 
            }
        }
        
        return dateRange;
    }
    
    /**
     * Expand recurring event for given time-range.
     * @param calendar calendar containing recurring event and modifications
     * @param rangeStart expand start
     * @param rangeEnd expand end
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurences of recurring event during
     *         time range
     */
    public InstanceList getOcurrences(Calendar calendar, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        ComponentList<VEvent> vevents = calendar.getComponents().getComponents(Component.VEVENT);
        
        List<Component> exceptions = new ArrayList<Component>();
        Component masterComp = null;
        
        // get list of exceptions (VEVENT with RECURRENCEID)
        for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
            VEvent event = i.next();
            if (event.getRecurrenceId() != null) {
                exceptions.add(event);
            }
            else {
                masterComp = event; 
            }
            
        }
        
        return getOcurrences(masterComp, exceptions, rangeStart, rangeEnd, timezone);
    }
    
    /**
     * Expand recurring compnent for given time-range.
     * @param component recurring component to expand
     * @param rangeStart expand start date
     * @param rangeEnd expand end date
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurences of recurring event during
     *         time range
     */
    public InstanceList getOcurrences(Component component, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        return getOcurrences(component, new ArrayList<Component>(0), rangeStart, rangeEnd, timezone);
    }
    
    /**
     * Expand recurring compnent for given time-range.
     * @param component recurring component to expand
     * @param modifications modifications to recurring component
     * @param rangeStart expand start date
     * @param rangeEnd expand end date
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurences of recurring event during
     *         time range 
     */
    public InstanceList getOcurrences(Component component, List<Component> modifications, Date rangeStart, 
                                        Date rangeEnd, TimeZone timezone) {
        InstanceList instances = new InstanceList();
        instances.setTimezone(timezone);
        instances.addMaster(component, rangeStart, rangeEnd);
        for(Component mod: modifications) {
            instances.addOverride(mod, rangeStart, rangeEnd);
        }
        
        return instances;
    }
    
    
    /**
     * Determine if date is a valid occurence in recurring calendar component
     * @param calendar recurring calendar component
     * @param occurrence occurrence date
     * @return true if the occurrence date is a valid occurrence, otherwise false
     */
    public boolean isOccurrence(Calendar calendar, Date occurrence) {
        java.util.Calendar cal = Dates.getCalendarInstance(occurrence);
        cal.setTime(occurrence);
       
        // Add a second or day (one unit forward) so we can set a range for
        // finding instances.  This is required because ical4j's Recur apis
        // only calculate recurring dates up until but not including the 
        // end date of the range.
        if(occurrence instanceof DateTime) {
            cal.add(java.util.Calendar.SECOND, 1);
        }
        else {
            cal.add(java.util.Calendar.DAY_OF_WEEK, 1);
        }
        
        Date rangeEnd = 
            org.unitedinternet.cosmo.calendar.util.Dates.getInstance(cal.getTime(), occurrence);
        
        TimeZone tz = null;
        
        for(Object obj : calendar.getComponents(Component.VEVENT)){
        	VEvent evt = (VEvent)obj;
        	if(evt.getRecurrenceId() == null && evt.getStartDate() != null){
        		tz = evt.getStartDate().getTimeZone();
        	}
        }
        InstanceList instances = getOcurrences(calendar, occurrence, rangeEnd, tz);
        
        for(Iterator<Instance> it = instances.values().iterator(); it.hasNext();) {
            Instance instance = it.next();
            if(instance.getRid().getTime()==occurrence.getTime()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets start date.
     * @param comp The component.
     * @return The date.
     */
    private Date getStartDate(Component comp) {
        DtStart prop = (DtStart) comp.getProperties().getProperty(
                Property.DTSTART);
        return (prop != null) ? prop.getDate() : null;
    }

    /**
     * Gets end date.
     * @param comp The component.
     * @return The date.
     */
    private Date getEndDate(Component comp) {
        DtEnd dtEnd = (DtEnd) comp.getProperties().getProperty(Property.DTEND);
        // No DTEND? No problem, we'll use the DURATION if present.
        if (dtEnd == null) {
            Date dtStart = getStartDate(comp);
            Duration duration = (Duration) comp.getProperties().getProperty(
                    Property.DURATION);
            if (duration != null) {
                dtEnd = new DtEnd(org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getDuration()
                        .getTime(dtStart), dtStart));
            }
        }
        return (dtEnd != null) ? dtEnd.getDate() : null;
    }
}
