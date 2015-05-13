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
package org.unitedinternet.cosmo.dav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.acl.DavPrivilegeSet;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.hibernate.HibTicket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple wrapper bean for a <code>Ticket</code> that can convert it
 * to and from XML request and response content.
 */
public class TicketContent implements XmlSerializable, DavConstants, TicketConstants, ExtendedDavConstants {
    private Ticket ticket;
    private String ownerHref;

    /**
     * Constructs an instance suitable for use as request content
     * @param ticket The ticket.
     */
    public TicketContent(Ticket ticket) {
        this(ticket, null);
    }

    /**
     * Constructor.
     * @param ticket The ticket.
     * @param ownerHref The owner href.
     */
    private TicketContent(Ticket ticket, String ownerHref) {
        this.ticket = ticket;
        this.ownerHref = ownerHref;
    }

    /**
     * Returns the underlying ticket.
     *
     * If this object represents a ticket included in request
     * content, the underlying ticket does not have an owner, a key or
     * a created date.
     *
     * If this object represents a ticket included in response
     * content, the underlying ticket does not have an owner but
     * rather an owner href.
     * @return The ticket.
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Returns the href representing the ticket's owner.
     *
     * Only returns something useful when this object represents a
     * ticket included in response content.
     * @return The owner href.
     */
    public String getOwnerHref() {
        return ownerHref;
    }

    /**
     * Converts the underlying ticket to an XML fragment suitable
     * for use as request content (ignores any key, owner, created
     * date).
     * @param doc The document.
     * @return The element.
     */
    public Element toXml(Document doc) {
        Element e = DomUtil.createElement(doc, ELEMENT_TICKET_TICKETINFO,
                                          NAMESPACE_TICKET);

        Element timeout = DomUtil.createElement(doc, ELEMENT_TICKET_TIMEOUT,
                                                NAMESPACE_TICKET);
        DomUtil.setText(timeout, ticket.getTimeout());
        e.appendChild(timeout);

        DavPrivilegeSet privileges = new DavPrivilegeSet(ticket);
        e.appendChild(privileges.toXml(doc));

        return e;
    }

    /**
     * Returns a <code>TicketContent> populated with information from
     * the given XML fragment which is assumed to be response content
     * (containing an owner href rather than a <code>User</code>).
     *
     * The root element of the fragment must be a
     * <code>ticket:ticketinfo</code> element.
     * @param root The element.
     * @return The ticket content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public static TicketContent createFromXml(Element root) throws Exception {
        if (! DomUtil.matches(root, ELEMENT_TICKET_TICKETINFO,
                              NAMESPACE_TICKET)) {
            throw new Exception("root element not ticketinfo");
        }

        Ticket ticket = new HibTicket();

        String id = DomUtil.getChildTextTrim(root, ELEMENT_TICKET_ID,
                                             NAMESPACE_TICKET);
        ticket.setKey(id);

        String timeout =
            DomUtil.getChildTextTrim(root, ELEMENT_TICKET_TIMEOUT,
                                     NAMESPACE_TICKET);
        ticket.setTimeout(timeout);

        Element pe = DomUtil.getChildElement(root, XML_PRIVILEGE, NAMESPACE);
        DavPrivilegeSet privileges = DavPrivilegeSet.createFromXml(pe);
        privileges.setTicketPrivileges(ticket);

        Element owner = DomUtil.getChildElement(root, XML_OWNER, NAMESPACE);
        String ownerHref =
            DomUtil.getChildTextTrim(owner, XML_HREF, NAMESPACE);

        return new TicketContent(ticket, ownerHref);
    }
}
