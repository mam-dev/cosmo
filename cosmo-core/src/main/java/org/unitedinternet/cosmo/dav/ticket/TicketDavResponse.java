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

import java.io.IOException;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;

/**
 * Provides response functionality required by ticket extensions to WebDAV.
 */
public interface TicketDavResponse {

    /**
     * Send the <code>ticketdiscovery</code> response to a
     * <code>MKTICKET</code> request.
     *
     * @param resource the resource on which the ticket was created
     * @param ticketId the id of the newly created ticket
     */
    void sendMkTicketResponse(DavItemResource resource,
                                     String ticketId)
        throws CosmoDavException, IOException;
}
