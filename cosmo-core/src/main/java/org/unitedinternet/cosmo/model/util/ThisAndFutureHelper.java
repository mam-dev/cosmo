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
package org.unitedinternet.cosmo.model.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;

import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.calendar.util.Dates;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;

/**
 * Helper class to handle breaking a recurring series to
 * provide "This And Future Items" support.  A "This And Future"
 * change to a recurring event is handled by creating a 
 * new recurring series starting on the selected occurrence
 * date (this) and modifying the existing recurring series to
 * end before the selected occurrence.  If the existing series
 * contains modifications, any modification that occurs after the
 * break has to be removed and added to the new series.
 */
public class ThisAndFutureHelper {
    
    /**
     * Given an existing recurring series and new series, break the
     * existing series at the given occurrence and move all modifications
     * from the existing series that apply to the new series to the 
     * new series.
     * @param oldSeries note representing recurring series to break
     * @param newSeries note representing new series
     * @param occurrence occurrence of old series 
     *        (NoteOccurrence or NoteItem modification) to break old 
     *        series at.
     * @return Set of modifications that need to be removed and added.  
     *         Removals are indicated with isActive==false.
     *         All other active NoteItems are considered additions.
     */
    public Set<NoteItem> breakRecurringEvent(NoteItem oldSeries, NoteItem newSeries, NoteItem occurrence) {
        Date lastRid = null;
        if(occurrence instanceof NoteOccurrence) {
            lastRid = ((NoteOccurrence) occurrence).getOccurrenceDate();
        }
        else {
            EventExceptionStamp ees = StampUtils.getEventExceptionStamp(occurrence);
            if(ees==null) {
                throw new IllegalArgumentException("occurence must have an event stamp");
            }
            lastRid = ees.getRecurrenceId();
        }
        
        return breakRecurringEvent(oldSeries, newSeries, lastRid);
    }
    
    /**
     * Given an existing recurring series and new series, break the
     * existing series at the given date and move all modifications
     * from the existing series that apply to the new series to the 
     * new series.
     * @param oldSeries note representing recurring series to break
     * @param newSeries note representing new series
     * @param lastRecurrenceId date to break the old series at
     * @return Set of modifications that need to be removed and added.  
     *         Removals are indicated with isActive==false.
     *         All other active NoteItems are considered additions.
     */
    public Set<NoteItem> breakRecurringEvent(NoteItem oldSeries, NoteItem newSeries, Date lastRecurrenceId) {
        
        LinkedHashSet<NoteItem> results = new LinkedHashSet<NoteItem>();
        
        HashSet<NoteItem> toRemove = new HashSet<NoteItem>();
        HashSet<NoteItem> toAdd = new HashSet<NoteItem>();
        
        // first break old series by setting UNTIL on RECURs
        modifyOldSeries(oldSeries, lastRecurrenceId);
        
        // get list of modifications that need to be moved
        List<NoteItem> modsToMove = getModificationsToMove(oldSeries, newSeries, lastRecurrenceId);
        
        // move modifications by creating copy
        for(NoteItem modToMove: modsToMove) {
            
            // copy modification
            NoteItem copy = (NoteItem) modToMove.copy();
            copy.setModifies(newSeries);
            copy.setIcalUid(null);
            
            EventExceptionStamp ees =
                StampUtils.getEventExceptionStamp(copy);
           
            ees.setIcalUid(newSeries.getIcalUid());
            
            Date recurrenceId = ees.getRecurrenceId();
           
            copy.setUid(new ModificationUidImpl(newSeries, recurrenceId).toString());
            
            // delete old
            modToMove.setIsActive(false);
            toRemove.add(modToMove);
            
            // add new
            toAdd.add(copy);
        }
        
        // add removals first
        results.addAll(toRemove);
        
        // then additions
        results.addAll(toAdd);
        
        return results;
    }
    
    private void modifyOldSeries(NoteItem oldSeries, Date lastRecurrenceId) {
        EventStamp event = StampUtils.getEventStamp(oldSeries);
      
        // We set the end date to 1 second before the begining of the next day
        java.util.Calendar untilDateCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        untilDateCalendar.setTime(lastRecurrenceId);
        untilDateCalendar.add(java.util.Calendar.DAY_OF_MONTH, -1);
        untilDateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        untilDateCalendar.set(java.util.Calendar.MINUTE, 59);
        untilDateCalendar.set(java.util.Calendar.SECOND, 59);
        Date untilDate = Dates.getInstance(untilDateCalendar.getTime(), lastRecurrenceId);
        
        // UNTIL must be UTC according to spec
        if(untilDate instanceof DateTime) {
            ((DateTime) untilDate).setUtc(true);
        }
        List<Recur> recurs = event.getRecurrenceRules();
        for (Recur recur : recurs) {
            recur.setUntil(untilDate);
        }
        
        // TODO: Figure out what to do with RDATEs
    }
    
    private List<NoteItem> getModificationsToMove(NoteItem oldSeries, NoteItem newSeries, Date lastRecurrenceId) {
        ArrayList<NoteItem> mods = new ArrayList<NoteItem>();
        RecurrenceExpander expander = new RecurrenceExpander();
        EventStamp newEvent = StampUtils.getEventStamp(newSeries);
        Calendar newEventCal = newEvent.getEventCalendar();
        
        Date newStartDate = newEvent.getStartDate();
        long delta = 0;
        
        if(!newStartDate.equals(lastRecurrenceId)) {
            delta = newStartDate.getTime() - lastRecurrenceId.getTime();
        }
           
        // Find all modifications with a recurrenceId that is in the set of
        // recurrenceIds for the new series
        for(NoteItem mod: oldSeries.getModifications()) {
            EventExceptionStamp event = StampUtils.getEventExceptionStamp(mod);
            
            // only interested in mods with event stamps
            if(event==null) {
                continue;
            }
            
            Date recurrenceId = event.getRecurrenceId();
            Date dtStart = event.getStartDate();
            
            // determine if modification start date is "missing", which
            // translates to the recurrenceId being the same as the dtstart
            boolean isDtStartMissing = dtStart.equals(recurrenceId)
                    && event.isAnyTime() == null;
            
            // Account for shift in startDate by calculating a new
            // recurrenceId, dtStart based on the shift.
            if(delta!=0) {
                java.util.Date newRidTime =
                    new java.util.Date(recurrenceId.getTime() + delta);
                recurrenceId = Dates.getInstance(newRidTime, recurrenceId);
                
                // If dtStart is missing then set it to recurrenceId
                if(isDtStartMissing) {
                    dtStart = 
                        Dates.getInstance(recurrenceId, dtStart);
                }
            }
            
            // If modification matches an occurrence in the new series
            // then add it to the list
            if(expander.isOccurrence(newEventCal, recurrenceId)) {
                event.setRecurrenceId(recurrenceId);
                event.setStartDate(dtStart);
                
                // If modification is the start of the series and there 
                // was a time change, then match up the startDate
                if(recurrenceId.equals(newStartDate) && delta!=0) {
                    event.setStartDate(newStartDate);
                }
                
                mods.add(mod);
            } 
        }
        
        return  mods;
    }
    
    
    
    
}
