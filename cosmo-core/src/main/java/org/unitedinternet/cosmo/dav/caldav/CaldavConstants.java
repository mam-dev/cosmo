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
package org.unitedinternet.cosmo.dav.caldav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.unitedinternet.cosmo.util.CosmoQName;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types defined by the CalDAV
 * spec.
 */
public interface CaldavConstants {
	
    public static final String VALUE_YES = "yes";
    public static final String VALUE_NO = "no";
    /** The iCalendar media type */
    public static final String CT_ICALENDAR = "text/calendar";
    /** The media type for calendar collections */
    public static final String CONTENT_TYPE_CALENDAR_COLLECTION =
        "application/x-calendar-collection";

    /** The CalDAV XML namespace */
    public static final String PRE_CALDAV = "C";
    public static final String NS_CALDAV =
        "urn:ietf:params:xml:ns:caldav";
    public static final Namespace NAMESPACE_CALDAV =
        Namespace.getNamespace(PRE_CALDAV, NS_CALDAV);
    
    /** The Calendar Server XML namespace */
    public static final String PRE_CS = "CS";
    public static final String NS_CS =
        "http://calendarserver.org/ns/";
    public static final Namespace NAMESPACE_CS =
        Namespace.getNamespace(PRE_CS, NS_CS);

    /** The CalDAV XML element name <CALDAV:mkcalendar> */
    public static final String ELEMENT_CALDAV_MKCALENDAR = "mkcalendar";
    public static final String QN_MKCALENDAR = 
        DomUtil.getExpandedName(ELEMENT_CALDAV_MKCALENDAR, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:calendar> */
    public static final String ELEMENT_CALDAV_CALENDAR = "calendar";
    /** The CalDAV XML element name <CALDAV:comp> */
    public static final String ELEMENT_CALDAV_COMP = "comp";
    /** The CalDAV XML element name <CALDAV:supported-collation> */
    public static final String ELEMENT_CALDAV_SUPPORTEDCOLLATION = "supported-collation";
    /** The CalDAV XML element name <CALDAV:calendar-data> */
    public static final String ELEMENT_CALDAV_CALENDAR_DATA = "calendar-data";
    public static final String QN_CALDAV_CALENDAR_DATA = 
        DomUtil.getExpandedName(ELEMENT_CALDAV_CALENDAR_DATA, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:timezone> */
    public static final String ELEMENT_CALDAV_TIMEZONE = "timezone";
    public static final String QN_CALDAV_TIMEZONE = 
        DomUtil.getExpandedName(ELEMENT_CALDAV_TIMEZONE, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:allcomp> */
    public static final String ELEMENT_CALDAV_ALLCOMP = "allcomp";
    /** The CalDAV XML element name <CALDAV:allprop> */
    public static final String ELEMENT_CALDAV_ALLPROP = "allprop";
    /** The CalDAV XML element name <CALDAV:prop> */
    public static final String ELEMENT_CALDAV_PROP = "prop";
    /** The CalDAV XML element name <CALDAV:expand> */
    public static final String ELEMENT_CALDAV_EXPAND = "expand";
    /** The CalDAV XML element name <CALDAV:limit-recurrence-set> */
    public static final String ELEMENT_CALDAV_LIMIT_RECURRENCE_SET =
        "limit-recurrence-set";
    /** The CalDAV XML element name <CALDAV:limit-freebusy-set> */
    public static final String ELEMENT_CALDAV_LIMIT_FREEBUSY_SET =
        "limit-freebusy-set";
    /** The CalDAV XML element name <CALDAV:filter> */
    public static final String ELEMENT_CALDAV_FILTER = "filter";
    /** The CalDAV XML element name <CALDAV:comp-filter> */
    public static final String ELEMENT_CALDAV_COMP_FILTER = "comp-filter";
    /** The CalDAV XML element name <CALDAV:prop-filter> */
    public static final String ELEMENT_CALDAV_PROP_FILTER = "prop-filter";
    /** The CalDAV XML element name <CALDAV:param-filter> */
    public static final String ELEMENT_CALDAV_PARAM_FILTER = "param-filter";
    /** The CalDAV XML element name <CALDAV:time-range> */
    public static final String ELEMENT_CALDAV_TIME_RANGE = "time-range";
    public static final String QN_CALDAV_TIME_RANGE = 
        DomUtil.getExpandedName(ELEMENT_CALDAV_TIME_RANGE, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:is-not-defined> */
    public static final String ELEMENT_CALDAV_IS_NOT_DEFINED = "is-not-defined";
    /** The (old) CalDAV XML element name <CALDAV:is-defined> */
    public static final String ELEMENT_CALDAV_IS_DEFINED = "is-defined";
    /** The CalDAV XML element name <CALDAV:text-match> */
    public static final String ELEMENT_CALDAV_TEXT_MATCH = "text-match";
    /** The CalDAV XML element name <CALDAV:calendar-multiget> */
    public static final String ELEMENT_CALDAV_CALENDAR_MULTIGET =
        "calendar-multiget";
    /** The CalDAV XML element name <CALDAV:calendar-query> */
    public static final String ELEMENT_CALDAV_CALENDAR_QUERY =
        "calendar-query";
    /** The CalDAV XML element name <CALDAV:free-busy-query> */
    public static final String ELEMENT_CALDAV_CALENDAR_FREEBUSY =
        "free-busy-query";
    /** The CalDAV XML element name <CALDAV:calendar-home-set> */
    public static final String ELEMENT_CALDAV_CALENDAR_HOME_SET =
        "calendar-home-set";
    /** The CalDAV XML element name <CALDAV:calendar-user-address-set> */
    public static final String ELEMENT_CALDAV_CALENDAR_USER_ADDRESS_SET =
        "calendar-user-address-set";
    /** The CalDAV XML element name <CALDAV:schedule-inbox> */
    public static final String ELEMENT_CALDAV_SCHEDULE_INBOX =
        "schedule-inbox";
    /** The CalDAV XML element name <CALDAV:schedule-outbox> */
    public static final String ELEMENT_CALDAV_SCHEDULE_OUTBOX =
        "schedule-outbox";
    /** The CalDAV XML element name <CALDAV:schedule-inbox-URL> */
    public static final String ELEMENT_CALDAV_SCHEDULE_INBOX_URL =
        "schedule-inbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-outbox-URL> */
    public static final String ELEMENT_CALDAV_SCHEDULE_OUTBOX_URL =
        "schedule-outbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-response> */
    public static final String ELEMENT_CALDAV_SCHEDULE_RESPONSE =
        "schedule-response";
    /** The CalDAV XML element name <CALDAV:response> */
    public static final String ELEMENT_CALDAV_RESPONSE = "response";
    /** The CalDAV XML element name <CALDAV:recipient> */
    public static final String ELEMENT_CALDAV_RECIPIENT = "recipient";
    /** The CalDAV XML element name <CALDAV:request-status> */
    public static final String ELEMENT_CALDAV_REQUEST_STATUS = "request-status";

    /** The CalDAV XML attribute name CALDAV:name */
    public static final String ATTR_CALDAV_NAME = "name";
    /** The CalDAV XML attribute name CALDAV:content-type */
    public static final String ATTR_CALDAV_CONTENT_TYPE = "content-type";
    /** The CalDAV XML attribute name CALDAV:version */
    public static final String ATTR_CALDAV_VERSION = "version";
    /** The CalDAV XML attribute name CALDAV:novalue */
    public static final String ATTR_CALDAV_NOVALUE = "novalue";
    /** The CalDAV XML attribute name CALDAV:collation */
    public static final String ATTR_CALDAV_COLLATION = "collation";
    /** The CalDAV XML attribute name CALDAV:negate-condition */
    public static final String ATTR_CALDAV_NEGATE_CONDITION = "negate-condition";
    /** The CalDAV XML attribute name CALDAV:start */
    public static final String ATTR_CALDAV_START = "start";
    /** The CalDAV XML attribute name CALDAV:end */
    public static final String ATTR_CALDAV_END = "end";

    /** The CalDAV property name CALDAV:calendar-data */
    public static final String PROPERTY_CALDAV_CALENDAR_DATA =
        "calendar-data";
    /** The CalDAV property name CALDAV:calendar-description */
    public static final String PROPERTY_CALDAV_CALENDAR_DESCRIPTION =
        "calendar-description";
    /** The CalDAV property name CALDAV:calendar-timezone */
    public static final String PROPERTY_CALDAV_CALENDAR_TIMEZONE =
        "calendar-timezone";
    /** The CalDAV property name CALDAV:supported-calendar-component-set */
    public static final String PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET = "supported-calendar-component-set";
    /** The CalDAV property name CALDAV:supported-collation-set */
    public static final String PROPERTY_CALDAV_SUPPORTED_COLLATION_SET = 
        "supported-collation-set";
    /** The CalDAV property name CALDAV:supported-calendar-data */
    public static final String PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA =
        "supported-calendar-data";
    /** The CalDAV property name CALDAV:calendar-home-set */
    public static final String PROPERTY_CALDAV_CALENDAR_HOME_SET =
        "calendar-home-set";
    /** The CalDAV XML element name <CALDAV:calendar-user-address-set> */
    public static final String PROPERTY_CALDAV_CALENDAR_USER_ADDRESS_SET =
        "calendar-user-address-set";
    /** The CalDAV XML element name <CALDAV:recipient> */
    public static final String PROPERTY_CALDAV_RECIPIENT =
        "recipient";
    /** The CalDAV XML element name <CALDAV:schedule-inbox-URL> */
    public static final String PROPERTY_CALDAV_SCHEDULE_INBOX_URL =
        "schedule-inbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-outbox-URL> */
    public static final String PROPERTY_CALDAV_SCHEDULE_OUTBOX_URL =
        "schedule-outbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-inbox> */
    public static final String PROPERTY_CALDAV_SCHEDULE_INBOX = 
        "schedule-inbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-outbox> */
    public static final String PROPERTY_CALDAV_SCHEDULE_OUTBOX =
        "schedule-outbox-URL";
    /** The CalDAV property name CALDAV:max-resource-size */
    public static final String PROPERTY_CALDAV_MAX_RESOURCE_SIZE =
        "max-resource-size";
    
    /** The Calendar Server property name CS:getctag */
    public static final String PROPERTY_CS_GET_CTAG = "getctag";

    /** The CalDAV property CALDAV:calendar-data */
    public static final DavPropertyName CALENDARDATA =
        DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_DATA,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-description */
    public static final DavPropertyName CALENDARDESCRIPTION =
        DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_DESCRIPTION,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-timezone */
    public static final DavPropertyName CALENDARTIMEZONE =
        DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_TIMEZONE,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-calendar-component-set */
    public static final DavPropertyName SUPPORTEDCALENDARCOMPONENTSET =
        DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-collation-component-set */
    public static final DavPropertyName SUPPORTEDCOLLATIONSET =
        DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_COLLATION_SET,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-calendar-data */
    public static final DavPropertyName SUPPORTEDCALENDARDATA =
        DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-home-set */
    public static final DavPropertyName CALENDARHOMESET =
        DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_HOME_SET,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-user-address-set */
    public static final DavPropertyName CALENDARUSERADDRESSSET =
        DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_USER_ADDRESS_SET,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:recipient */
    public static final DavPropertyName RECIPIENT =
        DavPropertyName.create(PROPERTY_CALDAV_RECIPIENT,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-inbox-URL */
    public static final DavPropertyName SCHEDULEINBOXURL =
        DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_INBOX_URL,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-outbox-URL */
    public static final DavPropertyName SCHEDULEOUTBOXURL =
        DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_OUTBOX_URL,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-inbox */
    public static final DavPropertyName SCHEDULEINBOX =
        DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_INBOX,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-outbox */
    public static final DavPropertyName SCHEDULEOUTBOX =
        DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_OUTBOX,
                               NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:max-resource-size */
    public static final DavPropertyName MAXRESOURCESIZE =
        DavPropertyName.create(PROPERTY_CALDAV_MAX_RESOURCE_SIZE,
                               NAMESPACE_CALDAV);

    public static final CosmoQName RESOURCE_TYPE_SCHEDULE_INBOX =
        new CosmoQName(NS_CALDAV, ELEMENT_CALDAV_SCHEDULE_INBOX, PRE_CALDAV);
    
    public static final CosmoQName RESOURCE_TYPE_SCHEDULE_OUTBOX =
        new CosmoQName(NS_CALDAV, ELEMENT_CALDAV_SCHEDULE_OUTBOX, PRE_CALDAV);
    
    public static final CosmoQName RESOURCE_TYPE_CALENDAR =
        new CosmoQName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR, PRE_CALDAV);
    
    /** The Calendar Server property CS:getctag */
    public static final DavPropertyName GET_CTAG =
        DavPropertyName.create(PROPERTY_CS_GET_CTAG,
                               NAMESPACE_CS);
}
