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
package org.unitedinternet.cosmo.dao.query.hibernate;

import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.ParamFilter;
import org.unitedinternet.cosmo.calendar.query.PropertyFilter;
import org.unitedinternet.cosmo.calendar.query.TextMatchFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.filter.Restrictions;
import org.unitedinternet.cosmo.model.filter.StampFilter;

import net.fortuna.ical4j.model.TimeZone;

/**
 * Translates <code>CalendarFilter</code> into <code>ItemFilter</code>
 */
public class CalendarFilterConverter {

    private static final String COMP_VCALENDAR = "VCALENDAR";
    private static final String COMP_VEVENT = "VEVENT";
    private static final String COMP_VTODO = "VTODO";
    private static final String PROP_UID = "UID";
    private static final String PROP_DESCRIPTION = "DESCRIPTION";
    private static final String PROP_SUMMARY = "SUMMARY";

    /**
     * Constructor.
     */
    public CalendarFilterConverter() {
    }

    /**
     * Tranlsate CalendarFilter to an equivalent ItemFilter.
     * For now, only the basic CalendarFilter is supported, which is
     * essentially a timerange filter.  The majority of CalendarFilters
     * will fall into this case.  More cases will be supported as they
     * are implemented.
     *
     * @param calendar       parent calendar
     * @param calendarFilter filter to translate
     * @return equivalent ItemFilter
     */
    public ItemFilter translateToItemFilter(CollectionItem calendar, CalendarFilter calendarFilter) {
        NoteItemFilter itemFilter = new NoteItemFilter();
        itemFilter.setParent(calendar);
        ComponentFilter rootFilter = calendarFilter.getFilter();
        if (!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName())) {
            throw new IllegalArgumentException("Unsupported component filter: " + rootFilter.getName());
        }

        for (ComponentFilter compFilter : rootFilter.getComponentFilters()) {
            handleCompFilter(compFilter, itemFilter);
        }

        return itemFilter;
    }

    /**
     * Translate CalendarFilter into an ItemFilter that can be used
     * as a first pass.  All items returned may or may not match the
     * specified CalendarFilter.
     *
     * @param calendar       calendar
     * @param calendarFilter filter to translate
     * @return ItemFilter that can be used as a first-pass, meaning
     *         not all items are guaranteed to match the CalendarFilter.
     *         Further processing is required.
     */
    public ItemFilter getFirstPassFilter(CollectionItem calendar, CalendarFilter calendarFilter) {
        ComponentFilter rootFilter = calendarFilter.getFilter();
        if (!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName())) {
            return null;
        }

        // only support single comp-filer
        if (rootFilter.getComponentFilters().size() != 1) {
            return null;
        }

        ComponentFilter compFilter = (ComponentFilter) rootFilter.getComponentFilters().get(0);

        // handle finding VTODO for now
        if (COMP_VTODO.equalsIgnoreCase(compFilter.getName())) {
            return createFirstPassTaskFilter(calendar);
        }

        return null;
    }

    private ItemFilter createFirstPassTaskFilter(CollectionItem collection) {
        NoteItemFilter filter = new NoteItemFilter();
        filter.setParent(collection);
        filter.setIsModification(false);
        filter.getStampFilters().add(new StampFilter(EventStamp.class, true));
        return filter;
    }

    private void handleCompFilter(ComponentFilter compFilter, NoteItemFilter itemFilter) {

        if (COMP_VEVENT.equalsIgnoreCase(compFilter.getName())) {
            handleEventCompFilter(compFilter, itemFilter);
        } else {
            throw new IllegalArgumentException("unsupported component filter: " + compFilter.getName());
        }
    }

    private void handleEventCompFilter(ComponentFilter compFilter, NoteItemFilter itemFilter) {
        // TODO: handle case of multiple VEVENT filters
        EventStampFilter eventFilter = new EventStampFilter();
        itemFilter.getStampFilters().add(eventFilter);

        TimeRangeFilter trf = compFilter.getTimeRangeFilter();

        // handle time-range filter
        if (trf != null) {
            eventFilter.setPeriod(trf.getPeriod());
            if (trf.getTimezone() != null) {
                eventFilter.setTimezone(new TimeZone(trf.getTimezone()));
            }
        }

        for (ComponentFilter subComp : compFilter.getComponentFilters()) {
            throw new IllegalArgumentException("unsupported sub component filter: " + subComp.getName());
        }

        for (PropertyFilter propFilter : compFilter.getPropFilters()) {
            handleEventPropFilter(propFilter, itemFilter);
        }
    }

    private void handleEventPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {

        if (PROP_UID.equalsIgnoreCase(propFilter.getName())) {
            handleUidPropFilter(propFilter, itemFilter);
        } else if (PROP_SUMMARY.equalsIgnoreCase(propFilter.getName())) {
            handleSummaryPropFilter(propFilter, itemFilter);
        } else if (PROP_DESCRIPTION.equalsIgnoreCase(propFilter.getName())) {
            handleDescriptionPropFilter(propFilter, itemFilter);
        } else {
            throw new IllegalArgumentException("unsupported prop filter: " + propFilter.getName());
        }
    }

    private void handleUidPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {

        for (ParamFilter paramFilter : propFilter.getParamFilters()) {
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }

        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if (textMatch == null) {
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        }

        if (textMatch.isCaseless()) {
            if (textMatch.isNegateCondition()) {
                itemFilter.setIcalUid(Restrictions.nilike(textMatch.getValue()));
            } else {
                itemFilter.setIcalUid(Restrictions.ilike(textMatch.getValue()));
            }
        } else {
            if (textMatch.isNegateCondition()) {
                itemFilter.setIcalUid(Restrictions.nlike(textMatch.getValue()));
            } else {
                itemFilter.setIcalUid(Restrictions.like(textMatch.getValue()));
            }
        }
    }

    private void handleDescriptionPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {

        for (ParamFilter paramFilter : propFilter.getParamFilters()) {            
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }

        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if (textMatch == null) {
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        }

        if (textMatch.isCaseless()) {
            if (textMatch.isNegateCondition()) {
                itemFilter.setBody(Restrictions.nilike(textMatch.getValue()));
            } else {
                itemFilter.setBody(Restrictions.ilike(textMatch.getValue()));
            }
        } else {
            if (textMatch.isNegateCondition()) {
                itemFilter.setBody(Restrictions.nlike(textMatch.getValue()));
            } else {
                itemFilter.setBody(Restrictions.like(textMatch.getValue()));
            }
        }
    }

    private void handleSummaryPropFilter(PropertyFilter propFilter, NoteItemFilter itemFilter) {

        for ( ParamFilter paramFilter : propFilter.getParamFilters()) {
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }

        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if (textMatch == null) {
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        }

        if (textMatch.isCaseless()) {
            if (textMatch.isNegateCondition()) {
                itemFilter.setDisplayName(Restrictions.nilike(textMatch.getValue()));
            } else {
                itemFilter.setDisplayName(Restrictions.ilike(textMatch.getValue()));
            }
        } else {
            if (textMatch.isNegateCondition()) {
                itemFilter.setDisplayName(Restrictions.nlike(textMatch.getValue()));
            } else {
                itemFilter.setDisplayName(Restrictions.like(textMatch.getValue()));
            }
        }
    }

}
