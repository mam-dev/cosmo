package org.unitedinternet.cosmo.service.impl;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.*;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.calendar.Instance;
import org.unitedinternet.cosmo.calendar.InstanceList;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;
import net.fortuna.ical4j.model.TemporalAmountAdapter;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;

@Component
public class NoteTriageStatusHelper {

    public static final Comparator<NoteItem> COMPARE_ASC = new NoteItemTriageStatusComparator(false);
    public static final Comparator<NoteItem> COMPARE_DESC = new NoteItemTriageStatusComparator(true);

    @Autowired
    private ContentDao contentDao;

    // 366 days (1 year)
    private TemporalAmount yearLaterDur = Duration.ofDays(366);
    private TemporalAmount yearDoneDur = Duration.ofDays(-366);

    // 31 days (1 month)
    private TemporalAmount monthLaterDur = Duration.ofDays(31);
    private TemporalAmount monthDoneDur = Duration.ofDays(-31);
    private static final Logger LOG = LoggerFactory.getLogger(StandardTriageStatusQueryProcessor.class);

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

    /**
     * @param note    NoteItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    public QueryResult getAll(NoteItem note,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        qr.add(getNow(note, context));
        qr.add(getDone(note, context));
        qr.add(getLater(note, context));

        return qr;
    }


    /**
     * NOW Query for a specific master NoteItem:<br/>
     * - Modifications with triage status NOW<br/>
     * - Occurrences whose period overlaps the current point in time
     * - Modifications with triage status null and whose period
     * overlaps the current point in time.
     *
     * @param master  NoteItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    public QueryResult getNow(NoteItem master,
                               TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();
        Set<NoteItem> mods = null;

        mods = getModificationsByTriageStatus(master, TriageStatus.CODE_NOW);
        qr.getResults().addAll(mods);

        // add all occurrences that occur NOW
        mods = getNowFromRecurringNote(master, context);
        qr.getResults().addAll(mods);

        // add master if necessary
        if (!qr.getResults().isEmpty()) {
            qr.getMasters().add(master);
        }

        return qr;
    }


    /**
     * LATER Query for a specific master NoteItem:<br/>
     * - the next occurring modification
     * with triage status LATER or the next occurrence, whichever occurs sooner
     *
     * @param master  NoteItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    public QueryResult getLater(NoteItem master,
                                 TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult(false, -1);

        // get the next occurring modification or occurrence
        NoteItem result = getLaterFromRecurringNote(master, context);

        // add result and master if present
        if (result != null) {
            qr.getMasters().add(master);
            qr.getResults().add(result);
        }

        // add all modifications with trigaeStatus LATER
        Set<NoteItem> mods =
                getModificationsByTriageStatus(master, TriageStatus.CODE_LATER);
        if (qr.getResults().addAll(mods)) {
            qr.getMasters().add(master);
        }

        return qr;
    }


    /**
     * DONE Query for a specific master NoteItem:<br/>
     * - the last occurring modification
     * with triage status DONE or the last occurrence, whichever occurred
     * most recently
     *
     * @param master  NoteItem
     * @param context TriageStatusQueryContext
     * @return QueryResult
     */
    public QueryResult getDone(NoteItem master,
                                TriageStatusQueryContext context) {
        QueryResult qr = new QueryResult();

        // get the most recently occurred modification or occurrence
        NoteItem result = getDoneFromRecurringNote(master, context);

        // add result and master if present
        if (result != null) {
            qr.getMasters().add(master);
            qr.getResults().add(result);
        }

        // add all modifications with trigaeStatus DONE
        Set<NoteItem> mods =
                getModificationsByTriageStatus(master, TriageStatus.CODE_DONE);
        if (qr.getResults().addAll(mods)) {
            qr.getMasters().add(master);
        }

        return qr;
    }

    /**
     * @param master       NoteItem
     * @param triageStatus Integer
     * @return Set<NoteItem>
     */
    public Set<NoteItem> getModificationsByTriageStatus(NoteItem master, Integer triageStatus) {

        HashSet<NoteItem> mods = new HashSet<NoteItem>();

        for (NoteItem mod : master.getModifications()) {
            if (mod.getTriageStatus() == null
                    || mod.getTriageStatus().getCode() == null
                    || mod.getTriageStatus().getCode().equals(triageStatus)) {
                continue;
            }

            mods.add(mod);
        }

        return mods;
    }


    /**
     * Get the last occurring modification or occurrence, whichever occurred
     * last.
     *
     * @param note    NoteItem
     * @param context TriageStatusQueryContext
     * @return NoteItem
     */
    public NoteItem
    getDoneFromRecurringNote(NoteItem note,
                             TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        Date currentDate = context.getPointInTime();
        Date pastDate = new TemporalAmountAdapter(getDurToUseForExpanding(eventStamp, false)).getTime(currentDate);

        // calculate the previous occurrence or modification
        NoteItem latest = getLatestInstanceOrModification(eventStamp, pastDate,
                currentDate, context.getTimeZone());

        return latest;
    }


    /**
     * @param es    EventStamp
     * @param later boolean
     * @return Dur
     */
    public TemporalAmount getDurToUseForExpanding(EventStamp es, boolean later) {
        List<Recur> rules = es.getRecurrenceRules();

        // No rules, assume RDATEs so expand a year
        if (rules.size() == 0) {
            return later ? yearLaterDur : yearDoneDur;
        }

        // Look at first rule only
        Recur recur = rules.get(0);

        // If rule is yearly or monthly then expand a year,
        // otherwise only expand a month
        if (Recur.Frequency.YEARLY.equals(recur.getFrequency()) ||
                Recur.Frequency.MONTHLY.equals(recur.getFrequency())) {
            return later ? yearLaterDur : yearDoneDur;
        } else {
            return later ? monthLaterDur : monthDoneDur;
        }
    }


