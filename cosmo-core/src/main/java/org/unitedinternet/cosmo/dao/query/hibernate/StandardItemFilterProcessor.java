/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.query.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hibernate.query.internal.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.CosmoConstants;
import org.unitedinternet.cosmo.calendar.Instance;
import org.unitedinternet.cosmo.calendar.InstanceList;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.filter.AttributeFilter;
import org.unitedinternet.cosmo.model.filter.BetweenExpression;
import org.unitedinternet.cosmo.model.filter.ContentItemFilter;
import org.unitedinternet.cosmo.model.filter.EqualsExpression;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.FilterCriteria;
import org.unitedinternet.cosmo.model.filter.FilterExpression;
import org.unitedinternet.cosmo.model.filter.FilterOrder;
import org.unitedinternet.cosmo.model.filter.FilterOrder.Order;
import org.unitedinternet.cosmo.model.filter.ILikeExpression;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.LikeExpression;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.filter.NullExpression;
import org.unitedinternet.cosmo.model.filter.StampFilter;
import org.unitedinternet.cosmo.model.filter.StringAttributeFilter;
import org.unitedinternet.cosmo.model.filter.TextAttributeFilter;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>. Translates filter into HQL Query, executes query and
 * processes the results.
 */
@Component
public class StandardItemFilterProcessor  implements ItemFilterProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardItemFilterProcessor.class);
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * Constructor.
     */
    public StandardItemFilterProcessor() {
    }

    @Override
    public Set<Item> processFilter(ItemFilter filter) {
        List<Item> queryResults = this.buildQueryInternal(filter).getResultList();
        return processResults(queryResults, filter);
    }

    /**
     * Build Hibernate Query from ItemFilter using HQL. The query returned is essentially the first pass at retrieving
     * the matched items. A second pass is required in order determine if any recurring events match a timeRange in the
     * filter. This is due to the fact that recurring events may have complicated recurrence rules that are extremely
     * hard to match using HQL.
     *
     * @param filter
     *            item filter
     * @return hibernate query built using HQL
     */
    private TypedQuery<Item> buildQueryInternal(ItemFilter filter) {
        StringBuilder selectBuf = new StringBuilder();
        StringBuilder whereBuf = new StringBuilder();
        StringBuilder orderBuf = new StringBuilder();

        HashMap<String, Object> params = new HashMap<String, Object>();

        if (filter instanceof NoteItemFilter) {
            handleNoteItemFilter(selectBuf, whereBuf, params, (NoteItemFilter) filter);
        } else if (filter instanceof ContentItemFilter) {
            handleContentItemFilter(selectBuf, whereBuf, params, (ContentItemFilter) filter);
        } else {
            handleItemFilter(selectBuf, whereBuf, params, filter);
        }

        selectBuf.append(whereBuf);

        for (FilterOrder fo : filter.getOrders()) {
            if (orderBuf.length() == 0) {
                orderBuf.append(" order by ");
            } else {
                orderBuf.append(", ");
            }

            orderBuf.append("i." + fo.getName());

            if (fo.getOrder().equals(Order.DESC)) {
                orderBuf.append(" desc");
            }
        }

        selectBuf.append(orderBuf);

        if (LOG.isDebugEnabled()) {
            LOG.debug(selectBuf.toString());
        }

        TypedQuery<Item> hqlQuery = this.em.createQuery(selectBuf.toString(), Item.class);

        for (Entry<String, Object> param : params.entrySet()) {
            hqlQuery.setParameter(param.getKey(), param.getValue());
        }

        if (filter.getMaxResults() != null) {
            hqlQuery.setMaxResults(filter.getMaxResults());
        }

        return hqlQuery;
    }

    /**
     * Defined for testing reasons.
     */
    protected QueryImpl<Item> buildQuery(ItemFilter filter) {
        return (QueryImpl<Item>) this.buildQueryInternal(filter);
    }

    private void handleItemFilter(StringBuilder selectBuf, StringBuilder whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {

        if ("".equals(selectBuf.toString())) {
            selectBuf.append("select i from HibItem i");
        }

        // filter on uid
        if (filter.getUid() != null) {
            formatExpression(whereBuf, params, "i.uid", filter.getUid());
        }

        // filter on parent
        if (filter.getParent() != null) {
            selectBuf.append(" join i.parentDetails pd");
            appendWhere(whereBuf, "pd.primaryKey.collection=:parent");
            params.put("parent", filter.getParent());
        }

        if (filter.getDisplayName() != null) {
            formatExpression(whereBuf, params, "i.displayName", filter.getDisplayName());
        }

        handleAttributeFilters(selectBuf, whereBuf, params, filter);
        handleStampFilters(selectBuf, whereBuf, filter);

    }

    private void handleAttributeFilters(StringBuilder selectBuf, StringBuilder whereBuf, HashMap<String, Object> params,
            ItemFilter filter) {
        for (AttributeFilter attrFilter : filter.getAttributeFilters()) {
            if (attrFilter instanceof TextAttributeFilter) {
                handleTextAttributeFilter(selectBuf, whereBuf, params, (TextAttributeFilter) attrFilter);
            } else if (attrFilter instanceof StringAttributeFilter) {
                handleStringAttributeFilter(selectBuf, whereBuf, params, (StringAttributeFilter) attrFilter);
            } else {
                handleAttributeFilter(whereBuf, params, attrFilter);
            }
        }
    }

    private void handleTextAttributeFilter(StringBuilder selectBuf, StringBuilder whereBuf,
            HashMap<String, Object> params, TextAttributeFilter filter) {

        String alias = "ta" + params.size();
        selectBuf.append(", HibTextAttribute " + alias);
        appendWhere(whereBuf, alias + ".item=i and " + alias + ".qname=:" + alias + "qname");
        params.put(alias + "qname", filter.getQname());
        formatExpression(whereBuf, params, alias + ".value", filter.getValue());
    }

    private void handleStringAttributeFilter(StringBuilder selectBuf, StringBuilder whereBuf,
            HashMap<String, Object> params, StringAttributeFilter filter) {

        String alias = "ta" + params.size();
        selectBuf.append(", HibStringAttribute " + alias);
        appendWhere(whereBuf, alias + ".item=i and " + alias + ".qname=:" + alias + "qname");
        params.put(alias + "qname", filter.getQname());
        formatExpression(whereBuf, params, alias + ".value", filter.getValue());
    }

    private void handleStampFilters(StringBuilder selectBuf, StringBuilder whereBuf, ItemFilter filter) {
        for (StampFilter stampFilter : filter.getStampFilters()) {
            if (stampFilter instanceof EventStampFilter) {
                handleEventStampFilter(selectBuf, whereBuf, (EventStampFilter) stampFilter);
            } else {
                handleStampFilter(whereBuf, stampFilter);
            }
        }
    }

    private void handleStampFilter(StringBuilder whereBuf, StampFilter filter) {

        String toAppend = "";
        if (filter.isMissing()) {
            toAppend += "not ";
        }
        toAppend += "exists (select s.id from HibStamp s where s.item=i and s.class=Hib"
                + filter.getStampClass().getSimpleName() + ")";
        appendWhere(whereBuf, toAppend);
    }

    private void handleAttributeFilter(StringBuilder whereBuf, HashMap<String, Object> params, AttributeFilter filter) {

        String param = "param" + params.size();
        String toAppend = "";
        if (filter.isMissing()) {
            toAppend += "not ";
        }

        toAppend += "exists (select a.id from HibAttribute a where a.item=i and a.qname=:" + param + ")";
        appendWhere(whereBuf, toAppend);
        params.put(param, filter.getQname());
    }

    private void handleEventStampFilter(StringBuilder selectBuf, StringBuilder whereBuf, EventStampFilter filter) {

        selectBuf.append(", HibBaseEventStamp es");
        appendWhere(whereBuf, "es.item=i");

        // handle recurring event filter
        if (filter.getIsRecurring() != null) {
            if (filter.getIsRecurring().booleanValue() == true) {
                appendWhere(whereBuf, "(es.timeRangeIndex.isRecurring=true or i.modifies is not null)");
            } else {
                appendWhere(whereBuf, "(es.timeRangeIndex.isRecurring=false and i.modifies is null)");
            }
        }

        // handle time range
        if (filter.getPeriod() != null) {
            whereBuf.append(" and ( ");
            whereBuf.append("(es.timeRangeIndex.isFloating=true and es.timeRangeIndex.startDate < '"
                    + filter.getFloatEnd() + "'");
            whereBuf.append(" and es.timeRangeIndex.endDate > '" + filter.getFloatStart() + "')");

            whereBuf.append(" or (es.timeRangeIndex.isFloating=false and " + "es.timeRangeIndex.startDate < '"
                    + filter.getUTCEnd() + "'");
            whereBuf.append(" and es.timeRangeIndex.endDate > '" + filter.getUTCStart() + "')");

            // edge case where start==end
            whereBuf.append(" or (es.timeRangeIndex.startDate=es.timeRangeIndex.endDate and "
                    + "(es.timeRangeIndex.startDate='" + filter.getFloatStart() + "' or es.timeRangeIndex.startDate='"
                    + filter.getUTCStart() + "'))");

            whereBuf.append(")");
        }
    }

    private void handleNoteItemFilter(StringBuilder selectBuf, StringBuilder whereBuf, HashMap<String, Object> params,
            NoteItemFilter filter) {
        selectBuf.append("select i from HibNoteItem i");
        handleItemFilter(selectBuf, whereBuf, params, filter);
        handleContentItemFilter(selectBuf, whereBuf, params, filter);

        // filter by icaluid
        if (filter.getIcalUid() != null) {
            formatExpression(whereBuf, params, "i.icalUid", filter.getIcalUid());
        }

        // filter by body
        if (filter.getBody() != null) {
            String alias = "ta" + params.size();
            selectBuf.append(", HibTextAttribute " + alias);
            appendWhere(whereBuf, alias + ".item=i and " + alias + ".qname=:" + alias + "qname");
            params.put(alias + "qname", HibNoteItem.ATTR_NOTE_BODY);
            formatExpression(whereBuf, params, alias + ".value", filter.getBody());
        }

        // filter by reminderTime
        if (filter.getReminderTime() != null) {
            String alias = "tsa" + params.size();
            selectBuf.append(", HibTimestampAttribute " + alias);
            appendWhere(whereBuf, alias + ".item=i and " + alias + ".qname=:" + alias + "qname");
            params.put(alias + "qname", HibNoteItem.ATTR_REMINDER_TIME);
            formatExpression(whereBuf, params, alias + ".value", filter.getReminderTime());
        }

        // filter by master NoteItem
        if (filter.getMasterNoteItem() != null) {
            appendWhere(whereBuf, "(i=:masterItem or i.modifies=:masterItem)");
            params.put("masterItem", filter.getMasterNoteItem());
        }

        // filter modifications
        if (filter.getIsModification() != null) {
            if (filter.getIsModification().booleanValue() == true) {
                appendWhere(whereBuf, "i.modifies is not null");
            } else {
                appendWhere(whereBuf, "i.modifies is null");
            }
        }

        if (filter.getHasModifications() != null) {
            if (filter.getHasModifications().booleanValue() == true) {
                appendWhere(whereBuf, "size(i.modifications) > 0");
            } else {
                appendWhere(whereBuf, "size(i.modifications) = 0");
            }
        }

        if (filter.getModifiedSince() != null) {
            formatExpression(whereBuf, params, "i.modifiedDate", filter.getModifiedSince());
        }
    }

    private void handleContentItemFilter(StringBuilder selectBuf, StringBuilder whereBuf, HashMap<String, Object> params,
            ContentItemFilter filter) {

        if ("".equals(selectBuf.toString())) {
            selectBuf.append("select i from HibContentItem i");
            handleItemFilter(selectBuf, whereBuf, params, filter);
        }

        // handle triageStatus filter
        if (filter.getTriageStatusCode() != null) {
            formatExpression(whereBuf, params, "i.triageStatus.code", filter.getTriageStatusCode());
        }
    }

    private void appendWhere(StringBuilder whereBuf, String toAppend) {
        if ("".equals(whereBuf.toString())) {
            whereBuf.append(" where " + toAppend);
        } else {
            whereBuf.append(" and " + toAppend);
        }
    }

    private String formatForLike(String toFormat) {
        return "%" + toFormat + "%";
    }

    /**
     * Because a timeRange query requires two passes: one to get the list of possible events that occur in the range,
     * and one to expand recurring events if necessary. This is required because we only index a start and end for the
     * entire recurrence series, and expansion is required to determine if the event actually occurs, and to return
     * individual occurences.
     */
    @Override
    public Set<Item> processResults(List<Item> results, ItemFilter itemFilter) {
        boolean hasTimeRangeFilter = false;
        boolean includeMasterInResults = true;
        boolean doTimeRangeSecondPass = true;

        HashSet<Item> processedResults = new HashSet<Item>();
        EventStampFilter eventFilter = (EventStampFilter) itemFilter.getStampFilter(EventStampFilter.class);

        if (eventFilter != null) {
            // does eventFilter have timeRange filter?
            hasTimeRangeFilter = eventFilter.getPeriod() != null;
        }

        // When expanding recurring events do we include the master item in
        // the results, or just the expanded occurrences/modifications
        if (hasTimeRangeFilter && "false"
                .equalsIgnoreCase(itemFilter.getFilterProperty(EventStampFilter.PROPERTY_INCLUDE_MASTER_ITEMS))) {
            includeMasterInResults = false;
        }

        // Should we do a second pass to expand recurring events to determine
        // if a recurring event actually occurs in the time-range specified,
        // or should we just return the recurring event without double-checking.
        if (hasTimeRangeFilter && "false"
                .equalsIgnoreCase(itemFilter.getFilterProperty(EventStampFilter.PROPERTY_DO_TIMERANGE_SECOND_PASS))) {
            doTimeRangeSecondPass = false;
        }

        for (Item item : results) {

            // If item is not a note, then nothing to do
            if (!(item instanceof NoteItem)) {
                processedResults.add(item);
                continue;
            }

            NoteItem note = (NoteItem) item;

            // If note is a modification then add both the modification and the
            // master.
            if (note.getModifies() != null) {
                processedResults.add(note);
                if (includeMasterInResults) {
                    processedResults.add(note.getModifies());
                }
            }
            // If filter doesn't have a timeRange, then we are done
            else if (!hasTimeRangeFilter) {
                processedResults.add(note);
            } else {
                processedResults
                        .addAll(processMasterNote(note, eventFilter, includeMasterInResults, doTimeRangeSecondPass));
            }
        }

        return processedResults;
    }

    private Collection<ContentItem> processMasterNote(NoteItem note, EventStampFilter filter,
            boolean includeMasterInResults, boolean doTimeRangeSecondPass) {
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
        InstanceList instances = expander.getOcurrences(eventStamp.getEvent(), eventStamp.getExceptions(),
                filter.getPeriod().getStart(), filter.getPeriod().getEnd(), filter.getTimezone());

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
        for (Iterator<Entry<String, Instance>> it = instances.entrySet().iterator(); it.hasNext();) {
            Entry<String, Instance> entry = it.next();

            // Ignore overrides as they are separate items that should have
            // already been added
            if (entry.getValue().isOverridden() == false) {
                results.add(NoteOccurrenceUtil.createNoteOccurrence(entry.getValue().getRid(), note));
            }
        }

        return results;
    }

    private void formatExpression(StringBuilder whereBuf, HashMap<String, Object> params, String propName,
            FilterCriteria fc) {

        StringBuilder expBuf = new StringBuilder();

        FilterExpression exp = (FilterExpression) fc;

        if (exp instanceof NullExpression) {
            expBuf.append(propName);
            if (exp.isNegated()) {
                expBuf.append(" is not null");
            } else {
                expBuf.append(" is null");
            }
        } else if (exp instanceof BetweenExpression) {
            BetweenExpression be = (BetweenExpression) exp;
            expBuf.append(propName);
            if (exp.isNegated()) {
                expBuf.append(" not");
            }

            String param = "param" + params.size();
            expBuf.append(" between :" + param);
            params.put(param, be.getValue1());
            param = "param" + params.size();
            expBuf.append(" and :" + param);
            params.put(param, be.getValue2());
        } else {
            String param = "param" + params.size();
            if (exp instanceof EqualsExpression) {
                expBuf.append(propName);
                if (exp.isNegated()) {
                    expBuf.append("!=");
                } else {
                    expBuf.append("=");
                }

                params.put(param, exp.getValue());

            } else if (exp instanceof LikeExpression) {
                expBuf.append(propName);
                if (exp.isNegated()) {
                    expBuf.append(" not like ");
                } else {
                    expBuf.append(" like ");
                }

                params.put(param, formatForLike(exp.getValue().toString()));
            } else if (exp instanceof ILikeExpression) {
                expBuf.append("lower(" + propName + ")");
                if (exp.isNegated()) {
                    expBuf.append(" not like ");
                } else {
                    expBuf.append(" like ");
                }

                params.put(param, formatForLike(exp.getValue().toString().toLowerCase(CosmoConstants.LANGUAGE_LOCALE)));
            }

            expBuf.append(":" + param);
        }

        appendWhere(whereBuf, expBuf.toString());
    }

}
