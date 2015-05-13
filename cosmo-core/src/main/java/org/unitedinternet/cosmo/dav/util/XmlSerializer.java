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
package org.unitedinternet.cosmo.dav.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.unitedinternet.cosmo.CosmoParseException;
import org.w3c.dom.Document;

/**
 * A utility class for serializing {@link XmlSerializable} objects.
 *
 * @see XmlSerializable
 */
public class XmlSerializer {
    private static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    public static String serialize(XmlSerializable serializable)
        throws IOException {
        StringWriter out = new StringWriter();
        try {
            Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
            doc.appendChild(serializable.toXml(doc));
            
            OutputFormat format = new OutputFormat("xml", "UTF-8", true);
            XMLSerializer serializer =
                new XMLSerializer(out, format);
            serializer.setNamespaces(true);
            serializer.asDOMSerializer().serialize(doc);

            return out.toString();
        } catch (ParserConfigurationException e) {
            throw new CosmoParseException(e);
        }
    }
}
