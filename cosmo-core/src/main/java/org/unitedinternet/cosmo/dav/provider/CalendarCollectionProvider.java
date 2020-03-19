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

import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.ICALENDAR_MEDIA_TYPE;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.ExistsException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.unitedinternet.cosmo.dav.caldav.MissingParentException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavItemCollection;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.util.FreeBusyUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements access to <code>DavCalendarCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavCalendarCollection
 */
public class CalendarCollectionProvider extends CollectionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarCollectionProvider.class);

    private static final String CHARSET_UTF8 = "UTF-8";

    private static final String PRODUCT_ID_KEY = "calendar.server.productId";

    private volatile String productId;

    public CalendarCollectionProvider(DavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods
    @Override
    public void mkcalendar(DavRequest request, DavResponse response, DavCollection collection)
            throws CosmoDavException, IOException {
        if (collection.exists()) {
            throw new ExistsException();
        }

        DavItemCollection parent = (DavItemCollection) collection.getParent();
        if (!parent.exists()) {
            throw new MissingParentException("One or more intermediate collections must be created");
        }
        if (parent.isCalendarCollection()) {
            throw new InvalidCalendarLocationException(
                    "A calendar collection may not be created within a calendar collection");
        }
        // XXX DAV:needs-privilege DAV:bind on parent collection

        if (LOG.isDebugEnabled()) {
            LOG.debug("MKCALENDAR at {}", collection.getResourcePath());
        }
        DavPropertySet properties = request.getMkCalendarSetProperties();
        MultiStatusResponse msr = collection.getParent().addCollection(collection, properties);

        if (properties.isEmpty() || !hasNonOK(msr)) {
            response.setStatus(201);
            response.setHeader("Cache-control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            return;
        }

        MultiStatus ms = new MultiStatus();
        ms.addResponse(msr);
        response.sendMultiStatus(ms);

    }

    @Override
    protected void spool(DavRequest request, DavResponse response, WebDavResource resource, boolean withEntity)
            throws CosmoDavException, IOException {
        Enumeration<String> acceptHeaders = request.getHeaders("Accept");

        if (acceptHeaders != null) {
            while (acceptHeaders.hasMoreElements()) {
                String headerValue = acceptHeaders.nextElement();
                if (ICALENDAR_MEDIA_TYPE.equalsIgnoreCase(headerValue)) {
                    writeContentOnResponse(request, response, resource);
                    return;
                }
            }
        }
        super.spool(request, response, resource, withEntity);
    }

    private void writeContentOnResponse(DavRequest request, DavResponse response, WebDavResource resource)
            throws IOException {
        // strip the content if there's a ticket with free-busy access
        if (!(resource instanceof DavCalendarCollection)) {
            throw new IllegalStateException("Incompatible resource type for this provider");
        }
        DavCalendarCollection davCollection = DavCalendarCollection.class.cast(resource);
        CollectionItem collectionItem = (CollectionItem) davCollection.getItem();

        Calendar result = getCalendarFromCollection(request, collectionItem);

        Ticket contextTicket = getSecurityContext().getTicket();
        Set<Ticket> collectionTickets = collectionItem.getTickets();
        if (contextTicket != null && collectionTickets != null) {
            if (collectionTickets.contains(contextTicket) && contextTicket.isFreeBusy()) {
                result = FreeBusyUtil.getFreeBusyCalendar(result, this.productId);
            }
        }
        response.setHeader("ETag", "\""+ resource.getETag() +"\"");
        response.setContentType(ICALENDAR_MEDIA_TYPE);
        response.setCharacterEncoding(CHARSET_UTF8);
        response.getWriter().write(result.toString());
        response.flushBuffer();
    }

    

    /**
     * @param collectionItem
     * @return
     */
    private Calendar getCalendarFromCollection(DavRequest req, CollectionItem collectionItem) {
        Calendar result = new Calendar();

        if (productId == null) {
            synchronized (this) {
                if (productId == null) {
                    Environment environment = WebApplicationContextUtils
                            .findWebApplicationContext(req.getServletContext()).getEnvironment();
                    productId = environment.getProperty(PRODUCT_ID_KEY);
                }
            }
        }

        result.getProperties().add(new ProdId(productId));
        result.getProperties().add(Version.VERSION_2_0);
        result.getProperties().add(CalScale.GREGORIAN);

        for (Item item : collectionItem.getChildren()) {
            if (!NoteItem.class.isInstance(item)) {
                continue;
            }
            for (Stamp s : item.getStamps()) {
                if (BaseEventStamp.class.isInstance(s)) {
                    BaseEventStamp baseEventStamp = BaseEventStamp.class.cast(s);
                    result.getComponents().add(baseEventStamp.getEvent());
                }
            }
        }
        return result;
    }
}