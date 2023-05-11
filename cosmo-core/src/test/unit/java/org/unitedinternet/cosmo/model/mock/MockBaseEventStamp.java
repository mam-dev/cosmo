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

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.NoteItem;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TemporalAmountAdapter;
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
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Trigger;

/**
 * Represents a calendar event.
 */
@SuppressWarnings("serial")
public abstract class MockBaseEventStamp extends MockStamp
        implements java.io.Serializable, ICalendarConstants, BaseEventStamp {

    protected static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    protected static final String VALUE_MISSING = "MISSING";

    private Calendar eventCalendar = null;

    /**
     * Gets event.
     * 
     * @return event.
     */
    public abstract VEvent getEvent();

    /**
     * Gets event calendar.
     * 
     * @return The calendar.
     */
    @Override
    public Calendar getEventCalendar() {
        return eventCalendar;
    }

    /**
     * Sets event calendar.
     * 
     * @param calendar The calendar.
     */
    @Override
    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }

    /**
     * Gets ical uid.
     * 
     * @return IcalUid.
     */
    @Override
    public String getIcalUid() {
        return getEvent().getUid().getValue();
    }

    /**
     * Sets ical uid.
     * 
     * @param uid The uid.
     */
    @Override
    public void setIcalUid(String uid) {
        ICalendarUtils.setUid(uid, getEvent());
    }

    /**
     * Gets start date.
     * 
     * @return date.
     */
    @Override
    public Date getStartDate() {
        VEvent event = getEvent();
        if (event == null) {
            return null;
        }

        DtStart dtStart = event.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    /**
     * Gets end date.
     * 
     * @return The date.
     */
    @Override
    public Date getEndDate() {
        VEvent event = getEvent();
        if (event == null) {
            return null;
        }
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            Date startDate = getStartDate();
            TemporalAmount duration = getDuration();

            // if no DURATION, then there is no end time
            if (duration == null) {
                return null;
            }

            Date endDate = null;
            if (startDate instanceof DateTime) {
                endDate = new DateTime(startDate);
            } else {
                endDate = new Date(startDate);
            }

            endDate.setTime(new TemporalAmountAdapter(duration).getTime(startDate).getTime());
            return endDate;
        }

        return dtEnd.getDate();
    }

    /**
     * Sets date list property value.
     * 
     * @param prop Date list property.
     */
    private void setDateListPropertyValue(DateListProperty prop) {
        if (prop == null) {
            return;
        }
        Value value = (Value) prop.getParameters().getParameter(Parameter.VALUE);
        if (value != null) {
            prop.getParameters().remove(value);
        }

        value = prop.getDates().getType();

        // set VALUE=DATE but not VALUE=DATE-TIME as its redundant
        if (value.equals(Value.DATE)) {
            prop.getParameters().add(value);
        }

        // update timezone for now because ical4j DateList doesn't
        Parameter param = (Parameter) prop.getParameters().getParameter(Parameter.TZID);
        if (param != null) {
            prop.getParameters().remove(param);
        }

        if (prop.getDates().getTimeZone() != null) {
            prop.getParameters().add(new TzId(prop.getDates().getTimeZone().getID()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDuration()
     */
    /**
     * Gets duration.
     * 
     * @return The duration.
     */
    public TemporalAmount getDuration() {
        return ICalendarUtils.getDuration(getEvent());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDuration(net.fortuna.ical4j.model.Dur)
     */
    /**
     * Sets duration.
     * 
     * @param dur The duration.
     */
    public void setDuration(TemporalAmount dur) {
        ICalendarUtils.setDuration(getEvent(), dur);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getRecurrenceRules()
     */
    /**
     * Gets recurrence rules.
     * 
     * @return The list.
     */
    public List<Recur> getRecurrenceRules() {
        ArrayList<Recur> l = new ArrayList<Recur>();
        VEvent event = getEvent();
        if (event != null) {
            for (Object rrule : getEvent().getProperties().getProperties(Property.RRULE)) {
                l.add(((RRule) rrule).getRecur());
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceRules(java.util.List)
     */
    /**
     * Sets recurrence rules.
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setRecurrenceRule(net.fortuna.ical4j.model.Recur)
     */
    /**
     * Sets recurrence rule.
     * 
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

    /**
     * Gets recurrence dates.
     * 
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
                } else {
                    l = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
                }
            }
            l.addAll(rdate.getDates());
        }

        return l;
    }

    /**
     * Gets exception dates.
     * 
     * @return date list.
     */
    public DateList getExceptionDates() {
        DateList l = null;
        for (Object property : getEvent().getProperties().getProperties(Property.EXDATE)) {
            ExDate exdate = (ExDate) property;
            if (l == null) {
                if (Value.DATE.equals(exdate.getParameter(Parameter.VALUE))) {
                    l = new DateList(Value.DATE);
                } else {
                    l = new DateList(Value.DATE_TIME, exdate.getDates().getTimeZone());
                }
            }
            l.addAll(exdate.getDates());
        }

        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarm()
     */
    /**
     * Gets display alarm.
     * 
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
     * 
     * @param event The event.
     * @return The alarm.
     */
    protected VAlarm getDisplayAlarm(VEvent event) {
        for (@SuppressWarnings("rawtypes")
        Iterator it = event.getAlarms().iterator(); it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(Action.DISPLAY)) {
                return alarm;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getDisplayAlarmTrigger()
     */
    /**
     * Gets display alarm trigger.
     * 
     * @return The trigger.
     */
    public Trigger getDisplayAlarmTrigger() {
        VAlarm alarm = getDisplayAlarm();
        if (alarm == null) {
            return null;
        }

        return (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#setDisplayAlarmTrigger(net.fortuna.ical4j.model.
     * property.Trigger)
     */
    /**
     * Sets display alarm trigger.
     * 
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

    /**
     * Sets exception dates.
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#getRecurrenceId()
     */
    /**
     * Gets recurrence id.
     * 
     * @return The date.
     */
    public Date getRecurrenceId() {
        RecurrenceId rid = getEvent().getRecurrenceId();
        if (rid == null) {
            return null;
        }
        return rid.getDate();
    }

    /**
     * Creates calendar.
     */
    protected void createCalendar() {

        NoteItem note = (NoteItem) getItem();

        String icalUid = note.getIcalUid();
        if (icalUid == null) {
            // A modifications UID will be the parent's icaluid
            // or uid
            if (note.getModifies() != null) {
                if (note.getModifies().getIcalUid() != null) {
                    icalUid = note.getModifies().getIcalUid();
                } else {
                    icalUid = note.getModifies().getUid();
                }
            } else {
                icalUid = note.getUid();
            }
        }

        Calendar cal = ICalendarUtils.createBaseCalendar(new VEvent(), icalUid);

        setEventCalendar(cal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBaseEventStamp#isRecurring()
     */
    /**
     * Is recurring.
     * 
     * @return boolean.
     */
    public boolean isRecurring() {
        if (getRecurrenceRules().size() > 0)
            return true;

        DateList rdates = getRecurrenceDates();

        return (rdates != null && rdates.size() > 0);
    }
}
