/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

/**
 * Filter for querying calendar events. The structure of this filter is based on
 * the structure of the <CALDAV:filter> element.
 * @author cdobrota 
 * See section 9.6 of the CalDAV spec
 */
public class CalendarFilter implements CaldavConstants {

    /**
     * Core filter for this calendar filter.
     */
    private ComponentFilter filter;

    /**
     * Constructor.
     */
    public CalendarFilter() {
    }

    /**
     * Constructor.
     * @param element The element.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public CalendarFilter(Element element) throws ParseException {
        this(element, null);
    }
    
    /**
     * Construct a CalendarFilter object from a DOM Element.
     * @param element The element.
     * @param timezone The timezone. 
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public CalendarFilter(Element element, VTimeZone timezone) throws ParseException {
        // Can only have a single comp-filter element
        final ElementIterator i = DomUtil.getChildren(element,
                ELEMENT_CALDAV_COMP_FILTER, NAMESPACE_CALDAV);
        if (!i.hasNext()) {
            throw new ParseException(
                    "CALDAV:filter must contain a comp-filter", -1);
        }

        final Element child = i.nextElement();

        if (i.hasNext()) {
            throw new ParseException(
                    "CALDAV:filter can contain only one comp-filter", -1);
        }

        // Create new component filter and have it parse the element
        filter = new ComponentFilter(child, timezone);
    }

    /**
     * A CalendarFilter has exactly one ComponentFilter.
     * @return The component filter.
     */
    public ComponentFilter getFilter() {
        return filter;
    }

    /**
     * @param filter The filter.
     */
    public void setFilter(ComponentFilter filter) {
        this.filter = filter;
    }

    /**
     * ToString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("filter", filter).
            toString();
    }
    
    /**
     * Validates.
     */
    public void validate() {
        if (filter != null) {
            filter.validate();
        }
    }
}
