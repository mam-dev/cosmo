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
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.MethodNotAllowedException;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavFile;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResponse;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavFile</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavFile
 */
public class FileProvider extends BaseProvider {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(FileProvider.class);

    public FileProvider(CalDavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void put(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource content)
        throws CosmoDavException, IOException {
        if (! content.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }
        
        if(!(content instanceof CalDavFile)){
            throw new IllegalArgumentException("Expected type for 'content' is: [" + CalDavFile.class.getName() + "]");
        }
        int status = content.exists() ? 204 : 201;
        ((CalDavCollection)content.getParent()).addContent((CalDavFile)content, createInputContext(request));
        response.setStatus(status);
        response.setHeader("ETag", ((DavItemResourceBase) content).getETag());
    }

    public void mkcol(CalDavRequest request,
                      CalDavResponse response,
                      CalDavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCOL not allowed for a file");
    }

    public void mkcalendar(CalDavRequest request,
                           CalDavResponse response,
                           CalDavCollection collection)
        throws CosmoDavException, IOException {
        throw new MethodNotAllowedException("MKCALENDAR not allowed for a file");
    }
}
