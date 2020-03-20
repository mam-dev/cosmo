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
package org.unitedinternet.cosmo.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * A helper for serializing a DOM structure to a UTF-8 string.
 * </p>
 * <p>
 * Only element, text and character data nodes are serialized. All other nodes are ignored. Whitespaces is significant.
 * Document order is preserved. Element attributes are serialized in document order. Prefixes for elements and
 * attributes are preserved.
 * </p>
 */
public class DomWriter {
   
    private static final Logger LOG = LoggerFactory.getLogger(DomWriter.class);
    
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    public static String write(Node n) throws XMLStreamException, IOException {
        XMLStreamWriter writer = null;
        try {
            if ((n.getNamespaceURI() != null && n.getAttributes().getLength() > 0)
                    && (n.getNamespaceURI().equals(n.getAttributes().item(0).getNodeValue()))) {
                n.getAttributes().removeNamedItem(n.getAttributes().item(0).getNodeName());
            }

            StringWriter out = new StringWriter();
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writeNode(n, writer);
            writer.close();
            return out.toString();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException e2) {
                    LOG.warn("Unable to close XML writer", e2);
                }
            }
        }
    }

    private static void writeNode(Node n, XMLStreamWriter writer) throws XMLStreamException {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            writeElement((Element) n, writer);
        } else if (n.getNodeType() == Node.CDATA_SECTION_NODE || n.getNodeType() == Node.TEXT_NODE) {
            writeCharacters((CharacterData) n, writer);
        } else {
            LOG.warn("Skipping element " + n.getNodeName());
        }
    }

    private static void writeElement(Element e, XMLStreamWriter writer) throws XMLStreamException {
        
        String local = e.getLocalName();
        if (local == null) {
            local = e.getNodeName();
        }

        String ns = e.getNamespaceURI();
        if (ns != null) {
            String prefix = e.getPrefix();
            if (prefix != null) {
                writer.writeStartElement(prefix, local, ns);
                writer.writeNamespace(prefix, ns);
            } else {
                writer.setDefaultNamespace(ns);
                writer.writeStartElement(ns, local);
                writer.writeDefaultNamespace(ns);
            }
        } else {
            writer.writeStartElement(local);
        }

        NamedNodeMap attributes = e.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            writeAttribute((Attr) attributes.item(i), writer);
        }

        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            writeNode(children.item(i), writer);
        }

        writer.writeEndElement();
    }

    private static void writeCharacters(CharacterData cd, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(cd.getData());
    }

    private static void writeAttribute(Attr a, XMLStreamWriter writer) throws XMLStreamException {        
        String local = a.getLocalName();
        if (local == null) {
            local = a.getNodeName();
        }
        String ns = a.getNamespaceURI();
        String value = a.getValue();

        // was handled by writing the default namespace in writeElement
        if (local.equals("xmlns")) {
            return;
        }

        if (ns != null) {
            String prefix = a.getPrefix();
            if (prefix != null) {
                writer.writeAttribute(prefix, ns, local, value);
            } else {
                writer.writeAttribute(ns, local, value);
            }
        } else {
            writer.writeAttribute(local, value);
        }
    }
}
