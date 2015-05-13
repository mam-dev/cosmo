/*
 * Copyright 2008 Open Source Applications Foundation
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

import net.fortuna.ical4j.model.property.RequestStatus;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarData;
import org.unitedinternet.cosmo.dav.caldav.property.Recipient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:response element.
 * 
 * @see http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05#section-11.2
 * @see http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05#section-8.1.2
 * 
 *      A response to a POST method that indicates status for one or more recipients MUST be a CALDAV:schedule-response
 *      XML element. This MUST contain one or more CALDAV:response elements for each recipient, with each of those
 *      containing elements that indicate which recipient they correspond to, the scheduling status of the request for
 *      that recipient, any error codes and an optional description.
 * 
 *      In the case of a freebusy request, the CALDAV:response elements can also contain CALDAV:calendar-data elements
 *      which contain freebusy information (e.g., an iCalendar VFREEBUSY component) indicating the busy state of the
 *      corresponding recipient, assuming that the freebusy request for that recipient succeeded.
 */
public class ScheduleResponse implements DavConstants, CaldavConstants, XmlSerializable {
    private String description = null;

    private CalendarData calendarData = null;

    private RequestStatus status = null;

    private Recipient recipient = null;

    /**
     * Creates ScheduleResponse based on recipient given, with status set to "2.0;Success"
     * 
     * @param recipient
     *            recipient of this response
     */
    public ScheduleResponse(String recipient) {
        this.recipient = new Recipient(recipient);
    }

    /**
     * Set the status of this response to given one
     * 
     * @param status
     *            status to set
     */
    public void setStatus(String status) {
        this.status = new RequestStatus(status, null, null);
    }

    /**
     * @return the status of this response
     */
    public RequestStatus getStatus() {
        if (status == null) {
            status = new RequestStatus("2.0", "Success", null);
        }
        return status;
    }

    /**
     * @return calendar data for this response, null if not set
     */
    public CalendarData getCalendarData() {
        return calendarData;
    }

    /**
     * Set calendar data of this response
     * 
     * @param calendarData
     *            data to set
     */
    public void setCalendarData(String calendarData) {
        this.calendarData = new CalendarData(calendarData);
    }

    /**
     * @return description of this response
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description of this response
     * 
     * @param description
     *            description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    /**
     * @param document The document.
     * @return The element created.
     */
    public Element toXml(Document document) {
        Element response = DomUtil.createElement(document, ELEMENT_CALDAV_RESPONSE, NAMESPACE_CALDAV);
        response.appendChild(recipient.toXml(document));

        Element statusElem = DomUtil.createElement(document, ELEMENT_CALDAV_REQUEST_STATUS, NAMESPACE_CALDAV, getStatus()
                .getValue());
        response.appendChild(statusElem);

        if (calendarData != null) {
            response.appendChild(calendarData.toXml(document));
        }
        if (description != null) {
            Element respDesc = DomUtil.createElement(document, XML_RESPONSEDESCRIPTION, NAMESPACE, description);
            response.appendChild(respDesc);
        }
        return response;
    }

}
