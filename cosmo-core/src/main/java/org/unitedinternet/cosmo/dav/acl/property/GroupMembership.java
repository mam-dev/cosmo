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
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserBase;
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




    public GroupMembership(DavResourceLocator locator, UserBase user) {
        super(GROUPMEMBERSHIP, hrefs(locator, user), true);

    }

    public Set<String> getHrefs() {
        return (Set<String>) getValue();
    }

    @Override
    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (String href : getHrefs()) {
            Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(e, href);
            name.appendChild(e);
        }

        return name;
    }

    private static HashSet<String> hrefs(DavResourceLocator locator,
                                         UserBase userOrGroup) {
        HashSet<String> hrefs = new HashSet<String>();
        if (userOrGroup instanceof User)
        // XXX: when we add groups, use the service locator to find the
        // principal url for each of the user's groups
        {
            User user = (User) userOrGroup;
            for (Group group : user.getGroups()) {
                hrefs.add(PrincipalUtils.href(locator, group));
            }
        } else if (userOrGroup instanceof Group) {
            // TODO groups inside groups
        } else {
            throw new CosmoException();
        }
        return hrefs;
    }
}
