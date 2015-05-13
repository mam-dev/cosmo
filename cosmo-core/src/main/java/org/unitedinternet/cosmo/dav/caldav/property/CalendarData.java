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
package org.unitedinternet.cosmo.dav.caldav.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the <code>CALDAV:calendar-data</code> property as used
 * to transmit a set of icalendar objects in the body of a report
 * response.
 */
public class CalendarData extends StandardDavProperty
    implements CaldavConstants, ICalendarConstants {

    /**
     * Constructor.
     * @param calendarData The calendar data used to transmit a set of icalendar object
     * in the report's body.
     */
    public CalendarData(String calendarData) {
        super(CALENDARDATA, calendarData, true);
    }
    /**
     * 
     * {@inheritDoc}
     */
    public Element toXml(Document document) {
        Element e = super.toXml(document);

        DomUtil.setAttribute(e, ATTR_CALDAV_CONTENT_TYPE,
                             NAMESPACE_CALDAV, ICALENDAR_MEDIA_TYPE);
        DomUtil.setAttribute(e, ATTR_CALDAV_VERSION,
                             NAMESPACE_CALDAV, ICALENDAR_VERSION);

        return e;
    }
}
