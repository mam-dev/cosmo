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
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:recipient property.
 *
 * The property is protected. The value is a single DAV:href element
 * containing the URI of the recipient.
 */
public class Recipient extends StandardDavProperty implements CaldavConstants {

    /**
     * Create Recipient object based on given parameters
     * @param recipient recipient to set
     */
    public Recipient(String recipient) {
        super(RECIPIENT, recipient, true);
    }

    /**
     * Create Recipient object based on given parameters
     * @param user user to set
     */
    public Recipient(User user) {
        super(RECIPIENT, href(user), true);
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

    private static String href(User user) {
        return "mailto:" + user.getUsername();
    }
}
