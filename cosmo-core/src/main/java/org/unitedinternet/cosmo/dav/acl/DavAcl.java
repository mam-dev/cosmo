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

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.unitedinternet.cosmo.dav.ExtendedDavConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A WebDAV access control list. An ACL is an ordered list of access control
 * entries (ACEs). Each ACE element specifies the set of privileges to be
 * either granted or denied to a single principal. If the ACL is empty, no
 * principal is granted any privilege.
 *
 * <pre>
 * <!ELEMENT acl (ace*) > 
 * </pre>
 */
public class DavAcl implements ExtendedDavConstants, XmlSerializable {

    private List<DavAce> aces;

    public DavAcl() {
        this.aces = new ArrayList<DavAce>(0);
    }

    public DavAcl(List<DavAce> aces) {
        this.aces = aces;
    }

    // XmlSerializable methods

    public Element toXml(Document document) {
        Element root =
            DomUtil.createElement(document, "acl", NAMESPACE);

        for (DavAce ace : aces)
            root.appendChild(ace.toXml(document));

        return root;
    }

    public List<DavAce> getAces() {
        return aces;
    }
}
