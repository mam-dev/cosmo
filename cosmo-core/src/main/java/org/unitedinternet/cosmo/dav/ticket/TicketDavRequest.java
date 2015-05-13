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
package org.unitedinternet.cosmo.dav.ticket;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.model.Ticket;

/**
 * Provides request functionality required for ticket extensions to
 * WebDAV.
 */
public interface TicketDavRequest {

    /**
     * Return a {@link Ticket} representing the information about a
     * ticket to be created by a <code>MKTICKET</code> request.
     *
     * @throws CosmoDavException if there is no ticket information in the request
     * or if the ticket information exists but is invalid
     */
    Ticket getTicketInfo()
        throws CosmoDavException;

    /**
     * Return the ticket key included in this request, if any. If
     * different ticket keys are included in the headers and URL, the
     * one from the URL is used.
     *
     * @throws CosmoDavException if there is no ticket key in the request.
     */
    String getTicketKey()
        throws CosmoDavException;
}
