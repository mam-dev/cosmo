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

import java.util.Set;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.BaseSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Standard implementation of {@link CosmoSecurityContext}. Wraps
 * an instance of Acegi Security's
 * {@link org.springframework.security.Authentication}.
 *
 * XXX: consider removing the direct dependency on Acegi Security,
 * instead possibly having separate implementations of
 * CosmoSecurityContext for users and tickets that don't use an
 * Authentication at all
 */
public class CosmoSecurityContextImpl extends BaseSecurityContext {
    
    public CosmoSecurityContextImpl(Authentication authentication, Set<Ticket> tickets) {
        super(authentication, tickets);
    }

    public CosmoSecurityContextImpl(Authentication authentication, Set<Ticket> tickets, User preAuthUser) {
        super(authentication, tickets, preAuthUser);
    }
    
    protected void processPrincipal() {  
        //anonymous principals do not have CosmoUserDetails and by
        //definition are not running as other principals
        if (getPrincipal() instanceof AnonymousAuthenticationToken) {
            setAnonymous(true);
        } else if (getPrincipal() instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails)
                ((Authentication) getPrincipal()).getPrincipal();
            setUser(details.getUser());
            setAdmin(details.getUser().getAdmin().booleanValue());
        } else if (getPrincipal() instanceof TicketAuthenticationToken) {
            Ticket ticket = (Ticket)((Authentication) getPrincipal()).getPrincipal();
            setTicket(ticket);
        } else {
            throw new CosmoException("Unknown principal type " + getPrincipal().getClass().getName(),
                    new CosmoException());
        }
    }
    
}
