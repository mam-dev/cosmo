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
package org.unitedinternet.cosmo.model.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.validation.constraints.NotNull;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
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

import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;


/**
 * Hibernate persistent BaseEventStamp.
 */

@Entity
@SecondaryTable(name="event_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")},
        indexes = {
                @Index(name = "idx_startdt",columnList = "startDate"),
                @Index(name = "idx_enddt",columnList = "endDate"),
                @Index(name = "idx_floating",columnList = "isFloating"),
                @Index(name = "idx_recurring",columnList = "isrecurring")}
)
@DiscriminatorValue("baseevent")
@SuppressWarnings("serial")
public abstract class HibBaseEventStamp extends HibStamp implements ICalendarConstants, BaseEventStamp {

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    public static final String TIME_INFINITY = "Z-TIME-INFINITY";
    
    protected static final String VALUE_MISSING = "MISSING";
    
    @Column(table="event_stamp", name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    private Calendar eventCalendar = null;

    @Embedded
    private HibEventTimeRangeIndex timeRangeIndex = null;
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getEvent()
     */
    public abstract VEvent getEvent();
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getEventCalendar()
     */
    public Calendar getEventCalendar() {
        return eventCalendar;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setEventCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }
    
    public HibEventTimeRangeIndex getTimeRangeIndex() {
        return timeRangeIndex;
    }

    public void setTimeRangeIndex(HibEventTimeRangeIndex timeRangeIndex) {
        this.timeRangeIndex = timeRangeIndex;
    }
    
      
    /**
     * Return BaseEventStamp from Item
     * @param item
     * @return BaseEventStamp from Item
     */
    public static BaseEventStamp getStamp(Item item) {
        return (BaseEventStamp) item.getStamp(BaseEventStamp.class);
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getIcalUid()
     */
    public String getIcalUid() {
        return getEvent().getUid().getValue();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setIcalUid(java.lang.String)
     */
    public void setIcalUid(String uid) {
        VEvent event = getEvent();
        if(event==null) {
            throw new IllegalStateException("no event");
        }
        ICalendarUtils.setUid(uid, getEvent());
    }

    protected void setIcalUid(String text, VEvent event) {
        event.getUid().setValue(text);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setSummary(java.lang.String)
     */
    public void setSummary(String text) {
        ICalendarUtils.setSummary(text, getEvent());
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDescription(java.lang.String)
     */
    public void setDescription(String text) {
        ICalendarUtils.setDescription(text, getEvent());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getStartDate()
     */
    public Date getStartDate() {
        VEvent event = getEvent();
        if(event==null) {
            return null;
        }
        
        DtStart dtStart = event.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setStartDate(net.fortuna.ical4j.model.Date)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getEndDate()
     */
    public Date getEndDate() {
        VEvent event = getEvent();
        if(event==null) {
            return null;
        }
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            Date startDate = getStartDate();
            Dur duration = getDuration();
            
            // if no DURATION, then there is no end time
            if(duration==null) {
                return null;
            }
            
            Date endDate = null;
            if(startDate instanceof DateTime) {
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setEndDate(net.fortuna.ical4j.model.Date)
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

    protected void setDatePropertyValue(DateProperty prop,
                                        Date date) {
        if (prop == null) {
            return;
        }
        Value value = (Value)
            prop.getParameters().getParameter(Parameter.VALUE);
        if (value != null) {
            prop.getParameters().remove(value);
        }
        
        // Add VALUE=DATE for Date values, otherwise
        // leave out VALUE=DATE-TIME because it is redundant
        if(! (date instanceof DateTime)) {
            prop.getParameters().add(Value.DATE);
        }
    }
    
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
        if(value.equals(Value.DATE)) {
            prop.getParameters().add(value);
        }
        
        // update timezone for now because ical4j DateList doesn't
        Parameter param = (Parameter) prop.getParameters().getParameter(
                Parameter.TZID);
        if (param != null) {
            prop.getParameters().remove(param);
        }
        
        if(prop.getDates().getTimeZone()!=null) {
            prop.getParameters().add(new TzId(prop.getDates().getTimeZone().getID()));
        }
    }

   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDuration()
     */
    public Dur getDuration() {
        return ICalendarUtils.getDuration(getEvent());
    }

  
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDuration(net.fortuna.ical4j.model.Dur)
     */
    public void setDuration(Dur dur) {
        ICalendarUtils.setDuration(getEvent(), dur);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getLocation()
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setLocation(java.lang.String)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getRecurrenceRules()
     */
    public List<Recur> getRecurrenceRules() {
        List<Recur> toReturn = new ArrayList<>();
        VEvent event = getEvent();
        if(event != null) {
            PropertyList<RRule> rruleProperties = event.getProperties().getProperties(Property.RRULE);
            for (RRule rrule : rruleProperties) {
                toReturn.add(rrule.getRecur());
            }
        }
        return toReturn;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setRecurrenceRules(java.util.List)
     */
    public void setRecurrenceRules(List<Recur> recurs) {
        if (recurs == null) {
            return;
        }
        PropertyList<Property> properties = getEvent().getProperties();
        for (Property rrule : properties.getProperties(Property.RRULE)) {
            properties.remove(rrule);
        }
        for (Recur recur : recurs) {
            properties.add(new RRule(recur));
        }      
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setRecurrenceRule(net.fortuna.ical4j.model.Recur)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getExceptionRules()
     */
    public List<Recur> getExceptionRules() {
        List<Recur> toReturn = new ArrayList<Recur>();
        PropertyList<ExRule> exRuleProperties = getEvent().getProperties().getProperties(Property.EXRULE);
        for (ExRule exrule : exRuleProperties) {
            toReturn.add(exrule.getRecur());
        }
        return toReturn;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setExceptionRules(java.util.List)
     */
    public void setExceptionRules(List<Recur> recurs) {
        if (recurs == null) {
            return;
        }
        PropertyList<Property> properties = getEvent().getProperties();
        for (Property exrule : properties.getProperties(Property.EXRULE)) {
            properties.remove(exrule);
        }
        for (Recur recur : recurs) {
            properties.add(new ExRule(recur));
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getRecurrenceDates()
     */
    public DateList getRecurrenceDates() {
        DateList dateList = null;

        VEvent event = getEvent();
        if (event == null) {
            return null;
        }

        PropertyList<RDate> rDateProperties = getEvent().getProperties().getProperties(Property.RDATE);
        for (RDate rdate : rDateProperties) {
            if (dateList == null) {
                if (Value.DATE.equals(rdate.getParameter(Parameter.VALUE))) {
                    dateList = new DateList(Value.DATE);
                } else {
                    dateList = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
                }
            }
            dateList.addAll(rdate.getDates());
        }

        return dateList;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setRecurrenceDates(net.fortuna.ical4j.model.DateList)
     */
    public void setRecurrenceDates(DateList dates) {
        if (dates == null) {
            return;
        }
        
        PropertyList<RDate> rdateList = getEvent().getProperties().getProperties(Property.RDATE);
        for (RDate rdate : rdateList) {
            rdateList.remove(rdate);
        }
        if (dates.isEmpty()) {
            return;
        }
        
        RDate rDate = new RDate(dates);
        setDateListPropertyValue(rDate);
        rdateList.add(rDate);   
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getExceptionDates()
     */
    public DateList getExceptionDates() {
        DateList dateList = null;
        PropertyList<ExDate> exDatesProperties = getEvent().getProperties().getProperties(Property.EXDATE);
        
        for (ExDate exdate : exDatesProperties) {
            if(dateList==null) {
                if(Value.DATE.equals(exdate.getParameter(Parameter.VALUE))) {
                    dateList = new DateList(Value.DATE);
                }
                else {
                    dateList = new DateList(Value.DATE_TIME, exdate.getDates().getTimeZone());
                }
            }
            dateList.addAll(exdate.getDates());
        }
            
        return dateList;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDisplayAlarm()
     */
    public VAlarm getDisplayAlarm() {
        VEvent event = getEvent();
       
        if(event==null) {
            return null;
        }
        
        return getDisplayAlarm(event);
    }
    
    protected VAlarm getDisplayAlarm(VEvent event) {
        ComponentList<VAlarm> alarmsList = event.getAlarms();
        for(VAlarm alarm: alarmsList) {
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY)) {
                return alarm;
            }
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#removeDisplayAlarm()
     */
    public void removeDisplayAlarm() {
        VEvent event = getEvent();
        
        if(event==null) {
            return;
        }
         
        ComponentList<VAlarm> alarmsList = event.getAlarms();
        for(VAlarm alarm: alarmsList) {
            if (alarm.getProperties().getProperty(Property.ACTION).equals(
                    Action.DISPLAY)) {
                alarmsList.remove(alarm);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDisplayAlarmDescription()
     */
    public String getDisplayAlarmDescription() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return null;
        }
        
        Description description = (Description) alarm.getProperties()
                .getProperty(Property.DESCRIPTION);
        
        if(description==null) {
            return null;
        }
        
        return description.getValue();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDisplayAlarmDescription(java.lang.String)
     */
    public void setDisplayAlarmDescription(String newDescription) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDisplayAlarmTrigger()
     */
    public Trigger getDisplayAlarmTrigger() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return null;
        }
        
        return (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDisplayAlarmTrigger(net.fortuna.ical4j.model.property.Trigger)
     */
    public void setDisplayAlarmTrigger(Trigger newTrigger) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return;
        }
        
        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(
                Property.TRIGGER);
        if (oldTrigger != null) {
            alarm.getProperties().remove(oldTrigger);
        }

        if(newTrigger!=null) {
            alarm.getProperties().add(newTrigger);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDisplayAlarmTriggerDate(net.fortuna.ical4j.model.DateTime)
     */
    public void setDisplayAlarmTriggerDate(DateTime triggerDate) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return;
        }

        Trigger oldTrigger = (Trigger) alarm.getProperties().getProperty(
                Property.TRIGGER);
        if (oldTrigger != null) {
            alarm.getProperties().remove(oldTrigger);
        }
        
        Trigger newTrigger = new Trigger();
        newTrigger.getParameters().add(Value.DATE_TIME);
        newTrigger.setDateTime(triggerDate);
        
        alarm.getProperties().add(newTrigger);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDisplayAlarmDuration()
     */
    public Dur getDisplayAlarmDuration() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return null;
        }
        
        Duration dur =  (Duration) alarm.getProperties().getProperty(Property.DURATION);
        if(dur!=null) {
            return dur.getDuration();
        }
        else {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDisplayAlarmDuration(net.fortuna.ical4j.model.Dur)
     */
    public void setDisplayAlarmDuration(Dur dur) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return;
        }
        
        Duration duration = (Duration) alarm.getProperties().getProperty(
                Property.DURATION);
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getDisplayAlarmRepeat()
     */
    public Integer getDisplayAlarmRepeat() {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
            return null;
        }
        
        Repeat repeat = (Repeat) alarm.getProperties().getProperty(Property.REPEAT);
        
        if(repeat==null) {
            return null;
        }
        
        return repeat.getCount();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setDisplayAlarmRepeat(java.lang.Integer)
     */
    public void setDisplayAlarmRepeat(Integer count) {
        VAlarm alarm = getDisplayAlarm();
        if(alarm==null) {
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setExceptionDates(net.fortuna.ical4j.model.DateList)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getRecurrenceId()
     */
    public Date getRecurrenceId() {
        RecurrenceId rid = getEvent().getRecurrenceId();
        if (rid == null) {
            return null;
        }
        return rid.getDate();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setRecurrenceId(net.fortuna.ical4j.model.Date)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getStatus()
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setStatus(java.lang.String)
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#isAnyTime()
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#getAnyTime()
     */
    public Boolean getAnyTime() {
        return isAnyTime();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#setAnyTime(java.lang.Boolean)
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
    
    protected Parameter getAnyTimeXParam() {
        return new XParameter(PARAM_X_OSAF_ANYTIME, VALUE_TRUE);
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#createCalendar()
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
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#isRecurring()
     */
    public boolean isRecurring() {
       if(getRecurrenceRules().size()>0) {
           return true;
       }
       
       DateList rdates = getRecurrenceDates();
       
       return rdates!=null && rdates.size()>0;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.BaseEventStamp#creatDisplayAlarm()
     */
    public void creatDisplayAlarm() {
        VAlarm alarm = new VAlarm();
        alarm.getProperties().add(Action.DISPLAY);
        getEvent().getAlarms().add(alarm);
        setDisplayAlarmDescription("Event Reminder");
    }
}
