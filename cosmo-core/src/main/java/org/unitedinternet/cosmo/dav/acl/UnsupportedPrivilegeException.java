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
 * because the request specified an access control entry that is not
 * supported by the targeted resource.
 * </p>
 */
@SuppressWarnings("serial")
public class UnsupportedPrivilegeException extends ForbiddenException {

    public UnsupportedPrivilegeException(String message) {
        super(message);
    }

    /**
     *
     */
    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement("DAV:", "not-supported-privilege");
        if (getMessage() != null) {
            writer.writeCharacters(getMessage());
        }
        writer.writeEndElement();
    }
}
