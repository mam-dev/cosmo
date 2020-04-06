/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamException;

import org.apache.abdera.util.EntityTag;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.WebdavRequestImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.DavPrivilegeSet;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.util.BufferedServletInputStream;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class StandardDavRequest extends WebdavRequestImpl implements DavRequest, ExtendedDavConstants, AclConstants, CaldavConstants,
        TicketConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(StandardDavRequest.class);
    
    private static final MimeType APPLICATION_XML = registerMimeType("application/xml");
    private static final MimeType TEXT_XML = registerMimeType("text/xml");
    
    /**
     * 
     * @param s mimeType as String
     * @return MimeType 
     */
    private static final MimeType registerMimeType(String s) {
        try {
            return new MimeType(s);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException("Can't register MIME type " + s, e);
        }
    }

    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;
    private DavPropertySet proppatchSet;
    private DavPropertyNameSet proppatchRemove;
    private DavPropertySet mkcalendarSet;
    private Ticket ticket;
    private ReportInfo reportInfo;
    private boolean bufferRequestContent = false;
    private long bufferedContentLength = -1;
    private DavResourceLocatorFactory locatorFactory;
    private DavResourceLocator locator;
    private DavResourceLocator destinationLocator;
    private EntityFactory entityFactory;
    private HttpServletRequest originalHttpServletRequest;
    
    /**
     * 
     * {@inheritDoc}
     */
    public Enumeration<String> getAttributeNames() {
        return originalHttpServletRequest.getAttributeNames();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public String getContentType() {
        return originalHttpServletRequest.getContentType();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Enumeration<String> getHeaders(String name) {
        return originalHttpServletRequest.getHeaders(name);
    }

    public Enumeration<String> getHeaderNames() {
        return originalHttpServletRequest.getHeaderNames();
    }

    public Map<String, String[]> getParameterMap() {
        return originalHttpServletRequest.getParameterMap();
    }

    public Enumeration<Locale> getLocales() {
        return originalHttpServletRequest.getLocales();
    }

    public String getLocalName() {
        return originalHttpServletRequest.getLocalName();
    }

    public String getLocalAddr() {
        return originalHttpServletRequest.getLocalAddr();
    }

    public int getLocalPort() {
        return originalHttpServletRequest.getLocalPort();
    }

    public Enumeration<String> getParameterNames() {
        return originalHttpServletRequest.getParameterNames();
    }
    


    public int getRemotePort() {
        return originalHttpServletRequest.getRemotePort();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return originalHttpServletRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return originalHttpServletRequest.isRequestedSessionIdFromURL();
    }
    
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return originalHttpServletRequest.isRequestedSessionIdFromUrl();
    }
    
    /**
     * 
     * @param request HttpServletRequest
     * @param factory DavResourceLocatorFactory
     * @param entityFactory EntityFactory
     */
    public StandardDavRequest(HttpServletRequest request,
            DavResourceLocatorFactory factory, EntityFactory entityFactory) {
        this(request, factory, entityFactory, false);
    }

    /**
     * 
     * @param request HttpServletRequest
     * @param factory DavResourceLocatorFactory
     * @param entityFactory EntityFactory
     * @param bufferRequestContent boolean
     */
    public StandardDavRequest(HttpServletRequest request,
            DavResourceLocatorFactory factory, EntityFactory entityFactory,
            boolean bufferRequestContent) {
        super(request, null);
        originalHttpServletRequest = request;
        this.locatorFactory = factory;
        this.bufferRequestContent = bufferRequestContent;
        this.entityFactory = entityFactory;
    }

    // DavRequest methods

    public EntityTag[] getIfMatch() {
        return EntityTag.parseTags(getHeader("If-Match"));
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Date getIfModifiedSince() {
        long value = getDateHeader("If-Modified-Since");
        return value != -1 ? new Date(value) : null;
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public EntityTag[] getIfNoneMatch() {
        return EntityTag.parseTags(getHeader("If-None-Match"));
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public Date getIfUnmodifiedSince() {
        long value = getDateHeader("If-Unmodified-Since");
        return value != -1 ? new Date(value) : null;
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public int getPropFindType() throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindType;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavPropertyNameSet getPropFindProperties() throws CosmoDavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindProps;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavPropertySet getProppatchSetProperties() throws CosmoDavException {
        if (proppatchSet == null) {
            parsePropPatchRequest();
        }
        return proppatchSet;
    }

    /**
     * 
     * 
     */
    public DavPropertyNameSet getProppatchRemoveProperties()
            throws CosmoDavException {
        if (proppatchRemove == null) {
            parsePropPatchRequest();
        }
        return proppatchRemove;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavResourceLocator getResourceLocator() {
        if (locator == null) {
            URL context = null;
            try {
                String basePath = getContextPath() + getServletPath();
                context = new URL(getScheme(), getServerName(),
                        getServerPort(), basePath);

                locator = locatorFactory.createResourceLocatorByUri(context,
                        getRequestURI());
            } catch (CosmoDavException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return locator;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavResourceLocator getDestinationResourceLocator()
            throws CosmoDavException {
        if (destinationLocator != null) {
            return destinationLocator;
        }

        String destination = getHeader(HEADER_DESTINATION);
        if (destination == null) {
            return null;
        }

        URL context = ((DavResourceLocator) getResourceLocator()).getContext();

        destinationLocator = locatorFactory.createResourceLocatorByUri(context,
                destination);

        return destinationLocator;
    }

    // CaldavRequest methods
    /**
     * 
     * {@inheritDoc}
     */
    public DavPropertySet getMkCalendarSetProperties() throws CosmoDavException {
        if (mkcalendarSet == null) {
            mkcalendarSet = parseMkCalendarRequest();
        }
        return mkcalendarSet;
    }

    // TicketDavRequest methods
    /**
     * 
     * {@inheritDoc}
     */
    public Ticket getTicketInfo() throws CosmoDavException {
        if (ticket == null) {
            ticket = parseTicketRequest();
        }
        return ticket;
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public String getTicketKey() throws CosmoDavException {
        String key = getParameter(PARAM_TICKET);
        if (key == null) {
            key = getHeader(HEADER_TICKET);
        }
        if (key != null) {
            return key;
        }
        throw new BadRequestException("Request does not contain a ticket key");
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public ReportInfo getReportInfo() throws CosmoDavException {
        if (reportInfo == null) {
            reportInfo = parseReportRequest();
        }
        return reportInfo;
    }

    // private methods
    /**
     * 
     * @return Document
     * @throws CosmoDavException 
     */
    private Document getSafeRequestDocument() throws CosmoDavException {
        return getSafeRequestDocument(true);
    }
    /**
     * 
     * @param requireDocument boolean 
     * @return Document
     * @throws CosmoDavException 
     */
    private Document getSafeRequestDocument(boolean requireDocument)
            throws CosmoDavException {
        try {
            if (StringUtils.isBlank(getContentType()) && requireDocument) {
                throw new BadRequestException("No Content-Type specified");
            }
            MimeType mimeType = new MimeType(getContentType());
            if (!(mimeType.match(APPLICATION_XML) || mimeType.match(TEXT_XML))) {
                throw new UnsupportedMediaTypeException(
                        "Expected Content-Type " + APPLICATION_XML + " or "
                                + TEXT_XML);
            }

            return getRequestDocument();

        } catch (MimeTypeParseException e) {
            throw new UnsupportedMediaTypeException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throwBadRequestExceptionFrom(e);
        } catch (DavException e) {
            throwBadRequestExceptionFrom(e);
        }

        return null;
    }
    
    /**
     * 
     * @param e 
     * @throws BadRequestException 
     */
    private void throwBadRequestExceptionFrom(Exception e)
            throws BadRequestException {
        Throwable cause = e.getCause();
        String msg = cause != null ? cause.getMessage()
                : "Unknown error parsing request document";
        throw new BadRequestException(msg);
    }
    
    /**
     * 
     * @param root Element
     */
    private void dumpPropFindRequest(Element root) {
        if (!LOG.isTraceEnabled()){
            return;
        }

        StringBuilder sb = new StringBuilder(
                "\n------------------------ Dump of propFind request -------------------\n");
        try {
            if (root == null){
                sb.append("ALL props requested\n");
            } else {
                sb.append(DomWriter.write(root)).append("\n");
            }
        } catch (IOException e) {
            LOG.warn("", e);
        } catch (XMLStreamException e) {
            LOG.warn("", e);
        }
        sb.append("------------------------ End dump of propFind request -------------------");
        LOG.trace(sb.toString());
    }
    
    /**
     * 
     * @throws CosmoDavException 
     */
    private void parsePropFindRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) {
            // treat as allprop
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();
            dumpPropFindRequest(null);
            return;
        }

        Element root = requestDocument.getDocumentElement();
        dumpPropFindRequest(root);
        if (!DomUtil.matches(root, XML_PROPFIND, NAMESPACE)) {
            throw new BadRequestException("Expected " + QN_PROPFIND
                    + " root element");
        }

        Element prop = DomUtil.getChildElement(root, XML_PROP, NAMESPACE);
        if (prop != null) {
            propfindType = PROPFIND_BY_PROPERTY;
            propfindProps = new DavPropertyNameSet(prop);
            return;
        }

        if (DomUtil.getChildElement(root, XML_PROPNAME, NAMESPACE) != null) {
            propfindType = PROPFIND_PROPERTY_NAMES;
            propfindProps = new DavPropertyNameSet();
            return;
        }

        if (DomUtil.getChildElement(root, XML_ALLPROP, NAMESPACE) != null) {
            propfindType = PROPFIND_ALL_PROP;
            propfindProps = new DavPropertyNameSet();

            Element include = DomUtil.getChildElement(root, "include",
                    NAMESPACE);
            if (include != null) {
                ElementIterator included = DomUtil.getChildren(include);
                while (included.hasNext()) {
                    DavPropertyName name = DavPropertyName
                            .createFromXml(included.nextElement());
                    propfindProps.add(name);
                }
            }

            return;
        }

        throw new BadRequestException("Expected one of " + XML_PROP + ", "
                + XML_PROPNAME + ", or " + XML_ALLPROP + " as child of "
                + QN_PROPFIND);
    }

    /**
     * 
     * @throws CosmoDavException 
     */
    private void parsePropPatchRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) {
            throw new BadRequestException("PROPPATCH requires entity body");
        }

        Element root = requestDocument.getDocumentElement();
        if (!DomUtil.matches(root, XML_PROPERTYUPDATE, NAMESPACE)) {
            throw new BadRequestException("Expected " + QN_PROPERTYUPDATE
                    + " root element");
        }

        ElementIterator sets = DomUtil.getChildren(root, XML_SET, NAMESPACE);
        ElementIterator removes = DomUtil.getChildren(root, XML_REMOVE,
                NAMESPACE);
        if (!(sets.hasNext() || removes.hasNext())) {
            throw new BadRequestException("Expected at least one of "
                    + QN_REMOVE + " and " + QN_SET + " as a child of "
                    + QN_PROPERTYUPDATE);
        }

        Element prop = null;
        ElementIterator i = null;

        proppatchSet = new DavPropertySet();
        while (sets.hasNext()) {
            Element set = sets.nextElement();
            prop = DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
            if (prop == null) {
                throw new BadRequestException("Expected " + QN_PROP
                        + " child of " + QN_SET);
            }
            i = DomUtil.getChildren(prop);
            while (i.hasNext()) {
                StandardDavProperty p = StandardDavProperty.createFromXml(i
                        .nextElement());
                proppatchSet.add(p);
            }
        }

        proppatchRemove = new DavPropertyNameSet();
        while (removes.hasNext()) {
            Element remove = removes.nextElement();
            prop = DomUtil.getChildElement(remove, XML_PROP, NAMESPACE);
            if (prop == null) {
                throw new BadRequestException("Expected " + QN_PROP
                        + " child of " + QN_REMOVE);
            }
            i = DomUtil.getChildren(prop);
            while (i.hasNext()){
                proppatchRemove.add(DavPropertyName.createFromXml(i
                        .nextElement()));
            }
        }
    }

    /**
     * 
     * @return DavPropertySet
     * @throws CosmoDavException 
     */
    private DavPropertySet parseMkCalendarRequest() throws CosmoDavException {
        DavPropertySet propertySet = new DavPropertySet();

        Document requestDocument = getSafeRequestDocument(false);
        if (requestDocument == null) {
            return propertySet;
        }

        Element root = requestDocument.getDocumentElement();
        if (!DomUtil.matches(root, ELEMENT_CALDAV_MKCALENDAR, NAMESPACE_CALDAV)) {
            throw new BadRequestException("Expected " + QN_MKCALENDAR
                    + " root element");
        }
        Element set = DomUtil.getChildElement(root, XML_SET, NAMESPACE);
        if (set == null) {
            throw new BadRequestException("Expected " + QN_SET + " child of "
                    + QN_MKCALENDAR);
        }
        Element prop = DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
        if (prop == null) {
            throw new BadRequestException("Expected " + QN_PROP + " child of "
                    + QN_SET);
        }
        ElementIterator i = DomUtil.getChildren(prop);
        while (i.hasNext()){
            propertySet.add(StandardDavProperty.createFromXml(i.nextElement()));
        }

        return propertySet;
    }

    /**
     * 
     * @return Ticket 
     * @throws CosmoDavException  
     */
    private Ticket parseTicketRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) {
            throw new BadRequestException("MKTICKET requires entity body");
        }

        Element root = requestDocument.getDocumentElement();
        if (!DomUtil.matches(root, ELEMENT_TICKET_TICKETINFO, NAMESPACE_TICKET)) {
            throw new BadRequestException("Expected " + QN_TICKET_TICKETINFO
                    + " root element");
        }
        if (DomUtil.hasChildElement(root, ELEMENT_TICKET_ID, NAMESPACE_TICKET)) {
            throw new BadRequestException(QN_TICKET_TICKETINFO
                    + " may not contain child " + QN_TICKET_ID);
        }
        if (DomUtil.hasChildElement(root, XML_OWNER, NAMESPACE)) {
            throw new BadRequestException(QN_TICKET_TICKETINFO
                    + " may not contain child " + QN_OWNER);
        }

        String timeout = DomUtil.getChildTextTrim(root, ELEMENT_TICKET_TIMEOUT,
                NAMESPACE_TICKET);
        if (timeout != null && !timeout.equals(TIMEOUT_INFINITE)) {
            try {
                int seconds = Integer.parseInt(timeout.substring(7));
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Timeout seconds: " + seconds);
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Malformed " + QN_TICKET_TIMEOUT
                        + " value " + timeout);
            }
        } else {
            timeout = TIMEOUT_INFINITE;
        }

        // visit limits are not supported

        Element pe = DomUtil.getChildElement(root, XML_PRIVILEGE, NAMESPACE);
        if (pe == null) {
            throw new BadRequestException("Expected " + QN_PRIVILEGE
                    + " child of " + QN_TICKET_TICKETINFO);
        }

        DavPrivilegeSet privileges = DavPrivilegeSet.createFromXml(pe);
        if (!privileges.containsAny(DavPrivilege.READ, DavPrivilege.WRITE,
                DavPrivilege.READ_FREE_BUSY)) {
            throw new BadRequestException("Empty or invalid " + QN_PRIVILEGE);
        }

        Ticket ticket = entityFactory.creatTicket();
        ticket.setTimeout(timeout);
        privileges.setTicketPrivileges(ticket);

        return ticket;
    }

    /**
     *  
     * @return ReportInfo
     * @throws CosmoDavException 
     */
    private ReportInfo parseReportRequest() throws CosmoDavException {
        Document requestDocument = getSafeRequestDocument();
        if (requestDocument == null) { // reports with no bodies are supported
                                        // for collections
            return null;
        }

        try {
            return new ReportInfo(requestDocument.getDocumentElement(),
                    getDepth(DEPTH_0));
        } catch (DavException e) {
            throw new CosmoDavException(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!bufferRequestContent) {
            return super.getInputStream();
        }

        BufferedServletInputStream is = new BufferedServletInputStream(
                super.getInputStream());
        bufferedContentLength = is.getLength();

        long contentLength = getContentLength();
        if (contentLength != -1 && contentLength != bufferedContentLength) {
            throw new IOException("Read only " + bufferedContentLength + " of "
                    + contentLength + " bytes");
        }

        return is;
    }

    public boolean isRequestContentBuffered() {
        return bufferRequestContent;
    }

    public long getBufferedContentLength() {
        return bufferedContentLength;
    }

    @Override
    public boolean authenticate(HttpServletResponse response)
            throws IOException, ServletException {
        return originalHttpServletRequest.authenticate(response);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return originalHttpServletRequest.getPart(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return originalHttpServletRequest.getParts();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        originalHttpServletRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        originalHttpServletRequest.logout();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return originalHttpServletRequest.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return originalHttpServletRequest.getDispatcherType();
    }

    @Override
    public ServletContext getServletContext() {
        return originalHttpServletRequest.getServletContext();
    }

    @Override
    public boolean isAsyncStarted() {
        return originalHttpServletRequest.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return originalHttpServletRequest.isAsyncSupported();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return originalHttpServletRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        return originalHttpServletRequest.startAsync(servletRequest,
                servletResponse);
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }
    
    
}