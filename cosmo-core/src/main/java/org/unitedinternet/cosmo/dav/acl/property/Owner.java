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

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.User;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the DAV:owner property.
 *
 * This property is protected. The value contains a DAV:href
 * elements specifying the principal URL for the owner of a resource.
 */
public class Owner extends StandardDavProperty {

    public Owner(DavResourceLocator locator,
                 User user) {
        super(OWNER, href(locator, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);
        if (getHref() != null) {
            name.appendChild(DomUtil.hrefToXml(getHref(), document));
        }
        return name;
    }

    private static String href(DavResourceLocator locator,
                               User user) {
        if (user == null) {
            return null;
        }
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(),
                                          user.getUsername());
    }
}
