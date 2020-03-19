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
package org.unitedinternet.cosmo.dav.acegisecurity;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.dav.CaldavMethodType;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.acl.AclEvaluator;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.TicketAclEvaluator;
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;

/**
 * <p>
 * Makes access control decisions for users and user
 * resources.  Allow service layer to handle authorization
 * for all other resources.
 * </p>
 */
@Service
public class DavAccessDecisionManager
        implements AccessDecisionManager, ExtendedDavConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavAccessDecisionManager.class);

    private final UserService userService;
    
    public DavAccessDecisionManager(UserService userService) {
        super();
        this.userService = userService;
    }

    // DavAccessDecisionManager methods

    /**
     * <p>
     * </p>
     *
     * @throws InsufficientAuthenticationException
     *          if
     *          <code>Authentication</code> is not a
     *          {@link UsernamePasswordAuthenticationToken} or a
     *          {@link TicketAuthenticationToken}.
     */
    @Override
    public void decide(Authentication authentication, Object object,
                       Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        AclEvaluator evaluator = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails)
                    authentication.getPrincipal();
            evaluator = new UserAclEvaluator(details.getUser());
        } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            User user = userService.getUser((String)authentication.getPrincipal());
            evaluator = new UserAclEvaluator(user);
        } else if (authentication instanceof TicketAuthenticationToken) {
            Ticket ticket = (Ticket) authentication.getPrincipal();
            evaluator = new TicketAclEvaluator(ticket);
        } else {
            LOG.error("Unrecognized authentication token");
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }

        HttpServletRequest request =
                ((FilterInvocation) object).getHttpRequest();

        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }
        // remove trailing slash that denotes a collection
        if (!path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        try {
            match(path, request.getMethod(), evaluator);
        } catch (AclEvaluationException e) {
            throw new DavAccessDeniedException(request.getRequestURI(),
                    e.getPrivilege());
        }
    }

    /**
     * Always returns true, as this manager does not support any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * Returns true if the secure object is a
     * {@link FilterInvocation}.
     */
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    // our methods

    protected void match(String path,
                         String method,
                         AclEvaluator evaluator)
            throws AclEvaluationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("matching resource {} with method {}", path, method);
        }

        UriTemplate.Match match = null;

        match = TEMPLATE_USERS.match(false, path);
        if (match != null) {
            evaluateUserPrincipalCollection(match, method, evaluator);
            return;
        }

        match = TEMPLATE_USER.match(false, path);
        if (match != null) {
            evaluateUserPrincipal(match, method, evaluator);
            return;
        }
    }

    protected void evaluateUserPrincipalCollection(UriTemplate.Match match,
                                                   String method,
                                                   AclEvaluator evaluator)
            throws AclEvaluationException {
        if (evaluator instanceof TicketAclEvaluator) {
            throw new IllegalStateException("A ticket may not be used to access the user principal collection");
        }
        if (method.equals("PROPFIND")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing method: {} so provider can evaluate check access itself", method);
            }
            return;
        }

        UserAclEvaluator uae = (UserAclEvaluator) evaluator;
        DavPrivilege privilege = CaldavMethodType.isReadMethod(method) ?
                DavPrivilege.READ : DavPrivilege.WRITE;
        if (!uae.evaluateUserPrincipalCollection(privilege)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Principal does not have privilege {}; denying access", privilege);
            }
            throw new AclEvaluationException(null, privilege);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Principal has privilege {}; allowing access", privilege);
        }
    }

    protected void evaluateUserPrincipal(UriTemplate.Match match,
                                         String method,
                                         AclEvaluator evaluator)
            throws AclEvaluationException {
        if (evaluator instanceof TicketAclEvaluator) {
            throw new IllegalStateException("A ticket may not be used to access the user principal collection");
        }

        String username = match.get("username");
        User user = this.userService.getUser(username);
        if (user == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User {} not found; allowing for 404", username);
            }
            return;
        }

        if (method.equals("PROPFIND")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Allowing method {} so provider can evaluate check access itself", method);
            }
            return;
        }

        UserAclEvaluator uae = (UserAclEvaluator) evaluator;
        DavPrivilege privilege = CaldavMethodType.isReadMethod(method) ?
                DavPrivilege.READ : DavPrivilege.WRITE;
        if (!uae.evaluateUserPrincipal(user, privilege)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Principal does not have privilege {}; denying access", privilege);
            }
            throw new AclEvaluationException(null, privilege);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Principal has privilege {}; allowing access", privilege);
        }
    }   

    @SuppressWarnings("serial")
    public static class AclEvaluationException extends Exception {
        private Item item;
        private transient DavPrivilege privilege;

        public AclEvaluationException(Item item,
                                      DavPrivilege privilege) {
            this.item = item;
            this.privilege = privilege;
        }

        public Item getItem() {
            return item;
        }

        public DavPrivilege getPrivilege() {
            return privilege;
        }
    }

}
