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
package org.unitedinternet.cosmo.dav.acl.resource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalMatchReport;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalSearchPropertySetReport;
import org.unitedinternet.cosmo.dav.impl.DavResourceBase;
import org.unitedinternet.cosmo.dav.property.CurrentUserPrincipal;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;

/**
 * <p>
 * Models a WebDAV principal collection (as described in RFC 3744) that contains a principal resource for each user
 * account in the server. The principal collection itself is not backed by a persistent entity.
 * </p>
 * 
 * @see DavResourceBase
 * @see DavCollection
 */
public class DavUserPrincipalCollection extends DavResourceBase implements DavCollection {
    
    private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();
    

    private DavAcl acl;

    static {
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(ExtendedDavConstants.CURRENTUSERPRINCIPAL);

        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
        REPORT_TYPES.add(PrincipalPropertySearchReport.REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);
        REPORT_TYPES.add(PrincipalSearchPropertySetReport.REPORT_TYPE_PRINCIPAL_SEARCH_PROPERTY_SET);
    }

    public DavUserPrincipalCollection(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
        acl = makeAcl();
    }

    // Jackrabbit WebDavResource

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
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
        return "User Principals";
    }

    public String getETag() {
        return null;
    }

    public void writeTo(OutputContext outputContext) throws CosmoDavException, IOException {
        throw new UnsupportedOperationException();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member, InputContext inputContext)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {        
        // Return an empty list to also support PROPFIND with depth 1 and depth infinity.
        return new DavResourceIteratorImpl(Collections.emptyList());
    }
    
    @Override
    public DavResourceIterator getCollectionMembers() {
        throw new UnsupportedOperationException();
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public WebDavResource getCollection() {
        throw new UnsupportedOperationException();
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination, boolean shallow)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    // WebDavResource

    public DavCollection getParent() throws CosmoDavException {
        return null;
    }

    // DavCollection

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public MultiStatusResponse addCollection(DavCollection collection, DavPropertySet properties) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public DavUserPrincipal findMember(String uri) throws CosmoDavException {
        DavResourceLocator locator = getResourceLocator().getFactory().createResourceLocatorByUri(
                getResourceLocator().getContext(), uri);
        return (DavUserPrincipal) getResourceFactory().resolve(locator);
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /**
     * Returns the resource's access control list. The list contains the following ACEs:
     * 
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code></li>
     * <li> <code>DAV:all</code>: allow <code>DAV:read, DAV:read-current-user-privilege-set</code></li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code></li>
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
     * Extends the superclass method to return {@link DavPrivilege#READ} if the the current principal is a non-admin
     * user.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (!privileges.isEmpty()) {
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
        properties.add(new CurrentUserPrincipal(getResourceLocator(),
                getSecurityManager().getSecurityContext().getUser()));
    }

    protected void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    protected void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void loadDeadProperties(DavPropertySet properties) {
    }

    protected void setDeadProperty(WebDavProperty property) throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    protected void removeDeadProperty(DavPropertyName name) throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

}
