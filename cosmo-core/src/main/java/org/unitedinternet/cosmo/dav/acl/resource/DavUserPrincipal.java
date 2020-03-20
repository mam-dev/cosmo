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
package org.unitedinternet.cosmo.dav.acl.resource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.CosmoException;
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
import org.unitedinternet.cosmo.dav.acl.property.AlternateUriSet;
import org.unitedinternet.cosmo.dav.acl.property.GroupMembership;
import org.unitedinternet.cosmo.dav.acl.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalMatchReport;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarUserAddressSet;
import org.unitedinternet.cosmo.dav.caldav.property.ScheduleInboxURL;
import org.unitedinternet.cosmo.dav.caldav.property.ScheduleOutboxURL;
import org.unitedinternet.cosmo.dav.impl.DavResourceBase;
import org.unitedinternet.cosmo.dav.property.CreationDate;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentity;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
* <p>
* Models a WebDAV principal resource (as described in RFC 3744) that
* represents a user account.
* </p>
 *
 * @see DavContent
 * @see DavResourceBase
 * @see User
 */
public class DavUserPrincipal extends DavResourceBase implements CaldavConstants, DavContent {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavUserPrincipal.class);    
    
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();

    static {
        registerLiveProperty(DavPropertyName.CREATIONDATE);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(CALENDARHOMESET);
        registerLiveProperty(CALENDARUSERADDRESSSET);
        registerLiveProperty(SCHEDULEINBOXURL);
        registerLiveProperty(SCHEDULEOUTBOXURL);
        registerLiveProperty(ALTERNATEURISET);
        registerLiveProperty(PRINCIPALURL);
        registerLiveProperty(GROUPMEMBERSHIP);

        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
    }

    private User user;
    private DavUserPrincipalCollection parent;
    private DavAcl acl;
    private UserIdentitySupplier userIdentitySupplier;

    public DavUserPrincipal(User user,
                            DavResourceLocator locator,
                            DavResourceFactory factory,
                            UserIdentitySupplier userIdentitySupplier)
        throws CosmoDavException {
        super(locator, factory);
        this.user = user;
        this.acl = makeAcl();
        this.userIdentitySupplier = userIdentitySupplier;
    }


    // Jackrabbit WebDavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
    }

    public boolean isCollection() {
        return false;
    }

    public long getModificationTime() {
        return user.getModifiedDate().getTime();
    }

    public boolean exists() {
        return true;
    }

    public String getDisplayName() {
    	UserIdentity userIdentity = userIdentitySupplier.forUser(user);
        String firstName = userIdentity.getFirstName();
        String lastName = userIdentity.getLastName();
        String email = userIdentity.getEmails().isEmpty() ? "" : userIdentity.getEmails().iterator().next();
        
        String toReturn = null;
        
        if(firstName == null && lastName == null){
            toReturn = email;
        }else if(firstName == null){
            toReturn = lastName;
        }else if(lastName == null){
            toReturn = firstName;
        }else {
            toReturn = firstName + " " + lastName; 
        }
        return toReturn;
    }

    public String getETag() {
        return "\"" + user.getEntityTag() + "\"";
    }

    public void writeTo(OutputContext context)
        throws CosmoDavException, IOException {
        writeHtmlRepresentation(context);
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        /*
         * While it would be ideal to throw an UnsupportedOperationException, MultiStatus tries to add a
         * MultiStatusResponse for every member of a WebDavResource regardless of whether or not it's a collection, so
         * we need to return an empty iterator.
         */
        return new DavResourceIteratorImpl(Collections.emptyList());
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public WebDavResource getCollection() {
        try {
            return getParent();
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
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

    // WebDavResource methods

    public DavCollection getParent()
        throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator =
                getResourceLocator().getParentLocator();
            parent = (DavUserPrincipalCollection)
                getResourceFactory().resolve(parentLocator);
        }

        return parent;
    }

    // our methods

    public User getUser() {
        return user;
    }

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_PRINCIPAL);
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
     * <li> <code>DAV:owner</code>: allow <code>DAV:all</code> </li>
     * <li> <code>DAV:all</code>: allow
     * <code>DAV:read-current-user-privilege-set</code> </li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code> </li>
     * </ol>
     *
     * <p>
     * TODO: Include administrative users in the ACL, probably with a group
     * principal.
     * </p>
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

        DavAce owner = new DavAce.SelfAce();
        owner.getPrivileges().add(DavPrivilege.ALL);
        owner.setProtected(true);
        acl.getAces().add(owner);

        DavAce allAllow = new DavAce.AllAce();
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
     * the the current principal is the non-admin user represented by this
     * resource.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (! privileges.isEmpty()) {
            return privileges;
        }

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null && user.equals(this.user)) {
            privileges.add(DavPrivilege.READ);
        }

        return privileges;
    }
    
    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new CreationDate(user.getCreationDate()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Etag(user.getEntityTag()));
        properties.add(new LastModified(user.getModifiedDate()));
        properties.add(new CalendarHomeSet(getResourceLocator(), user));
        
        // for now scheduling is an option
        if(isSchedulingEnabled()) {
            properties.add(new CalendarUserAddressSet(user, userIdentitySupplier));
            properties.add(new ScheduleInboxURL(getResourceLocator(), user));
            properties.add(new ScheduleOutboxURL(getResourceLocator(), user));
        }
        
        properties.add(new AlternateUriSet());
        properties.add(new PrincipalUrl(getResourceLocator(), user));
        properties.add(new GroupMembership());
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
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }

    protected void removeDeadProperty(DavPropertyName name)
        throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this resource");
    }

    private void writeHtmlRepresentation(OutputContext context)
        throws CosmoDavException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing html representation for user principal {}", getDisplayName());
        }

        context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

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
                        LOG.warn("Error serializing value for property {}", prop.getName());
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
    
            WebDavResource parent = getParent();
            writer.write("<a href=\"");
            writer.write(parent.getResourceLocator().getHref(true));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
            writer.write("</a></li>\n");
            
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
            }
    
            writer.write("</body>");
            writer.write("</html>\n");
        }finally{
            writer.close();
        }
    }
}
