/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CalDAV supported-calendar-data property.
 */
public class SupportedCalendarData extends StandardDavProperty
    implements ICalendarConstants, CaldavConstants {

    /**
     */
    public SupportedCalendarData() {
        super(SUPPORTEDCALENDARDATA, null, true);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Element toXml(Document document) {
        Element name = getName().toXml(document);
        
        Element element =
            DomUtil.createElement(document, ELEMENT_CALDAV_CALENDAR_DATA,
                                  NAMESPACE_CALDAV);
        DomUtil.setAttribute(element, ATTR_CALDAV_CONTENT_TYPE,
                             NAMESPACE_CALDAV, ICALENDAR_MEDIA_TYPE);
        DomUtil.setAttribute(element, ATTR_CALDAV_VERSION,
                             NAMESPACE_CALDAV, ICALENDAR_VERSION);
        
        name.appendChild(element);
        
        return name;
    }
   
}
