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

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.util.CosmoQName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * A WebDAV access control privilege.
 * </p>
 * <strong>Aggregate Privileges</strong>
 * <p>
 * From RFC 3744:
 * </p>
 * <blockquote>
 * Privileges may be containers of other privileges, in which case they are
 * termed "aggregate privileges". If a principal is granted or denied an
 * aggregate privilege, it is semantically equivalent to granting or denying
 * each of the aggregated privileges individually.
 * </blockquote>
 * <strong>Abstract Privileges</strong>
 * <p>
 * From RFC 3744:
 * </p>
 * <blockquote>
 * Privileges may be declared to be "abstract" for a given resource, in
 * which case they cannot be set in an ACE on that resource. Aggregate
 * and non-aggregate privileges are both capable of being abstract. Abstract
 * privileges are useful for modeling privileges that otherwise would not be
 * exposed via the protocol.
 * </blockquote>
 */
public class DavPrivilege
    implements ExtendedDavConstants, CaldavConstants, XmlSerializable,
    Comparable<DavPrivilege> {

    public static final DavPrivilege READ =
        new DavPrivilege(qn("read"));

    public static final DavPrivilege WRITE_PROPERTIES =
        new DavPrivilege(qn("write-properties"), true);
    public static final DavPrivilege WRITE_CONTENT =
        new DavPrivilege(qn("write-content"), true);
    public static final DavPrivilege BIND =
        new DavPrivilege(qn("bind"), true);
    public static final DavPrivilege UNBIND =
        new DavPrivilege(qn("unbind"), true);
    public static final DavPrivilege WRITE =
        new DavPrivilege(qn("write"),
                         new DavPrivilege[] { WRITE_PROPERTIES, WRITE_CONTENT,
                                              BIND, UNBIND });

    public static final DavPrivilege READ_CURRENT_USER_PRIVILEGE_SET =
        new DavPrivilege(qn("read-current-user-privilege-set"));
    public static final DavPrivilege READ_FREE_BUSY =
        new DavPrivilege(qn(NAMESPACE_CALDAV, "read-free-busy"));

    public static final DavPrivilege ALL =
        new DavPrivilege(qn("all"),
                         new DavPrivilege[] { READ, WRITE,
                                              READ_CURRENT_USER_PRIVILEGE_SET,
                                              READ_FREE_BUSY });

    private CosmoQName qname;
    private boolean isAbstract;
    private Set<DavPrivilege> subPrivileges;

    public DavPrivilege(CosmoQName qname) {
        this(qname, false, new DavPrivilege[0]);
    }

    public DavPrivilege(CosmoQName qname,
                        DavPrivilege[] subPrivileges) {
        this(qname, false, subPrivileges);
    }

    public DavPrivilege(CosmoQName qname,
                        boolean isAbstract) {
        this(qname, isAbstract, null);
    }

    public DavPrivilege(CosmoQName qname,
                        boolean isAbstract,
                        DavPrivilege[] subPrivileges) {
        this.qname = qname;
        this.isAbstract = isAbstract;
        this.subPrivileges = new HashSet<DavPrivilege>();
        if (subPrivileges != null) {
            for (DavPrivilege subPrivilege : subPrivileges) {
                this.subPrivileges.add(subPrivilege);
            }
        }
    }

    // XmlSerializable methods

    public Element toXml(Document document) {
        if (isAbstract) {
            return null;
        }
        Element privilege = DomUtil.createElement(document, 
                XML_PRIVILEGE, DavConstants.NAMESPACE); 
        privilege.appendChild(DomUtil.createElement(document, 
                qname.getLocalPart(), ns(qname)));
        return privilege;
    }

    // Comparable methods

    public int compareTo(DavPrivilege o) {
        return getQName().toString().compareTo(o.getQName().toString());
    }

    // our methods

    public boolean equals(Object o) {
        if (! (o instanceof DavPrivilege)) {
            return false;
        }
        DavPrivilege p = (DavPrivilege) o;
        return p.getQName().equals(qname);
    }
    
    public int hashCode() {
        return 1;
    }

    public boolean containsRecursive(DavPrivilege test) {
        for (DavPrivilege p : subPrivileges) {
            if (p.equals(test) || p.containsRecursive(test)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return qname.toString();
    }

    public CosmoQName getQName() {
        return qname;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public Set<DavPrivilege> getSubPrivileges() {
        return subPrivileges;
    }

    private static final CosmoQName qn(String local) {
        return qn(NAMESPACE, local);
    }

    private static final CosmoQName qn(Namespace ns,
                                  String local) {
        return new CosmoQName(ns.getURI(), local, ns.getPrefix());
    }

    private static final Namespace ns(CosmoQName qn) {
        return Namespace.getNamespace(qn.getPrefix(), qn.getNamespaceURI());
    }
}
