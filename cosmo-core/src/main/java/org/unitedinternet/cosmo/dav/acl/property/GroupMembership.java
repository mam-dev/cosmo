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
package org.unitedinternet.cosmo.dav.acl.property;

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the DAV:group-membership property.
 *
 * The property is protected. The value is a list of hrefs indicating
 * the groups in which the principal is directly a member. The list
 * will always contain 0 elements since groups are not yet supported.
 */
public class GroupMembership extends StandardDavProperty
    implements AclConstants {

    public GroupMembership() {
        super(GROUPMEMBERSHIP, new HashSet<String>(), true);
    }

    public Set<String> getHrefs() {
        return (Set<String>) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (String href : getHrefs()) {
            Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(e, href);
            name.appendChild(e);
        }

        return name;
    }

    /*private static HashSet<String> hrefs(DavResourceLocator locator,
                                         User user) {
        HashSet<String> hrefs = new HashSet<String>();
        // XXX: when we add groups, use the service locator to find the
        // principal url for each of the user's groups
        return hrefs;
    }*/
}
