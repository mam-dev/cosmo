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
package org.unitedinternet.cosmo.dav.property;

import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the DAV:resourcetype property.
 */
public class ResourceType extends StandardDavProperty {

    public ResourceType(Set<QName> qnames) {
        super(DavPropertyName.RESOURCETYPE, qnames, true);
    }

    public Set<QName> getQnames() {
        return (Set<QName>) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (QName qn : getQnames()) {
            Namespace ns =
                Namespace.getNamespace(qn.getPrefix(), qn.getNamespaceURI());
            Element e =
                DomUtil.createElement(document, qn.getLocalPart(), ns);
            name.appendChild(e);
        }

        return name;
    }
}
