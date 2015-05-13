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
package org.unitedinternet.cosmo.dav.ticket.property;

import java.util.Set;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.acl.DavPrivilegeSet;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.model.Ticket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the WebDAV Tickets ticketdiscovery property.
 */
public class TicketDiscovery extends StandardDavProperty
    implements TicketConstants {

    private DavResourceLocator locator;

    public TicketDiscovery(DavResourceLocator locator,
                           Set<Ticket> tickets) {
        super(TICKETDISCOVERY, tickets, true);
        this.locator = locator;
    }

    public Set<Ticket> getTickets() {
        return (Set<Ticket>) getValue();
    }
    
    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        String ownerBase = locator.getBaseHref(false);

        for (Ticket ticket : getTickets()) {
            Element ticketInfo =
                DomUtil.createElement(document, ELEMENT_TICKET_TICKETINFO,
                                      NAMESPACE_TICKET);
            name.appendChild(ticketInfo);

            Element id =
                DomUtil.createElement(document, ELEMENT_TICKET_ID,
                                      NAMESPACE_TICKET);
            DomUtil.setText(id, ticket.getKey());
            ticketInfo.appendChild(id);

            Element owner =
                DomUtil.createElement(document, XML_OWNER, NAMESPACE);
            Element href =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            String url =
                TEMPLATE_USER.bindAbsolute(ownerBase,
                                           ticket.getOwner().getUsername());
            DomUtil.setText(href, url);
            owner.appendChild(href);
            ticketInfo.appendChild(owner);

            Element timeout =
                DomUtil.createElement(document, ELEMENT_TICKET_TIMEOUT,
                                      NAMESPACE_TICKET);
            DomUtil.setText(timeout, ticket.getTimeout());
            ticketInfo.appendChild(timeout);
 
            // visit limits are not supported; the element remains to
            // comply with the current draft of the spec
            Element visits =
                DomUtil.createElement(document, ELEMENT_TICKET_VISITS,
                                      NAMESPACE_TICKET);
            DomUtil.setText(visits, VALUE_INFINITY);
            ticketInfo.appendChild(visits);

            DavPrivilegeSet privileges = new DavPrivilegeSet(ticket);
            ticketInfo.appendChild(privileges.toXml(document));
        }

        return name;
    }
}
