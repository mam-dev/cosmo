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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResponse;
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
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(UserPrincipalProvider.class);

    public UserPrincipalProvider(CalDavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void put(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource content)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("PUT not allowed for user principal");
    }

    public void delete(CalDavRequest request,
                       CalDavResponse response,
                       CalDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELETE not allowed for user principal");
    }

    public void copy(CalDavRequest request,
                     CalDavResponse response,
                     CalDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("COPY not allowed for user principal");
    }

    public void move(CalDavRequest request,
                     CalDavResponse response,
                     CalDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MOVE not allowed for user principal");
    }

    public void mkcol(CalDavRequest request,
                      CalDavResponse response,
                      CalDavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCOL not allowed for user principal");
    }

    public void mkcalendar(CalDavRequest request,
                           CalDavResponse response,
                           CalDavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCALENDAR not allowed for user principal");
    }

    public void mkticket(CalDavRequest request,
                         CalDavResponse response,
                         CalDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKTICKET not allowed for user principal");
    }

    public void delticket(CalDavRequest request,
                          CalDavResponse response,
                          CalDavResource resource)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("DELTICKET not allowed for user principal");
    }
}
