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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.hibernate.validator.EventException;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Hibernate persistent EventExceptionStamp.
 */
@Entity
@DiscriminatorValue("eventexception")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventExceptionStamp extends HibBaseEventStamp implements EventExceptionStamp {

    private static final long serialVersionUID = 3992468809776886156L;
    
    
    public HibEventExceptionStamp() {
        //Default constructor.
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
    
    
    @Override
    public VEvent getExceptionEvent() {
        return ICalendarUtils.getEventFrom(this.getEventCalendar());
    }
   
    @Override
    public void setExceptionEvent(VEvent event) {
        Calendar calendar = this.getEventCalendar();
        if (calendar == null) {
            calendar = this.createCalendar();
        }
        // Remove all events
        calendar.getComponents().removeAll(calendar.getComponents().getComponents(Component.VEVENT));

        // Add event exception
        calendar.getComponents().add(event);
        this.setEventCalendar(calendar);
    }

    private Calendar createCalendar() {
        
        NoteItem note = (NoteItem) getItem();
       
        String icalUid = note.getIcalUid();
        if(icalUid==null) {
            // A modifications UID will be the parent's icaluid or uid
    
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
    
        return  ICalendarUtils.createBaseCalendar(new VEvent(), icalUid);
                
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
