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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Trigger;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.hibernate.validator.EventException;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

/**
 * Represents an event exception.
 */
public class MockEventExceptionStamp extends MockBaseEventStamp implements
        java.io.Serializable, EventExceptionStamp {

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
     * @param item The item.
     */
    public MockEventExceptionStamp(Item item) {
        setItem(item);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#getType()
     */
    /**
     * Gets type.
     * @return The type.
     */
    public String getType() {
        return "eventexception";
    }
    
    /** 
     * Used by the hibernate validator.
     * @return The calendar.
     */
    @EventException
    private Calendar getValidationCalendar() {
        return getEventCalendar();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#getEvent()
     */
    /**
     * Gets event.
     * @return The event.
     */
    @Override
    public VEvent getEvent() {
        return getExceptionEvent();
    }
     
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#getExceptionEvent()
     */
    /**
     * Gets exception event.
     * @return The event.
     */
    public VEvent getExceptionEvent() {
        return (VEvent) getEventCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#setExceptionEvent(net.fortuna.ical4j.model.component.VEvent)
     */
    /**
     * Sets exception event.
     * @param event The event.
     */
    public void setExceptionEvent(VEvent event) {
        if (getEventCalendar()==null) {
            createCalendar();
        }
        
        // remove all events
        getEventCalendar().getComponents().removeAll(
                getEventCalendar().getComponents().getComponents(Component.VEVENT));
        
        // add event exception
        getEventCalendar().getComponents().add(event);
    }
 
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#setAnyTime(java.lang.Boolean)
     */
    /**
     * Sets any time.
     * @param isAnyTime Boolean.
     */
    @Override
    public void setAnyTime(Boolean isAnyTime) {
        // Interpret null as "missing" anyTime, meaning inherited from master
        if(isAnyTime==null) {
            DtStart dtStart = getEvent().getStartDate();
            if (dtStart == null) {
                throw new IllegalStateException("event has no start date");
            }
            Parameter parameter = dtStart.getParameters().getParameter(
                    PARAM_X_OSAF_ANYTIME);
            if (parameter != null) {
                dtStart.getParameters().remove(parameter);
            }
            
            // "missing" anyTime is represented as X-OSAF-ANYTIME=MISSING
            dtStart.getParameters().add(getInheritedAnyTimeXParam());
        } else {
            super.setAnyTime(isAnyTime);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#isAnyTime()
     */
    /**
     * Is any time.
     * @return boolean.
     */
    @Override
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
     
        // return null for "missing" anyTime
        if (VALUE_MISSING.equals(parameter.getValue())) {
            return null;
        }

        return Boolean.valueOf(VALUE_TRUE.equals(parameter.getValue()));
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#getDisplayAlarmTrigger()
     */
    @Override
    public Trigger getDisplayAlarmTrigger() {
        Trigger trigger =  super.getDisplayAlarmTrigger();
        if (trigger!=null && isMissing(trigger)) {
            return null;
        }
        else {
            return trigger; 
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#setDisplayAlarmTrigger
     * (net.fortuna.ical4j.model.property.Trigger)
     */
    @Override
    public void setDisplayAlarmTrigger(Trigger newTrigger) {
        if(newTrigger==null) {
            newTrigger = new Trigger(new Dur("-PT15M"));
            setMissing(newTrigger, true);
        }
        super.setDisplayAlarmTrigger(newTrigger);    
    }

    /**
     * 
     * @param prop The property.
     * @return Id the property is missing or not.
     */
    protected boolean isMissing(Property prop) {
        Parameter parameter = 
            prop.getParameters().getParameter(PARAM_OSAF_MISSING);
        return (parameter!=null);
    }
    
    /**
     * Sets missing.
     * @param prop The property.
     * @param missing boolean.
     */
    protected void setMissing(Property prop, boolean missing) {
        Parameter parameter = 
            prop.getParameters().getParameter(PARAM_OSAF_MISSING);
        
        if (missing) {
            if (parameter == null) {
                prop.getParameters().add(new XParameter(PARAM_OSAF_MISSING, VALUE_TRUE));
            }
        } else {
            if (parameter != null) {
                prop.getParameters().remove(parameter);
            }
        }
    }
    
    private Parameter getInheritedAnyTimeXParam() {
        return new XParameter(PARAM_X_OSAF_ANYTIME, VALUE_MISSING);
    }

    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceEventExceptionStamp#getMasterStamp()
     */
    /**
     * Gets master stamp.
     * @return The event stamp.
     */
    public EventStamp getMasterStamp() {
        NoteItem note = (NoteItem) getItem();
        return MockEventStamp.getStamp(note.getModifies());
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param item The item.
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#copy()
     */
    /**
     * Copy.
     * @return The stamp.
     */
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
