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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.external.UuidExternalGenerator;
import org.unitedinternet.cosmo.dao.subscription.UuidSubscriptionGenerator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.service.CalendarService;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

/**
 * 
 * Standard implementation of <code>CalendarService</code>.
 * 
 * @author izein
 *
 */
@Service
public class StandardCalendarService implements CalendarService {

    private static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Autowired
    private CalendarDao calendarDao;

    @Autowired
    private ContentDao contentDao;

    @Override
    public Set<Item> findEvents(CollectionItem collection, Date rangeStart, Date rangeEnd, String timeZoneId,
            boolean expandRecurringEvents) {
        Set<Item> resultSet = new HashSet<>();
        String uid = collection.getUid();
        if (UuidExternalGenerator.get().containsUuid(uid) || UuidSubscriptionGenerator.get().containsUuid(uid)) {
            NoteItemFilter filter = new NoteItemFilter();
            filter.setParent(collection);
            EventStampFilter eventFilter = new EventStampFilter();
            eventFilter.setTimeRange(rangeStart, rangeEnd);
            if (timeZoneId != null) {
                TimeZone timezone = TIMEZONE_REGISTRY.getTimeZone(timeZoneId);
                eventFilter.setTimezone(timezone);
            }
            filter.getStampFilters().add(eventFilter);
            Set<Item> externalItems = this.contentDao.findItems(filter);
            if (externalItems != null) {
                resultSet.addAll(externalItems);
            }
        } else {
            Set<Item> internalItems = calendarDao.findEvents(collection, rangeStart, rangeEnd, timeZoneId,
                    expandRecurringEvents);
            resultSet.addAll(internalItems);
        }
        return resultSet;
    }

    @Override
    public ContentItem findEventByIcalUid(String uid, CollectionItem collection) {
        return calendarDao.findEventByIcalUid(uid, collection);
    }

    @Override
    public Map<String, List<NoteItem>> findNoteItemsByIcalUid(Item collection, List<String> uid) {
        Map<String, List<NoteItem>> itemsMap = new HashMap<>();

        if (!(collection instanceof CollectionItem)) {
            return itemsMap;
        }
        CollectionItem collectionItem = (CollectionItem) collection;
        List<NoteItem> items = new ArrayList<>();
        for (String id : uid) {
            ContentItem item = calendarDao.findEventByIcalUid(id, collectionItem);
            if (item != null) {
                items.add((NoteItem) item);
            }
        }
        itemsMap.put(collection.getName(), items);
        return itemsMap;
    }

    @Override
    public Set<ICalendarItem> splitCalendarItems(Calendar calendar, User cosmoUser) {
        return calendarDao.findCalendarEvents(calendar, cosmoUser);
    }

    @Override
    public void init() {
        if (calendarDao == null || contentDao == null) {
            throw new IllegalStateException("calendarDao and contentDao properties must not be null");
        }
    }

    @Override
    public void destroy() {

    }

    /** */
    public void setCalendarDao(CalendarDao dao) {
        this.calendarDao = dao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }
}
