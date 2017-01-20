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
package org.unitedinternet.cosmo.dav.caldav;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.fortuna.ical4j.model.Component;

import org.apache.commons.lang.StringUtils;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

/**
 * An exception indicating that a calendar resource did not contain any
 * supported calendar components.
 */
@SuppressWarnings("serial")
public class SupportedCalendarComponentException
    extends PreconditionFailedException
    implements ICalendarConstants, CaldavConstants {
    
    private static String[] SUPPORTED_COMPONENT_TYPES = { Component.VEVENT,
        Component.VTODO, Component.VJOURNAL, Component.VFREEBUSY,
        COMPONENT_VAVAILABLITY };
    /**
     * Constructor.
     */
    public SupportedCalendarComponentException() {
        super("Calendar object must contain at least one of " +
              StringUtils.join(SUPPORTED_COMPONENT_TYPES, ", "));
        getNamespaceContext().addNamespace(PRE_CALDAV, NS_CALDAV);
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_CALDAV, "supported-calendar-component");
        writer.writeCharacters(getMessage());
        writer.writeEndElement();
    }
}
