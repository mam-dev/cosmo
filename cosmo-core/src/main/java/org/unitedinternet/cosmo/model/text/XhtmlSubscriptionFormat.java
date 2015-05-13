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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitedinternet.cosmo.CosmoXMLStreamException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Ticket;

/**
 * Parses and formats subscriptions in XHTML with a custom microformat
 * (yet to be described.)
 */
public class XhtmlSubscriptionFormat extends BaseXhtmlFormat
    implements SubscriptionFormat {
    private static final Log LOG =
        LogFactory.getLog(XhtmlSubscriptionFormat.class);

    public CollectionSubscription parse(String source, EntityFactory entityFactory)
        throws ParseException {
        CollectionSubscription sub = entityFactory.createCollectionSubscription();

        try {
            if (source == null) {
                throw new ParseException("Source has no XML data", -1);
            }
            StringReader sr = new StringReader(source);
            XMLStreamReader reader = createXmlReader(sr);

            boolean inLocalSub = false;
            boolean inCollection = false;
            boolean inTicket = false;
            while (reader.hasNext()) {
                reader.next();
                if (! reader.isStartElement()) {
                    continue;
                }

                if (hasClass(reader, "local-subscription")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found local-subscription element");
                    }
                    inLocalSub = true;
                    continue;
                }

                if (inLocalSub && hasClass(reader, "name")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found name element");
                    }

                    String name = reader.getElementText();
                    if (StringUtils.isBlank(name)) {
                        handleParseException("Name element must not be empty", reader);
                    }
                    sub.setDisplayName(name);

                    continue;
                }

                if (inLocalSub && hasClass(reader, "collection")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found collection element");
                    }
                    inCollection = true;
                    inTicket = false;
                    continue;
                }

                if (inCollection && hasClass(reader, "uuid")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found uuid element");
                    }

                    String uuid = reader.getElementText();
                    if (StringUtils.isBlank(uuid)) {
                        handleParseException("Uuid element must not be empty", reader);
                    }
                    sub.setCollectionUid(uuid);

                    continue;
                }

                if (inLocalSub && hasClass(reader, "ticket")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found ticket element");
                    }
                    inCollection = false;
                    inTicket = true;
                    continue;
                }

                if (inTicket && hasClass(reader, "key")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("found key element");
                    }

                    String key = reader.getElementText();
                    if (StringUtils.isBlank(key)) {
                        handleParseException("Key element must not be empty", reader);
                    }
                    sub.setTicketKey(key);

                    continue;
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            handleXmlException("Error reading XML", e);
        }

        return sub;
    }

    public String format(CollectionSubscription sub) {
        return format(sub, false, null, false, null);
    }

    public String format(CollectionSubscription sub,
                         CollectionItem collection,
                         Ticket ticket) {
        return format(sub, true, collection, true, ticket);
    }

    private String format(CollectionSubscription sub,
                          boolean isCollectionProvided,
                          CollectionItem collection,
                          boolean isTicketProvided,
                          Ticket ticket) {
        try {
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = createXmlWriter(sw);

            writer.writeStartElement("div");
            writer.writeAttribute("class", "local-subscription");

            if (sub.getDisplayName() != null) {
                writer.writeCharacters("Subscription: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "name");
                writer.writeCharacters(sub.getDisplayName());
                writer.writeEndElement();
            }

            if (sub.getCollectionUid() != null) {
                writer.writeStartElement("div");
                writer.writeAttribute("class", "collection");
                writer.writeCharacters("Collection: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "uuid");
                writer.writeCharacters(sub.getCollectionUid());
                writer.writeEndElement();
                if (isCollectionProvided) {
                    writer.writeCharacters(" Exists? ");
                    writer.writeStartElement("span");
                    writer.writeAttribute("class", "exists");
                    writer.writeCharacters(Boolean.valueOf(collection != null).
                                           toString());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            if (sub.getTicketKey() != null) {
                writer.writeStartElement("div");
                writer.writeAttribute("class", "ticket");
                writer.writeCharacters("Ticket: ");
                writer.writeStartElement("span");
                writer.writeAttribute("class", "key");
                writer.writeCharacters(sub.getTicketKey());
                writer.writeEndElement();
                if (isTicketProvided) {
                    writer.writeCharacters(" Exists? ");
                    writer.writeStartElement("span");
                    writer.writeAttribute("class", "exists");
                    writer.writeCharacters(Boolean.valueOf(ticket != null).
                                           toString());
                    writer.writeEndElement();
                }
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
