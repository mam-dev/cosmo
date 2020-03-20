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
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * A helper for deserializing a UTF-8 string into a DOM structure.
 * </p>
 * <p>
 * Only element, text and character data nodes are serialized. All other nodes are ignored. Whitespaces is significant.
 * Document order is preserved. Element attributes are serialized in document order. Prefixes for elements and
 * attributes are preserved.
 * </p>
 */
public class DomReader {
    
    private static final Logger LOG = LoggerFactory.getLogger(DomReader.class);
    
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();
    private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    public static Node read(String in) throws ParserConfigurationException, XMLStreamException, IOException {
        return read(new StringReader(in));
    }

    public static Node read(Reader in) throws ParserConfigurationException, XMLStreamException, IOException {
        XMLStreamReader reader = null;
        try {
            Document d = BUILDER_FACTORY.newDocumentBuilder().newDocument();
            reader = XML_INPUT_FACTORY.createXMLStreamReader(in);
            return readNode(d, reader);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e2) {
                    LOG.warn("Unable to close XML reader", e2);
                }
            }
        }
    }

    private static Node readNode(Document d, XMLStreamReader reader) throws XMLStreamException {
        Node root = null;
        Node current = null;

        while (reader.hasNext()) {
            reader.next();

            if (reader.isEndElement()) {
                
                if (current.getParentNode() == null) {
                    break;
                }
                current = current.getParentNode();
            }

            if (reader.isStartElement()) {
                Element e = readElement(d, reader);
                if (root == null) {
                    root = e;
                }

                if (current != null) {
                    current.appendChild(e);
                }

                current = e;
                continue;
            }

            if (reader.isCharacters()) {
                CharacterData cd = d.createTextNode(reader.getText());
                if (root == null) {
                    return cd;
                }
                if (current == null) {
                    return cd;
                }
                current.appendChild(cd);

                continue;
            }
        }

        return root;
    }

    private static Element readElement(Document d, XMLStreamReader reader) throws XMLStreamException {
        Element e = null;

        String local = reader.getLocalName();
        String ns = reader.getNamespaceURI();
        if (ns != null && !ns.equals("")) {
            String prefix = reader.getPrefix();
            String qualified = prefix != null && !prefix.isEmpty() ? prefix + ":" + local : local;
            e = d.createElementNS(ns, qualified);
        } else {
            e = d.createElement(local);
        }

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            Attr a = readAttribute(i, d, reader);
            if (a.getNamespaceURI() != null) {
                e.setAttributeNodeNS(a);
            } else {
                e.setAttributeNode(a);
            }
        }

        return e;
    }

    private static Attr readAttribute(int i, Document d, XMLStreamReader reader) throws XMLStreamException {
        Attr a = null;

        String local = reader.getAttributeLocalName(i);
        String ns = reader.getAttributeNamespace(i);
        if (ns != null && !ns.equals("")) {
            String prefix = reader.getAttributePrefix(i);
            String qualified = prefix != null ? prefix + ":" + local : local;
            a = d.createAttributeNS(ns, qualified);
        } else {
            a = d.createAttribute(reader.getAttributeLocalName(i));
        }
        a.setValue(reader.getAttributeValue(i));

        return a;
    }
}
