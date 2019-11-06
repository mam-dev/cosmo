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
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.model.Ticket;

import org.unitedinternet.cosmo.security.Permission;
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
        for (Permission perm : ticket.getPermissions()) {
            add(PermissionPrivilegeConstants.PERMISSION_TO_PRIVILEGE.get(perm));
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
        ticket.getPermissions().clear();
        for (DavPrivilege privilege : PermissionPrivilegeConstants.PRIVILEGE_TO_PERMISSION.keySet()) {
            if (containsRecursive(privilege)) {
                ticket.getPermissions().add(PermissionPrivilegeConstants.PRIVILEGE_TO_PERMISSION.get(privilege));
            }
        }
    }

    public String toString() {
        return StringUtils.join(this, ", ");
    }

    /**
     * This method creates a DavPrivilegeSet from a set of DAV:privilege nodes as per ACL method in RFC.
     *
     * @param root parent of DAV:privilege nodes
     * @return
     */
    public static final DavPrivilegeSet fromXmlAcl(Element root) {
        DavPrivilegeSet dps = new DavPrivilegeSet();
        for (ElementIterator it = DomUtil.getChildren(root); it.hasNext(); ) {
            Element priv = it.next();
            dps.add(DavPrivilege.fromXml(priv));
        }
        return dps;
    }

    /***
     * This method creates a DavPrivilegeSet from a single DAV:privilege node encompassing all the children privileges
     * @param root DAV:privilege node with many children
     * @return
     */
    public static final DavPrivilegeSet createFromXml(Element root) {
        if (!DomUtil.matches(root, "privilege", NAMESPACE)) {
            throw new IllegalArgumentException("must be a DAV:privilege element, found " + root.getNodeName());
        }
        DavPrivilegeSet privileges = new DavPrivilegeSet();
        for (ElementIterator it = DomUtil.getChildren(root); it.hasNext(); ) {
            Element elem = it.next();
            privileges.add(DavPrivilege.extractPrivilege(elem));
        }
        return privileges;
    }
}
