/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.TaskStamp;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

/**
 * A component that converts iCalendar objects to entities and vice versa.
 * Often this is not a straight one-to-one mapping, because recurring
 * iCalendar events are modeled as multiple events in a single
 * {@link Calendar}, whereas recurring items are modeled as a master
 * {@link NoteItem} with zero or more {@link NoteItem} modifications and
 * potentially also {@link NoteOccurrence}s.
 */
@org.springframework.stereotype.Component
public class EntityConverter { 
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private EntityFactory entityFactory;
    
    public static final String X_OSAF_STARRED = "X-OSAF-STARRED";
    
    /**
     * Constructor.
     * @param entityFactory The entity factory.
     */
    public EntityConverter(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    /**
     * Converts a single calendar containing many different
     * components and component types into a set of
     * {@link ICalendarItem}.
     * 
     * @param calendar calendar containing any number and type
     *        of calendar components
     * @return set of ICalendarItems
     */
    public Set<ICalendarItem> convertCalendar(Calendar calendar) {
        Set<ICalendarItem> items = new LinkedHashSet<ICalendarItem>();
        for(CalendarContext cc: splitCalendar(calendar)) {
            if(cc.type.equals(Component.VEVENT)) {
                items.addAll(convertEventCalendar(cc.calendar));
            }
            else if(cc.type.equals(Component.VTODO)) {
                items.add(convertTaskCalendar(cc.calendar));
            }
            else if(cc.type.equals(Component.VJOURNAL)) {
                items.add(convertJournalCalendar(cc.calendar));
            }
            else if(cc.type.equals(Component.VFREEBUSY)) {
                items.add(convertFreeBusyCalendar(cc.calendar));
            }
        }
        
        return items;
    }
    
    /**
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     * <p>
     * The provided note corresponds to the recurrence master or, for
     * non-recurring items, the single event in the calendar. The result set
     * includes both the master note as well as a modification note for
     * exception event in the calendar.
     * </p>
     * <p>
     * If the master note does not already have a UUID or an event stamp, one
     * is assigned to it. A UUID is assigned because any modification items
     * that are created require the master's UUID in order to construct
     * their own.
     * </p>
     * <p>
     * If the given note is already persistent, and the calendar does not
     * contain an exception event matching an existing modification, that
     * modification is set inactive. It is still returned in the result set.
     * </p>
     * @param note The note item.
     * @param calendar The calendar.
     * @return set note item.
     */
    public Set<NoteItem> convertEventCalendar(NoteItem note, Calendar calendar) {
        EventStamp eventStamp = (EventStamp) note.getStamp(EventStamp.class);
        
        if (eventStamp == null) {
            eventStamp = entityFactory.createEventStamp(note);
            note.addStamp(eventStamp);
        }

        if (note.getUid() == null) {
            note.setUid(entityFactory.generateUid());
        }

        updateEventInternal(note, calendar);

        LinkedHashSet<NoteItem> items = new LinkedHashSet<NoteItem>();
        items.add(note);

        // add modifications to set of items
        for(Iterator<NoteItem> it = note.getModifications().iterator(); it.hasNext();) {
            NoteItem mod = it.next();
            items.add(mod);
        }

        return items;
    }
    
    /**
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     * @param calendar The calendar.
     * @return The convertion.
     */
    public Set<NoteItem> convertEventCalendar(Calendar calendar) {
        NoteItem note = entityFactory.createNote();
        note.setUid(entityFactory.generateUid());
        setBaseContentAttributes(note);
        return convertEventCalendar(note, calendar);
    }
    
    /**
     * Convert calendar containing single VJOURNAL into NoteItem
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public NoteItem convertJournalCalendar(Calendar calendar) {
        NoteItem note = entityFactory.createNote();
        note.setUid(entityFactory.generateUid());
        setBaseContentAttributes(note);
        return convertJournalCalendar(note, calendar);
    }
    
    /**
     * Update existing NoteItem with calendar containing single VJOURNAL
     * @param note note to update
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public NoteItem convertJournalCalendar(NoteItem  note, Calendar calendar) {
        
        VJournal vj = (VJournal) getMasterComponent(calendar.getComponents(Component.VJOURNAL));
        setCalendarAttributes(note, vj);
        return note;
    }
    
    /**
     * Convert calendar containing single VTODO into NoteItem
     * 
     * @param calendar
     *            calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public NoteItem convertTaskCalendar(Calendar calendar) {
        NoteItem note = entityFactory.createNote();
        note.setUid(entityFactory.generateUid());
        setBaseContentAttributes(note);
        return convertTaskCalendar(note, calendar);
    }
    
    /**
     * Convert calendar containing single VTODO into NoteItem
     * 
     * @param note
     *            note to update
     * @param calendar
     *            calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public NoteItem convertTaskCalendar(NoteItem  note, Calendar calendar) {
        
        note.setTaskCalendar(calendar);
        VToDo todo = (VToDo) getMasterComponent(calendar.getComponents(Component.VTODO));
        
        setCalendarAttributes(note, todo);
        
        return note;
    }
    
    /**
     * Convert calendar containing single VFREEBUSY into FreeBusyItem
     * @param calendar calendar containing VFREEBUSY
     * @return FreeBusyItem representation of VFREEBUSY
     */
    public FreeBusyItem convertFreeBusyCalendar(Calendar calendar) {
        FreeBusyItem freeBusy = entityFactory.createFreeBusy();
        freeBusy.setUid(entityFactory.generateUid());
        setBaseContentAttributes(freeBusy);
        return convertFreeBusyCalendar(freeBusy, calendar);
    }
    
    /**
     * Convert calendar containing single VFREEBUSY into FreeBusyItem
     * @param freeBusy freebusy to update
     * @param calendar calendar containing VFREEBUSY
     * @return FreeBusyItem representation of VFREEBUSY
     */
    public FreeBusyItem convertFreeBusyCalendar(FreeBusyItem freeBusy, Calendar calendar) {
       
        freeBusy.setFreeBusyCalendar(calendar);
        VFreeBusy vfb = (VFreeBusy) getMasterComponent(calendar.getComponents(Component.VFREEBUSY));
        setCalendarAttributes(freeBusy, vfb);
        
        return freeBusy;
    }

    /**
     * Returns an icalendar representation of a calendar collection.  
     * @param collection calendar collection
     * @return icalendar representation of collection
     */
    public Calendar convertCollection(CollectionItem collection) {
        
        // verify collection is a calendar
        CalendarCollectionStamp ccs = StampUtils
                .getCalendarCollectionStamp(collection);

        if (ccs == null) {
            return null;
        }
        
        Calendar calendar = ICalendarUtils.createBaseCalendar();

        // extract the supported calendar components for each child item and
        // add them to the collection calendar object.
        // index the timezones by tzid so that we only include each tz
        // once. if for some reason different calendar items have
        // different tz definitions for a tzid, *shrug* last one wins
        // for this same reason, we use a single calendar builder/time
        // zone registry.
        Map<String, CalendarComponent> tzIdx = new HashMap<>();
        
        for (Item item: collection.getChildren()) {
           if (!(item instanceof ContentItem)) {
               continue;
           }
           
           ContentItem contentItem = (ContentItem) item;
           Calendar childCalendar = convertContent(contentItem);
           
           // ignore items that can't be converted
           if (childCalendar == null) {
               continue;
           }
           
           // index VTIMEZONE and add all other components
           for (Object obj: childCalendar.getComponents()) {
               CalendarComponent comp = (CalendarComponent)obj; 
               if(Component.VTIMEZONE.equals(comp.getName())) {
                   Property tzId = comp.getProperties().getProperty(Property.TZID);
                   if (! tzIdx.containsKey(tzId.getValue())) {
                       tzIdx.put(tzId.getValue(), comp);
                   }
               } else {
                   calendar.getComponents().add(comp);
               }
           }
        }
        
        // add VTIMEZONEs
        for (CalendarComponent comp: tzIdx.values()) {
            calendar.getComponents().add(0,comp);
        }
       
        return calendar;
    }
    
    /**
     * Returns a calendar representing the item.
     * <p>
     * If the item is a {@link NoteItem}, delegates to
     * {@link #convertNote(NoteItem)}. If the item is a {@link ICalendarItem},
     * delegates to {@link ICalendarItem#getFullCalendar()}. Otherwise,
     * returns null.
     * @param item The content item.
     * @return The calendar.
     * </p>
     */
    public Calendar convertContent(ContentItem item) {

        if(item instanceof NoteItem) {
            return convertNote((NoteItem) item);
        }
        else if(item instanceof FreeBusyItem) {
            return convertFreeBusyItem((FreeBusyItem) item);
        }
        else if(item instanceof AvailabilityItem) {
            return convertAvailability((AvailabilityItem) item);
        }

        return null;
    }

    /**
     * Returns a calendar representing the note.
     * <p>
     * If the note is a modification, returns null. If the note has an event
     * stamp, returns a calendar containing the event and any exceptions. If
     * the note has a task stamp, returns a calendar containing the task.
     * Otherwise, returns a calendar containing a journal.
     * </p>
     * @param note The note item.
     * @return calendar The calendar.
     */
    public Calendar convertNote(NoteItem note) {

        // must be a master note
        if (note.getModifies()!=null) {
            return null;
        }

        EventStamp event = StampUtils.getEventStamp(note);
        if (event!=null) {
            return getCalendarFromEventStamp(event);
        }

        return getCalendarFromNote(note);
    }
    
    /**
     * Converts FreeBusy item.
     * @param freeBusyItem The freeBusy item.
     * @return The calendar.
     */
    public Calendar convertFreeBusyItem(FreeBusyItem freeBusyItem) {
        return freeBusyItem.getFreeBusyCalendar();
    }
    
    /**
     * Converts availability.
     * @param availability The availability item.
     * @return The calendar.
     */
    public Calendar convertAvailability(AvailabilityItem availability) {
        return availability.getAvailabilityCalendar();
    }
   
    /**
     * Gets calendar from note.
     * @param note The note item.
     * @return The calendar.
     */
    protected Calendar getCalendarFromNote(NoteItem note) {
        // Start with existing calendar if present
        Calendar calendar = note.getTaskCalendar();
        
        // otherwise, start with new calendar
        if (calendar == null) {
            calendar = ICalendarUtils.createBaseCalendar(new VToDo());
        }
        else {
            // use copy when merging calendar with item properties
            calendar = CalendarUtils.copyCalendar(calendar);
        }
        
        // merge in displayName,body
        VToDo task = (VToDo) calendar.getComponent(Component.VTODO);
        mergeCalendarProperties(task, note);
        
        return calendar;
    }
    
    /**
     * gets calendar from event stamp.
     * @param stamp The event stamp.
     * @return The calendar.
     */
    protected Calendar getCalendarFromEventStamp(EventStamp stamp) {
        Calendar masterCal = CalendarUtils.copyCalendar(stamp.getEventCalendar());
        if (masterCal == null) {
            return null;
        }
       
        // the master calendar might not have any events; for
        // instance, a client might be trying to save a VTODO
        if (masterCal.getComponents(Component.VEVENT).isEmpty()) {
            return masterCal;
        }

        VEvent masterEvent = (VEvent) masterCal.getComponents(Component.VEVENT).get(0);
        VAlarm masterAlarm = getDisplayAlarm(masterEvent);
        String masterLocation = stamp.getLocation();
        
        // build timezone map that includes all timezones in master calendar
        ComponentList<VTimeZone> timezones = masterCal.getComponents(Component.VTIMEZONE);
        HashMap<String, VTimeZone> tzMap = new HashMap<String, VTimeZone>();
        for(VTimeZone vtz : timezones) {             
            tzMap.put(vtz.getTimeZoneId().getValue(), vtz);
        }
        
        // check start/end date tz is included, and add if it isn't
        String tzid = getTzId(stamp.getStartDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            if(tz!=null) {
                VTimeZone vtz = tz.getVTimeZone();
                masterCal.getComponents().add(0, vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        tzid = getTzId(stamp.getEndDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            if(tz!=null) {
                VTimeZone vtz = tz.getVTimeZone();
                masterCal.getComponents().add(0, vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        // merge item properties to icalendar props
        mergeCalendarProperties(masterEvent, (NoteItem) stamp.getItem());
        
        // bug 9606: handle displayAlarm with no trigger by not including
        // in exported icalendar
        if(masterAlarm!=null && stamp.getDisplayAlarmTrigger()==null) {
            masterEvent.getAlarms().remove(masterAlarm);
            masterAlarm = null;
        }
        
        // If event is not recurring, skip all the event modification
        // processing
        if (!stamp.isRecurring()) {
            return masterCal;
        }
        
        // add all exception events
        NoteItem note = (NoteItem) stamp.getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<String, VEvent>();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            
            // if modification isn't stamped as an event then ignore
            if (exceptionStamp==null) {
                continue;
            }
            
            // Get exception event copy
            VEvent exceptionEvent = (VEvent) CalendarUtils
                    .copyComponent(exceptionStamp.getExceptionEvent());

            // ensure DURATION or DTEND exists on modfication
            if (ICalendarUtils.getDuration(exceptionEvent) == null) {
                ICalendarUtils.setDuration(exceptionEvent, ICalendarUtils
                        .getDuration(masterEvent));
            }
            
            // merge item properties to icalendar props
            mergeCalendarProperties(exceptionEvent, exception);
          
            // check for inherited anyTime
            if(exceptionStamp.isAnyTime()==null) {
                DtStart modDtStart = exceptionEvent.getStartDate();
                // remove "missing" value
                modDtStart.getParameters().remove(modDtStart.getParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME));
                // add inherited value
                if(stamp.isAnyTime()) {
                    modDtStart.getParameters().add(getAnyTimeXParam());
                }
            }
                
            // Check for inherited displayAlarm, which is represented
            // by a valarm with no TRIGGER
            VAlarm displayAlarm = getDisplayAlarm(exceptionEvent);
            if(displayAlarm !=null && exceptionStamp.getDisplayAlarmTrigger()==null) {
                exceptionEvent.getAlarms().remove(displayAlarm);
                if (masterAlarm != null) {
                    exceptionEvent.getAlarms().add(masterAlarm);
                }
            }
            
            // Check for inherited LOCATION which is represented as null LOCATION
            // If inherited, and master event has a LOCATION, then add it to exception
            if(exceptionStamp.getLocation()==null && masterLocation!=null) {
                ICalendarUtils.setLocation(masterLocation, exceptionEvent);
            }
            
            sortedMap.put(exceptionStamp.getRecurrenceId().toString(), exceptionEvent);
            
            // verify that timezones are present for exceptions, and add if not
            tzid = getTzId(exceptionStamp.getStartDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
                if(tz!=null) {
                    VTimeZone vtz = tz.getVTimeZone();
                    masterCal.getComponents().add(0, vtz);
                    tzMap.put(tzid, vtz);
                }
            }
            
            tzid = getTzId(exceptionStamp.getEndDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
                if(tz!=null) {
                    VTimeZone vtz = tz.getVTimeZone();
                    masterCal.getComponents().add(0, vtz);
                    tzMap.put(tzid, vtz);
                }
            }
        }
        
        masterCal.getComponents().addAll(sortedMap.values());
        
        return masterCal;
    }
    
    /**
     * Merges calendar properties.
     * @param event The event.
     * @param note The note item.
     */
    private void mergeCalendarProperties(VEvent event, NoteItem note) {
        //summary = displayName
        //description = body
        //uid = icalUid
        //dtstamp = clientModifiedDate/modifiedDate
        
        boolean isMod = note.getModifies()!=null;
        if (isMod) {
            ICalendarUtils.setUid(note.getModifies().getIcalUid(), event);
        }
        else {
            ICalendarUtils.setUid(note.getIcalUid(), event);
        }
        
        // inherited displayName and body should always be serialized
        if (event.getSummary() != null) {
            ICalendarUtils.setSummary(event.getSummary().getValue(), event);
        } else if (note.getDisplayName()==null && isMod) {
            ICalendarUtils.setSummary(note.getModifies().getDisplayName(), event);
        }
        else  {
            ICalendarUtils.setSummary(note.getDisplayName(), event);
        }
        if (event.getDescription() != null) {
            ICalendarUtils.setDescription(event.getDescription().getValue(), event);
        } else if (note.getBody()==null && isMod) {
            ICalendarUtils.setDescription(note.getModifies().getBody(), event);
        } else {
            ICalendarUtils.setDescription(note.getBody(), event);
        }
       
       
        if (note.getClientModifiedDate()!=null) {
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), event);
        }
        else {
            ICalendarUtils.setDtStamp(note.getModifiedDate(), event);
        }
        
        if (StampUtils.getTaskStamp(note) != null) {
            ICalendarUtils.setXProperty(X_OSAF_STARRED, "TRUE", event);
        }
        else {
            ICalendarUtils.setXProperty(X_OSAF_STARRED, null, event);
        }
    }
    
    /**
     * Merges calendar properties.
     * @param task The task.
     * @param note The note item.
     */
    private void mergeCalendarProperties(VToDo task, NoteItem note) {
        //uid = icaluid or uid
        //summary = displayName
        //description = body
        //dtstamp = clientModifiedDate/modifiedDate
        //completed = triageStatus==DONE/triageStatusRank
        
        String icalUid = note.getIcalUid();
        if (icalUid==null) {
            icalUid = note.getUid();
        }
        
        if (note.getClientModifiedDate()!=null) {
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), task);
        }
        else {
            ICalendarUtils.setDtStamp(note.getModifiedDate(), task);
        }
        
        ICalendarUtils.setUid(icalUid, task);
        ICalendarUtils.setSummary(note.getDisplayName(), task);
        ICalendarUtils.setDescription(note.getBody(), task);
        
        // Set COMPLETED/STATUS if triagestatus is DONE
        TriageStatus ts = note.getTriageStatus();
        DateTime completeDate = null;
        if(ts!=null && ts.getCode()==TriageStatus.CODE_DONE) {
            ICalendarUtils.setStatus(Status.VTODO_COMPLETED, task);
            if (ts.getRank() != null) {
                completeDate =  new DateTime(TriageStatusUtil.getDateFromRank(ts.getRank()));
            }
        }
        
        ICalendarUtils.setCompleted(completeDate, task);
        
        if (StampUtils.getTaskStamp(note) != null) {
            ICalendarUtils.setXProperty(X_OSAF_STARRED, "TRUE", task);
        }
        else {
            ICalendarUtils.setXProperty(X_OSAF_STARRED, null, task);
        }
    }
    
    /**
     * Gets display alarm.
     * @param event The event.
     * @return The alarm.
     */
    private VAlarm getDisplayAlarm(VEvent event) {
        for(VAlarm alarm : event.getAlarms()) {
            if (alarm.getProperties().getProperty(Property.ACTION).equals(Action.DISPLAY)) {
                return alarm;
            }
        }
        
        return null;
    }
    
    /**
     * Gets timezone id.
     * @param date The date.
     * @return The id.
     */
    private String getTzId(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            if (dt.getTimeZone()!=null) {
                return dt.getTimeZone().getID();
            }
        }
        
        return null;
    }
    
