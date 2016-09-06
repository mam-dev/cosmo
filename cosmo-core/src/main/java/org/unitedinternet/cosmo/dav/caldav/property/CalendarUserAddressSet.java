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

import java.util.Set;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:calendar-user-address-set property.
 *
 * The property is protected. The value is a single DAV:href element
 * containing the URI of the user address.
 */
public class CalendarUserAddressSet extends StandardDavProperty implements CaldavConstants {
	private final Set<String> userEmails;
    /**
     * Create CalendarUserAddressSet object based on given parameters
     * @param locator DavResourceLocator
     * @param user Cosmo User
     */
    public CalendarUserAddressSet(User user, UserIdentitySupplier identitySupplier) {
        super(CALENDARUSERADDRESSSET, identitySupplier.forUser(user).getEmails(), true);
        this.userEmails = identitySupplier.forUser(user).getEmails();
    }

   

    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dav.property.StandardDavProperty#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document document) {
        Element calendarUserAddressSetNode = getName().toXml(document);
        
        for(String emailAddress : userEmails){
	        Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
	        DomUtil.setText(e, href(emailAddress));
	        calendarUserAddressSetNode.appendChild(e);
        }

        return calendarUserAddressSetNode;
    }

    private static String href(String emailAddress) {
        return "mailto:" + emailAddress;
    }
}