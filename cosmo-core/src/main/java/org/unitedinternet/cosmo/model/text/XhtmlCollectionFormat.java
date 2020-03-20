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
package org.unitedinternet.cosmo.model.text;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.CosmoXMLStreamException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * Parses and formats a simple XHTML representation of a collection. This representation only describes the properties
 * of a collection that are directly settable by a protocol client (e.g. the display name).
 */
public class XhtmlCollectionFormat extends BaseXhtmlFormat implements CollectionFormat {

    private static final Logger LOG = LoggerFactory.getLogger(XhtmlCollectionFormat.class);

    public CollectionItem parse(String source, EntityFactory entityFactory) throws ParseException {
        CollectionItem collection = entityFactory.createCollection();

        try {
            if (source == null) {
                throw new ParseException("Source has no XML data", -1);
            }
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);

            boolean inCollection = false;
            while (reader.hasNext()) {
                reader.next();
                if (!reader.isStartElement()) {
                    continue;
                }

                if (hasClass(reader, "collection")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found collection element");
                    }
                    inCollection = true;
                    continue;
                }

                if (inCollection && hasClass(reader, "name")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found name element");
                    }

                    String name = reader.getElementText();
                    if (StringUtils.isBlank(name)) {
                        throw new ParseException("Empty name not allowed", reader.getLocation().getCharacterOffset());
                    }
                    collection.setDisplayName(name);

                    continue;
                }

                if (inCollection && hasClass(reader, "uuid")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found uuid element");
                    }

                    String uuid = reader.getElementText();
                    if (StringUtils.isBlank(uuid)) {
                        throw new ParseException("Empty uuid not allowed", reader.getLocation().getCharacterOffset());
                    }
                    collection.setUid(uuid);

                    continue;
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        return collection;
    }

    public String format(CollectionItem collection) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "collection");

            writer.writeCharacters("Collection: ");

            if (collection.getDisplayName() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "name");
                writer.writeCharacters(collection.getDisplayName());
                writer.writeEndElement();
            }

            if (collection.getUid() != null) {
                writer.writeCharacters(" (uuid ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "uuid");
                writer.writeCharacters(collection.getUid());
                writer.writeEndElement();
                writer.writeCharacters(")");
            }

            writer.writeEndElement();
            writer.close();

            return sw.toString();
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException("Error formatting XML", e);
        }
    }
}
