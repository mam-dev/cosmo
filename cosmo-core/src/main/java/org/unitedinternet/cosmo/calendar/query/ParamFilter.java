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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:param-filter element. From sec 9.6.3:
 * 
 * Name: param-filter
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Limits the search to specific parameter values.
 * 
 * Description: The CALDAV:param-filter XML element specifies a search criteria
 * on a specific calendar property parameter (e.g., PARTSTAT) in the scope of a
 * given CALDAV:prop-filter. A calendar property is said to match a
 * CALDAV:param-filter if:
 * 
 * A parameter of the type specified by the "name" attribute exists, and the
 * CALDAV:param-filter is empty, or it matches the CALDAV:text-match conditions
 * if specified.
 * 
 * or: A parameter of the type specified by the "name" attribute does not exist,
 * and the CALDAV:is-not-defined element is specified.
 * 
 * Definition:
 * 
 * <!ELEMENT param-filter (is-not-defined | text-match)?>
 * 
 * <!ATTLIST param-filter name CDATA #REQUIRED>
 * 
 * name value: a property parameter name (e.g., "PARTSTAT")
 * 
 */
public class ParamFilter implements DavConstants, CaldavConstants {

    private IsNotDefinedFilter isNotDefinedFilter = null;

    private TextMatchFilter textMatchFilter = null;

    private String name = null;

    /**
     * Constructor.
     * @param name The name.
     */
    public ParamFilter(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     */
    public ParamFilter() {
    }

    /**
     * Construct a ParamFilter object from a DOM Element
     * @param element The element.
     * @throws ParseException - if something is wrong this exception is thrown.
     */
    public ParamFilter(Element element) throws ParseException {
        // Get name which must be present
        name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);

        if (name == null) {
            throw new ParseException(
                    "CALDAV:param-filter a property parameter name (e.g., \"PARTSTAT\") is required",
                    -1);
        }

        // Can only have a single ext-match element
        ElementIterator i = DomUtil.getChildren(element);
        
        if(i.hasNext()) {
            
            Element child = i.nextElement();

            if (i.hasNext()) {
                throw new ParseException(
                        "CALDAV:param-filter only a single text-match or is-not-defined element is allowed",
                        -1);
            }

            if (ELEMENT_CALDAV_TEXT_MATCH.equals(child.getLocalName())) {
                textMatchFilter = new TextMatchFilter(child);

            } else if (ELEMENT_CALDAV_IS_NOT_DEFINED.equals(child.getLocalName())) {

                isNotDefinedFilter = new IsNotDefinedFilter();
            } else {
                throw new ParseException("CALDAV:param-filter an invalid element name found", -1);
            }
        }
    }

    /**
     * Is not defined filter.
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
     * Gets text match filter.
     * @return The text match filter.
     */
    public TextMatchFilter getTextMatchFilter() {
        return textMatchFilter;
    }

    /**
     * Sets textMatchFilter.
     * @param textMatchFilter textMatchFilter.
     */
    public void setTextMatchFilter(TextMatchFilter textMatchFilter) {
        this.textMatchFilter = textMatchFilter;
    }

    /**
     * ToString.
     * {@inheritDoc}
     * The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", name).
            append("textMatchFilter", textMatchFilter).
            append("isNotDefinedFilter", isNotDefinedFilter).
            toString();
    }
    
    /**
     * Validates.
     */
    public void validate() {
        if(textMatchFilter!=null) {
            textMatchFilter.validate();
        }
    }
}
