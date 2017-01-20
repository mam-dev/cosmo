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

import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

/**
 * An exception indicating that the data enclosed in a calendar resource
 * was not of a supported media type.
 */
@SuppressWarnings("serial")
public class UnsupportedCalendarDataException
    extends ForbiddenException
    implements ICalendarConstants, CaldavConstants {

    /**
     * Constructor.
     */
    public UnsupportedCalendarDataException() {
        super("Calendar data must be of media type " + ICALENDAR_MEDIA_TYPE + ", version " + ICALENDAR_VERSION);
        getNamespaceContext().addNamespace(PRE_CALDAV, NS_CALDAV);
    }
    /**
     * Constructor.
     * @param mediaType The media type.
     */
    public UnsupportedCalendarDataException(String mediaType) {
        super("Calendar data of type " + mediaType + " not allowed; only " +
              CT_ICALENDAR);
        getNamespaceContext().addNamespace(PRE_CALDAV, NS_CALDAV);
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_CALDAV, "supported-calendar-data");
        writer.writeCharacters(getMessage());
        writer.writeEndElement();
    }
}