    /**
     * Gets any time x param.
     * @return The parameter.
     */
    private Parameter getAnyTimeXParam() {
        return new XParameter(ICalendarConstants.PARAM_X_OSAF_ANYTIME, ICalendarConstants.VALUE_TRUE);
    }
    
    /**
     * Updates event internal.
     * @param masterNote The master note.
     * @param calendar The calendar.
     */
    private void updateEventInternal(NoteItem masterNote, Calendar calendar) {
        HashMap<Date, VEvent> exceptions = new HashMap<Date, VEvent>();
        
        Calendar masterCalendar = calendar;
        
        ComponentList<VEvent> vevents = masterCalendar.getComponents().getComponents(
                Component.VEVENT);
        EventStamp eventStamp = StampUtils.getEventStamp(masterNote);

        // get list of exceptions (VEVENT with RECURRENCEID)
        for (VEvent event : vevents) {            
            // make sure event has DTSTAMP, otherwise validation will fail
            if (event.getDateStamp()==null) {
                event.getProperties().add(new DtStamp(new DateTime()));
            }
            if (event.getRecurrenceId() != null) {
                Date recurrenceIdDate = event.getRecurrenceId().getDate();
                exceptions.put(recurrenceIdDate, event);
            }
        }
        
        // Remove all exceptions from master calendar as these
        // will be stored in each NoteItem modification's EventExceptionStamp
        for (Entry<Date, VEvent> entry : exceptions.entrySet()) {
            masterCalendar.getComponents().remove(entry.getValue());
        }

        // Master calendar includes everything in the original calendar minus
        // any exception events (VEVENT with RECURRENCEID)
        eventStamp.setEventCalendar(masterCalendar);
        compactTimezones(masterCalendar);
        
        VEvent event = eventStamp.getEvent();
        
        // verify master event exists
        if (event==null) {
            throw new ModelValidationException("no master calendar component found");
        }
        
        setCalendarAttributes(masterNote, event);
        
        // synchronize exceptions with master NoteItem modifications
        syncExceptions(exceptions, masterNote);
    }

