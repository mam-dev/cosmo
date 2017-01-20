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
package org.unitedinternet.cosmo.dav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An exception indicating that the request attempted to modify a
 * protected property.
 */
@SuppressWarnings("serial")
public class ProtectedPropertyModificationException
    extends ForbiddenException {
    
    public ProtectedPropertyModificationException(DavPropertyName name) {
        super("Property " + name + " is protected");
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement("DAV:", "cannot-modify-protected-property");
        writer.writeEndElement();
    }
}
