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
package org.unitedinternet.cosmo.dav.impl;

import java.util.Set;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;

/**
 * An interface for DAV resources that are backed by Cosmo content (e.g.
 * collections or items).
 */
public interface DavItemResource extends WebDavResource {

    Item getItem();

    void setItem(Item item)
        throws CosmoDavException;

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    void saveTicket(Ticket ticket) throws CosmoDavException;

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    void removeTicket(Ticket ticket) throws CosmoDavException;

    /**
     * @return the ticket with the given id on this resource.
     */
    Ticket getTicket(String id);

    /**
     *@return all visible tickets (those owned by the currently
     * authenticated user) on this resource, or an empty
     * <code>Set</code> if there are no visible tickets.
     */
    Set<Ticket> getTickets();
}
