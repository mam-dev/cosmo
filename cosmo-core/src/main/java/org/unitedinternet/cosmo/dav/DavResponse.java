/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav;

import java.io.IOException;

import org.apache.jackrabbit.webdav.WebdavResponse;

import org.unitedinternet.cosmo.dav.ticket.TicketDavResponse;

/**
 * A marker interface that collects the functionality defined by
 * the various WebDAV extensions implemented by the DAV service.
 */
public interface DavResponse
    extends WebdavResponse, TicketDavResponse {

    void sendDavError(CosmoDavException e)
        throws IOException;
}
