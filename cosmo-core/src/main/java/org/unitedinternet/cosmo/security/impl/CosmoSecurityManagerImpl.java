/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.PermissionDeniedException;
import org.unitedinternet.cosmo.service.UserService;

/**
 * The default implementation of the {@link CosmoSecurityManager} interface that provides a {@link CosmoSecurityContext}
 * from security information contained in JAAS or Acegi Security.
 */
@Component
public class CosmoSecurityManagerImpl implements CosmoSecurityManager {

    private static final Logger LOG = LoggerFactory.getLogger(CosmoSecurityManagerImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    // store additional tickets for authenticated principal
    private ThreadLocal<Set<Ticket>> tickets = new ThreadLocal<Set<Ticket>>();

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a Cosmo user previously authenticated by the Cosmo
     * security system.
     */
    public CosmoSecurityContext getSecurityContext() throws CosmoSecurityException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authen = context.getAuthentication();
        if (authen == null) {
            throw new CosmoSecurityException("no Authentication found in " + "SecurityContext");
        }

        if (authen instanceof PreAuthenticatedAuthenticationToken) {
            User user = userService.getUser((String) authen.getPrincipal());
            return new CosmoSecurityContextImpl(authen, tickets.get(), user);
        }

        return createSecurityContext(authen);
    }

    /**
     * Authenticate the given Cosmo credentials and register a <code>CosmoSecurityContext</code> for them. This method
     * is used when Cosmo components need to programatically log in a user rather than relying on a security context
     * already being in place.
     */
    public CosmoSecurityContext initiateSecurityContext(String username, String password)
            throws CosmoSecurityException {
        try {
            UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(username,
                    password);
            Authentication authentication = authenticationManager.authenticate(credentials);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authentication);
            return createSecurityContext(authentication);
        } catch (AuthenticationException e) {
            throw new CosmoSecurityException("can't establish security context", e);
        }
    }

    /**
     * Initiate the current security context with the current user. This method is used when the server needs to run
     * code as a specific user.
     */
    public CosmoSecurityContext initiateSecurityContext(User user) throws CosmoSecurityException {

        UserDetails details = new CosmoUserDetails(user);

        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(details, "",
                details.getAuthorities());

        credentials.setDetails(details);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(credentials);
        return createSecurityContext(credentials);
    }

    /**
     * Validates that the current security context has the requested permission for the given item.
     *
     * @throws PermissionDeniedException if the security context does not have the required permission
     */
    public void checkPermission(Item item, int permission) throws PermissionDeniedException, CosmoSecurityException {
        CosmoSecurityContext ctx = getSecurityContext();

        if (ctx.isAnonymous()) {
            LOG.warn("Anonymous access attempted to item {}", item.getUid());
            throw new PermissionDeniedException("Anonymous principals have no permissions");
        }

        // administrators can do anything to any item
        if (ctx.isAdmin()) {
            return;
        }

        User user = ctx.getUser();
        if (user != null) {
            // an item's owner can do anything to an item he owns
            if (user.equals(item.getOwner())) {
                return;
            }
            LOG.warn("User {} attempted access to item {} owned by {}", user.getUsername(), item.getUid());
            throw new PermissionDeniedException("User does not have appropriate permissions on item " + item.getUid());
        }

        Ticket ticket = ctx.getTicket();
        if (ticket != null) {
            if (!ticket.isGranted(item)) {
                LOG.warn("Non-granted ticket {} attempted access to item {}",ticket.getKey(), item.getUid());
                throw new PermissionDeniedException(
                        "Ticket " + ticket.getKey() + " is not granted on item " + item.getUid());
            }
            // Assume that when the security context was initiated the
            // ticket's expiration date was checked
            if (permission == Permission.READ && ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ)) {
                return;
            }
            if (permission == Permission.WRITE && ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE)) {
                return;
            }
            if (permission == Permission.FREEBUSY && ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY)) {
                return;
            }
            LOG.warn("Granted ticket {} attempted access to item {}", ticket.getKey(), item.getUid());
            throw new PermissionDeniedException(
                    "Ticket " + ticket.getKey() + " does not have appropriate permissions on item " + item.getUid());
        }
    }

    /* ----- our methods ----- */

    /**
     */
    protected CosmoSecurityContext createSecurityContext(Authentication authen) {
        return new CosmoSecurityContextImpl(authen, tickets.get());
    }

    /**
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 
     * @param userService UserService
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 
     * @return UserService
     */
    public UserService getUserService() {
        return userService;
    }

    public void registerTickets(Set<Ticket> tickets) {
        Set<Ticket> currentTickets = this.tickets.get();
        if (currentTickets == null) {
            this.tickets.set(new HashSet<Ticket>());
        }
        this.tickets.get().addAll(tickets);
    }

    public void unregisterTickets() {
        this.tickets.remove();
    }
}
