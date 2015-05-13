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

import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.Component;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CalDAV supported-calendar-component-set
 * property. Valid component types are defined by
 * {@link ComponentTypes}.
 */
public class SupportedCalendarComponentSet extends StandardDavProperty
    implements CaldavConstants, ICalendarConstants {
    private static String[] SUPPORTED_COMPONENT_TYPES = { Component.VEVENT,
        Component.VTODO, Component.VJOURNAL, Component.VFREEBUSY,
        COMPONENT_VAVAILABLITY };
    /**
     * Constructor.
     */
    public SupportedCalendarComponentSet() {
        this(SUPPORTED_COMPONENT_TYPES);
    }

    /**
     * Constructor.
     * @param componentTypes The component type.
     */
    public SupportedCalendarComponentSet(Set<String> componentTypes) {
        this((String[]) componentTypes.toArray(new String[componentTypes.size()]));
    }

    /**
     * Constructor.
     * @param componentTypes The componene type.
     */
    public SupportedCalendarComponentSet(String[] componentTypes) {
        super(SUPPORTEDCALENDARCOMPONENTSET, componentTypes(componentTypes),
                true);
        for (String type : componentTypes) {
            if (!CalendarUtils.isSupportedComponent(type)) {
                throw new IllegalArgumentException("Invalid component type '"
                        + type + "'.");
            }
        }
    }
    
    private static HashSet<String> componentTypes(String[] types) {
        HashSet<String> typesSet = new HashSet<String>();
        
        for (String t: types) {
            typesSet.add(t);
        }
        return typesSet;
    }

    public Set<String> getComponentTypes() {
        return (Set<String>) getValue();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (String type : getComponentTypes()) {
            Element e = DomUtil.createElement(document,
                    ELEMENT_CALDAV_COMP, NAMESPACE_CALDAV);
            DomUtil.setAttribute(e, ATTR_CALDAV_NAME,  Namespace.EMPTY_NAMESPACE, 
                    type);
            name.appendChild(e);
        }

        return name;
    }
}
