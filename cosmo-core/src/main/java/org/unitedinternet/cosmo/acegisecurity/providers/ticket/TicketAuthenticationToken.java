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

import java.util.Arrays;
import java.util.Set;

import org.unitedinternet.cosmo.model.Ticket;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Represents a ticket-based
 * {@link org.springframework.security.Authentication}.
 *
 * Before being authenticated, the token contains the ticket id and
 * the path of the ticketed resource. After authentication, the
 * token's principal is the {@link Ticket} itself.
 */
public class TicketAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 1106578947954376394L;

    private static final GrantedAuthority[] AUTHORITIES = { 
                new SimpleGrantedAuthority("ROLE_TICKET") };
    
    private boolean authenticated;
    private String path;
    private Set<String> keys;
    private Ticket ticket;

    /**
     * @param path the absolute URI path to the ticketed resource
     * @param keys all ticket keys provided for the resource
     */
    public TicketAuthenticationToken(String path,
                                     Set<String> keys) {
        super( Arrays.asList(AUTHORITIES));
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("path may not be null or empty");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys may not be null or empty");
        }
        this.path = path;
        this.keys = keys;
        authenticated = false;
    }

    // Authentication methods

    /**
     * Sets authenticated.
     * @param isAuthenticated is authenticated boolean.
     */
    public void setAuthenticated(boolean isAuthenticated) {
        authenticated = isAuthenticated;
    }

    /**
     * Verify if the user is authenticated.
     * {@inheritDoc}
     * @return If the user is authenticated or not.
     * 
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Always returns an empty <code>String</code>.
     * @return The credentials.
     */
    public Object getCredentials() {
        return "";
    }

    /**
     * Returns the ticket.
     * @return The ticket.
     */
    public Object getPrincipal() {
        return ticket;
    }

    // our methods

    /**
     * Sets the ticket.
     * @param ticket The ticket.
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Gets path.
     * @return The path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets keys.
     * @return the keys.
     */
    public Set<String> getKeys() {
        return keys;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

  

    /**
     * Equals.
     * {@inheritDoc}
     * @param obj The obj.
     * @return The result.
     */
    @Override
    public boolean equals(Object obj) {
        if (! super.equals(obj)) {
            return false;
        }
        if (! (obj instanceof TicketAuthenticationToken)) {
            return false;
        }
        TicketAuthenticationToken test = (TicketAuthenticationToken) obj;
        //After authentication, the token's principal is the {@link Ticket} itself.
        return ticket.equals(test.getPrincipal());
    }
    
    
}
