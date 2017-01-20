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
package org.unitedinternet.cosmo.model.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.unitedinternet.cosmo.calendar.Instance;
import org.unitedinternet.cosmo.calendar.InstanceList;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;

/**
 * Provides api for post-processing a list of filtered items
 * to take into account recurrence expansion and including
 * master items, etc.
 */
public class ItemFilterPostProcessor {
     
    /**
     * Because a timeRange query requires two passes: one to get the list
     * of possible events that occur in the range, and one 
     * to expand recurring events if necessary.
     * This is required because we only index a start and end
     * for the entire recurrence series, and expansion is required to determine
     * if the event actually occurs, and to return individual occurences.
     * @param results The results.
     * @param itemFilter The item filter.
     * @return The items.
     */
    public Set<Item> processResults(Set<Item> results, ItemFilter itemFilter) {
        boolean hasTimeRangeFilter = false;
        boolean includeMasterInResults = true;
        boolean doTimeRangeSecondPass = true;
        
        HashSet<Item> processedResults = new HashSet<Item>();
        EventStampFilter eventFilter = (EventStampFilter) itemFilter.getStampFilter(EventStampFilter.class);
        
        
        if(eventFilter!=null) {
            // does eventFilter have timeRange filter?
            hasTimeRangeFilter = (eventFilter.getPeriod() != null);
        }
        
        // When expanding recurring events do we include the master item in 
        // the results, or just the expanded occurrences/modifications
        if(hasTimeRangeFilter && "false".equalsIgnoreCase(itemFilter
                .getFilterProperty(EventStampFilter.PROPERTY_INCLUDE_MASTER_ITEMS))) {
            includeMasterInResults = false;
        } 
        
        // Should we do a second pass to expand recurring events to determine
        // if a recurring event actually occurs in the time-range specified,
        // or should we just return the recurring event without double-checking.
        if (hasTimeRangeFilter && "false".equalsIgnoreCase(itemFilter
                 .getFilterProperty(EventStampFilter.PROPERTY_DO_TIMERANGE_SECOND_PASS))) {
            doTimeRangeSecondPass = false;
        }
        
        for(Item item: results) {
            
            // If item is not a note, then nothing to do
            if(!(item instanceof NoteItem)) {
                processedResults.add(item);
                continue;
            }
            
            NoteItem note = (NoteItem) item;
            
            // If note is a modification then add both the modification and the 
            // master.
            if(note.getModifies()!=null) {
                processedResults.add(note);
                if (includeMasterInResults) {
                    processedResults.add(note.getModifies());
                }
            } 
            // If filter doesn't have a timeRange, then we are done
            else if(!hasTimeRangeFilter) {
                processedResults.add(note);
            }
            else {
                processedResults.addAll(processMasterNote(note, eventFilter,
                        includeMasterInResults, doTimeRangeSecondPass));
            }
        }
        
        return processedResults;
    }
    
    /**
     * Process master note.
     * @param note The note item.
     * @param filter The event stamp filter.
     * @param includeMasterInResults boolean.
     * @param doTimeRangeSecondPass boolean
     * @return content item.
     */
    private Collection<ContentItem> processMasterNote(NoteItem note,
            EventStampFilter filter, boolean includeMasterInResults,
            boolean doTimeRangeSecondPass) {
        EventStamp eventStamp = (EventStamp) note.getStamp(EventStamp.class);
        ArrayList<ContentItem> results = new ArrayList<ContentItem>();

        // If the event is not recurring or the filter is configured
        // to not do a second pass then just return the note
        if (!eventStamp.isRecurring() || !doTimeRangeSecondPass) {
            results.add(note);
            return results;
        }

        // Otherwise, expand the recurring item to determine if it actually
        // occurs in the time range specified
        RecurrenceExpander expander = new RecurrenceExpander();
        InstanceList instances = expander.getOcurrences(eventStamp.getEvent(),
                eventStamp.getExceptions(), filter.getPeriod().getStart(),
                filter.getPeriod().getEnd(), filter.getTimezone());

        // If recurring event occurs in range, add master unless the filter
        // is configured to not return the master
        if (instances.size() > 0 && includeMasterInResults) {
            results.add(note);
        }
        
        // If were aren't expanding, then return
        if (filter.isExpandRecurringEvents() == false) {
            return results;
        }
        
        // Otherwise, add an occurence item for each occurrence
        for (Iterator<Entry<String, Instance>> it = instances.entrySet()
                .iterator(); it.hasNext();) {
            Entry<String, Instance> entry = it.next();

            // Ignore overrides as they are separate items that should have
            // already been added
            if (entry.getValue().isOverridden() == false) {
                results.add(NoteOccurrenceUtil.createNoteOccurrence(entry.getValue().getRid(), note));
            }
        }

        return results;
    }
}
