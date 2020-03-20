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

import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavAvailability;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.DavFreeBusy;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;
import org.unitedinternet.cosmo.dav.impl.DavJournal;
import org.unitedinternet.cosmo.dav.impl.DavTask;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.server.ServerConstants;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavCalendarResource</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavFile
 */
public class CalendarResourceProvider extends FileProvider {

    public CalendarResourceProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }
    
    // DavProvider methods

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws CosmoDavException, IOException {

        // do content.getParent() check only for normal auth only, ticket auth is on the item only, not its parent.
        if (request.getHeader(ServerConstants.HEADER_TICKET) == null && 
            ! content.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }

        int status = content.exists() ? 204 : 201;
        DavInputContext ctx = (DavInputContext) createInputContext(request);
        if (! content.exists()) {
            content = createCalendarResource(request, response,
                                             content.getResourceLocator(),
                                             ctx.getCalendar());
        }
        
        // case when event updates comes through a ticket, 
        // the ticket is attached to the item (event) and not ticket is associated to the parent item (calendar)
        if(request.getHeader(ServerConstants.HEADER_TICKET) != null && content instanceof DavEvent ) {
            ((DavEvent) content).updateContent(content, ctx);
        } else {
            content.getParent().addContent(content, ctx);
        }
        response.setStatus(status);
        // since the iCalendar body is parsed and re-serialized for storage,
        // it's possible that what will be served for subsequent GETs is
        // slightly different than what was provided in the PUT, so send a
        // weak etag
        response.setHeader("ETag", "W/" + ((DavItemResourceBase) content).getETag());
    }

    protected WebDavResource resolveDestination(DavResourceLocator locator,
                                             WebDavResource original)
        throws CosmoDavException {
        if (locator == null) {
            return null;
        }
        if (original instanceof DavTask) {
            return new DavTask(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof DavJournal) {
            return new DavJournal(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof DavFreeBusy) {
            return new DavFreeBusy(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof DavAvailability) {
            return new DavAvailability(locator, getResourceFactory(), getEntityFactory());
        }
        return new DavEvent(locator, getResourceFactory(), getEntityFactory());
    }

    protected DavContent createCalendarResource(DavRequest request,
                                                DavResponse response,
                                                DavResourceLocator locator,
                                                Calendar calendar)
        throws CosmoDavException {
        if (!calendar.getComponents(Component.VEVENT).isEmpty()) {
            return new DavEvent(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VTODO).isEmpty()) {
            return new DavTask(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
            return new DavJournal(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VFREEBUSY).isEmpty()) {
            return new DavFreeBusy(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(ICalendarConstants.COMPONENT_VAVAILABLITY)
                .isEmpty()) {
            return new DavAvailability(locator, getResourceFactory(), getEntityFactory());
        }
        throw new SupportedCalendarComponentException();
  }
}
