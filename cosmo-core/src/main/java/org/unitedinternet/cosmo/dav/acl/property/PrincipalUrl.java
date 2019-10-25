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

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;

import org.unitedinternet.cosmo.model.UserBase;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the DAV:principal-URL property.
 *
 * The property is protected. The value is a single DAV:href element
 * containing the URL of the home collection.
 */
public class PrincipalUrl extends StandardDavProperty
    implements AclConstants {

    public PrincipalUrl(DavResourceLocator locator,
                        UserBase user) {
        super(PRINCIPALURL, PrincipalUtils.href(locator, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = DomUtil.createElement(document, XML_HREF, NAMESPACE);
        DomUtil.setText(href, getHref());
        name.appendChild(href);

        return name;
    }
}
