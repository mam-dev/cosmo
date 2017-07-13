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
package org.unitedinternet.cosmo.dav.provider;

import java.io.IOException;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResponse;

/**
 * <p>
 * An interface for components that execute requests against WebDAV resources.
 * </p>
 */
public interface DavProvider {

    void get(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource resource)
        throws CosmoDavException, IOException;

    void head(CalDavRequest request,
                     CalDavResponse response,
                     CalDavResource resource)
        throws CosmoDavException, IOException;

    void propfind(CalDavRequest request,
                         CalDavResponse response,
                         CalDavResource resource)
        throws CosmoDavException, IOException;
    
    void proppatch(CalDavRequest request,
                          CalDavResponse response,
                          CalDavResource resource)
        throws CosmoDavException, IOException;

    void put(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource content)
        throws CosmoDavException, IOException;

    void post(CalDavRequest request,
            CalDavResponse response,
            CalDavResource resource)
        throws CosmoDavException, IOException;

    void delete(CalDavRequest request,
                       CalDavResponse response,
                       CalDavResource resource)
        throws CosmoDavException, IOException;

    void copy(CalDavRequest request,
                     CalDavResponse response,
                     CalDavResource resource)
        throws CosmoDavException, IOException;

    void move(CalDavRequest request,
                     CalDavResponse response,
                     CalDavResource resource)
        throws CosmoDavException, IOException;

    void mkcol(CalDavRequest request,
                      CalDavResponse response,
                      CalDavCollection collection)
        throws CosmoDavException, IOException;

    void report(CalDavRequest request,
                       CalDavResponse response,
                       CalDavResource resource)
        throws CosmoDavException, IOException;

    void mkcalendar(CalDavRequest request,
                           CalDavResponse response,
                           CalDavCollection collection)
        throws CosmoDavException, IOException;

    void mkticket(CalDavRequest request,
                         CalDavResponse response,
                         CalDavResource resource)
        throws CosmoDavException, IOException;

    void delticket(CalDavRequest request,
                          CalDavResponse response,
                          CalDavResource resource)
        throws CosmoDavException, IOException;

    void acl(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource resource)
        throws CosmoDavException, IOException;
}
