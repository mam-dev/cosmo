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
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Preference;

/**
 * Parses and formats preferences in XHTML with a custom microformat (yet to be described.)
 */
public class XhtmlPreferenceFormat extends BaseXhtmlFormat implements PreferenceFormat {
    
    private static final Logger LOG = LoggerFactory.getLogger(XhtmlPreferenceFormat.class);

    public Preference parse(String source, EntityFactory entityFactory) throws ParseException {
        Preference pref = entityFactory.createPreference();

        try {
            if (source == null) {
                throw new ParseException("Source has no XML data", -1);
            }
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);

            boolean inPreference = false;
            while (reader.hasNext()) {
                reader.next();
                if (!reader.isStartElement()) {
                    continue;
                }

                if (hasClass(reader, "preference")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found preference element");
                    }
                    inPreference = true;
                    continue;
                }

                if (inPreference && hasClass(reader, "key")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found key element");
                    }

                    String key = reader.getElementText();
                    if (StringUtils.isBlank(key)) {
                        handleParseException("Key element must not be empty", reader);
                    }
                    pref.setKey(key);

                    continue;
                }

                if (inPreference && hasClass(reader, "value")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found value element");
                    }

                    String value = reader.getElementText();
                    if (StringUtils.isBlank(value)) {
                        value = "";
                    }
                    pref.setValue(value);

                    continue;
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        return pref;
    }

    public String format(Preference pref) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "preference");

            writer.writeCharacters("Preference: ");

            if (pref.getKey() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "key");
                writer.writeCharacters(pref.getKey());
                writer.writeEndElement();
            }

            writer.writeCharacters(" = ");

            if (pref.getValue() != null) {
                writer.writeStartElement("span");
                writer.writeAttribute("class", "value");
                writer.writeCharacters(pref.getValue());
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.close();

            return sw.toString();
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException("Error formatting XML", e);
        }
    }
}
