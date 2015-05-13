/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.acl;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.model.Ticket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * A set of WebDAV access control privileges.
 * </p>
 
 */
public class DavPrivilegeSet extends HashSet<DavPrivilege>
    implements ExtendedDavConstants, CaldavConstants, XmlSerializable {

    private static final long serialVersionUID = 1977693588813371074L;

    public DavPrivilegeSet() {
        super();
    }

    public DavPrivilegeSet(DavPrivilege... privileges) {
        super(Arrays.asList(privileges));
    }

    public DavPrivilegeSet(Ticket ticket) {
        super();
        for (String priv : ticket.getPrivileges()) {
            if (priv.equals(Ticket.PRIVILEGE_READ)) {
                add(DavPrivilege.READ);
            }
            else if (priv.equals(Ticket.PRIVILEGE_WRITE)) {
                add(DavPrivilege.WRITE);
            }
            else if (priv.equals(Ticket.PRIVILEGE_FREEBUSY)) {
                add(DavPrivilege.READ_FREE_BUSY);
            }
            else {
                throw new IllegalStateException("Unrecognized ticket privilege " + priv);
            }
        }
    }

    // XmlSerializable methods

    public Element toXml(Document document) {
        Element root =
            DomUtil.createElement(document, "privilege", NAMESPACE);
        for (DavPrivilege p : this) {
            if (p.isAbstract()) {
                continue;
            }
            root.appendChild(p.toXml(document));
        }
        return root;
    }

    // our methods

    public boolean containsAny(DavPrivilege... privileges) {
        for (DavPrivilege p : privileges) {
            if (contains(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsRecursive(DavPrivilege test) {
        for (DavPrivilege p : this) {
            if (p.equals(test) || p.containsRecursive(test)) {
                return true;
            }
        }
        return false;
    }

    public void setTicketPrivileges(Ticket ticket) {
        ticket.getPrivileges().clear();
        if (contains(DavPrivilege.READ)) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        }
        if (contains(DavPrivilege.WRITE)) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
        }
        if (contains(DavPrivilege.READ_FREE_BUSY)) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_FREEBUSY);
        }
    }

    public String toString() {
        return StringUtils.join(this, ", ");
    }

    public static final DavPrivilegeSet createFromXml(Element root) {
        if (! DomUtil.matches(root, "privilege", NAMESPACE)) {
            throw new IllegalArgumentException("Expected DAV:privilege element");
        }
        DavPrivilegeSet privileges = new DavPrivilegeSet();

        if (DomUtil.hasChildElement(root, "read", NAMESPACE)) {
            privileges.add(DavPrivilege.READ);
        }
        if (DomUtil.hasChildElement(root, "write", NAMESPACE)) {
            privileges.add(DavPrivilege.WRITE);
        }
        if (DomUtil.hasChildElement(root, "read-free-busy", NAMESPACE_CALDAV)) {
            privileges.add(DavPrivilege.READ_FREE_BUSY);
        }

        return privileges;
    }
}
