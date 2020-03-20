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
package org.unitedinternet.cosmo.acegisecurity.providers.ticket;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.unitedinternet.cosmo.dav.CaldavMethodType;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;

/**
 * Votes affirmatively if the authenticated principal is a ticket and
 * the ticket has the privilege required by the requested WebDAV
 * method.
 *
 * This is a temporary approach until a full ACL system is in place.
 */
public class TicketVoter implements AccessDecisionVoter<Object> {
    
    /**
     * @param authentication The authentication.
     * @param object The obj.
     * @param attributes The attributes.
     * @return The type of access.
     */
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if (! (authentication instanceof TicketAuthenticationToken)) {
            return ACCESS_ABSTAIN;
        }

        Ticket ticket = (Ticket) authentication.getPrincipal();

        FilterInvocation fi = (FilterInvocation) object;
        String method = fi.getHttpRequest().getMethod();

        // freebusy reports and certain propfinds have their own rules, and
        // since we haven't parsed the request body yet, we have to defer
        // authorization to the servlet layer
        if (method.equals("REPORT") ||
            method.equals("PROPFIND")) {
            return ACCESS_GRANTED;
        }

        // you can't make or delete a ticket with another ticket
        if (method.equals("MKTICKET") || method.equals("DELTICKET")) {
            return ACCESS_DENIED;
        }

        if (CaldavMethodType.isReadMethod(method)) {
            return ticket.getPrivileges().contains(TicketType.PRIVILEGE_READ) ?
                ACCESS_GRANTED :
                ACCESS_DENIED;
        }

        if (CaldavMethodType.isWriteMethod(method)) {
            return ticket.getPrivileges().contains(TicketType.PRIVILEGE_WRITE) ?
                ACCESS_GRANTED :
                ACCESS_DENIED;
        }

        return ACCESS_ABSTAIN;
    }

    /**
     * Always returns true, since this voter does not examine any
     * config attributes.
     * @param attribute The config attribute.
     * @return True.
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * Returns true if the secure object is a
     * {@link org.springframework.security.intercept.web.FilterInvocation}
     * @param clazz Clazz.
     * @return boolean.
     */
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
