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
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavUserPrincipal</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavUserPrincipal
 */
public class UserPrincipalProvider extends BaseProvider {

    public UserPrincipalProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("PUT not allowed for user principal");
    }

    public void delete(DavRequest request,
                       DavResponse response,
                       WebDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELETE not allowed for user principal");
    }

    public void copy(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("COPY not allowed for user principal");
    }

    public void move(DavRequest request,
                     DavResponse response,
                     WebDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MOVE not allowed for user principal");
    }

    public void mkcol(DavRequest request,
                      DavResponse response,
                      DavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCOL not allowed for user principal");
    }

    public void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCALENDAR not allowed for user principal");
    }

    public void mkticket(DavRequest request,
                         DavResponse response,
                         WebDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKTICKET not allowed for user principal");
    }

    public void delticket(DavRequest request,
                          DavResponse response,
                          WebDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELTICKET not allowed for user principal");
    }
}
