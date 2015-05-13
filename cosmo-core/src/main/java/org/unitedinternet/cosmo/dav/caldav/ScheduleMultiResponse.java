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

import java.util.ArrayList;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:schedule-response element.
 * @see http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05#section-11.1
 * @see http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05#section-8.1.2
 *
 * A response to a POST method that indicates status for one or more
 * recipients MUST be a CALDAV:schedule-response XML element.  This MUST
 * contain one or more CALDAV:response elements for each recipient, with
 * each of those containing elements that indicate which recipient they
 * correspond to, the scheduling status of the request for that
 * recipient, any error codes and an optional description.
 */
public class ScheduleMultiResponse implements CaldavConstants, XmlSerializable {
    private ArrayList<ScheduleResponse> responses = new ArrayList<ScheduleResponse>(1);

    public boolean addResponse(ScheduleResponse response) {
        return responses.add(response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document )
     */
    public Element toXml(Document document) {
        Element multistatus = DomUtil.createElement(document, ELEMENT_CALDAV_SCHEDULE_RESPONSE, NAMESPACE_CALDAV);
        for (ScheduleResponse response : responses) {
            multistatus.appendChild(response.toXml(document));
        }
        return multistatus;
    }

}
