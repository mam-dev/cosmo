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

import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

/**
 * Interface for components that create dav resources to represent
 * persisted items.
 *
 * @see WebDavResource
 * @see Item
 */
public interface DavResourceFactory {

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exist and the request method
     * indicates that one is to be created, returns a resource backed by a 
     * newly-instantiated item that has not been persisted or assigned a UID.
     * Otherwise, if the resource does not exists, then a
     * {@link NotFoundException} is thrown.
     * </p>
     */
    WebDavResource resolve(DavResourceLocator locator,
                               DavRequest request)
        throws CosmoDavException;

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exists, returns <code>null</code>.
     * </p>
     */
    WebDavResource resolve(DavResourceLocator locator)
        throws CosmoDavException;

    /**
     * <p>
     * Instantiates a <code>WebDavResource</code> representing the
     * <code>Item</code> located by the given <code>DavResourceLocator</code>.
     * </p>
     */
    WebDavResource createResource(DavResourceLocator locator,
                                      Item item)
        throws CosmoDavException;

    ContentService getContentService();
    
    ICalendarClientFilterManager getClientFilterManager();

    CalendarQueryProcessor getCalendarQueryProcessor();
    
    UserService getUserService();

    CosmoSecurityManager getSecurityManager();
}
