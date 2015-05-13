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
package org.unitedinternet.cosmo.server;

/**
 * Constants related to the protocols and interfaces presented by the
 * server.
 */
public interface ServerConstants {

    /** The request parameter containing a ticket key */
    String PARAM_TICKET = "ticket";

    /** The request header containing a ticket key */
    String HEADER_TICKET = "Ticket";

    String ATTR_SERVICE_EXCEPTION = "server.exception";

    /**
     * The service id for CMP
     */
    String SVC_CMP = "cmp";
    /**
     * The service id for WebDAV
     */
    String SVC_DAV = "dav";
    /**
     * The service id for WebDAV principals
     */
    String SVC_DAV_PRINCIPAL = "davPrincipal";
    /**
     * The service id for CalDAV calendar homes
     */
    String SVC_DAV_CALENDAR_HOME = "davCalendarHome";

}