    /**
     * Compact timezones.
     * @param calendar The calendar.
     */
    private void compactTimezones(Calendar calendar) {
        
        if (calendar==null) {
            return;
        }

        // Get list of timezones in master calendar and remove all timezone
        // definitions that are in the registry.  The idea is to not store
        // extra data.  Instead, the timezones will be added to the calendar
        // by the getCalendar() api.
        ComponentList<VTimeZone> timezones = calendar.getComponents(Component.VTIMEZONE);
        List<VTimeZone> toRemove = new ArrayList<>();
        for(VTimeZone vtz : timezones) {
            String tzid = vtz.getTimeZoneId().getValue();
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            //  Remove timezone iff it matches the one in the registry
            if(tz!=null && vtz.equals(tz.getVTimeZone())) {
                toRemove.add(vtz);
            }
        }

        // remove known timezones from master calendar
        calendar.getComponents().removeAll(toRemove);
    }

    /**
     * Sync exceptions.
     * @param exceptions The exceptions.
     * @param masterNote The master note.
     */
    private void syncExceptions(Map<Date, VEvent> exceptions,
                                NoteItem masterNote) {
        for (Entry<Date, VEvent> entry : exceptions.entrySet()) {
            syncException(entry.getValue(), masterNote);
        }

        // remove old exceptions
        for (NoteItem noteItem : masterNote.getModifications()) {
            EventExceptionStamp eventException =
                StampUtils.getEventExceptionStamp(noteItem);
            if (eventException==null || !exceptions.containsKey(eventException.getRecurrenceId())) {
                noteItem.setIsActive(false);
            }
        }
    }

