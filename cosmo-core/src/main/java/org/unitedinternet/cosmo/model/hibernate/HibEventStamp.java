/*
 * Copyright 2006 Open Source Applications Foundation
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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.hibernate.validator.Event;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;


/**
 * Hibernate persistent EventStamp.
 */
@Entity
@DiscriminatorValue("event")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventStamp extends HibBaseEventStamp implements EventStamp {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;
    
    
    /** default constructor */
    public HibEventStamp() {
    }
    
    public HibEventStamp(Item item) {
        this();
        setItem(item);
    }
    
    public String getType() {
        return "event";
    }

    @Override
    public VEvent getEvent() {
        return getMasterEvent();
    }

    /** Used by the hibernate validator **/
    @Event
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }
    
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventStamp#getExceptions()
     */
    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<Component>();
        
        // add all exception events
        NoteItem note = (NoteItem) getItem();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp!=null) {
                exceptions.add(exceptionStamp.getEvent());
            }
        }
        
        return exceptions;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventStamp#getMasterEvent()
     */
    public VEvent getMasterEvent() {
        if(getEventCalendar()==null) {
            return null;
        }
        
        ComponentList<VEvent> events = getEventCalendar().getComponents().getComponents(
                Component.VEVENT);
        
        if(events.size()==0) {
            return null;
        }
        
        return (VEvent) events.get(0);
    }

    /**
     * Return EventStamp from Item
     * @param item
     * @return EventStamp from Item
     */
    public static EventStamp getStamp(Item item) {
        return (EventStamp) item.getStamp(EventStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#copy()
     */
    public Stamp copy() {
        EventStamp stamp = new HibEventStamp();
        
        // Need to copy Calendar, and indexes
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
