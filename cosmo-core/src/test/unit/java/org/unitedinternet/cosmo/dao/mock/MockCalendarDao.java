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
package org.unitedinternet.cosmo.dao.mock;

import java.util.HashSet;
import java.util.Set;

import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;

/**
 * Mock implementation of <code>CalendarDao</code> useful for testing.
 *
 * @see CalendarDao
 * @see CalendarItem
 * @see CalendarEventItem
 * @see CalendarCollectionItem
 */
@SuppressWarnings("unchecked")
public class MockCalendarDao extends MockItemDao implements CalendarDao {

    private CalendarFilter lastCalendarFilter;
    
    /** 
     * Useful for unit tests.
     * @return Calendar filer.
     */
    public CalendarFilter getLastCalendarFilter() {
        return lastCalendarFilter;
    }

    /**
     * The constructor.
     * @param storage The mock dao storage.
     */
    public MockCalendarDao(MockDaoStorage storage) {
        super(storage);
    }

    // CalendarDao methods

  
    /**
     * Find calendar events by filter.
     * NOTE: This impl always returns an empty set, but has the side effect 
     * of setting the last 
     * @param collection
     *            calendar collection to search
     * @param filter
     *            filter to use in search
     * @return set CalendarEventItem objects matching specified
     *         filter.
     */
    public Set<ICalendarItem> findCalendarItems(CollectionItem collection,
                                             CalendarFilter filter) {
        lastCalendarFilter = filter;
        HashSet<ICalendarItem> results = new HashSet<ICalendarItem>();
        CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();
        
        // Evaluate filter against all calendar items
        for (Item child : collection.getChildren()) {
            
            // only care about calendar items
            if (child instanceof ICalendarItem) {
                
                ICalendarItem content = (ICalendarItem) child;
                Calendar calendar = new EntityConverter(null).convertContent(content);
                
                if(calendar!=null) {
                    if (evaluater.evaluate(calendar, filter) == true) {
                        results.add(content);
                    }
                }
            }
        }
        
        return results;

    }

    /**
     * Finds event by Ical Uid.
     * {@inheritDoc}
     * @param uid The uid.
     * @param calendar Collection item.
     * @return content item.
     */
    public ContentItem findEventByIcalUid(String uid, CollectionItem calendar) {
        throw new UnsupportedOperationException();
    }

    /**
     * Finds event.
     * {@inheritDoc}
     * @param collection The collection item.
     * @param rangeStart DateTime.
     * @param rangeEnd DateTime.
     * @param expandRecurringEvents Expand recurring events.
     * @throws UnsupportedOperationException - if something is wrong this exception is thrown.
     */
    public Set<ContentItem> findEvents(CollectionItem collection, Date rangeStart, Date rangeEnd,
            boolean expandRecurringEvents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ICalendarItem> findCalendarEvents(Calendar calendar, User cosmoUser) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Item> findEvents(CollectionItem collection, Date rangeStart, Date rangeEnd, String timeZoneId,
            boolean expandRecurringEvents) {
        throw new UnsupportedOperationException();
    }
    
    
}
