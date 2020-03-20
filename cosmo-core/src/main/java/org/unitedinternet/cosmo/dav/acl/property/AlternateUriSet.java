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
 * Represents the DAV:alternate-URI-set property.
 *
 * This property is protected. Its value is empty.
 */
public class AlternateUriSet extends StandardDavProperty implements AclConstants {
    
    public AlternateUriSet() {
        super(ALTERNATEURISET, hrefs(), true);
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

    private static HashSet<String> hrefs() {
        HashSet<String> hrefs = new HashSet<String>();

        // XXX: find a way to add Atom and CMP URLs here

        return hrefs;
    }
}
