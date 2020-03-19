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
package org.unitedinternet.cosmo.dav.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
 * <p>
 * Models a WebDAV Outbox collection 
 * (as described in http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05) that
 * contains a contains used as the target for initiating the processing of 
 * manual scheduling messages.  Currently the only defined use for this is for 
 * "VEVENT", "VTODO", and "VJOURNAL" "REFRESH" iTIP messages as well as 
 * "VFREEBUSY" "REQUEST" iTIP messages to request a synchronous freebusy lookup 
 * for a number of calendar users. The Outbox collection itself is not backed 
 * by a persistent entity.
 * </p>
 *
 * @see DavResourceBase
 * @see DavCollection
 */
public class DavOutboxCollection extends DavResourceBase implements DavCollection, CaldavConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavOutboxCollection.class);
    
    private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

    private DavAcl acl;
    private DavHomeCollection parent;

    static {
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
    }

    public DavOutboxCollection(DavResourceLocator locator,
                                      DavResourceFactory factory)
        throws CosmoDavException {
        super(locator, factory);
        acl = makeAcl();
    }

    // Jackrabbit WebDavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPFIND, PROPPATCH, LOCK, UNLOCK, REPORT, ACL";
    }

    public boolean isCollection() {
        return true;
    }

    public long getModificationTime() {
        return -1;
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
        return "Outbox";
    }

    public String getETag() {
        return "";
    }

    public void writeTo(OutputContext outputContext)
        throws CosmoDavException, IOException {
        writeHtmlDirectoryIndex(outputContext);
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        return DavResourceIteratorImpl.EMPTY;
    }

    @Override
    public DavResourceIterator getCollectionMembers() {
        return DavResourceIteratorImpl.EMPTY;
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public WebDavResource getCollection() {
        throw new UnsupportedOperationException();
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination,
                     boolean shallow)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    // WebDavResource

    public DavCollection getParent() throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator = getResourceLocator().getParentLocator();
            parent = (DavHomeCollection)getResourceFactory().resolve(parentLocator);
        }

        return parent;
    }

    // DavCollection

    public void addContent(DavContent content,
                           InputContext context)
        throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    public MultiStatusResponse addCollection(DavCollection collection,
                                             DavPropertySet properties)
        throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    public DavUserPrincipal findMember(String uri)
        throws CosmoDavException {
        DavResourceLocator locator = getResourceLocator().getFactory().
            createResourceLocatorByUri(getResourceLocator().getContext(),
                                       uri);
        return (DavUserPrincipal) getResourceFactory().resolve(locator);
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(2);
        rt.add(RESOURCE_TYPE_COLLECTION);
        rt.add(RESOURCE_TYPE_SCHEDULE_OUTBOX);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /**
     * Returns the resource's access control list. The list contains the
     * following ACEs:
     *
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code> </li>
     * <li> <code>DAV:all</code>: allow
     * <code>DAV:read, DAV:read-current-user-privilege-set</code> </li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code> </li>
     * </ol>
     */
    protected DavAcl getAcl() {
        return acl;
    }

    private DavAcl makeAcl() {
        DavAcl acl = new DavAcl();

        DavAce unauthenticated = new DavAce.UnauthenticatedAce();
        unauthenticated.setDenied(true);
        unauthenticated.getPrivileges().add(DavPrivilege.ALL);
        unauthenticated.setProtected(true);
        acl.getAces().add(unauthenticated);

        DavAce allAllow = new DavAce.AllAce();
        allAllow.getPrivileges().add(DavPrivilege.READ);
        allAllow.getPrivileges().add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
        allAllow.setProtected(true);
        acl.getAces().add(allAllow);

        DavAce allDeny = new DavAce.AllAce();
        allDeny.setDenied(true);
        allDeny.getPrivileges().add(DavPrivilege.ALL);
        allDeny.setProtected(true);
        acl.getAces().add(allDeny);

        return acl;
    }

    /**
     * <p>
     * Extends the superclass method to return {@link DavPrivilege#READ} if
     * the the current principal is a non-admin user.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (! privileges.isEmpty()) {
            return privileges;
        }

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            privileges.add(DavPrivilege.READ);
        }

        return privileges;
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Etag(getETag()));
    }

    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    protected void removeLiveProperty(DavPropertyName name)
        throws CosmoDavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void loadDeadProperties(DavPropertySet properties) {
    }

    protected void setDeadProperty(WebDavProperty property)
        throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    protected void removeDeadProperty(DavPropertyName name)
        throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    private void writeHtmlDirectoryIndex(OutputContext context)
        throws CosmoDavException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing html directory index for {}", getDisplayName());
        }
        context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
        // no modification time or etag

        if (! context.hasStream()) {
            return;
        }

        PrintWriter writer =
            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
                                                   "utf8"));
        try{
            writer.write("<html>\n<head><title>");
            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
            writer.write("</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
            writer.write("</h1>\n");
    
            writer.write("<h2>Properties</h2>\n");
            writer.write("<dl>\n");
            for (DavPropertyIterator i=getProperties().iterator(); i.hasNext();) {
                WebDavProperty prop = (WebDavProperty) i.nextProperty();
                Object value = prop.getValue();
                String text = null;
                if (value instanceof Element) {
                    try {
                        text = DomWriter.write((Element)value);
                    } catch (XMLStreamException e) {
                        LOG.warn("Error serializing value for property " + prop.getName());
                    }
                }
                if (text == null) {
                    text = prop.getValueText();
                }
                writer.write("<dt>");
                writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
                writer.write("</dt><dd>");
                writer.write(StringEscapeUtils.escapeHtml(text));
                writer.write("</dd>\n");
            }
            writer.write("</dl>\n");
    
            User user = getSecurityManager().getSecurityContext().getUser();
            if (user != null) {
                writer.write("<p>\n");
                DavResourceLocator homeLocator =
                    getResourceLocator().getFactory().
                    createHomeLocator(getResourceLocator().getContext(), user);
                writer.write("<a href=\"");
                writer.write(homeLocator.getHref(true));
                writer.write("\">");
                writer.write("Home collection");
                writer.write("</a><br>\n");
    
                DavResourceLocator principalLocator = 
                    getResourceLocator().getFactory().
                    createPrincipalLocator(getResourceLocator().getContext(),
                                           user);
                writer.write("<a href=\"");
                writer.write(principalLocator.getHref(false));
                writer.write("\">");
                writer.write("Principal resource");
                writer.write("</a><br>\n");
            }
    
            writer.write("</body>");
            writer.write("</html>\n");
        }finally{
            writer.close();
        }
    }

}