    /**
     * Sync exception.
     * @param event The event.
     * @param masterNote The master note.
     */
    private void syncException(VEvent event, NoteItem masterNote) {
        NoteItem mod =
            getModification(masterNote, event.getRecurrenceId().getDate());

        if (mod == null) {
            // create if not present
            createNoteModification(masterNote, event);
        } else {
            // update existing mod
            updateNoteModification(mod, event);
        }
    }

    /**
     * Gets modification.
     * @param masterNote The master note.
     * @param recurrenceId The reccurence id.
     * @return The note item.
     */
    private NoteItem getModification(NoteItem masterNote,
                                     Date recurrenceId) {
        for (NoteItem mod : masterNote.getModifications()) {
            EventExceptionStamp exceptionStamp =
                StampUtils.getEventExceptionStamp(mod);
            // only interested in mods with event stamp
            if (exceptionStamp == null) {
                continue;
            }
            if (exceptionStamp.getRecurrenceId().equals(recurrenceId)) {
                return mod;
            }
        }

        return null;
    }
    
    /**
     * Creates note modification.
     * @param masterNote masterNote.
     * @event The event.
     */
    private void createNoteModification(NoteItem masterNote, VEvent event) {
        NoteItem noteMod = entityFactory.createNote();
        Calendar exceptionCal = null;
        // a note modification should inherit the calendar product info as its master component.
        if(masterNote.getStamp(EventStamp.class) != null) {
            EventStamp masterStamp = (EventStamp) masterNote.getStamp(EventStamp.class);
            Calendar masterCal = masterStamp.getEventCalendar();
            if(masterCal != null && masterCal.getProductId() != null) {
                exceptionCal = new Calendar();
                exceptionCal.getProperties().add(masterCal.getProductId());
                exceptionCal.getProperties().add(masterCal.getVersion() != null? masterCal.getVersion(): Version.VERSION_2_0);
                exceptionCal.getProperties().add(masterCal.getCalendarScale() != null? masterCal.getCalendarScale(): CalScale.GREGORIAN);
                exceptionCal.getComponents().add(event);
            }
        }

        EventExceptionStamp exceptionStamp =
            entityFactory.createEventExceptionStamp(noteMod);
        exceptionStamp.setEventCalendar(exceptionCal);
        exceptionStamp.setExceptionEvent(event);
        noteMod.addStamp(exceptionStamp);

        noteMod.setUid(new ModificationUidImpl(masterNote, event.getRecurrenceId()
                .getDate()).toString());
        noteMod.setOwner(masterNote.getOwner());
        noteMod.setName(noteMod.getUid());
        
        // copy VTIMEZONEs to front if present
        EventStamp es = StampUtils.getEventStamp(masterNote);
        ComponentList<VTimeZone> vtimezones = es.getEventCalendar().getComponents(Component.VTIMEZONE);
        for( VTimeZone vtimezone : vtimezones) {
            exceptionStamp.getEventCalendar().getComponents().add(0, vtimezone);
        }
        
        setBaseContentAttributes(noteMod);
        noteMod.setLastModifiedBy(masterNote.getLastModifiedBy());
        noteMod.setModifies(masterNote);
        masterNote.addModification(noteMod);
        
        setCalendarAttributes(noteMod, event);
    }

