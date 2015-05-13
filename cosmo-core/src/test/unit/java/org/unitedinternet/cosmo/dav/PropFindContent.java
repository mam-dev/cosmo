/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav;

import java.util.HashSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Bean that encapsulates information about a DAV PROPFIND request.
 */
public class PropFindContent implements XmlSerializable, DavConstants {

    private HashSet<DavPropertyName> propertyNames;

    /**
     * Constructor.
     */
    public PropFindContent() {
        propertyNames = new HashSet<DavPropertyName>();
    }

    /**
     * Adds property name.
     * @param name Dav property name.
     */
    public void addPropertyName(DavPropertyName name) {
        propertyNames.add(name);
    }

    /**
     * toXML.
     * {@inheritDoc}
     * @param doc - The document.
     * @return The element.
     */
    public Element toXml(Document doc) {
        Element propfind = DomUtil.createElement(doc, XML_PROPFIND, NAMESPACE);

        if (propertyNames.isEmpty()) {
            // allprop
            Element allprop =
                DomUtil.createElement(doc, XML_ALLPROP, NAMESPACE);
            propfind.appendChild(allprop);
        }
        else {
            for (DavPropertyName propname: propertyNames) {
                Element name =
                    DomUtil.createElement(doc, propname.getName(),
                                          propname.getNamespace());

                Element prop = DomUtil.createElement(doc, XML_PROP, NAMESPACE);
                prop.appendChild(name);

                propfind.appendChild(prop);
            }
        }

        return propfind;
    }

    /**
     * ToString
     * {@inheritDoc}
     * @return toString.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("propertyNames", propertyNames).
            toString();
    }
}
