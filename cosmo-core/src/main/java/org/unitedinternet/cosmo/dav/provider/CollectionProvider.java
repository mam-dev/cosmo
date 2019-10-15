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
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.ExistsException;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavCollection
 */
public class CollectionProvider extends BaseProvider {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(CollectionProvider.class);

    public CollectionProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("PUT not allowed for a collection");
    }

    public void mkcol(DavRequest request,
                      DavResponse response,
                      DavCollection collection)
        throws CosmoDavException, IOException {
        if (collection.exists()) {
            throw new ExistsException();
        }
        if (! collection.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }
        DavPropertySet properties = request.getMkcolProperties();
        MultiStatusResponse msr = collection.getParent().addCollection(collection, null);
        sendMultiStatus(properties, msr, response);
    }

    /**
     * Send multi status response (according to WebDAV RFC) using WebDAV OR send empty response with code 201
     * if no properties are sent.
     * @param properties DAV properties
     * @param msr a MultiStatusResponse that you get from calling addCollection on DavCollection
     * @param response
     * @throws java.io.IOException
     * @see DavCollection
     */
    protected void  sendMultiStatus (DavPropertySet properties, MultiStatusResponse msr, DavResponse response) throws java.io.IOException {
        if (properties.isEmpty() || !hasNonOK(msr)) {
            response.setStatus(201);
            response.setHeader("Cache-control", "no-cache");
            response.setHeader("Pragma", "no-cache");
        }
        MultiStatus ms = new MultiStatus();
        ms.addResponse(msr);
        response.sendMultiStatus(ms);

    }
    public void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws CosmoDavException, IOException {  
        throw new UnsupportedOperationException();
    }
}
