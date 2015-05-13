/*
 * Copyright 007 Open Source Applications Foundation
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

import java.net.URL;

import org.unitedinternet.cosmo.model.User;

/**
 * <p>
 * The interface for factory classes that create instances of
 * {@link DavResourceLocator}.
 * </p>
 */
public interface DavResourceLocatorFactory {

    /**
     * <p>
     * Returns a locator for the resource at the specified path relative
     * to the given context URL. The context URL includes enough path
     * information to identify the dav namespace.
     * </p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param path the unescaped path of the resource
     * @return The locator for the resource at the specified path relative to
     * the given context URL.
     */
    DavResourceLocator createResourceLocatorByPath(URL context,
                                                          String path);

    /**
     * <p>
     * Returns a locator for the resource at the specified URI relative
     * to the given context URL. The context URL includes enough path
     * information to identify the dav namespace.
     * </p>
     * <p>
     * If the URI is absolute, its scheme and authority must match those of
     * the context URL. The URI path must begin with the context URL's path.
     * </p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param uri the unescaped path of the resource
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    DavResourceLocator createResourceLocatorByUri(URL context,
                                                         String uri)
        throws CosmoDavException;

    /**
     * <p>
     * Returns a locator for the home collection of the given user
     * relative to the given context URL.
     * </p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param user the user
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    DavResourceLocator createHomeLocator(URL context,
                                                User user)
        throws CosmoDavException;

    /**
     * <p>
     * Returns a locator for the principal resource of the given user
     * relative to the given context URL.
     * </p>
     *
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param user the user
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    DavResourceLocator createPrincipalLocator(URL context,
                                                     User user)
        throws CosmoDavException;
}
