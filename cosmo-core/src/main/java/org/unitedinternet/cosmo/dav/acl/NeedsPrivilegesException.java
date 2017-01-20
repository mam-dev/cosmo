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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.unitedinternet.cosmo.dav.ForbiddenException;

/**
 * <p>
 * An exception indicating that the request is not allowed to be processed
 * because the authenticated principal does not have the required privilege
 * to perform the requested method.
 * </p>
 */
@SuppressWarnings("serial")
public class NeedsPrivilegesException extends ForbiddenException {

    private String href;
    private transient DavPrivilege privilege;

    public NeedsPrivilegesException(String message) {
        super(message);
    }

    public NeedsPrivilegesException(String href,
                                    DavPrivilege privilege) {
        super(null);
        this.href = href;
        this.privilege = privilege;
    }

    /**
     * <pre>
     * !ELEMENT need-privileges (resource)* >
     * <!ELEMENT resource ( href , privilege ) >
     * </pre>
     */
    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement("DAV:", "needs-privileges");
        if (href != null && privilege != null) {
            writer.writeStartElement("DAV:", "resource");
            writer.writeStartElement("DAV:", "href");
            writer.writeCharacters(href);
            writer.writeEndElement();
            writer.writeStartElement("DAV:", "privilege");
            writer.writeStartElement(privilege.getQName().getNamespaceURI(),
                                     privilege.getQName().getLocalPart());
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        } else if (getMessage() != null) {
            writer.writeCharacters(getMessage());
        }
        writer.writeEndElement();
    }

    public String getHref() {
        return href;
    }

    public DavPrivilege getPrivilege() {
        return privilege;
    }
}
