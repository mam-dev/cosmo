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

import static net.fortuna.ical4j.model.Property.DTEND;
import static net.fortuna.ical4j.model.Property.DTSTART;
import static net.fortuna.ical4j.model.Property.EXDATE;
import static net.fortuna.ical4j.model.Property.RECURRENCE_ID;
import static net.fortuna.ical4j.model.Property.RRULE;
import static net.fortuna.ical4j.model.Property.UID;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements access to <code>DavCalendarCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavCalendarCollection
 */
public class CalendarCollectionProvider extends CollectionProvider {

    private static final Log LOG = LogFactory.getLog(CalendarCollectionProvider.class);

    private static final String FREE_BUSY_TEXT = "FreeBusy";

    private static final String FREE_BUSY_X_PROPERTY = Property.EXPERIMENTAL_PREFIX + "FREE-BUSY";

    private static final List<String> FREE_BUSY_PROPERTIES = Arrays
            .asList(new String[] { UID, DTSTART, DTEND, RRULE, RECURRENCE_ID, EXDATE });

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
            LOG.debug("MKCALENDAR at " + collection.getResourcePath());
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
                if ("text/calendar".equalsIgnoreCase(headerValue)) {
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
                result = getFreeBusyCalendar(result);
            }
        }

        response.setContentType("text/calendar");
        response.getWriter().write(result.toString());
        response.flushBuffer();
    }

    /**
     * @param original
     * @return
     */
    @SuppressWarnings("unchecked")
    private Calendar getFreeBusyCalendar(Calendar original) {
        // Make a copy of the original calendar
        Calendar copy = new Calendar();
        copy.getProperties().add(new ProdId(productId));
        copy.getProperties().add(Version.VERSION_2_0);
        copy.getProperties().add(CalScale.GREGORIAN);
        copy.getProperties().add(new XProperty(FREE_BUSY_X_PROPERTY, Boolean.TRUE.toString()));
        
        List<Component> events = original.getComponents(Component.VEVENT);
        for (Component event : events) {
            copy.getComponents().add(this.getFreeBusyEvent((VEvent) event));
        }
        return copy;
    }

    private VEvent getFreeBusyEvent(VEvent vEvent) {

        try {
            VEvent freeBusyEvent = new VEvent();
            freeBusyEvent.getProperties().add(new Summary(FREE_BUSY_TEXT));
            for (String propertyName : FREE_BUSY_PROPERTIES) {
                Property property = vEvent.getProperty(propertyName);
                if (property != null) {
                    freeBusyEvent.getProperties().add(property);
                }
            }
            return freeBusyEvent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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