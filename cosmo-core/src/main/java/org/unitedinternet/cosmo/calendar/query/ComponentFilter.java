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
package org.unitedinternet.cosmo.calendar.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Element;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Represents a component filter as defined in the CalDAV spec:
 * 
 * from Section 9.6.1:
 * 
 * CALDAV:comp-filter <!ELEMENT comp-filter (is-not-defined | (time-range?,
 * prop-filter*, comp-filter*))>
 * 
 * <!ATTLIST comp-filter name CDATA #REQUIRED>
 * 
 * name value: a calendar component name (e.g., "VEVENT")
 * 
 * Note: This object model does not validate. For example you can create a
 * ComponentFilter with a IsNotDefinedFilter and a TimeRangeFilter. It is the
 * responsibility of the business logic to enforce this. This may be changed
 * later.
 * @author cdobrota
 */
public class ComponentFilter implements CaldavConstants, ICalendarConstants {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentFilter.class);

    /**
     * Component filters container.
     */
    private List<ComponentFilter> componentFilters = new ArrayList<>();

    /**
     * Prop filters container.
     */
    private List<PropertyFilter> propFilters = new ArrayList<>();

    /**
     * The the CALDAV:is-not-defined element for this filter.
     */
    private IsNotDefinedFilter isNotDefinedFilter;

    
    /**
     * The CALDAV:time-range element for this filter.
     */
    private TimeRangeFilter timeRangeFilter;

    /**
     * This filter's name.
     */
    private String name;

    /**
     * Constructor.
     */
    public ComponentFilter() {
    }

    /**
     * Constructor.
     * @param element The element.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public ComponentFilter(Element element) throws ParseException {
        this(element, null);
    }
    /**
     * 
     * @author cdobrota
     *
     */
    private interface InitializationOperation {
        /**
         * 
         * @param element - the child element
         * @param timezone - user's timezone
         * @param componentFilter - object to be initialed
         * @param childCount number of children at an iteration step 
         * @throws ParseException if something goes wrong or invalid Element received
         */
        void initialize(Element element, 
                        VTimeZone timezone, 
                        ComponentFilter componentFilter, 
                        int childCount) throws ParseException;
    }
    /**
     * Factory + singletons for initializing this object.
     * @author cdobrota
     *
     */
    private static enum Initializers implements InitializationOperation {
        /**
         * time range initialier.
         */
        TIME_RANGE(ELEMENT_CALDAV_TIME_RANGE) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void initialize(Element element, 
                                    VTimeZone timezone, 
                                    ComponentFilter componentFilter, 
                                    int childCount) throws ParseException {
             // Can only have one time-range element in a comp-filter
                if (componentFilter.timeRangeFilter != null) {
                    throw new ParseException(
                            "CALDAV:comp-filter only one time-range element permitted",
                            -1);
                }

                componentFilter.timeRangeFilter = new TimeRangeFilter(element, timezone);
            }
        },
        /**
         * comp filter initializer.
         */
        COMP_FILTER(ELEMENT_CALDAV_COMP_FILTER) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void initialize(Element element, 
                                    VTimeZone timezone, 
                                    ComponentFilter componentFilter, 
                                    int childCount) throws ParseException {
             // Add to list
                componentFilter.componentFilters.add(new ComponentFilter(element, timezone));
            }
        },
        /**
         * prop filter initializer.
         */
        PROP_FILTER(ELEMENT_CALDAV_PROP_FILTER) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void initialize(Element element,     
                                    VTimeZone timezone, 
                                    ComponentFilter componentFilter, 
                                    int childCount) throws ParseException { 
             // Add to list
                componentFilter.propFilters.add(new PropertyFilter(element, timezone));
            }
        },
        /**
         * not defined filter initializer.
         */
        NOT_DEFINED_FILTER(ELEMENT_CALDAV_IS_NOT_DEFINED) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void initialize(Element element,     
                                    VTimeZone timezone,  
                                    ComponentFilter componentFilter, 
                                    int childCount) throws ParseException {
                if (childCount > 1) {
                    throw new ParseException(
                            "CALDAV:is-not-defined cannnot be present with other child elements", -1);
                }
                componentFilter.isNotDefinedFilter = new IsNotDefinedFilter();
            }
        },
        /**
         * defined filter initializer.
         */
        DEFINED_FILTER(ELEMENT_CALDAV_IS_DEFINED) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void initialize(Element element, 
                                    VTimeZone timezone, 
                                    ComponentFilter componentFilter, 
                                    int childCount) throws ParseException {
             // XXX provided for backwards compatibility with
                // Evolution 2.6, which does not implement
                // is-not-defined;
                if (childCount > 1) {
                    throw new ParseException(
                            "CALDAV:is-defined cannnot be present with other child elements", -1);
                }
                LOG.warn("old style 'is-defined' ignored from (outdated) client!");
            }
            
        };
        /**
         * The local name for this filter.
         */
        private String name;
        /**
         * 
         * @return the used local name.
         */
        String getName() {
            return name;
        }
        
        Initializers(String name) {
            this.name = name;
        }
        
        static InitializationOperation getInitializer(String elementLocalName) throws ParseException {
            for (Initializers initializer : values()) {
                if (initializer.getName().equals(elementLocalName)) {
                    return initializer;
                }
            }
            throw new ParseException("CALDAV:comp-filter an invalid element name found", -1);
        }
    }
    /**
     * Construct a ComponentFilter object from a DOM Element.
     * @param element The element.
     * @param timezone The timezone.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public ComponentFilter(Element element, VTimeZone timezone) throws ParseException {
        // Name must be present
        validateName(element);

        final ElementIterator i = DomUtil.getChildren(element);
        int childCount = 0;
        
        while (i.hasNext()) {
            final Element child = i.nextElement();
            childCount++;

            // if is-not-defined is present, then nothing else can be present
            validateNotDefinedState(childCount);

            Initializers.getInitializer(child.getLocalName()).initialize(child, timezone, this, childCount);
        }
    }
    
    private void validateName(Element element) throws ParseException {
        name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);

        if (name == null) {
            throw new ParseException(
                    "CALDAV:comp-filter a calendar component name  (e.g., \"VEVENT\") is required",
                    -1);
        }

        if (!(name.equals(Calendar.VCALENDAR) 
            || CalendarUtils.isSupportedComponent(name) 
            || name.equals(Component.VALARM) 
            || name.equals(Component.VTIMEZONE))) {
            throw new ParseException(name + " is not a supported iCalendar component", -1);
        }
    }
    
    /**
     * Validates this filter's state  
     * @param childCount number of element's children
     * @throws ParseException in case of invalid state
     */
    private void validateNotDefinedState(int childCount) throws ParseException {
        if (childCount > 1 && isNotDefinedFilter != null) {
            throw new ParseException(
                    "CALDAV:is-not-defined cannnot be present with other child elements", -1);
        }
    }

    /**
     * Create a new ComponentFilter with the specified component name.
     * 
     * @param name
     *            component name
     */
    public ComponentFilter(String name) {
        this.name = name;
    }

    /**
     * Gets is not defined filter.
     * @return isNotDefinedFilter.
     */
    public IsNotDefinedFilter getIsNotDefinedFilter() {
        return isNotDefinedFilter;
    }

    /**
     * Sets isNotDefinedFilter.
     * @param isNotDefinedFilter IsNotDefinedFilter.
     */
    public void setIsNotDefinedFilter(IsNotDefinedFilter isNotDefinedFilter) {
        this.isNotDefinedFilter = isNotDefinedFilter;
    }

    /**
     * Gets name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets component filter.
     * @return The component filter.
     */
    public List<ComponentFilter> getComponentFilters() {
        return componentFilters;
    }

    /**
     * Sets component filter.
     * @param componentFilters The component filter.
     */
    public void setComponentFilters(List<ComponentFilter> componentFilters) {
        this.componentFilters = componentFilters;
    }

    /**
     * Gets time range filter.
     * @return The time range filter.
     */
    public TimeRangeFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }

    /**
     * Sets time range filter.
     * @param timeRangeFilter The time range filter.
     */
    public void setTimeRangeFilter(TimeRangeFilter timeRangeFilter) {
        this.timeRangeFilter = timeRangeFilter;
    }

    /**
     * Gets prop filters.
     * @return The prop filters.
     */
    public List<PropertyFilter> getPropFilters() {
        return propFilters;
    }

    /**
     * Sets prop filters.
     * @param propFilters The prop filters.
     */
    public void setPropFilters(List<PropertyFilter> propFilters) {
        this.propFilters = propFilters;
    }

    /**
     * ToString.
     * {@inheritDoc}
     * The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", name).
            append("isNotDefinedFilter", isNotDefinedFilter).
            append("timeRangeFilter", timeRangeFilter).
            append("componentFilters", componentFilters).
            append("propFilters", propFilters).
            toString();
    }
    
    /**
     * Validates this filter.
     */
    public void validate() {
        for (final Iterator<ComponentFilter> it = componentFilters.iterator(); it.hasNext();) {
            it.next().validate();
        }
        for (final Iterator<PropertyFilter> it = propFilters.iterator(); it.hasNext();) {
            it.next().validate();
        }
    }
}
