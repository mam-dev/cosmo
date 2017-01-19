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

import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:prop-filter element. From sec 9.6.2:
 * 
 * Name: prop-filter
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies search criteria on calendar properties.
 * 
 * Description: The CALDAV:prop-filter XML element specifies a search criteria
 * on a specific calendar property (e.g., CATEGORIES) in the scope of a given
 * CALDAV:comp-filter. A calendar component is said to match a
 * CALDAV:prop-filter if:
 * 
 * A property of the type specified by the "name" attribute exists, and the
 * CALDAV:prop-filter is empty, or it matches the CALDAV:time-range XML element
 * or CALDAV:text-match conditions if specified, and that any
 * CALDAV:param-filter child elements also match.
 * 
 * or:
 * A property of the type specified by the "name" attribute does not exist,
 * and the CALDAV:is-not-defined element is specified.
 * 
 * Definition:
 * 
 * <!ELEMENT prop-filter ((is-not-defined | ((time-range | text-match)?,
 * param-filter*))>
 * 
 * <!ATTLIST prop-filter name CDATA #REQUIRED> 
 * name value: a calendar property
 * name (e.g., "ATTENDEE")
 * 
 */
public class PropertyFilter implements DavConstants, CaldavConstants {
    private IsNotDefinedFilter isNotDefinedFilter = null;

    private TimeRangeFilter timeRangeFilter = null;

    private TextMatchFilter textMatchFilter = null;

    private List<ParamFilter> paramFilters = new ArrayList<>();

    private String name = null;

    /**
     * Constructor.
     * @param name The name.
     */
    public PropertyFilter(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     */
    public PropertyFilter() {
    }
    
    /**
     * Constructor.
     * @param element The element.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public PropertyFilter(Element element) throws ParseException {
        this(element, null);
    }
    
    /**
     * Construct a PropertyFilter object from a DOM Element
     * @param element The element.
     * @param timezone The timezone.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public PropertyFilter(Element element, VTimeZone timezone) throws ParseException {
        // Name must be present
        name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);
        if (name == null) {
            throw new ParseException("CALDAV:prop-filter a calendar property name (e.g., \"ATTENDEE\") is required", -1);
        }

        ElementIterator i = DomUtil.getChildren(element);
        int childCount = 0;
        
        while (i.hasNext()) {
            Element child = i.nextElement();
            childCount++;
            
            // if is-not-defined is present, then nothing else can be present
            if(childCount>1 && isNotDefinedFilter!=null) {
                throw new ParseException("CALDAV:is-not-defined cannnot be present with other child"
                        + " elements",-1); 
            }
            if (ELEMENT_CALDAV_TIME_RANGE.
                equals(child.getLocalName())) {

                // Can only have one time-range or text-match
                if (timeRangeFilter!=null) {
                    throw new ParseException("CALDAV:prop-filter only one time-range or text-match "
                            + "element permitted", -1);
                }
                timeRangeFilter = new TimeRangeFilter(child, timezone);
            } else if (ELEMENT_CALDAV_TEXT_MATCH.
                       equals(child.getLocalName())) {

                // Can only have one time-range or text-match
                if (textMatchFilter!=null) {
                    throw new ParseException("CALDAV:prop-filter only one time-range or text-match element permitted", -1);
                }
      
                textMatchFilter = new TextMatchFilter(child);

            } else if (ELEMENT_CALDAV_PARAM_FILTER.
                       equals(child.getLocalName())) {

                // Add to list
                paramFilters.add(new ParamFilter(child));
            } else if(ELEMENT_CALDAV_IS_NOT_DEFINED.equals(child.getLocalName())) {
                if(childCount>1) {
                    throw new ParseException("CALDAV:is-not-defined cannnot be present with other "
                            + "child elements",-1);
                }
                isNotDefinedFilter = new IsNotDefinedFilter();
            } else {
                throw new ParseException("CALDAV:prop-filter an invalid element name found", -1);
            }
        }
    }

    /**
     * Gets isNotDefinedFilter.
     * @return isNotDefinedFilter.
     */
    public IsNotDefinedFilter getIsNotDefinedFilter() {
        return isNotDefinedFilter;
    }

    /**
     * Sets isNotDefinedFilter.
     * @param isNotDefinedFilter isNotDefinedFilter.
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
     * Gets param filters.
     * @return The filters.
     */
    public List<ParamFilter> getParamFilters() {
        return paramFilters;
    }

    /**
     * Sets param filters.
     * @param paramFilters Param filters.
     */
    public void setParamFilters(List<ParamFilter> paramFilters) {
        this.paramFilters = paramFilters;
    }

    /**
     * Gets text match filters.
     * @return The text match filters.
     */
    public TextMatchFilter getTextMatchFilter() {
        return textMatchFilter;
    }

    /**
     * Sets text match filters.
     * @param textMatchFilter The text match filters.
     */
    public void setTextMatchFilter(TextMatchFilter textMatchFilter) {
        this.textMatchFilter = textMatchFilter;
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
     * ToString
     * {@inheritDoc}
     * The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", name).
            append("timeRangeFilter", timeRangeFilter).
            append("textMatchFilter", textMatchFilter).
            append("isNotDefinedFilter", isNotDefinedFilter).
            append("paramFilters", paramFilters).
            toString();
    }
    
    /**
     * Validates.
     */
    public void validate() {
        if(textMatchFilter!=null) {
            textMatchFilter.validate();
        }
        for(Iterator<ParamFilter> it= paramFilters.iterator(); it.hasNext();) {
            it.next().validate();
        }
    }
}
