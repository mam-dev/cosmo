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

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.BaseSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;

/**
 * A mock implementation of {@link CosmoSecurityContext} that provides
 * dummy instances for use with unit mocks.
 */
public class MockSecurityContext extends BaseSecurityContext {
    /**
     * Constructor.
     * @param principal The principal.
     */
    public MockSecurityContext(Principal principal) {
        super(principal, null);
    }
    
    /**
     * Constructor.
     * @param principal The principal
     * @param tickets The tickets.
     */
    public MockSecurityContext(Principal principal, Set<Ticket> tickets) {
        super(principal, tickets);
    }

    /**
     * Process principal.
     * {@inheritDoc}
     */
    protected void processPrincipal() {
        if (getPrincipal() instanceof MockAnonymousPrincipal) {
            setAnonymous(true);
        }
        else if (getPrincipal() instanceof MockUserPrincipal) {
            User user = ((MockUserPrincipal) getPrincipal()).getUser();
            setUser(user);
            setAdmin(user.getAdmin().booleanValue());
        }
        else if (getPrincipal() instanceof MockTicketPrincipal) {
            setTicket(((MockTicketPrincipal) getPrincipal()).getTicket());
        }
        else {
            throw new CosmoException("unknown principal type " + getPrincipal().getClass().getName(), new CosmoException());
        }
    }
}
