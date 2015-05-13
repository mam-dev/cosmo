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
package org.unitedinternet.cosmo.dav.caldav.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.model.User;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the CALDAV:schedule-inbox-URL property.
 *
 * The property is protected. The value is a single DAV:href element
 * containing the URI of schedule inbox.
 */
public class ScheduleInboxURL extends StandardDavProperty implements CaldavConstants {

    /**
     * Create ScheduleInboxURL object based on given parameters
     * @param locator DavResourceLocator
     * @param user Cosmo User
     */
    public ScheduleInboxURL(DavResourceLocator locator, User user) {
        super(SCHEDULEINBOXURL, href(locator, user), true);
    }

    /**
     * @return href value of this element
     */
    public String getHref() {
        return (String) getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dav.property.StandardDavProperty#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
        DomUtil.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(DavResourceLocator locator, User user) {
        return TEMPLATE_USER_INBOX.bindAbsolute(locator.getBaseHref(), user.getUsername());
    }
}
