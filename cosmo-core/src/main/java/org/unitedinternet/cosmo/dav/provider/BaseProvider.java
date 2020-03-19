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
package org.unitedinternet.cosmo.dav.provider;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.AclEvaluator;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.acl.TicketAclEvaluator;
import org.unitedinternet.cosmo.dav.acl.UnsupportedPrivilegeException;
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipalCollection;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.DavInboxCollection;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;
import org.unitedinternet.cosmo.dav.impl.DavOutboxCollection;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;

/**
 * <p>
 * A base class for implementations of <code>DavProvider</code>.
 * </p>
 *
 * @see DavProvider
 */
public abstract class BaseProvider implements DavProvider, DavConstants, AclConstants, TicketConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(BaseProvider.class);

    private DavResourceFactory resourceFactory;
    private EntityFactory entityFactory;

    /**
     * 
     * @param resourceFactory
     * @param entityFactory
     */
    public BaseProvider(DavResourceFactory resourceFactory, EntityFactory entityFactory) {
        this.resourceFactory = resourceFactory;
        this.entityFactory = entityFactory;
    }

    // DavProvider methods
    /**
     * 
     * {@inheritDoc}
     */
    public void get(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        spool(request, response, resource, true);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void head(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        spool(request, response, resource, false);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void post(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("POST not allowed for a collection");
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void propfind(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        int depth = getDepth(request);
        if (depth != DEPTH_0 && !resource.isCollection()) {
            throw new BadRequestException("Depth must be 0 for non-collection resources");
        }

        DavPropertyNameSet props = null;
        int type = -1;

        try {

            props = request.getPropFindProperties();

            type = request.getPropFindType();

        } catch (DavException de) {
            throw new CosmoDavException(de);
        }

        // Since the propfind properties could not be determined in the
        // security filter in order to check specific property privileges, the
        // check must be done manually here.
        checkPropFindAccess(resource, props, type);

        MultiStatus ms = new MultiStatus();
        ms.addResourceProperties(resource, props, type, depth);

        response.sendMultiStatus(ms);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void proppatch(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        DavPropertySet set = request.getProppatchSetProperties();
        DavPropertyNameSet remove = request.getProppatchRemoveProperties();

        MultiStatus ms = new MultiStatus();
        MultiStatusResponse msr = resource.updateProperties(set, remove);
        ms.addResponse(msr);

        response.sendMultiStatus(ms);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void delete(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (depth != DEPTH_INFINITY) {
            throw new BadRequestException("Depth for DELETE must be infinity");
        }

        try {
            resource.getParent().removeMember(resource);
            response.setStatus(204);
        } catch (DavException e) {
            throw new CosmoDavException(e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void copy(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        int depth = getDepth(request);
        if (!(depth == DEPTH_0 || depth == DEPTH_INFINITY)) {
            throw new BadRequestException("Depth for COPY must be 0 or infinity");
        }
        WebDavResource destination = resolveDestination(request.getDestinationResourceLocator(), resource);
        validateDestination(request, destination);

        checkCopyMoveAccess(resource, destination);

        try {
            if (destination.exists() && request.isOverwrite()) {
                destination.getCollection().removeMember(destination);
            }
            resource.copy(destination, depth == DEPTH_0);
            response.setStatus(destination.exists() ? 204 : 201);
        } catch (DavException e) {
            if (e instanceof CosmoDavException) {
                throw (CosmoDavException) e;
            }
            throw new CosmoDavException(e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void move(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        WebDavResource destination = resolveDestination(request.getDestinationResourceLocator(), resource);
        validateDestination(request, destination);

        checkCopyMoveAccess(resource, destination);

        try {
            if (destination.exists() && request.isOverwrite()) {
                destination.getCollection().removeMember(destination);
            }
            resource.move(destination);
            response.setStatus(destination.exists() ? 204 : 201);
        } catch (DavException e) {
            if (e instanceof CosmoDavException) {
                throw (CosmoDavException) e;
            }
            throw new CosmoDavException(e);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void report(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        try {
            ReportInfo info = request.getReportInfo();
            if (info == null) {
                if (resource.isCollection()) {
                    return;
                } else {
                    throw new BadRequestException("REPORT requires entity body");
                }
            }
            /*
             * Since the report type could not be determined in the security filter in order to check ticket permissions
             * on REPORT, the check must be done manually here.
             */
            checkReportAccess(resource, info);

            ((ReportBase) resource.getReport(info)).run(response);
        } catch (DavException e) {
            if (e instanceof CosmoDavException) {
                throw (CosmoDavException) e;
            }
            throw new CosmoDavException(e);
        }        
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void mkticket(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        if (!(resource instanceof DavItemResource)) {
            throw new MethodNotAllowedException("MKTICKET requires a content collection or content resource");
        }
        DavItemResource dir = (DavItemResource) resource;

        if (LOG.isDebugEnabled()) {
            LOG.debug("MKTICKET for {}", resource.getResourcePath());
        }

        Ticket ticket = request.getTicketInfo();
        ticket.setOwner(getSecurityContext().getUser());

        dir.saveTicket(ticket);

        response.sendMkTicketResponse(dir, ticket.getKey());
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void delticket(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        if (!(resource instanceof DavItemResource)) {
            throw new MethodNotAllowedException("DELTICKET requires a content collection or content resource");
        }
        DavItemResource dir = (DavItemResource) resource;

        if (LOG.isDebugEnabled()) {
            LOG.debug("DELTICKET for {}", resource.getResourcePath());
        }

        String key = request.getTicketKey();
        Ticket ticket = dir.getTicket(key);
        if (ticket == null) {
            throw new PreconditionFailedException("Ticket " + key + " does not exist");
        }
        dir.removeTicket(ticket);

        response.setStatus(204);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void acl(DavRequest request, DavResponse response, WebDavResource resource)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("ACL for {}", resource.getResourcePath());
        }
        throw new UnsupportedPrivilegeException("No unprotected ACEs are supported on this resource");
    }

    // our methods
    /**
     * 
     * @param request
     * @param response
     * @param resource
     * @param withEntity
     * @throws CosmoDavException
     * @throws IOException
     */
    protected void spool(DavRequest request, DavResponse response, WebDavResource resource, boolean withEntity)
            throws CosmoDavException, IOException {
        if (!resource.exists()) {
            throw new NotFoundException();
        }
        checkNoRequestBody(request);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spooling resource {}", resource.getResourcePath());
        }
        resource.writeTo(createOutputContext(response, withEntity));
        response.flushBuffer();
    }

    /**
     * 
     * @param request
     * @return InputContext
     * @throws CosmoDavException
     * @throws IOException
     */
    protected InputContext createInputContext(DavRequest request) throws CosmoDavException, IOException {
        String xfer = request.getHeader("Transfer-Encoding");
        boolean chunked = xfer != null && xfer.equals("chunked");
        if (xfer != null && !chunked) {
            throw new BadRequestException("Unknown Transfer-Encoding " + xfer);
        }
        if (request.getContentLength() <= 0) {
            throw new BadRequestException("no content length set to input stream.");
        }

        return new DavInputContext(request, request.getInputStream());
    }

    /**
     * 
     * @param response
     *            DavResponse
     * @param withEntity
     *            boolean
     * @return OutputContext
     * @throws IOException
     */
    protected OutputContext createOutputContext(DavResponse response, boolean withEntity) throws IOException {
        OutputStream out = withEntity ? response.getOutputStream() : null;
        return new OutputContextImpl(response, out);
    }

    /**
     * 
     * @param locator
     *            DavResourceLocator
     * @param original
     *            WebDavResource
     * @return WebDavResource
     * @throws CosmoDavException
     */
    protected WebDavResource resolveDestination(DavResourceLocator locator, WebDavResource original)
            throws CosmoDavException {
        if (locator == null) {
            return null;
        }
        WebDavResource destination = resourceFactory.resolve(locator);
        return destination != null ? destination : new DavFile(locator, resourceFactory, entityFactory);
    }

    /**
     * 
     * @param request
     *            DavRequest
     * @param destination
     *            WebDavResource
     * @throws CosmoDavException
     */
    protected void validateDestination(DavRequest request, WebDavResource destination) throws CosmoDavException {
        if (destination == null) {
            throw new BadRequestException("Destination required");
        }
        if (destination.getResourceLocator().equals(request.getResourceLocator())) {
            throw new ForbiddenException("Destination URI is the same as the original resource URI");
        }
        if (destination.exists() && !request.isOverwrite()) {
            throw new PreconditionFailedException("Overwrite header false was not specified for existing destination");
        }
        if (!destination.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }
    }

    /**
     * 
     * @param source
     *            WebDavResource
     * @param destination
     *            WebDavResource
     * @throws CosmoDavException
     */
    protected void checkCopyMoveAccess(WebDavResource source, WebDavResource destination) throws CosmoDavException {
        // XXX refactor a BaseItemProvider so we don't have to do this check
        if (!(source instanceof DavItemResource)) {
            // we're operating on a principal resource which can't be moved
            // anyway
            return;
        }

        // because the security filter let us get this far, we know the
        // security context has access to the source resource. we have to
        // check that it also has access to the destination resource.

        if (getSecurityContext().isAdmin()) {
            return;
        }

        WebDavResource toCheck = destination.exists() ? destination : destination.getParent();
        Item item = ((DavItemResource) toCheck).getItem();
        DavResourceLocator locator = toCheck.getResourceLocator();
        String href = locator.getHref(toCheck.isCollection());
        DavPrivilege privilege = destination.exists() ? DavPrivilege.WRITE : DavPrivilege.BIND;

        User user = getSecurityContext().getUser();
        if (user != null) {
            UserAclEvaluator evaluator = new UserAclEvaluator(user);
            if (evaluator.evaluate(item, privilege)) {
                return;
            }
            throw new NeedsPrivilegesException(href, privilege);
        }

        Ticket ticket = getSecurityContext().getTicket();
        if (ticket != null) {
            TicketAclEvaluator evaluator = new TicketAclEvaluator(ticket);
            if (evaluator.evaluate(item, privilege)) {
                return;
            }
            throw new NeedsPrivilegesException(href, privilege);
        }

        throw new NeedsPrivilegesException(href, privilege);
    }

    /**
     * 
     * @return AclEvaluator
     */
    protected AclEvaluator createAclEvaluator() {
        User user = getSecurityContext().getUser();
        if (user != null) {
            return new UserAclEvaluator(user);
        }
        Ticket ticket = getSecurityContext().getTicket();
        if (ticket != null) {
            return new TicketAclEvaluator(ticket);
        }
        throw new IllegalStateException("Anonymous principal not supported for ACL evaluation");
    }

    /**
     * 
     * @param resource
     *            WebDavResource
     * @param evaluator
     *            AclEvaluator
     * @param privilege
     *            DavPrivilege
     * @return boolean
     */
    protected boolean hasPrivilege(WebDavResource resource, AclEvaluator evaluator, DavPrivilege privilege) {
        boolean hasPrivilege = false;
        if (resource instanceof DavItemResource) {
            Item item = ((DavItemResource) resource).getItem();
            hasPrivilege = evaluator.evaluate(item, privilege);
        } else {
            if (evaluator instanceof TicketAclEvaluator) {
                throw new IllegalStateException(
                        "A ticket may not be used to access a user principal collection or resource");
            }
            UserAclEvaluator uae = (UserAclEvaluator) evaluator;

            if (resource instanceof DavUserPrincipalCollection || resource instanceof DavInboxCollection
                    || resource instanceof DavOutboxCollection) {
                hasPrivilege = uae.evaluateUserPrincipalCollection(privilege);
            } else {
                User user = ((DavUserPrincipal) resource).getUser();
                hasPrivilege = uae.evaluateUserPrincipal(user, privilege);
            }
        }

        if (hasPrivilege) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Principal has privilege {}", privilege);
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Principal does not have privilege " + privilege);
        }
        return false;
    }

    protected void checkPropFindAccess(WebDavResource resource, DavPropertyNameSet props, int type)
            throws CosmoDavException {
        AclEvaluator evaluator = createAclEvaluator();

        // If the principal has DAV:read, then the propfind can continue
        if (hasPrivilege(resource, evaluator, DavPrivilege.READ)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing PROPFIND");
            }
            return;
        }

        /*
         * If there is at least one property that can be viewed with DAV:read-current-user-privilege-set, then check for
         * that privilege as well.
         */
        int unprotected = 0;
        if (props.contains(CURRENTUSERPRIVILEGESET)) {
            unprotected++;
        }
        // Ticket discovery is only unprotected when the principal is a ticket
        if (props.contains(TICKETDISCOVERY) && evaluator instanceof TicketAclEvaluator) {
            unprotected++;
        }

        if (unprotected > 0 && hasPrivilege(resource, evaluator, DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)) {

            if (props.getContentSize() > unprotected) {
                // XXX: if they don't have DAV:read, they shouldn't be able to access any other properties
                LOG.warn("Exposing secured properties to ticket without DAV:read");
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing PROPFIND");
            }
            return;
        }

        // Don't allow the client to know that this resource actually exists
        if (LOG.isDebugEnabled()) {
            LOG.debug("Denying PROPFIND");
        }
        throw new NotFoundException();
    }

    protected void checkReportAccess(WebDavResource resource, ReportInfo info) throws CosmoDavException {
        AclEvaluator evaluator = createAclEvaluator();

        // if the principal has DAV:read, then the propfind can continue
        if (hasPrivilege(resource, evaluator, DavPrivilege.READ)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing REPORT");
            }
            return;
        }

        // If this is a free-busy report, then check CALDAV:read-free-busy also
        if (isFreeBusyReport(info) && hasPrivilege(resource, evaluator, DavPrivilege.READ_FREE_BUSY)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing REPORT");
            }
            return;
        }

        // Don't allow the client to know that this resource actually exists
        if (LOG.isDebugEnabled()) {
            LOG.debug("Denying PROPFIND");
        }
        throw new NotFoundException();
    }

    /**
     * 
     * @param request
     *            DavRequest
     * @throws CosmoDavException
     */
    protected void checkNoRequestBody(DavRequest request) throws CosmoDavException {
        boolean hasBody = false;
        try {
            hasBody = request.getRequestDocument() != null;
        } catch (IllegalArgumentException e) {
            // parse error indicates that there was a body to parse
            hasBody = true;
        } catch (DavException e) {
            throw new CosmoDavException(e);
        }

        if (hasBody) {
            throw new UnsupportedMediaTypeException("Body not expected for method " + request.getMethod());
        }
    }

    /**
     * 
     * @param request
     *            DavRequest
     * @return depth
     * @throws CosmoDavException
     */
    protected int getDepth(DavRequest request) throws CosmoDavException {
        try {
            return request.getDepth();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected CosmoSecurityContext getSecurityContext() {
        return getResourceFactory().getSecurityManager().getSecurityContext();
    }

    public DavResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * 
     * @param info
     *            ReportInfo
     * @return boolean
     */
    private boolean isFreeBusyReport(ReportInfo info) {
        return FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY.isRequestedReportType(info);
    }

    /**
     * 
     * @param msr
     *            MultiStatusResponse
     * @return boolean
     */
    public static boolean hasNonOK(MultiStatusResponse msr) {
        if (msr == null || msr.getStatus() == null) {
            return false;
        }

        for (Status status : msr.getStatus()) {

            if (status != null) {
                int statusCode = status.getStatusCode();

                if (statusCode != 200) {
                    return true;
                }
            }
        }
        return false;
    }
}