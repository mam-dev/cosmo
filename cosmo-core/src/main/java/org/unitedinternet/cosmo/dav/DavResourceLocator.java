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

import java.net.URL;

/**
 * <p>
 * The interface for classes that encapsulate information about the location
 * of a {@link WebDavResource}.
 * </p>
 * <p>
 * The URL of a resource is defined as
 * <code>{prefix}{base path}{dav path}</code>.
 * The prefix is the leading portion of the URL that includes the
 * scheme and authority sections of the URL. The base path is the leading
 * portion of the URL path that locates the root dav namespace. The dav path
 * is the trailing portion of the URL path that locates the resource within
 * the dav namespace.
 * </p>
 */
public interface DavResourceLocator {

    /**
     * @return Returns the absolute escaped URL of the resource that can be used
     * as the value for a <code>DAV:href</code> property. Appends a trailing
     * slash if <code>isCollection</code>.
     */
    String getHref(boolean isCollection);

    /**
     * Returns the escaped URL of the resource that can be used as the value
     * for a <code>DAV:href</code> property. The href is either absolute or
     * absolute-path relative (i.e. including the base path but not the
     * prefix). Appends a trailing slash if <code>isCollection</code>.
     */
    String getHref(boolean absolute,
                          boolean isCollection);

    /**
     * Returns a URL representing the href as per {@link getHref(boolean)}.
     */
    URL getUrl(boolean isCollection);

    /**
     * Returns a URL representing the href as per
     * {@link getHref(boolean, boolean)}.
     */
    URL getUrl(boolean absolute,
                      boolean isCollection);

    /**
     * Returns the scheme and authority portion of the URL.
     */
    String getPrefix();

    /**
     * Returns the leading portion of the path (unescaped) that identifies the
     * root of the dav namespace.
     */
    String getBasePath();

    /**
     * Returns the absolute escaped URL corresponding to the base path that
     * can be used as the value for a <code>DAV:href</code> property.
     */
    String getBaseHref();

    /**
     * Returns the escaped URL corresponding to the base path that can be
     * used as the value for a <code>DAV:href</code> property. The href is
     * either absolute or absolute-path relative (i.e. including the base
     * path but not the prefix).
     */
    String getBaseHref(boolean absolute);

    /**
     * Returns the trailing portion of the path that identifies the resource
     * within the dav namespace.
     */
    String getPath();

    /**
     * Returns a URL that provides context for resolving other locators.
     * The context URL has the scheme and authority defined by this
     * locator's prefix, and its path is the same as this locator's base
     * path.
     */
    URL getContext();

    /**
     * Returns a locator identifying the parent resource.
     */
    DavResourceLocator getParentLocator();

    /**
     * Returns the factory that instantiated this locator.
     */
    DavResourceLocatorFactory getFactory();
}
