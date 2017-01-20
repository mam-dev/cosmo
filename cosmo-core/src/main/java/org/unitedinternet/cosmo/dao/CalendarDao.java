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
package org.unitedinternet.cosmo.dao;

import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;

import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;

/**
 * Interface for DAO that provides query apis for finding 
 * ContentItems with EventStamps matching certain criteria.
 * 
 */
public interface CalendarDao {

    /**
     * Find calendar event with a specified icalendar uid. The icalendar format
     * requires that an event's uid is unique within a calendar.
     * 
     * @param uid
     *            icalendar uid of calendar event
     * @param collection
     *            collection to search
     * @return calendar event represented by uid and calendar
     */
    public ContentItem findEventByIcalUid(String uid, CollectionItem collection);
    

    /**
     * Find calendar items by calendar filter.  Calendar filter is
     * based on the CalDAV filter element.
     *
     * @param collection
     *            collection to search
     * @param filter
     *            filter to use in search
     * @return set ICalendar objects that match specified
     *         filter.
     */
    public Set<ICalendarItem> findCalendarItems(CollectionItem collection,
                                             CalendarFilter filter);
        
    
    /**
     * Find calendar events by time range.
     *
     * @param collection
     *            collection to search
     * @param rangeStart time range start
     * @param rangeEnd time range end
     * @param timeZoneId id for timezone; null if not used.  
     * @param expandRecurringEvents if true, recurring events will be expanded
     *        and each occurrence will be returned as a NoteItemOccurrence.
     * @return set ContentItem objects that contain EventStamps that occur
     *         int the given timeRange.
     */
    public Set<Item> findEvents(CollectionItem collection,
                                             Date rangeStart, Date rangeEnd, String timeZoneId,
                                             boolean expandRecurringEvents);
    
    
    /**
     * 
     * @param calendar Calendar
     * @param User cosmoUser
     * @return Set<ContentItem>
     */
    public Set<ICalendarItem> findCalendarEvents(Calendar calendar, User cosmoUser);

}
