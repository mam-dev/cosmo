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
package org.unitedinternet.cosmo.dav.acl.property;

import java.util.Set;

import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the DAV:current-user-privilege-set property.
 * </p>
 * <p>
 * The property is protected. The value is a set of privileges granted to
 * the currently authenticated principal.
 * </p>
 * <pre>
 * <!ELEMENT current-user-privilege-set (privilege*)>
 * <!ELEMENT privilege ANY>
 * </pre>
 */
public class CurrentUserPrivilegeSet extends StandardDavProperty
    implements AclConstants {

    public CurrentUserPrivilegeSet(Set<DavPrivilege> privileges) {
        super(CURRENTUSERPRIVILEGESET, privileges, true);
    }

    public Set<DavPrivilege> getPrivileges() {
        return (Set<DavPrivilege>) getValue();
    }

    public Element toXml(Document document) {
        Element e = getName().toXml(document);

        for (DavPrivilege privilege : getPrivileges()) {
            e.appendChild(privilegeXml(document, privilege));
        }

        return e;
    }

    public Element privilegeXml(Document document,
                                DavPrivilege privilege) {
        Element p = privilege.toXml(document);

        for (DavPrivilege subPrivilege : privilege.getSubPrivileges()) {
            if (subPrivilege.isAbstract()) {
                continue;
            }
            p.appendChild(privilegeXml(document, subPrivilege));
        }

        return p;
    }
}
