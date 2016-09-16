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
package org.unitedinternet.cosmo.dao.hibernate;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

/**
 * Implementation of CalendarDao using Hibernate persistence objects.
 */
public class CalendarDaoImpl extends AbstractDaoImpl implements CalendarDao {

    private static final Log LOG = LogFactory.getLog(CalendarDaoImpl.class);

    private EntityFactory entityFactory;
    private ItemFilterProcessor itemFilterProcessor = null;
    private EntityConverter entityConverter = new EntityConverter(null);

    /*
     * TODO Note that this method is used for CalDav REPORT and it needs to be properly implemented
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Set<ICalendarItem> findCalendarItems(CollectionItem collection, CalendarFilter filter) {
        try {
            CalendarFilterConverter filterConverter = new CalendarFilterConverter();
            try {
                if (collection instanceof HibCollectionItem) {
                    // Translate CalendarFilter to ItemFilter and execute filter. This does not make sense for external collections which are
                    ItemFilter itemFilter = filterConverter.translateToItemFilter(collection, filter);
                    Set results = itemFilterProcessor.processFilter(itemFilter);
                    return (Set<ICalendarItem>) results;
                }
            } catch (Exception e) {
                /* Set this log message to debug because all iPad requests trigger it and log files get polluted. */
                LOG.debug("Illegal filter item. Only VCALENDAR is supported so far.", e);
            }
            /*
             * Use brute-force method if CalendarFilter can't be translated to an ItemFilter (slower but at least gets
             * the job done).
             */
            Set<ICalendarItem> results = new HashSet<ICalendarItem>();
            Set<Item> itemsToProcess = collection.getChildren();

            /*
             * Optimization: Do a first pass query if possible to reduce the number of items that needs to be examined.
             * Otherwise we have to examine all items.
             */
            /* TODO Left only for historical reasons.
             * ItemFilter firstPassItemFilter = filterConverter.getFirstPassFilter(collection, filter); if
             * (firstPassItemFilter != null) { itemsToProcess = itemFilterProcessor.processFilter(firstPassItemFilter);
             * } else { itemsToProcess = collection.getChildren(); }
             */
            
            CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();

            // Evaluate filter against all calendar items
            for (Item child : itemsToProcess) {

                // only care about calendar items
                if (child instanceof ICalendarItem) {

                    ICalendarItem content = (ICalendarItem) child;
                    Calendar calendar = entityConverter.convertContent(content);

                    if (calendar != null && evaluater.evaluate(calendar, filter)) {
                        results.add(content);
                    }
                }
            }

            return results;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.CalendarDao#findEvents(org.unitedinternet.cosmo.model.CollectionItem,
     * net.fortuna.ical4j.model.DateTime, net.fortuna.ical4j.model.DateTime, boolean)
     */
    public Set<ContentItem> findEvents(CollectionItem collection, Date rangeStart, Date rangeEnd, String timezoneId,
            boolean expandRecurringEvents) {

        // Create a NoteItemFilter that filters by parent
        NoteItemFilter itemFilter = new NoteItemFilter();
        itemFilter.setParent(collection);

        // and EventStamp by timeRange
        EventStampFilter eventFilter = new EventStampFilter();

        if (timezoneId != null) {
            TimeZone timeZone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(timezoneId);
            eventFilter.setTimezone(timeZone);
        }

        eventFilter.setTimeRange(rangeStart, rangeEnd);
        eventFilter.setExpandRecurringEvents(expandRecurringEvents);
        itemFilter.getStampFilters().add(eventFilter);

        try {
            Set results = itemFilterProcessor.processFilter(itemFilter);
            return (Set<ContentItem>) results;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.CalendarDao#findEventByIcalUid(java.lang.String,
     * org.unitedinternet.cosmo.model.CollectionItem)
     */
    public ContentItem findEventByIcalUid(String uid, CollectionItem calendar) {
        try {
            Query hibQuery = getSession().getNamedQuery("event.by.calendar.icaluid");
            hibQuery.setParameter("calendar", calendar);
            hibQuery.setParameter("uid", uid);
            return (ContentItem) hibQuery.uniqueResult();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public ItemFilterProcessor getItemFilterProcessor() {
        return itemFilterProcessor;
    }

    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }

    /**
     * Initializes the DAO, sanity checking required properties and defaulting optional properties.
     */
    public void init() {

        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

        if (entityFactory == null) {
            throw new IllegalStateException("entityFactory is required");
        }

        entityConverter = new EntityConverter(this.entityFactory);
    }

    @Override
    public Set<ICalendarItem> findCalendarEvents(Calendar calendar, User cosmoUser) {
        return entityConverter.convertCalendar(calendar);
    }

    /**
     * @param entityFactory
     *            the entityFactory to set
     */
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

}