    /**
     * Updates note modification.
     * @param noteMod The note item modified.
     * @param event The event.
     */
    private void updateNoteModification(NoteItem noteMod,
                                        VEvent event) {
        EventExceptionStamp exceptionStamp =
            StampUtils.getEventExceptionStamp(noteMod);
        exceptionStamp.setExceptionEvent(event);
        
        // copy VTIMEZONEs to front if present
        ComponentList<VTimeZone> vtimezones = exceptionStamp.getMasterStamp()
                .getEventCalendar().getComponents(Component.VTIMEZONE);
        for(VTimeZone vtimezone : vtimezones) {
            exceptionStamp.getEventCalendar().getComponents().add(0, vtimezone);
        }
        
        noteMod.setClientModifiedDate(new Date());
        noteMod.setLastModifiedBy(noteMod.getModifies().getLastModifiedBy());
        noteMod.setLastModification(ContentItem.Action.EDITED);
        
        setCalendarAttributes(noteMod, event);
    }
    
    /**
     * Sets base content attributes.
     * @param item The content item.
     */
    private void setBaseContentAttributes(ContentItem item) {
        
        TriageStatus ts = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(ts);

        item.setClientCreationDate(new Date());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(ts);
        item.setLastModification(ContentItem.Action.CREATED);
        
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
    }    

    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param event The event.
     */
    private void setCalendarAttributes(NoteItem note, VEvent event) {
        
        // UID (only set if master)
        if(event.getUid()!=null && note.getModifies()==null) {
            note.setIcalUid(event.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (event.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(event.getSummary()
                    .getValue(), 0, 1024));
        }

        if (event.getDescription() != null) {
            note.setBody(event.getDescription().getValue());
        }

        // look for DTSTAMP
        if(event.getDateStamp()!=null) {
            note.setClientModifiedDate(event.getDateStamp().getDate());
        }
        
        // look for absolute VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(event);
        if (va != null && va.getTrigger()!=null) {
            Trigger trigger = va.getTrigger();
            Date reminderTime = trigger.getDateTime();
            if (reminderTime != null) {
                note.setReminderTime(reminderTime);
            }
        }

        // calculate triage status based on start date
        java.util.Date now =java.util.Calendar.getInstance().getTime();
        Date eventStartDate = event.getStartDate() != null && event.getStartDate().getDate() != null 
        		? event.getStartDate().getDate()
        		:new Date();
        boolean later = eventStartDate.after(now);
        int code = later ? TriageStatus.CODE_LATER : TriageStatus.CODE_DONE;
        
        TriageStatus triageStatus = note.getTriageStatus();
        
        // initialize TriageStatus if not present
        if (triageStatus == null) {
            triageStatus = TriageStatusUtil.initialize(entityFactory
                    .createTriageStatus());
            note.setTriageStatus(triageStatus);
        }

        triageStatus.setCode(code);
        
        // check for X-OSAF-STARRED
        if ("TRUE".equals(ICalendarUtils.getXProperty(X_OSAF_STARRED, event))) {
            TaskStamp ts = StampUtils.getTaskStamp(note);
            if (ts == null) {
                note.addStamp(entityFactory.createTaskStamp());
            }
        }
    }
    
    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param task The task vToDo.
     */
    private void setCalendarAttributes(NoteItem note, VToDo task) {
        
        // UID
        if(task.getUid()!=null) {
            note.setIcalUid(task.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (task.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(task.getSummary()
                    .getValue(), 0, 1024));
        }

        if (task.getDescription() != null) {
            note.setBody(task.getDescription().getValue());
        }

        // look for DTSTAMP
        if (task.getDateStamp() != null) {
            note.setClientModifiedDate(task.getDateStamp().getDate());
        }

        // look for absolute VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(task);
        if (va != null && va.getTrigger()!=null) {
            Trigger trigger = va.getTrigger();
            Date reminderTime = trigger.getDateTime();
           if (reminderTime != null) {
                note.setReminderTime(reminderTime);
           }
        }
        
        // look for COMPLETED or STATUS:COMPLETED
        Completed completed = task.getDateCompleted();
        Status status = task.getStatus();
        TriageStatus ts = note.getTriageStatus();
        
        // Initialize TriageStatus if necessary
        if(completed!=null || Status.VTODO_COMPLETED.equals(status)) {
            if (ts == null) {
                ts = TriageStatusUtil.initialize(entityFactory
                        .createTriageStatus());
                note.setTriageStatus(ts);
            }
            
            // TriageStatus.code will be DONE
            note.getTriageStatus().setCode(TriageStatus.CODE_DONE);
            
            // TriageStatus.rank will be the COMPLETED date if present
            // or currentTime
            if(completed!=null) {
                note.getTriageStatus().setRank(
                        TriageStatusUtil.getRank(completed.getDate().getTime()));
            }
            else {
                note.getTriageStatus().setRank(
                        TriageStatusUtil.getRank(System.currentTimeMillis()));
            }
        }
        
        // check for X-OSAF-STARRED
        if ("TRUE".equals(ICalendarUtils.getXProperty(X_OSAF_STARRED, task))) {
            TaskStamp taskStamp = StampUtils.getTaskStamp(note);
            if (taskStamp == null) {
                note.addStamp(entityFactory.createTaskStamp());
            }
        }
    }
    
    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param journal The VJournal.
     */
    private void setCalendarAttributes(NoteItem note, VJournal journal) {
        // UID
        if(journal.getUid()!=null) {
            note.setIcalUid(journal.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (journal.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(journal.getSummary()
                    .getValue(), 0, 1024));
        }

        if (journal.getDescription() != null) {
            note.setBody(journal.getDescription().getValue());
        }

        // look for DTSTAMP
        if (journal.getDateStamp() != null) {
            note.setClientModifiedDate(journal.getDateStamp().getDate());
        }
    }
    
    /**
     * Sets calendar attributes.
     * @param freeBusy The freeBusy.
     * @param vfb The VFreeBusy.
     */
    private void setCalendarAttributes(FreeBusyItem freeBusy, VFreeBusy vfb) {
        // UID
        if(vfb.getUid()!=null) {
            freeBusy.setIcalUid(vfb.getUid().getValue());
        }
        
        // look for DTSTAMP
        if (vfb.getDateStamp() != null) {
            freeBusy.setClientModifiedDate(vfb.getDateStamp().getDate());
        }
    }
    
    /**
     * gets master component.
     * @param components The component list.
     * @return The component.
     */
    private Component getMasterComponent(ComponentList<? extends Component> components) {
        Iterator<? extends Component> it = components.iterator();
        while(it.hasNext()) {
            Component c = it.next();
            if(c.getProperty(Property.RECURRENCE_ID)==null) {
                return c;
            }
        }
        
        throw new IllegalArgumentException("no master found");
    }
    
    /**
     * Given a Calendar with no VTIMZONE components, go through
     * all other components and add all relevent VTIMEZONES.
     * @param calendar The calendar.
     */
    private void addTimezones(Calendar calendar) {
        ComponentList<CalendarComponent> comps = calendar.getComponents();
        Set<VTimeZone> timezones = new HashSet<VTimeZone>();
        
        for(CalendarComponent comp : comps) {            
            PropertyList<Property> props = comp.getProperties();
            for(Property prop : props) {
                if(prop instanceof DateProperty) {
                    DateProperty dateProp = (DateProperty) prop;
                    Date d = dateProp.getDate();
                    if(d instanceof DateTime) {
                        DateTime dt = (DateTime)d;
                        if(dt.getTimeZone()!=null) {
                            timezones.add(dt.getTimeZone().getVTimeZone());
                        }
                    }
                } else if(prop instanceof DateListProperty) {
                    DateListProperty dateProp = (DateListProperty) prop;
                    if(dateProp.getDates().getTimeZone()!=null) {
                        timezones.add(dateProp.getDates().getTimeZone().getVTimeZone());
                    }
                }
            }
        }
        
        for(VTimeZone vtz: timezones) {
            calendar.getComponents().add(0, vtz);
        }
    }
    
    /**
     * Given a calendar with many different components, split into
     * separate calendars that contain only a single component type
     * and a single UID.
     * @param calendar The calendar.
     * @return The split calendar.
     */
    private CalendarContext[] splitCalendar(Calendar calendar) {
        Vector<CalendarContext> contexts = new Vector<CalendarContext>();
        Set<String> allComponents = new HashSet<String>();
        Map<String, ComponentList<CalendarComponent>> componentMap = new HashMap<>();
        
        ComponentList<CalendarComponent> comps = calendar.getComponents();
        for(CalendarComponent comp : comps) {            
            // ignore vtimezones for now
            if(comp instanceof VTimeZone) {
                continue;
            }
            
            Uid uid = (Uid) comp.getProperty(Property.UID);
            RecurrenceId rid = (RecurrenceId) comp.getProperty(Property.RECURRENCE_ID);
            
            String key = uid.getValue();
            if(rid!=null) {
                key+=rid.toString();
            }
            
            // ignore duplicates
            if(allComponents.contains(key)) {
                continue;
            }
            
            allComponents.add(key);
            
            ComponentList<CalendarComponent> cl = componentMap.get(uid.getValue());
            
            if(cl==null) {
                cl = new ComponentList<>();
                componentMap.put(uid.getValue(), cl);
            }
            
            cl.add(comp);
        }
        
        for(Entry<String, ComponentList<CalendarComponent>> entry : componentMap.entrySet()) {
           
            Component firstComp = (Component) entry.getValue().get(0);
            
            Calendar cal = ICalendarUtils.createBaseCalendar();
            cal.getComponents().addAll(entry.getValue());
            addTimezones(cal);
            
            CalendarContext cc = new CalendarContext();
            cc.calendar = cal;
            cc.type = firstComp.getName();
            
            contexts.add(cc);
        }
        
        return contexts.toArray(new CalendarContext[contexts.size()]);
    }
    

    /**
     * Gets entity factory.
     * @return The entity factory.
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }
    
    /**
     * Container for a calendar containing single component type (can
     * be multiple components if the component is recurring and has
     * modifications), and the component type.
     */
    static class CalendarContext {
        String type;
        Calendar calendar;
    }
}
