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
package org.unitedinternet.cosmo.security.mock;

import java.security.Principal;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.PermissionDeniedException;

/**
 * A mock implementation of the {@link CosmoSecurityManager} interface that provides a dummy
 * {@link CosmoSecurityContext} for unit mocks.
 */
public class MockSecurityManager implements CosmoSecurityManager {

    private static final Logger LOG = LoggerFactory.getLogger(MockSecurityManager.class);

    private static ThreadLocal<CosmoSecurityContext> contexts = new ThreadLocal<>();

    private TestHelper testHelper;

    /**
     * Constructor.
     */
    public MockSecurityManager() {
        testHelper = new TestHelper();
    }

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a Cosmo user previously authenticated by the Cosmo
     * security system.
     * 
     * @return cosmo security context.
     * @throws CosmoSecurityException - if something is wrong this exception is thrown.
     */
    public CosmoSecurityContext getSecurityContext() throws CosmoSecurityException {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            throw new CosmoSecurityException("security context not set up");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting security context for {}", context.getName());
        }
        return context;
    }

    /**
     * Authenticate the given Cosmo credentials and register a <code>CosmoSecurityContext</code> for them. This method
     * is used when Cosmo components need to programatically log in a user rather than relying on a security context
     * already being in place.
     * 
     * @param username The username.
     * @param password The password.
     * @return Cosmo security context.
     * @throws CosmoSecurityException - if something is wrong this exception is thrown.
     */
    public CosmoSecurityContext initiateSecurityContext(String username, String password)
            throws CosmoSecurityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initiating security context for {}", username);
        }
        Principal principal = testHelper.makeDummyUserPrincipal(username, password);
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        return context;
    }

    /**
     * Initiate the current security context with the current user. This method is used when the server needs to run
     * code as a specific user.
     * 
     * @param user The user.
     * @return Cosmo security context.
     * @throws CosmoSecurityException - if something is wrong this exception is thrown.
     */
    public CosmoSecurityContext initiateSecurityContext(User user) throws CosmoSecurityException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initiating security context for {}", user.getUsername());
        }
        Principal principal = testHelper.makeDummyUserPrincipal(user);
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        return context;
    }

    // for testing
    /**
     * Initiates security context.
     * 
     * @param context The cosmo security context.
     */
    public void initiateSecurityContext(CosmoSecurityContext context) {
        contexts.set(context);
    }

    /**
     * Overwrite the existing <code>CosmoSecurityContext</code>. This method is used when Cosmo components need to
     * replace the existing security context with a different one (useful when executing multiple operations which
     * require different security contexts).
     * 
     * @param securityContext The security context.
     */
    public void refreshSecurityContext(CosmoSecurityContext securityContext) {
        contexts.set(securityContext);
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshing security context for {}", securityContext.getName());
        }
    }

    /* ----- our methods ----- */

    /**
     * Sets up mock security context.
     * 
     * @param principal The principal.
     * @return The cosmo security context.
     */
    public CosmoSecurityContext setUpMockSecurityContext(Principal principal) {
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting up security context for {}", context.getName());
        }
        return context;
    }

    /**
     * Tears down mock security context.
     */
    public void tearDownMockSecurityContext() {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("tearing down security context for {}", context.getName());
        }
        contexts.set(null);
    }

    /**
     * Registers tickets. {@inheritDoc}
     * 
     * @param tickets The tickets.
     */
    public void registerTickets(Set<Ticket> tickets) {
        // for now nothing
    }

    /**
     * Unregisters tickets. {@inheritDoc}
     */
    public void unregisterTickets() {
        // for now nothing
    }

    /**
     * Checks persmission. {@inheritDoc}
     * 
     * @param item       The item.
     * @param permission The permission.
     * @throws PermissionDeniedException - if something is wrong this exception is thrown.
     */
    public void checkPermission(Item item, int permission) throws PermissionDeniedException {
        return; // TODO does this Mock need more?
    }

    /**
     * Creates security context.
     * 
     * @param principal The principal.
     * @return The cosmo security context.
     */
    protected CosmoSecurityContext createSecurityContext(Principal principal) {
        return new MockSecurityContext(principal);
    }

}
