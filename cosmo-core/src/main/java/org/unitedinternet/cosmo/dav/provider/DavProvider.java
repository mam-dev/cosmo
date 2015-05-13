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

import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.DavResponse;

/**
 * <p>
 * An interface for components that execute requests against WebDAV resources.
 * </p>
 */
public interface DavProvider {

    void get(DavRequest request,
                    DavResponse response,
                    WebDavResource resource)
        throws CosmoDavException, IOException;

    void head(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException;

    void propfind(DavRequest request,
                         DavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException;
    
    void proppatch(DavRequest request,
                          DavResponse response,
                          WebDavResource resource)
        throws CosmoDavException, IOException;

    void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws CosmoDavException, IOException;

    void post(DavRequest request,
            DavResponse response,
            WebDavResource resource)
        throws CosmoDavException, IOException;

    void delete(DavRequest request,
                       DavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException;

    void copy(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException;

    void move(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException;

    void mkcol(DavRequest request,
                      DavResponse response,
                      DavCollection collection)
        throws CosmoDavException, IOException;

    void report(DavRequest request,
                       DavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException;

    void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws CosmoDavException, IOException;

    void mkticket(DavRequest request,
                         DavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException;

    void delticket(DavRequest request,
                          DavResponse response,
                          WebDavResource resource)
        throws CosmoDavException, IOException;

    void acl(DavRequest request,
                    DavResponse response,
                    WebDavResource resource)
        throws CosmoDavException, IOException;
}