    /**
     * Calculate and return the latest ocurring instance or modification for the
     * specified master event and date range.
     * The instance must end before the end of the range.
     * If the latest instance is a modification, then the modification must
     * have a triageStatus of DONE
     *
     * @param event      Eventstamp
     * @param rangeStart Date
     * @param rangeEnd   Date
     * @param timezone   Timezone
     * @return NoteItem
     */
    public NoteItem getLatestInstanceOrModification(EventStamp event, Date rangeStart, Date rangeEnd,
                                                     net.fortuna.ical4j.model.TimeZone timezone) {
        NoteItem note = (NoteItem) event.getItem();
        RecurrenceExpander expander = new RecurrenceExpander();

        InstanceList instances = expander.getOcurrences(event.getEvent(), event.getExceptions(),
                new DateTime(rangeStart), new DateTime(rangeEnd), timezone);

        // Find the latest occurrence that ends before the end of the range
        while (instances.size() > 0) {
            String lastKey = (String) instances.lastKey();
            Instance instance = (Instance) instances.remove(lastKey);
            if (instance.getEnd().before(rangeEnd)) {
                if (instance.isOverridden()) {
                    ModificationUid modUid = new ModificationUidImpl(note, instance.getRid());
                    NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                    // shouldn't happen, but log and continue if it does
                    if (mod == null) {
                        LOG.error("no modification found for uid: {}", modUid.toString());
                        continue;
                    }
                    TriageStatus status = mod.getTriageStatus();
                    if (status == null || status.getCode().equals(TriageStatus.CODE_DONE)) {
                        return mod;
                    }
                } else {
                    return NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note);
                }
            }

        }

        return null;
    }


    /**
     * Get all instances that are occuring during a given point in time
     *
     * @param note    NoteItem
     * @param context TriageStatusQueryContext
     * @return Set<NoteItem>
     */
    public Set<NoteItem>
    getNowFromRecurringNote(NoteItem note,
                            TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        DateTime currentDate = new DateTime(context.getPointInTime());
        RecurrenceExpander expander = new RecurrenceExpander();
        HashSet<NoteItem> results = new HashSet<NoteItem>();

        // Get all occurrences that overlap current instance in time
        InstanceList occurrences = expander.getOcurrences(
                eventStamp.getEvent(), eventStamp.getExceptions(), currentDate,
                currentDate, context.getTimeZone());

        for (Instance instance : (Collection<Instance>) occurrences.values()) {
            // Not interested in modifications
            if (!instance.isOverridden()) {
                // add occurrence
                results.add(NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note));
            } else {
                // return modification if it has no triage-status
                ModificationUid modUid = new ModificationUidImpl(note, instance.getRid());
                NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                if (mod.getTriageStatus() == null || mod.getTriageStatus().getCode() == null) {
                    results.add(mod);
                }
            }
        }

        return results;
    }

    /**
     * Get the next occurrence or modification for a recurring event, whichever
     * occurrs sooner relative to a point in time.
     *
     * @param note    NoteItem
     * @param context TriageStatusQueryContext
     * @return Set<NoteItem>
     */
    public NoteItem
    getLaterFromRecurringNote(NoteItem note,
                              TriageStatusQueryContext context) {
        EventStamp eventStamp = StampUtils.getEventStamp(note);
        Date currentDate = context.getPointInTime();
        Date futureDate = new TemporalAmountAdapter(getDurToUseForExpanding(eventStamp, true)).getTime(currentDate);

        // calculate the next occurrence or LATER modification
        NoteItem first = getFirstInstanceOrModification(eventStamp,
                currentDate, futureDate, context.getTimeZone());

        return first;
    }


    /**
     * Calculate and return the first ocurring instance or modification
     * for the specified master event and date range.
     * The instance must begin after the start of the range and if it
     * is a modification it must have a triageStatus of LATER.
     *
     * @param event      Eventstamp
     * @param rangeStart Date
     * @param rangeEnd   Date
     * @param timezone   Timezone
     * @return NoteItem
     */
    public NoteItem getFirstInstanceOrModification(EventStamp event, Date rangeStart, Date rangeEnd, net.fortuna.ical4j.model.TimeZone timezone) {
        NoteItem note = (NoteItem) event.getItem();
        RecurrenceExpander expander = new RecurrenceExpander();

        InstanceList instances = expander.getOcurrences(event.getEvent(),
                event.getExceptions(), new DateTime(rangeStart), new DateTime(rangeEnd),
                timezone);

        // Find the first occurrence that begins after the start range
        while (instances.size() > 0) {
            String firstKey = (String) instances.firstKey();
            Instance instance = (Instance) instances.remove(firstKey);
            if (instance.getStart().after(rangeStart)) {
                if (instance.isOverridden()) {
                    ModificationUid modUid = new ModificationUidImpl(note, instance.getRid());
                    NoteItem mod = (NoteItem) contentDao.findItemByUid(modUid.toString());
                    // shouldn't happen, but log and continue if it does
                    if (mod == null) {
                        LOG.error("no modification found for uid: {}", modUid.toString());
                        continue;
                    }
                    TriageStatus status = mod.getTriageStatus();
                    if (status == null || status.getCode().equals(TriageStatus.CODE_LATER)) {
                        return mod;
                    }
                } else {
                    return NoteOccurrenceUtil.createNoteOccurrence(instance.getRid(), note);
                }
            }
        }

        return null;
    }

}
