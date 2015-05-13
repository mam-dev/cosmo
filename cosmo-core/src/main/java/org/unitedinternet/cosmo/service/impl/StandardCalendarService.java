/*
 * StandardCalendarService.java, Jun 30, 2014, izein
 * 
 * Copyright (c) 2014 1&1 Internet AG, Karlsruhe. All rights reserved.
 */

/**
 * 
 */
package org.unitedinternet.cosmo.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;

import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.CalendarService;

/**
 * 
 * Standard implementation of <code>CalendarService</code>.
 * 
 * @author izein
 *
 */
public class StandardCalendarService implements CalendarService {

    private CalendarDao calendarDao;
    
    @Override
    public Set<ContentItem> findEvents(CollectionItem collection,
            Date rangeStart, Date rangeEnd, String timeZoneId,
            boolean expandRecurringEvents) {
        return calendarDao.findEvents(collection, rangeStart, rangeEnd, timeZoneId, expandRecurringEvents);
    }

    @Override
    public ContentItem findEventByIcalUid(String uid, CollectionItem collection) {
        return calendarDao.findEventByIcalUid(uid, collection);
    }

    @Override
    public Map<String, List<NoteItem>> findNoteItemsByIcalUid(Item collection, List<String> uid) {
        Map<String, List<NoteItem>> itemsMap = new HashMap<>();
        
        if(! (collection instanceof CollectionItem) ){
            return itemsMap;
        }
        CollectionItem collectionItem = (CollectionItem) collection;
        List<NoteItem> items = new ArrayList<>();
        for (String id : uid) {
            ContentItem item = calendarDao.findEventByIcalUid(id, collectionItem);
            if (item != null) {
                items.add((NoteItem)item);
            }
        }
        itemsMap.put(collection.getName(), items);
        return itemsMap;
    }

    @Override
    public Set<ICalendarItem> splitCalendarItems(Calendar calendar,
            User cosmoUser) {
        return calendarDao.findCalendarEvents(calendar, cosmoUser);
    }
    
    @Override
    public void init() {
        if (calendarDao == null) {
            throw new IllegalStateException("calendarDao must not be null");
        }
    }

    @Override
    public void destroy() {

    }

    /** */
    public void setCalendarDao(CalendarDao dao) {
        calendarDao = dao;
    }

}
