/*
 * CalendarService.java, Jun 30, 2014, izein
 * 
 * Copyright (c) 2014 1&1 Internet AG, Karlsruhe. All rights reserved.
 */

/**
 * 
 */
package org.unitedinternet.cosmo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;

/**
 * @author izein
 *
 */
public interface CalendarService extends Service {
    
    /**
     * Find calendar events by time range.
     * 
     * @param collection collection to search
     * @param rangeStart time range start
     * @param rangeEnd time range end
     * @param timeZoneId id for timezone; null if not used.
     * @param expandRecurringEvents if true, recurring events will be expanded
     *        and each occurrence will be returned as a NoteItemOccurrence.
     * @return Set<ContentItem> set ContentItem objects that contain EventStamps that occur
     *         int the given timeRange
     */
    Set<Item> findEvents(CollectionItem collection, Date rangeStart,
            Date rangeEnd, String timeZoneId, boolean expandRecurringEvents);

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
    ContentItem findEventByIcalUid(String uid, CollectionItem collection);

    /**
     * Find events by ical uid.
     * @param collection Calendar.
     * @param uid The event's uid.
     */
    Map<String, List<NoteItem>> findNoteItemsByIcalUid(Item collection, List<String> uid);    
    
    /**
     * splits the Calendar Object received into different calendar Components>
     * 
     * @param calendar Calendar
     * @param User cosmoUser
     * @return Set<ContentItem>
     */
    public Set<ICalendarItem> splitCalendarItems(Calendar calendar, User cosmoUser);

}
