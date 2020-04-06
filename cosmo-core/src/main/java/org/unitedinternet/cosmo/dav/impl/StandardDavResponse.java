/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.dav.ticket.property.TicketDiscovery;
import org.unitedinternet.cosmo.dav.util.XmlSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extends {@link org.apache.jackrabbit.webdav.WebdavResponseImpl} and
 * implements methods for the DAV ticket extension.
 */
public class StandardDavResponse extends WebdavResponseImpl implements DavResponse, DavConstants, TicketConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(StandardDavResponse.class);
    
    private static final XMLOutputFactory XML_OUTPUT_FACTORY =  XMLOutputFactory.newInstance();

    private HttpServletResponse originalHttpServletResponse;

    /**
     */
    public StandardDavResponse(HttpServletResponse response) {
        super(response);
        originalHttpServletResponse = response;
    }
    
    
    public void addCookie(Cookie cookie) {
        originalHttpServletResponse.addCookie(cookie);
    }


    public boolean containsHeader(String name) {
        return originalHttpServletResponse.containsHeader(name);
    }


    public String encodeURL(String url) {
        return originalHttpServletResponse.encodeURL(url);
    }


    public String getCharacterEncoding() {
        return originalHttpServletResponse.getCharacterEncoding();
    }


    public String encodeRedirectURL(String url) {
        return originalHttpServletResponse.encodeRedirectURL(url);
    }


    public String getContentType() {
        return originalHttpServletResponse.getContentType();
    }

    @Deprecated
    public String encodeUrl(String url) {
        return originalHttpServletResponse.encodeUrl(url);
    }


    public String encodeRedirectUrl(String url) {
        return originalHttpServletResponse.encodeRedirectURL(url);
    }


    public ServletOutputStream getOutputStream() throws IOException {
        return originalHttpServletResponse.getOutputStream();
    }


    public void sendError(int sc, String msg) throws IOException {
        originalHttpServletResponse.sendError(sc, msg);
    }


    public PrintWriter getWriter() throws IOException {
        return originalHttpServletResponse.getWriter();
    }


    public void sendError(int sc) throws IOException {
        originalHttpServletResponse.sendError(sc);
    }


    public void setCharacterEncoding(String charset) {
        originalHttpServletResponse.setCharacterEncoding(charset);
    }


    public void sendRedirect(String location) throws IOException {
        originalHttpServletResponse.sendRedirect(location);
    }


    public void setDateHeader(String name, long date) {
        originalHttpServletResponse.setDateHeader(name, date);
    }


    public void setContentLength(int len) {
        originalHttpServletResponse.setContentLength(len);
    }


    public void addDateHeader(String name, long date) {
        originalHttpServletResponse.addDateHeader(name, date);
    }


    public void setContentType(String type) {
        originalHttpServletResponse.setContentType(type);
    }


    public void setHeader(String name, String value) {
        //Including unvalidated data in an HTTP response header can enable 
        //cache-poisoning, cross-site scripting, cross-user defacement, page hijacking, 
        //cookie manipulation or open redirect.
        value = value.replaceAll("\r", " ").replace("\n", " ");
        originalHttpServletResponse.setHeader(name, value);
    }


    public void addHeader(String name, String value) {
        originalHttpServletResponse.addHeader(name, value);
    }


    public void setBufferSize(int size) {
        originalHttpServletResponse.setBufferSize(size);
    }


    public void setIntHeader(String name, int value) {
        originalHttpServletResponse.setIntHeader(name, value);
    }


    public void addIntHeader(String name, int value) {
        originalHttpServletResponse.addIntHeader(name, value);
    }


    public void setStatus(int sc) {
        originalHttpServletResponse.setStatus(sc);
    }


    public int getBufferSize() {
        return originalHttpServletResponse.getBufferSize();
    }


    public void flushBuffer() throws IOException {
        originalHttpServletResponse.flushBuffer();
    }

    @Deprecated
    public void setStatus(int sc, String sm) {
        originalHttpServletResponse.setStatus(sc, sm);
    }


    public void resetBuffer() {
        originalHttpServletResponse.resetBuffer();
    }


    public boolean isCommitted() {
        return originalHttpServletResponse.isCommitted();
    }


    public void reset() {
        originalHttpServletResponse.reset();
    }


    public void setLocale(Locale loc) {
        originalHttpServletResponse.setLocale(loc);
    }


    public Locale getLocale() {
        return originalHttpServletResponse.getLocale();
    }


    @Override
    public void sendXmlResponse(XmlSerializable serializable, int status) throws IOException {
        super.sendXmlResponse(serializable, status);
        if (serializable != null && LOG.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("\n------------------------ Dump of response -------------------\n");
            sb.append("Status: ").append(status).append("\n");
            sb.append(XmlSerializer.serialize(serializable));
            sb.append("\n------------------------ End dump of response -------------------");
            LOG.trace(sb.toString());
        }
    }

    @Override
    public String getHeader(String name) {
        return originalHttpServletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return originalHttpServletResponse.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return originalHttpServletResponse.getHeaders(name);
    }

    @Override
    public int getStatus() {
        return originalHttpServletResponse.getStatus();
    }




    // DavResponse methods

    /**
     * Send the <code>ticketdiscovery</code> response to a <code>MKTICKET</code> request.
     *
     * @param resource the resource on which the ticket was created
     * @param ticketId the id of the newly created ticket
     */
    public void sendMkTicketResponse(DavItemResource resource, String ticketId) throws CosmoDavException, IOException {
        setHeader(HEADER_TICKET, ticketId);

        TicketDiscovery ticketdiscovery = (TicketDiscovery) resource.getProperties().get(TICKETDISCOVERY);
        MkTicketInfo info = new MkTicketInfo(ticketdiscovery);

        sendXmlResponse(info, SC_OK);
    }

    private static class MkTicketInfo implements XmlSerializable {
        private TicketDiscovery td;

        public MkTicketInfo(TicketDiscovery td) {
            this.td = td;
        }

        public Element toXml(Document document) {
            Element prop = DomUtil.createElement(document, XML_PROP, NAMESPACE);
            if (td != null) {
                prop.appendChild(td.toXml(document));
            }
            return prop;
        }
    }

    public void sendDavError(CosmoDavException e)
        throws IOException {
        setStatus(e.getErrorCode());
        if (! e.hasContent()) {
            return;
        }

        XMLStreamWriter writer = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
            writer.writeStartDocument();
            e.writeTo(writer);
            writer.writeEndDocument();

            setContentType("text/xml; charset=UTF-8");
            byte[] bytes = out.toByteArray();
            setContentLength(bytes.length);
            getOutputStream().write(bytes);
        } catch (Exception e2) {
            LOG.error("Error writing XML", e2);
            LOG.error("Original exception", e);
            setStatus(500);
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


    @Override
    public void setContentLengthLong(long len) {
        
    }
    
    
}
