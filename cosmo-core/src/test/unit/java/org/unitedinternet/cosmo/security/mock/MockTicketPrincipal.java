/*
 * Copyright 2006 Open Source Applications Foundation
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

import org.unitedinternet.cosmo.model.Ticket;

/**
 */
public class MockTicketPrincipal implements Principal {
    private Ticket ticket;

    /**
     * Constructor.
     * @param ticket The ticket.
     */
    public MockTicketPrincipal(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param another another.
     * @return The boolean for equals.
     */
    public boolean equals(Object another) {
        if (!(another instanceof MockTicketPrincipal)) {
            return false;
        }
        return ticket.equals(((MockTicketPrincipal)another).getTicket());
    }

    /**
     * ToString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return ticket.toString();
    }

    /**
     * The hashcode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    public int hashCode() {
        return ticket.hashCode();
    }

    /**
     * Gets name.
     * {@inheritDoc}
     * @return The name.
     */
    public String getName() {
        return ticket.getKey();
    }

    /**
     * Gets ticket.
     * @return The ticket.
     */
    public Ticket getTicket() {
        return ticket;
    }
}
