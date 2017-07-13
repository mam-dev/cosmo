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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavAvailability;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.DavFreeBusy;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;
import org.unitedinternet.cosmo.dav.impl.DavJournal;
import org.unitedinternet.cosmo.dav.impl.DavTask;
import org.unitedinternet.cosmo.dav.impl.parallel.AvailabilityFile;
import org.unitedinternet.cosmo.dav.impl.parallel.EventFile;
import org.unitedinternet.cosmo.dav.impl.parallel.FreeBusyFile;
import org.unitedinternet.cosmo.dav.impl.parallel.JournalFile;
import org.unitedinternet.cosmo.dav.impl.parallel.TaskFile;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.parallel.CalDavContentResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.parallel.CalDavResponse;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.server.ServerConstants;

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
    @SuppressWarnings("unused")
    private static final Log LOG =
        LogFactory.getLog(CalendarResourceProvider.class);

    public CalendarResourceProvider(CalDavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }
    
    // DavProvider methods

    public void put(CalDavRequest request,
                    CalDavResponse response,
                    CalDavResource content)
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
                                             content.getCalDavResourceLocator(),
                                             ctx.getCalendar());
        }
        
        // case when event updates comes through a ticket, 
        // the ticket is attached to the item (event) and not ticket is associated to the parent item (calendar)
        if(request.getHeader(ServerConstants.HEADER_TICKET) != null && content instanceof EventFile ) {
            ((EventFile) content).updateContent(content, ctx);
        } else {
            content.getParent().addContent((CalDavContentResource)content, ctx);
        }
        response.setStatus(status);
        // since the iCalendar body is parsed and re-serialized for storage,
        // it's possible that what will be served for subsequent GETs is
        // slightly different than what was provided in the PUT, so send a
        // weak etag
        response.setHeader("ETag", "W/" +  content.getETag());
    }

    protected CalDavResource resolveDestination(CalDavResourceLocator locator,
                                             CalDavResource original)
        throws CosmoDavException {
        if (locator == null) {
            return null;
        }
        
        if (original instanceof TaskFile) {
            return new TaskFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof JournalFile) {
            return new JournalFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof FreeBusyFile) {
            return new FreeBusyFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (original instanceof AvailabilityFile) {
            return new AvailabilityFile(locator, getResourceFactory(), getEntityFactory());
        }
        return new EventFile(locator, getResourceFactory(), getEntityFactory());
    }

    protected CalDavResource createCalendarResource(CalDavRequest request,
                                                CalDavResponse response,
                                                CalDavResourceLocator locator,
                                                Calendar calendar)
        throws CosmoDavException {
        if (!calendar.getComponents(Component.VEVENT).isEmpty()) {
            return new EventFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VTODO).isEmpty()) {
            return new TaskFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
            return new JournalFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(Component.VFREEBUSY).isEmpty()) {
            return new FreeBusyFile(locator, getResourceFactory(), getEntityFactory());
        }
        if (!calendar.getComponents(ICalendarConstants.COMPONENT_VAVAILABLITY).isEmpty()) {
            return new AvailabilityFile(locator, getResourceFactory(), getEntityFactory());
        }
        throw new SupportedCalendarComponentException();
  }
}
