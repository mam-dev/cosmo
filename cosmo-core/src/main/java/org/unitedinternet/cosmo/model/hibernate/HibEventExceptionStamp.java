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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Trigger;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.hibernate.validator.EventException;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

/**
 * Hibernate persistent EventExceptionStamp.
 */
@Entity
@DiscriminatorValue("eventexception")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventExceptionStamp extends HibBaseEventStamp implements EventExceptionStamp {

    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;
    
    public static final String PARAM_OSAF_MISSING = "X-OSAF-MISSING";
    
    /** default constructor */
    public HibEventExceptionStamp() {
    }
    
    public HibEventExceptionStamp(Item item) {
        setItem(item);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "eventexception";
    }
    
    /** Used by the hibernate validator **/
    @EventException
    
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }
    
    @Override
    public VEvent getEvent() {
        return getExceptionEvent();
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#getExceptionEvent()
     */
    public VEvent getExceptionEvent() {
        return (VEvent) getEventCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#setExceptionEvent(net.fortuna.ical4j.model.component.VEvent)
     */
    public void setExceptionEvent(VEvent event) {
        if(getEventCalendar()==null) {
            createCalendar();
        }
        
        // remove all events
        getEventCalendar().getComponents().removeAll(
                getEventCalendar().getComponents().getComponents(Component.VEVENT));
        
        // add event exception
        getEventCalendar().getComponents().add(event);
    }
 
    /**
     * Toggle the event exception anytime parameter.
     * @param isAnyTime True if the event occurs anytime<br/>
     *                  False if the event does not occur anytime</br>
     *                  null if the event should inherit the anyTime
     *                  attribute of the master event.
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
            if(parameter!=null) {
                dtStart.getParameters().remove(parameter);
            }
            
            // "missing" anyTime is represented as X-OSAF-ANYTIME=MISSING
            dtStart.getParameters().add(getInheritedAnyTimeXParam());
        } else {
            super.setAnyTime(isAnyTime);
        }
    }
    
    /**
     * Is the event exception marked as anytime.
     * @return True if the event is an anytime event<br/>
     *         False if it is not an anytime event<br/>
     *         null if the anyTime attribute is "missing", ie inherited
     *         from the master event.
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
        if(!VALUE_MISSING.equals(parameter.getValue())) {
            return Boolean.valueOf(VALUE_TRUE.equals(parameter.getValue()));
        }
        
        return Boolean.FALSE;
    }
    
    
    /**
     * Override to handle "missing" trigger by searching for a
     * custom X-PARAM "X-OSAF-MISSING".  If present, then this
     * trigger is "missing" and null should be returned, since
     * for now we are representing "missing" values using null.
     */
    @Override
    public Trigger getDisplayAlarmTrigger() {
        Trigger trigger =  super.getDisplayAlarmTrigger();
        if(trigger!=null && isMissing(trigger)) {
            return null;
        }
        else {
            return trigger;
        }
    }

    /**
     * Override to handle "missing" trigger by appending a
     * custom X-PARAM "X-OSAF-MISSING".  If present, then this
     * trigger is "missing".  Since a "missing" trigger is
     * represented by a null trigger, whenever a null trigger
     * is set, then a basic trigger property with this custom
     * "X-OSAF-MISSING" param will be created instead.
     */
    @Override
    public void setDisplayAlarmTrigger(Trigger newTrigger) {
        if(newTrigger==null) {
            newTrigger = new Trigger(new Dur("-PT15M"));
            setMissing(newTrigger, true);
        }
        super.setDisplayAlarmTrigger(newTrigger);    
    }

    protected boolean isMissing(Property prop) {
        return prop.getParameters().getParameter(PARAM_OSAF_MISSING) != null;
    }
    
    protected void setMissing(Property prop, boolean missing) {
        Parameter parameter = 
            prop.getParameters().getParameter(PARAM_OSAF_MISSING);
        
        if (missing) {
            if (parameter == null) {
                prop.getParameters().add(
                        new XParameter(PARAM_OSAF_MISSING, VALUE_TRUE));
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
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#getMasterStamp()
     */
    public EventStamp getMasterStamp() {
        NoteItem note = (NoteItem) getItem();
        return HibEventStamp.getStamp(note.getModifies());
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param item
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#copy()
     */
    public Stamp copy() {
        EventExceptionStamp stamp = new HibEventExceptionStamp();
        
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
