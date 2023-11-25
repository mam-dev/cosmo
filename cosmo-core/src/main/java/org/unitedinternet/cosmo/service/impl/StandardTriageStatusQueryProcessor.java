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
package org.unitedinternet.cosmo.service.impl;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TemporalAmountAdapter;
import net.fortuna.ical4j.model.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.filter.*;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryProcessor;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.*;

/**
 * Standard implementation of TriageStatusQueryProcessor that
 * uses NoteItemFilters and custom logic to process a
 * TriageStatus query.
 */
@Component
public class StandardTriageStatusQueryProcessor implements TriageStatusQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardTriageStatusQueryProcessor.class);

    @Autowired
    private ContentDao contentDao;

    public static final Comparator<NoteItem> COMPARE_ASC = new NoteItemTriageStatusComparator(false);
    public static final Comparator<NoteItem> COMPARE_DESC = new NoteItemTriageStatusComparator(true);

    NoteTriageStatusHelper noteTriageStatusHelper = new NoteTriageStatusHelper();

    // Durations used to search forward/backward for recurring events
    // and used to determine time periods that events will be expanded
    // to determine the previous/next occurrence

    // 31 days (1 month)
    private TemporalAmount monthLaterDur = Duration.ofDays(31);
    private TemporalAmount monthDoneDur = Duration.ofDays(-31);

    // 366 days (1 year)
    private TemporalAmount yearLaterDur = Duration.ofDays(366);
    private TemporalAmount yearDoneDur = Duration.ofDays(-366);

    // number of DONE items to return
    private int maxDone = 25;

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.service.triage.TriageStatusQueryProcessor#processTriageStatusQuery
     * (org.unitedinternet.cosmo.model.CollectionItem, java.lang.String, java.util.Date,
     * net.fortuna.ical4j.model.TimeZone)
     */
    /**
     *
     * {@inheritDoc}
     */
    public SortedSet<NoteItem>
    processTriageStatusQuery(CollectionItem collection,
                             TriageStatusQueryContext context) {
        if (context.isAll()) {
            return getAll(collection, context).merge();
        }
        if (context.isDone()) {
            return getDone(collection, context).merge();
        }
        else if (context.isNow()) {
            return getNow(collection, context).merge();
        }
        else if (context.isLater()) {
            return getLater(collection, context).merge();
        }
        else {
            throw new IllegalArgumentException("invalid status: " + context.getTriageStatus());
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    public SortedSet<NoteItem>
    processTriageStatusQuery(NoteItem note,
                             TriageStatusQueryContext context) {
        if (context.isAll()) {
            return noteTriageStatusHelper.getAll(note, context).merge();
        }
        if (context.isDone()) {
            return noteTriageStatusHelper.getDone(note, context).merge();
        }
        else if (context.isNow()) {
            return noteTriageStatusHelper.getNow(note, context).merge();
        }
        else if (context.isLater()) {
            return noteTriageStatusHelper.getLater(note, context).merge();
        }
        else {
            throw new IllegalArgumentException("invalid status: " + context.getTriageStatus());
        }
    }

    /**
     *
     * @param collection CollectionItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    private QueryResult getAll(CollectionItem collection,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        qr.add(getNow(collection, context));
        qr.add(getDone(collection, context));
        qr.add(getLater(collection, context));

        return qr;
    }

    /**
     * NOW Query:<br/>
     *   - Non-recurring with no or null triage status<br/>
     *   - Non-recurring with triage status NOW<br/>
     *   - Modifications with triage status NOW<br/>
     *   - Occurrences whose period overlaps the current point in time
     *   - Modifications with triage status null and whose period
     *     overlaps the current point in time.
     * @param collection CollectionItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    private QueryResult getNow(CollectionItem collection,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        // filter for NOW triage notes
        NoteItemFilter nowFilter =
                getTriageStatusFilter(collection, TriageStatus.CODE_NOW);

        // filter for no (null) triage status
        NoteItemFilter noTriageStatusFilter =
                getTriageStatusFilter(collection, -1);
        noTriageStatusFilter.setIsModification(false);

        // recurring event filter
        NoteItemFilter eventFilter =
                getRecurringEventFilter(collection, context.getPointInTime(),
                        context.getPointInTime(),
                        context.getTimeZone());

        // Add all non-recurring items that are have an explicit NOW triage,
        // modifications with NOW triage, or no triage (null triage)
        ItemFilter[] filters = new ItemFilter[] {
                nowFilter, noTriageStatusFilter
        };
        for(Item item : contentDao.findItems(filters)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);

            // Don't add recurring events
            if(eventStamp==null || !eventStamp.isRecurring()) {
                qr.getResults().add(note);
                // keep track of master
                if(note.getModifies()!=null) {
                    qr.getMasters().add(note.getModifies());
                }
            }
        }

        // Now process recurring events, returning only occurrences that overlap
        // current instant in time
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null) {
                continue;
            }
            Set<NoteItem> occurrences =
                    noteTriageStatusHelper.getNowFromRecurringNote(note, context);
            if(occurrences.size()>0) {
                qr.getResults().addAll(occurrences);
                qr.getMasters().add(note);
            }
        }

        return qr;
    }

    /**
     * LATER Query:<br/>
     *   - Non-recurring with triage status LATER<br/>
     *   - For each recurring item, either the next occurring modification
     *     with triage status LATER or the next occurrence, whichever occurs sooner
     * @param collection CollectionItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    private QueryResult getLater(CollectionItem collection,
                                 TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(false, -1);

        // filter for LATER triage status
        NoteItemFilter laterFilter =
                getTriageStatusFilter(collection, TriageStatus.CODE_LATER);

        // Recurring event filter
        // Search for recurring events that have occurrences up to a
        // year from point in time
        NoteItemFilter eventFilter =
                getRecurringEventFilter(collection, context.getPointInTime(),
                        new TemporalAmountAdapter(yearLaterDur).getTime(context.getPointInTime()),
                        context.getTimeZone());

        // Add all items that are have an explicit LATER triage
        for(Item item : contentDao.findItems(laterFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);

            // Don't add recurring events
            if(eventStamp==null || !eventStamp.isRecurring()) {
                qr.getResults().add(note);
                // keep track of masters
                if(note.getModifies()!=null) {
                    qr.getMasters().add(note.getModifies());
                }
            }
        }

        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;

            // per bug 10623:
            // return all modifications for later
            if(note.getModifies()!=null) {
                qr.getResults().add(note);
                qr.getMasters().add(note.getModifies());
                continue;
            }

            NoteItem laterItem =
                    noteTriageStatusHelper.getLaterFromRecurringNote(note, context);

            // add laterItem and master if present
            if(laterItem!=null) {
                qr.getResults().add(laterItem);
                qr.getMasters().add(note);
            }
        }

        return qr;
    }

    /**
     * DONE Query:<br/>
     *   - Non-recurring with triage status DONE<br/>
     *   - For each recurring item, either the most recently occurring
     *     modification with triage status DONE or the most recent occurrence,
     *     whichever occurred most recently
     *   - Limit to maxDone results
     * @param collection CollectionItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    private QueryResult getDone(CollectionItem collection,
                                TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(true, maxDone);

        // filter for DONE triage status
        NoteItemFilter doneFilter =
                getTriageStatusFilter(collection, TriageStatus.CODE_DONE);

        // Limit the number of items with DONE status so we don't load
        // tons of items on the server before merging with the recurring
        // item occurrences and sorting.  Anything over this number will
        // be thrown away during the limit/sorting phase so no need to pull
        // more than maxDone items as long as they are sorted by rank.
        doneFilter.setMaxResults(maxDone);
        doneFilter.addOrderBy(ContentItemFilter.ORDER_BY_TRIAGE_STATUS_RANK_ASC);

        // Recurring event filter
        // Search for recurring events that had occurrences up to a
        // year from point in time
        NoteItemFilter eventFilter =
                getRecurringEventFilter(collection,
                        new TemporalAmountAdapter(yearDoneDur).getTime(context.getPointInTime()),
                        context.getPointInTime(),
                        context.getTimeZone());

        // Add all items that are have an explicit DONE triage
        for(Item item : contentDao.findItems(doneFilter)) {
            NoteItem note = (NoteItem) item;
            EventStamp eventStamp = StampUtils.getEventStamp(note);

            // Don't add recurring events
            if(eventStamp==null || !eventStamp.isRecurring()) {
                qr.getResults().add(note);
            }
        }

        // Now process recurring events
        for(Item item: contentDao.findItems(eventFilter)) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null) {
                continue;
            }

            NoteItem doneItem = noteTriageStatusHelper.getDoneFromRecurringNote(note, context);
            // add doneItem and master if present
            if(doneItem!=null) {
                qr.getResults().add(doneItem);
            }
        }

        // add masters for all ocurrences and modifications
        for(NoteItem note: qr.getResults()) {
            if(note instanceof NoteOccurrence) {
                qr.getMasters().add(((NoteOccurrence) note).getMasterNote());
            }
            else if(note.getModifies()!=null) {
                qr.getMasters().add(note.getModifies());
            }
        }

        return qr;
    }

    /**
     * Create NoteItemFilter that matches a parent collection and a specific
     * TriageStatus code.  The filter matches only master events (no modifications).
     *
     * @param collection CollectionItem
     * @param code int
     * @return NoteItemFilter
     */
    private NoteItemFilter getTriageStatusFilter(CollectionItem collection, int code) {
        NoteItemFilter triageStatusFilter = new NoteItemFilter();
        triageStatusFilter.setParent(collection);
        if(code==-1) {
            triageStatusFilter.setTriageStatusCode(Restrictions.isNull());
        }
        else {
            triageStatusFilter.setTriageStatusCode(Restrictions.eq(Integer.valueOf(code)));
        }
        return triageStatusFilter;
    }


    /**
     * Create NoteItemFilter that matches all recurring event NoteItems that belong
     * to a specified parent collection.
     *
     * @param collection CollectionItem
     * @param start Date
     * @param end Date
     * @param timezone Timezone
     * @return NoteItemFiler
     */
    private NoteItemFilter getRecurringEventFilter(CollectionItem collection, Date start, Date end, TimeZone timezone) {
        NoteItemFilter eventNoteFilter = new NoteItemFilter();
        eventNoteFilter.setFilterProperty(EventStampFilter.PROPERTY_DO_TIMERANGE_SECOND_PASS, "false");
        EventStampFilter eventFilter = new EventStampFilter();
        eventFilter.setIsRecurring(true);
        eventFilter.setTimeRange(new DateTime(start), new DateTime(end));
        eventFilter.setTimezone(timezone);
        eventNoteFilter.setParent(collection);
        eventNoteFilter.getStampFilters().add(eventFilter);
        return eventNoteFilter;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public void setMaxDone(int maxDone) {
        this.maxDone = maxDone;
    }


    public static class QueryResult {
        private ArrayList<NoteItem> results = new ArrayList<NoteItem>();
        private HashSet<NoteItem> masters = new HashSet<NoteItem>();
        private Comparator<NoteItem> comparator;
        private int limit;

        /**
         *
         */
        public QueryResult() {
            this(true, -1);
        }

        /**
         * @param ascending boolean
         * @param limit     int
         */
        public QueryResult(boolean ascending, int limit) {
            results = new ArrayList<NoteItem>();
            masters = new HashSet<NoteItem>();
            comparator = ascending ? COMPARE_ASC : COMPARE_DESC;
            this.limit = limit;
        }

        public ArrayList<NoteItem> getResults() {
            return results;
        }

        /**
         *
         */
        public void processResults() {

            // sort
            Collections.sort(results, comparator);

            // trim based on limit
            if (limit != -1 && results.size() > limit) {
                while (results.size() > limit) {
                    results.remove(results.size() - 1);
                }

                // rebuild masters list as it may have changed
                masters.clear();
                for (NoteItem note : results) {
                    if (note instanceof NoteOccurrence) {
                        masters.add(((NoteOccurrence) note).getMasterNote());
                    } else if (note.getModifies() != null) {
                        masters.add(note.getModifies());
                    }
                }
            }
        }

        public HashSet<NoteItem> getMasters() {
            return masters;
        }

        /**
         * @param qr QueryResult
         */
        public void add(QueryResult qr) {
            qr.processResults();
            results.addAll(qr.getResults());
            masters.addAll(qr.getMasters());
        }

        /**
         * @return SortedSet<NoteItem>
         */
        public SortedSet<NoteItem> merge() {

            TreeSet<NoteItem> merged = new TreeSet<NoteItem>(comparator);
            merged.addAll(results);
            merged.addAll(masters);

            return merged;
        }
    }

}
