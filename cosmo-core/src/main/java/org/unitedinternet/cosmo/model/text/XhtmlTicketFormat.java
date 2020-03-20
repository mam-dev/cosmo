/*
 * Copyright 2008 Open Source Applications Foundation
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
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;

/**
 * Parses and formats tickets in XHTML with a custom microformat (yet to be described.)
 */
public class XhtmlTicketFormat extends BaseXhtmlFormat implements TicketFormat {

    private static final Logger LOG = LoggerFactory.getLogger(XhtmlTicketFormat.class);

    public Ticket parse(String source, EntityFactory entityFactory) throws ParseException {

        String key = null;
        TicketType type = null;
        Integer timeout = null;
        try {
            if (source == null) {
                throw new ParseException("Source has no XML data", -1);
            }
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);

            boolean inTicket = false;
            while (reader.hasNext()) {
                reader.next();
                if (!reader.isStartElement()) {
                    continue;
                }

                if (hasClass(reader, "ticket")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found ticket element");
                    }
                    inTicket = true;
                    continue;
                }

                if (inTicket && hasClass(reader, "key")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found key element");
                    }

                    key = reader.getElementText();
                    if (StringUtils.isBlank(key)) {
                        handleParseException("Key element must not be empty", reader);
                    }

                    continue;
                }

                if (inTicket && hasClass(reader, "type")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found type element");
                    }

                    String typeId = reader.getAttributeValue(null, "title");
                    if (StringUtils.isBlank(typeId)) {
                        handleParseException("Ticket type title must not be empty", reader);
                    }
                    type = TicketType.createInstance(typeId);

                    continue;
                }
                if (inTicket && hasClass(reader, "timeout")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found timeout element");
                    }

                    String timeoutString = reader.getAttributeValue(null, "title");
                    if (StringUtils.isBlank(timeoutString)) {
                        timeout = null;
                    } else {
                        timeout = Integer.getInteger(timeoutString);
                    }

                    continue;
                }
            }
            if (type == null || key == null) {
                handleParseException("Ticket must have type and key", reader);
            }
            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        Ticket ticket = entityFactory.createTicket(type);
        ticket.setKey(key);
        if (timeout == null) {
            ticket.setTimeout(Ticket.TIMEOUT_INFINITE);
        } else {
            ticket.setTimeout(timeout);
        }

        return ticket;
    }

    public String format(Ticket ticket) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "ticket");

            if (ticket.getKey() != null) {
                writer.writeCharacters("Key: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "key");
                writer.writeCharacters(ticket.getKey());
                writer.writeEndElement();
            }

            if (ticket.getType() != null) {
                writer.writeCharacters("Type: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "type");
                writer.writeAttribute("title", ticket.getType().toString());
                writer.writeCharacters(ticket.getType().toString());
                writer.writeEndElement();
            }

            if (ticket.getTimeout() != null) {
                writer.writeCharacters("Timeout: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "timeout");
                writer.writeAttribute("title", ticket.getTimeout());
                writer.writeCharacters(ticket.getTimeout());
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
