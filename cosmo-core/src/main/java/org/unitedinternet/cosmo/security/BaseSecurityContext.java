/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;

/**
 * Base class for implementations of {@link CosmoSecurityContext}.
 */
public abstract class BaseSecurityContext implements CosmoSecurityContext {
   

    private boolean admin;
    private boolean anonymous;
    private Principal principal;
    private Ticket ticket;
    private User user;
    private Set<Ticket> tickets;

    /**
     */
    public BaseSecurityContext(Principal principal, Set<Ticket> tickets) {
        this.anonymous = false;
        this.principal = principal;
        this.admin = false;
        this.tickets = tickets;
        processPrincipal();
    }

    /**
     */
    public BaseSecurityContext(Principal principal, Set<Ticket> tickets, User preAuthUser) {
        this.anonymous = false;
        this.principal = principal;
        this.admin = preAuthUser.getAdmin().booleanValue();
        this.tickets = tickets;
        this.user = preAuthUser;
    }
    
    /* ----- CosmoSecurityContext methods ----- */

    /**
     * @return a name describing the principal for this security
     * context (the name of the Cosmo user, the id of the ticket, or
     * the string <code>anonymous</code>.
     */
    public String getName() {
        if (isAnonymous()) {
            return "anonymous";
        }
        if (ticket != null) {
            return ticket.getKey();
        }
        return user.getUsername();
    }

    /**
     * @return Determines whether or not the context represents an anonymous
     * Cosmo user.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * @return an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context does not represent a user.
     */
    public User getUser() {
        return user;
    }

    /**
     * @return an instance of {@link Ticket} describing the ticket
     * represented by the security context, or <code>null</code> if
     * the context does not represent a ticket.
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * @return Determines whether or not the security context represents an
     * administrator
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @param item The given item that are visible to the current security context.
     * @return The set of tickets granted on the given item that are
     * visible to the current security context.
     */
    public Set<Ticket> findVisibleTickets(Item item) {
        HashSet<Ticket> visible = new HashSet<Ticket>();

        // Admin context has access to all tickets
        if (admin) {
            visible.addAll(item.getTickets());
        }

        // Ticket context can only see itself
        else if (ticket != null) {
            for (Ticket t : (Set<Ticket>) item.getTickets()) {
                if (ticket.equals(t)) {
                    visible.add(t);
                }
            }
        }

        // User context can only see the tickets he owns
        else if (user != null) {
            for (Ticket t : (Set<Ticket>) item.getTickets()) {
                if (user.equals(t.getOwner())) {
                    visible.add(t);
                }
            }
        }

        // Anonymous context can't see any tickets

        return visible;
    }

    /* ----- our methods ----- */

    protected Principal getPrincipal() {
        return principal;
    }

    protected void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    protected void setUser(User user) {
        this.user = user;
    }

    protected void setAdmin(boolean admin) {
        this.admin = admin;
    }

    protected void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Called by the constructor to set the context state. Examines
     * the principal to decide if it represents a ticket or user or
     * anonymous access.
     */
    protected abstract void processPrincipal();

    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }
}
