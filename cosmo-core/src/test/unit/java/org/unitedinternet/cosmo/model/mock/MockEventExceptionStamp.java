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

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.hibernate.validator.EventException;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Trigger;

/**
 * Represents an event exception.
 */
public class MockEventExceptionStamp extends MockBaseEventStamp implements java.io.Serializable, EventExceptionStamp {

    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;

    public static final String PARAM_OSAF_MISSING = "X-OSAF-MISSING";

    /** default constructor */
    public MockEventExceptionStamp() {
    }

    /**
     * Contructor.
     * 
     * @param item The item.
     */
    public MockEventExceptionStamp(Item item) {
        setItem(item);
    }

    
    /**
     * Gets type.
     * 
     * @return The type.
     */
    @Override
    public String getType() {
        return "eventexception";
    }

    /**
     * Used by the hibernate validator.
     * 
     * @return The calendar.
     */
    @EventException
    private Calendar getValidationCalendar() {
        return getEventCalendar();
    }

    /**
     * Gets event.
     * 
     * @return The event.
     */
    @Override
    public VEvent getEvent() {
        return getExceptionEvent();
    }


    /**
     * Gets exception event.
     * 
     * @return The event.
     */
    @Override
    public VEvent getExceptionEvent() {
        return (VEvent) getEventCalendar().getComponents().getComponents(Component.VEVENT).get(0);
    }

    /**
     * Sets exception event.
     * 
     * @param event The event.
     */
    @Override
    public void setExceptionEvent(VEvent event) {
        if (getEventCalendar() == null) {
            createCalendar();
        }

        // remove all events
        getEventCalendar().getComponents()
                .removeAll(getEventCalendar().getComponents().getComponents(Component.VEVENT));

        // add event exception
        getEventCalendar().getComponents().add(event);
    }

    @Override
    public Trigger getDisplayAlarmTrigger() {
        VEvent event = this.getEvent();
        if (event == null) {
            return null;
        }
        VAlarm alarm = getDisplayAlarm(event);
        if (alarm == null) {
            return null;
        }
        return (Trigger) alarm.getProperties().getProperty(Property.TRIGGER);
    }

    /**
     * Gets master stamp.
     * 
     * @return The event stamp.
     */
    @Override
    public EventStamp getMasterStamp() {
        NoteItem note = (NoteItem) getItem();
        return MockEventStamp.getStamp(note.getModifies());
    }

    /**
     * Return EventExceptionStamp from Item
     * 
     * @param item The item.
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }

   
    /**
     * Copy.
     * 
     * @return The stamp.
     */
    @Override
    public Stamp copy() {
        EventExceptionStamp stamp = new MockEventExceptionStamp();

        // Need to copy Calendar
        try {
            stamp.setEventCalendar(new Calendar(getEventCalendar()));
        } catch (Exception e) {
            throw new CosmoException("Cannot copy calendar", e);
        }

        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
