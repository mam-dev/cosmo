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
package org.unitedinternet.cosmo.dav.acl.report;

import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;
import org.unitedinternet.cosmo.model.User;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>DAV:principal-match</code> report that
 * provides a mechanism for finding resources that match the
 * current user.
 * </p>
 * <p>
 * If the report includes the <code>DAV:self</code> element, it matches
 * any principal resource in the target collection that represents the
 * currently authenticated user. This form of the report is used to
 * search through a principal collection for any principal resources that match
 * the current user. Alternatively, it can be used to find information about a
 * particular principal by targeting a specific principal resource.
 * </p>
 * <p>
 * If the report includes the <code>DAV:principal-property</code> element,
 * that element's first child element is taken to be the name of a
 * property that identifies the principal associated with a resource. The
 * report matches any resource in the target collection that 1) has
 * the specified principal property, 2) the principal property contains at
 * least one child <code>DAV:href</code> element, and 3) at least one of the
 * hrefs matches the principal URL of the currently authenticated user. This 
 * form of the report is used to search through an item collection for any
 * resources that are associated with the current user via the specified
 * principal property (usually <code>DAV:owner</code>).
 * </p>
 * <p>
 * Both forms of the report may optionally include a <code>DAV:prop</code>
 * element specifying the names of properties that are to be included in the
 * response for each matching resource. If <code>DAV:prop</code> is not
 * included in the report, then only the href and response status are
 * provided for each resource.
 * </p>
 * <p>
 * As per RFC 3744, the report must be specified with depth 0.
 * </p>
 */
public class PrincipalMatchReport extends MultiStatusReport
    implements AclConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(PrincipalMatchReport.class);

    public static final ReportType REPORT_TYPE_PRINCIPAL_MATCH =
        ReportType.register(ELEMENT_ACL_PRINCIPAL_MATCH, NAMESPACE,
                            PrincipalMatchReport.class);

    private boolean self;
    private DavPropertyName principalProperty;
    private User currentUser;
    private String currentUserPrincipalUrl;

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_PRINCIPAL_MATCH;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting self, principal property and
     * return properties.
     */
    protected void parseReport(ReportInfo info)
        throws CosmoDavException {
        if (! getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType().getReportName());
        }

        if (info.getDepth() != DEPTH_0) {
            throw new BadRequestException(getType().getReportName() + " report must be made with depth 0");
        }

        setPropFindProps(info.getPropertyNameSet());
        setPropFindType(PROPFIND_BY_PROPERTY);

        self = findSelf(info);
        if (self) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matching self");
            }
        } else {
            principalProperty = findPrincipalProperty(info);
            if (principalProperty != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Matching principal property " + principalProperty);
                }
            } else {
                throw new UnprocessableEntityException("Expected either " + QN_ACL_SELF + " or " + 
                        QN_ACL_PRINCIPAL_PROPERTY + " child of " + REPORT_TYPE_PRINCIPAL_MATCH);
            }
        }

        currentUser = getResource().getResourceFactory().getSecurityManager().
            getSecurityContext().getUser();
        if (currentUser == null) {
            throw new ForbiddenException("Authenticated principal is not a user");
        }
        String base = getResource().getResourceLocator().getBaseHref();
        currentUserPrincipalUrl =
            TEMPLATE_USER.bindAbsolute(base, currentUser.getUsername());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Matching against current user " +
                      currentUser.getUsername() + " (" +
                      currentUserPrincipalUrl + ")");
        }
    }

    /**
     * <p>
     * Executes the report query and stores the result. Behaves like the
     * superclass method except that it does not check depth, since the
     * report by definition always uses depth 0.
     * </p>
     */
    protected void runQuery()
        throws CosmoDavException {
        doQuerySelf(getResource());
        if (! getResource().isCollection()) {
            return;
        }
        DavCollection collection = (DavCollection) getResource();
        doQueryChildren(collection);
        /*
         * Don't use doQueryDescendents, because that would cause us to have to iterate through the members twice.
         * instead, we implement doQueryChildren to call itself recursively.
         */
        /*
         * XXX: refactor ReportBase.runQuery() to use a helper object rather than specifying doQuerySelf etc interface
         * methods.
         */
    }

    protected void doQuerySelf(WebDavResource resource)
        throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Querying {}", resource.getResourcePath());
        }
        if (self && matchesUserPrincipal(resource)) {
            getResults().add(resource);
        }
        if (principalProperty != null && matchesPrincipalProperty(resource)) {
            getResults().add(resource);
        }
    }

    protected void doQueryChildren(DavCollection collection)
        throws CosmoDavException {
        for (DavResourceIterator i = collection.getMembers(); i.hasNext();) {
            WebDavResource member = (WebDavResource) i.nextResource();
            if (member.isCollection()) {
                DavCollection dc = (DavCollection) member;
                doQuerySelf(dc);
                doQueryChildren(dc);
            } else {
                doQuerySelf(member);
            }
        }
    }

    // our methods

    public boolean isSelf() {
        return self;
    }

    public DavPropertyName getPrincipalProperty() {
        return principalProperty;
    }

    private boolean matchesUserPrincipal(WebDavResource resource)
        throws CosmoDavException {
        if (! (resource instanceof DavUserPrincipal)) {
            return false;
        }
        User principal = ((DavUserPrincipal)resource).getUser();
        if (! currentUser.equals(principal)) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Matched {}", resource.getResourcePath());
        }
        return true;
    }

    private boolean matchesPrincipalProperty(WebDavResource resource) 
        throws CosmoDavException {
        WebDavProperty prop = (WebDavProperty)
            resource.getProperty(principalProperty);
        if (prop == null) {
            return false;
        }
        Object value = prop.getValue();
        if (value == null) {
            return false;
        }
        /*
         * We assume that the DAV:href is the only child element of the property and that the url itself has been set
         * as the property value so that the DAV:href is reconstructed when the property is serialized.
         */
        if (value.toString().equals(currentUserPrincipalUrl)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched {}", resource.getResourcePath());
            }
            return true;
        }
        return false;
    }

    private static boolean findSelf(ReportInfo info)
        throws CosmoDavException {
        return info.containsContentElement(ELEMENT_ACL_SELF, NAMESPACE);
    }

    private static DavPropertyName findPrincipalProperty(ReportInfo info)
        throws CosmoDavException {
        Element pp =
            info.getContentElement(ELEMENT_ACL_PRINCIPAL_PROPERTY, NAMESPACE);
        if (pp == null) {
            return null;
        }
        ElementIterator ei = DomUtil.getChildren(pp);
        if (! ei.hasNext()) {
            return null;
        }
        return DavPropertyName.createFromXml(ei.nextElement());
    }
}
