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
package org.unitedinternet.cosmo.dav.caldav;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.model.IcalUidInUseException;

/**
 * An exception indicating that the UID of a submitted calendar resource is
 * already in use within the calendar collection.
 */
public class UidConflictException extends ConflictException implements  CaldavConstants {

    private static final long serialVersionUID = 5465939098795076368L;
    private IcalUidInUseException root;
    
    /**
     * Constructor.
     * @param e The exception indicated the UID.
     */
    public UidConflictException(IcalUidInUseException e) {
        super(e.getMessage());
        this.root = e;
        getNamespaceContext().addNamespace(PRE_CALDAV, NS_CALDAV);
        getNamespaceContext().addNamespace(PRE_COSMO, NS_COSMO);
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_CALDAV, "no-uid-conflict");
        writer.writeStartElement(NS_COSMO, "existing-uuid");
        writer.writeCharacters(root.getExistingUid());
        writer.writeEndElement();
        writer.writeStartElement(NS_COSMO, "conflicting-uuid");
        writer.writeCharacters(root.getTestUid());
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
