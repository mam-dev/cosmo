/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;

import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;


/**
 * Represents a calendar event.
 */
public abstract class MockBaseEventStamp extends MockStamp
    implements java.io.Serializable, ICalendarConstants, BaseEventStamp {

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    protected static final String VALUE_MISSING = "MISSING";
    
    private Calendar eventCalendar = null;
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getEvent()
     */
    /**
     * Gets event.
     * @return event.
     */
    public abstract VEvent getEvent();
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getEventCalendar()
     */
    /**
     * Gets event calendar.
     * @return The calendar.
     */
    public Calendar getEventCalendar() {
        return eventCalendar;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setEventCalendar(net.fortuna.ical4j.model.Calendar)
     */
    /**
     * Sets event calendar.
     * @param calendar The calendar.
     */
    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }
    
      
    /**
     * Return BaseEventStamp from Item
     * @param item The item.
     * @return BaseEventStamp from Item
     */
    public static BaseEventStamp getStamp(Item item) {
        return (BaseEventStamp) item.getStamp(BaseEventStamp.class);
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getIcalUid()
     */
    /**
     * Gets ical uid.
     * @return IcalUid.
     */
    public String getIcalUid() {
        return getEvent().getUid().getValue();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setIcalUid(java.lang.String)
     */
    /**
     * Sets ical uid.
     * @param uid The uid.
     */
    public void setIcalUid(String uid) {
        ICalendarUtils.setUid(uid, getEvent());
    }

    /**
     * Sets ical uid.
     * @param text The text.
     * @param event The event.
     */
    protected void setIcalUid(String text, VEvent event) {
        event.getUid().setValue(text);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setSummary(java.lang.String)
     */
    /**
     * Sets summary.
     * @param text The text.
     */
    public void setSummary(String text) {
        ICalendarUtils.setSummary(text, getEvent());
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDescription(java.lang.String)
     */
    /**
     * Sets description.
     * @param text The text.
     */
    public void setDescription(String text) {
        ICalendarUtils.setDescription(text, getEvent());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getStartDate()
     */
    /**
     * Gets start date.
     * @return date.
     */
    public Date getStartDate() {
        VEvent event = getEvent();
        if (event==null) {
            return null;
        }
        
        DtStart dtStart = event.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setStartDate(net.fortuna.ical4j.model.Date)
     */
    /**
     * Sets start date.
     * @param date The date.
     */
    public void setStartDate(Date date) {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart != null) {
            dtStart.setDate(date);
        }
        else {
            dtStart = new DtStart(date);
            getEvent().getProperties().add(dtStart);
        }
        setDatePropertyValue(dtStart, date);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getEndDate()
     */
    /**
     * Gets end date.
     * @return The date.
     */
    public Date getEndDate() {
        VEvent event = getEvent();
        if (event==null) {
            return null;
        }
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            Date startDate = getStartDate();
            Dur duration = getDuration();
            
            // if no DURATION, then there is no end time
            if (duration == null) {
                return null;
            }
            
            Date endDate = null;
            if (startDate instanceof DateTime) {
                endDate = new DateTime(startDate);
            }
            else {
                endDate = new Date(startDate);
            }
            
            endDate.setTime(duration.getTime(startDate).getTime());
            return endDate;
        }
            
        return dtEnd.getDate();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setEndDate(net.fortuna.ical4j.model.Date)
     */
    /**
     * Sets endDate.
     * @param date The date.
     */
    public void setEndDate(Date date) {
        DtEnd dtEnd = getEvent().getEndDate(false);
        if (dtEnd != null && date != null) {
            dtEnd.setDate(date);
        }
        else  if(dtEnd !=null && date == null) {
            // remove DtEnd if there is no end date
            getEvent().getProperties().remove(dtEnd);
        }
        else {
            // remove the duration if there was one
            Duration duration = (Duration) getEvent().getProperties().
                getProperty(Property.DURATION);
            if (duration != null) {
                getEvent().getProperties().remove(duration);
            }
            dtEnd = new DtEnd(date);
            getEvent().getProperties().add(dtEnd);
        }
        setDatePropertyValue(dtEnd, date);
    }

    /**
     * Sets date property value
     * @param prop The date property.
     * @param date The date.
     */
    protected void setDatePropertyValue(DateProperty prop, Date date) {
        if (prop == null) {
            return;
        }
        Value value = (Value) prop.getParameters()
                .getParameter(Parameter.VALUE);
        if (value != null) {
            prop.getParameters().remove(value);
        }

        // Add VALUE=DATE for Date values, otherwise
        // leave out VALUE=DATE-TIME because it is redundant
        if (!(date instanceof DateTime)) {
            prop.getParameters().add(Value.DATE);
        }
    }
    
    /**
     * Sets date list property value.
     * @param prop Date list property.
     */
    protected void setDateListPropertyValue(DateListProperty prop) {
        if (prop == null) {
            return;
        }
        Value value = (Value)
            prop.getParameters().getParameter(Parameter.VALUE);
        if (value != null) {
            prop.getParameters().remove(value);
        }
        
        value = prop.getDates().getType();
        
        // set VALUE=DATE but not VALUE=DATE-TIME as its redundant
        if (value.equals(Value.DATE)) {
            prop.getParameters().add(value);
        }
        
        // update timezone for now because ical4j DateList doesn't
        Parameter param = (Parameter) prop.getParameters().getParameter(
                Parameter.TZID);
        if (param != null) {
            prop.getParameters().remove(param);
        }
        
        if (prop.getDates().getTimeZone() != null) {
            prop.getParameters().add(new TzId(prop.getDates().getTimeZone().getID()));
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDuration()
     */
    /**
     * Gets duration.
     * @return The duration.
     */
    public Dur getDuration() {
        return ICalendarUtils.getDuration(getEvent());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDuration(net.fortuna.ical4j.model.Dur)
     */
    /**
     * Sets duration.
     * @param dur The duration.
     */
    public void setDuration(Dur dur) {
        ICalendarUtils.setDuration(getEvent(), dur);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getLocation()
     */
    /**
     * Gets location.
     * @return The location.
     */
    public String getLocation() {
        Property p = getEvent().getProperties().
            getProperty(Property.LOCATION);
        if (p == null) {
            return null;
        }
        return p.getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setLocation(java.lang.String)
     */
    /**
     * Sets location.
     * @param text The text.
     */
    public void setLocation(String text) {
        
        Location location = (Location)
            getEvent().getProperties().getProperty(Property.LOCATION);
        
        if (text == null) {
            if (location != null) {
                getEvent().getProperties().remove(location);
            }
            return;
        }                
        if (location == null) {
            location = new Location();
            getEvent().getProperties().add(location);
        }
        location.setValue(text);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getRecurrenceRules()
     */
    /**
     * Gets recurrence rules.
     * @return The list.
     */
    public List<Recur> getRecurrenceRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        VEvent event = getEvent();
        if(event!=null) {
            for (Object rrule : getEvent().getProperties().getProperties(Property.RRULE)) {
                l.add(((RRule)rrule).getRecur());
            }
        }
        return l;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceRules(java.util.List)
     */
    /**
     * Sets recurrence rules.
     * @param recurs List with recurrence rules.
     */
    public void setRecurrenceRules(List<Recur> recurs) {
        if (recurs == null) {
            return;
        }
        PropertyList<Property> pl = getEvent().getProperties();
        for (Property rrule : pl.getProperties(Property.RRULE)) {
            pl.remove(rrule);
        }
        for (Recur recur : recurs) {
            pl.add(new RRule(recur));
        }
      
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceRule(net.fortuna.ical4j.model.Recur)
     */
    /**
     * Sets recurrence rule.
     * @param recur The recurrence.
     */
    public void setRecurrenceRule(Recur recur) {
        if (recur == null) {
            return;
        }
        ArrayList<Recur> recurs = new ArrayList<Recur>(1);
        recurs.add(recur);
        setRecurrenceRules(recurs);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getExceptionRules()
     */
    /**
     * Gets exception rules.
     * @return list<Recur>.
     */
    public List<Recur> getExceptionRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        for (Object exrule : getEvent().getProperties().
                 getProperties(Property.EXRULE))
            l.add(((ExRule)exrule).getRecur());
        return l;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setExceptionRules(java.util.List)
     */
    /**
     * Sets exception rules.
     * @param recurs The list.
     */
    public void setExceptionRules(List<Recur> recurs) {
        if (recurs == null) {
            return;
        }
        PropertyList<Property> pl = getEvent().getProperties();
        for (Property exrule : pl.getProperties(Property.EXRULE)) {
            pl.remove(exrule);
        }
        for (Recur recur : recurs) {
            pl.add(new ExRule(recur));
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getRecurrenceDates()
     */
    /**
     * Gets recurrence dates.
     * @return The date list.
     */
    public DateList getRecurrenceDates() {
        
        DateList l = null;
        
        VEvent event = getEvent();
        if (event == null) {
            return null;
        }
        
        for (Object property : event.getProperties().getProperties(Property.RDATE)) {
            RDate rdate = (RDate) property;
            if (l == null) {
                if (Value.DATE.equals(rdate.getParameter(Parameter.VALUE))) {
                    l = new DateList(Value.DATE);
                }
                else {
                    l = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
                }
            }
            l.addAll(rdate.getDates());
        }
            
        return l;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceDates(net.fortuna.ical4j.model.DateList)
     */
    /**
     * Sets recurrence dates.
     * @param dates The date list.
     */    
    public void setRecurrenceDates(DateList dates) { 
        if (dates == null) {
            return;
        }
        
        PropertyList<Property> pl = getEvent().getProperties();
        for (Property rdate : pl.getProperties(Property.RDATE)) {
            pl.remove(rdate);
        }
        if (dates.isEmpty()) {
            return;
        }
        
        RDate rDate = new RDate(dates);
        setDateListPropertyValue(rDate);
        pl.add(rDate);   
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getExceptionDates()
     */
    /**
     * Gets exception dates.
     * @return date list.
     */
    public DateList getExceptionDates() {
        DateList l = null;
        for (Object property : getEvent().getProperties().getProperties(Property.EXDATE)) {
            ExDate exdate = (ExDate) property;
            if(l==null) {
                if (Value.DATE.equals(exdate.getParameter(Parameter.VALUE))) {
                    l = new DateList(Value.DATE);
                }
                else {
                    l = new DateList(Value.DATE_TIME, exdate.getDates().getTimeZone());
                }
            }
            l.addAll(exdate.getDates());
        }
            
        return l;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarm()
     */
    /**
     * Gets display alarm.
     * @return The alarm.
     */
    public VAlarm getDisplayAlarm() {
        VEvent event = getEvent();
       
        if (event == null) {
            return null;
        }
        
        return getDisplayAlarm(event);
    }
    
    /**
     * Display alarm.
     * @param event The event.
     * @return The alarm.
     */
    protected VAlarm getDisplayAlarm(VEvent event) {
        for(@SuppressWarnings("rawtypes")
        Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(Action.DISPLAY)) {
                return alarm;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#removeDisplayAlarm()
     */
    /**
     * Removes display alarm.
     */
    public void removeDisplayAlarm() {
        VEvent event = getEvent();
        
        if (event == null) {
            return;
        }
         
        for(@SuppressWarnings("rawtypes")
        Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY)) {
                it.remove();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarmDescription()
     */
    /**
     * Gets display alarm description.
     * @return The alarm description.
     */
    public String getDisplayAlarmDescription() {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return null;
        }
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        
        if (description == null) {
            return null;
        }
        
        return description.getValue();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmDescription(java.lang.String)
     */
    /**
     * Sets display alarm description.
     * @param newDescription The new description.
     */
    public void setDisplayAlarmDescription(String newDescription) {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return;
        }
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        
        if (description == null) {
            description = new Description();
            alarm.getProperties().add(description);
        }
        
        description.setValue(newDescription);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarmTrigger()
     */
    /**
     * Gets display alarm trigger.
     * @return The trigger.
     */
    public Trigger getDisplayAlarmTrigger() {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return null;
        }
        
        return (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmTrigger(net.fortuna.ical4j.model.property.Trigger)
     */
    /**
     * Sets display alarm trigger.
     * @param newTrigger The new trigger.
     */
    public void setDisplayAlarmTrigger(Trigger newTrigger) {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return;
        }
        
        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
        if (oldTrigger != null) {
            alarm.getProperties().remove(oldTrigger);
        }

        if (newTrigger != null) {
            alarm.getProperties().add(newTrigger);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmTriggerDate(net.fortuna.ical4j.model.DateTime)
     */
    /**
     * Sets display alarm trigger date.
     * @param triggerDate The trigger date.
     */
    public void setDisplayAlarmTriggerDate(DateTime triggerDate) {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return;
        }
        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
        if (oldTrigger != null) {
            alarm.getProperties().remove(oldTrigger);
        }
        
        Trigger newTrigger = new Trigger();
        newTrigger.getParameters().add(Value.DATE_TIME);
        newTrigger.setDateTime(triggerDate);
        
        alarm.getProperties().add(newTrigger);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarmDuration()
     */
    /**
     * Gets display alarm duration.
     * @return The duration.
     */
    public Dur getDisplayAlarmDuration() {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return null;
        }
        
        Duration dur =  (Duration) alarm.getProperties().getProperty(Property.DURATION);
        if (dur != null) {
            return dur.getDuration();
        }
        else {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmDuration(net.fortuna.ical4j.model.Dur)
     */
    /**
     * Sets display alarm duration.
     * @param dur - The duration.
     */
    public void setDisplayAlarmDuration(Dur dur) {
        VAlarm alarm = getDisplayAlarm();
        if (alarm==null) {
            return;
        }
        
        Duration duration = (Duration) alarm.getProperties().getProperty(Property.DURATION);
        if (dur == null) {
            if (duration != null) {
                alarm.getProperties().remove(duration);
            }
            return;
        }
        if (duration == null) {
            duration = new Duration();
            alarm.getProperties().add(duration);
        }
        
        duration.setDuration(dur);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarmRepeat()
     */
    /**
     * Gets display alarm repeat.
     * @return The display alarm repeat.
     */
    public Integer getDisplayAlarmRepeat() {
        VAlarm alarm = getDisplayAlarm();
        if (alarm==null) {
            return null;
        }
        
        Repeat repeat = (Repeat) alarm.getProperties().getProperty(Property.REPEAT);
        
        if (repeat==null) {
            return null;
        }
        
        return repeat.getCount();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmRepeat(java.lang.Integer)
     */
    /**
     * Sets diplay alarm repeat.
     * @param count 
     */
    public void setDisplayAlarmRepeat(Integer count) {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return;
        }
        
        Repeat repeat = (Repeat) alarm.getProperties().getProperty(Property.REPEAT);
        if (count == null) {
            if (repeat != null) {
                alarm.getProperties().remove(repeat);
            }
            return;
        }
        if (repeat == null) {
            repeat = new Repeat();
            alarm.getProperties().add(repeat);
        }

        repeat.setCount(count.intValue());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setExceptionDates(net.fortuna.ical4j.model.DateList)
     */
    /**
     * Sets exception dates.
     * @param dates The date list.
     */    
    public void setExceptionDates(DateList dates) {
        if (dates == null) {
            return;
        }
        
        PropertyList<Property> properties = getEvent().getProperties();
        for (Property exdate : properties.getProperties(Property.EXDATE)) {
            properties.remove(exdate);
        }
        if (dates.isEmpty()) {
            return;
        }
        
        ExDate exDate = new ExDate(dates);
        setDateListPropertyValue(exDate);
        properties.add(exDate);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getRecurrenceId()
     */
    /**
     * Gets recurrence id.
     * @return The date.
     */
    public Date getRecurrenceId() {
        RecurrenceId rid = getEvent().getRecurrenceId();
        if (rid == null) {
            return null;
        }
        return rid.getDate();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceId(net.fortuna.ical4j.model.Date)
     */
    /**
     * Sets recurrence id.
     * @param date The date.
     */
    public void setRecurrenceId(Date date) {
        RecurrenceId recurrenceId = (RecurrenceId)
            getEvent().getProperties().
            getProperty(Property.RECURRENCE_ID);
        if (date == null) {
            if (recurrenceId != null) {
                getEvent().getProperties().remove(recurrenceId);
            }
            return;
        }
        if (recurrenceId == null) {
            recurrenceId = new RecurrenceId();
            getEvent().getProperties().add(recurrenceId);
        }
        
        recurrenceId.setDate(date);
        setDatePropertyValue(recurrenceId, date);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getStatus()
     */
    /**
     * Gets status.
     * @return The status.
     */
    public String getStatus() {
        Property p = getEvent().getProperties().
            getProperty(Property.STATUS);
        if (p == null) {
            return null;
        }
        return p.getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setStatus(java.lang.String)
     */
    /**
     * Sets status.
     * @param text The text.
     */
    public void setStatus(String text) {
        // ical4j Status value is immutable, so if there's any change
        // at all, we have to remove the old status and add a new
        // one.
        Status status = (Status)
            getEvent().getProperties().getProperty(Property.STATUS);
        if (status != null) {
            getEvent().getProperties().remove(status);
        }
        if (text == null) {
            return;
        }
        getEvent().getProperties().add(new Status(text));
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#isAnyTime()
     */
    /**
     * Is any time
     * @return boolean.
     */
    public Boolean isAnyTime() {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart == null) {
            return Boolean.FALSE;
        }
        Parameter parameter = dtStart.getParameters()
            .getParameter(PARAM_X_OSAF_ANYTIME);
        if (parameter == null) {
            return Boolean.FALSE;
        }

        return Boolean.valueOf(VALUE_TRUE.equals(parameter.getValue()));
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getAnyTime()
     */
    /**
     * Gets any time.
     * @return the boolean.
     */
    public Boolean getAnyTime() {
        return isAnyTime();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setAnyTime(java.lang.Boolean)
     */
    /**
     * Sets any time.
     * @param isAnyTime isAnyTime.
     */
    public void setAnyTime(Boolean isAnyTime) {
        DtStart dtStart = getEvent().getStartDate();
        if (dtStart == null) {
            throw new IllegalStateException("event has no start date");
        }
        Parameter parameter = dtStart.getParameters().getParameter(
                PARAM_X_OSAF_ANYTIME);

        // add X-OSAF-ANYTIME if it doesn't exist
        if (parameter == null && Boolean.TRUE.equals(isAnyTime)) {
            dtStart.getParameters().add(getAnyTimeXParam());
            return;
        }

        // if it exists, update based on isAnyTime
        if (parameter != null) {
            dtStart.getParameters().remove(parameter);
            if (Boolean.TRUE.equals(isAnyTime)) {   
                dtStart.getParameters().add(getAnyTimeXParam());
            }
        }
    }
    
    /**
     * Gets any time param.
     * @return The parameter.
     */
    protected Parameter getAnyTimeXParam() {
        return new XParameter(PARAM_X_OSAF_ANYTIME, VALUE_TRUE);
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#createCalendar()
     */
    /**
     * Creates calendar.
     */
    public void createCalendar() {
        
        NoteItem note = (NoteItem) getItem();
       
        String icalUid = note.getIcalUid();
        if(icalUid==null) {
            // A modifications UID will be the parent's icaluid
            // or uid
            if(note.getModifies()!=null) { 
                if(note.getModifies().getIcalUid()!=null) {
                    icalUid = note.getModifies().getIcalUid();
                }
                else {
                    icalUid = note.getModifies().getUid();
                }
            } else {
                icalUid = note.getUid();
            }
        }

        Calendar cal = ICalendarUtils.createBaseCalendar(new VEvent(), icalUid);
        
        setEventCalendar(cal);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#isRecurring()
     */
    /**
     * Is recurring.
     * @return boolean.
     */
    public boolean isRecurring() {
       if(getRecurrenceRules().size()>0)
           return true;
       
       DateList rdates = getRecurrenceDates();
       
       return (rdates!=null && rdates.size()>0);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#creatDisplayAlarm()
     */
    /**
     * Creates display alarm.
     */
    public void creatDisplayAlarm() {
        VAlarm alarm = new VAlarm();
        alarm.getProperties().add(Action.DISPLAY);
        getEvent().getAlarms().add(alarm);
        setDisplayAlarmDescription("Event Reminder");
    }
}
