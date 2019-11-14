/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.acl.resource.DavGroupPrincipalCollection;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipalCollection;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.carddav.CarddavConstants;
import org.unitedinternet.cosmo.dav.impl.*;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static org.unitedinternet.cosmo.dav.ExtendedDavConstants.*;

/**
 * Standard implementation of <code>DavResourceFactory</code>.
 *
 * @see WebDavResource
 * @see Item
 */
@Service
@Transactional(readOnly = true)
public class StandardResourceFactory implements DavResourceFactory {

    private static final Log LOG = LogFactory.getLog(StandardResourceFactory.class);

    private ContentService contentService;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private EntityFactory entityFactory;
    private CalendarQueryProcessor calendarQueryProcessor;
    private ICalendarClientFilterManager clientFilterManager;
    private UserIdentitySupplier userIdentitySupplier;

    private Map<DavPropertyName, Class> collectionResourceTypes = new HashMap<>();

    private boolean schedulingEnabled = false;

    public StandardResourceFactory(ContentService contentService, UserService userService,
            CosmoSecurityManager securityManager, EntityFactory entityFactory,
            CalendarQueryProcessor calendarQueryProcessor, ICalendarClientFilterManager clientFilterManager,
            UserIdentitySupplier userIdentitySupplier,
            @Value("${cosmo.caldav.schedulingEnabled}") boolean schedulingEnabled) {

        this.contentService = contentService;
        this.userService = userService;
        this.securityManager = securityManager;
        this.entityFactory = entityFactory;
        this.calendarQueryProcessor = calendarQueryProcessor;
        this.clientFilterManager = clientFilterManager;
        this.userIdentitySupplier = userIdentitySupplier;
        this.schedulingEnabled = schedulingEnabled;

        /* Resource type AGAINST collection type */
        collectionResourceTypes.put(CaldavConstants.CALENDAR, DavCalendarCollection.class);
        collectionResourceTypes.put(CarddavConstants.ADDRESSBOOK, DavAddressbookCollection.class);
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exist and the request method indicates that one is to be created, returns a
     * resource backed by a newly-instantiated item that has not been persisted or assigned a UID. Otherwise, if the
     * resource does not exists, then a {@link NotFoundException} is thrown.
     * </p>
     * <p>
     * The type of resource to create is chosen as such:
     * <ul>
     * <li><code>MKCALENDAR</code>: {@link DavCalendarCollection}</li>
     * <li><code>MKCOL</code>: {@link DavCollectionBase}</li>
     * <li><code>PUT</code>, <code>COPY</code>, <code>MOVE</code></li>: {@link DavFile}</li>
     * </ul>
     */
    public WebDavResource resolve(DavResourceLocator locator, DavRequest request) throws CosmoDavException {
        WebDavResource resource = this.resolve(locator);
        if (resource != null) {
            return resource;
        }

        /*
         * We didn't find an item in storage for the resource, so either the request is creating a resource or the
         * request is targeting a nonexistent item.
         */
        if (request.getMethod().equals("MKCALENDAR")) {
            return new DavCalendarCollection(locator, this, entityFactory);
        }
        if (request.getMethod().equals("MKCOL")) {
            /* Read properties and decide what collection to create */
            DavPropertySet properties = request.getMkcolProperties();
            if (properties.contains(DavPropertyName.RESOURCETYPE)) {
                WebDavProperty resourceType = (WebDavProperty) properties.get(DavPropertyName.RESOURCETYPE);

                Element e = (Element)resourceType.getValue();
                ElementIterator i = DomUtil.getChildren(e);
                DavCollection collection = null;
                while (i.hasNext()){
                    Element child = i.next();
                    StandardDavProperty prop = StandardDavProperty.createFromXml(child);
                    if (collectionResourceTypes.containsKey(prop.getName())) {
                        LOG.debug("Found collection type: " + prop.getName().toString());
                        if (collection == null)
                        {
                            try {
                                collection = (DavCollection)collectionResourceTypes.get(prop.getName())
                                        .getConstructor(DavResourceLocator.class, DavResourceFactory.class, EntityFactory.class).
                                                newInstance(locator, this, entityFactory);

                            } catch (Exception ex) {
                                String message = "Unable to instantiate Collection class for resource type: " + prop.getName() + ": " + ex.toString();
                                LOG.error(message);
                                throw new BadRequestException(message);
                            }
                        } else {
                            throw new BadRequestException("Two incompatible resource types specified");
                        }
                    }
                }
                return collection;
            }

            return new DavCollectionBase(locator, this, entityFactory);
        }
        if (request.getMethod().equals("PUT")) {
            // Will be replaced by the provider if a different resource type is required
            WebDavResource parent = resolve(locator.getParentLocator());
            if (parent instanceof DavCalendarCollection) {
                return new DavEvent(locator, this, entityFactory);
            }
        }

        // Handle OPTIONS for non-existent resource
        if (request.getMethod().equals("OPTIONS")) {
            // Ensure parent exists first
            WebDavResource parent = resolve(locator.getParentLocator());
            if (parent != null && parent.exists()) {
                if (parent instanceof DavCalendarCollection) {
                    return new DavEvent(locator, this, entityFactory);
                } else {
                    return new DavCollectionBase(locator, this, entityFactory);
                }
            }
        }
        throw new NotFoundException();
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exists, returns <code>null</code>.
     * </p>
     */
    public WebDavResource resolve(DavResourceLocator locator) throws CosmoDavException {
        String uri = locator.getPath();
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolving URI " + uri);
        }

        UriTemplate.Match match = null;

        match = TEMPLATE_COLLECTION.match(uri);
        if (match != null) {
            return createUidResource(locator, match);
        }

        match = TEMPLATE_ITEM.match(uri);
        if (match != null) {
            return createUidResource(locator, match);
        }

        match = TEMPLATE_USERS.match(uri);
        if (match != null) {
            return new DavUserPrincipalCollection(locator, this);
        }

        match = TEMPLATE_GROUPS.match(uri);
        if (match != null) {
            return new DavGroupPrincipalCollection(locator, this);
        }

        match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }


        match = TEMPLATE_GROUP.match(uri);
        if (match != null) {
            return createGroupPrincipalResource(locator, match);
        }

        if (schedulingEnabled) {
            match = TEMPLATE_USER_INBOX.match(uri);
            if (match != null) {
                return new DavInboxCollection(locator, this);
            }

            match = TEMPLATE_USER_OUTBOX.match(uri);
            if (match != null) {
                return new DavOutboxCollection(locator, this);
            }
        }


        return createUnknownResource(locator, uri);
    }

    /**
     * <p>
     * Instantiates a <code>WebDavResource</code> representing the <code>Item</code> located by the given
     * <code>DavResourceLocator</code>.
     * </p>
     */
    public WebDavResource createResource(DavResourceLocator locator, Item item) throws CosmoDavException {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (item instanceof HomeCollectionItem) {
            return new DavHomeCollection((HomeCollectionItem) item, locator, this, entityFactory);
        }

        if (item instanceof CollectionItem) {
            if (item.getStamp(CalendarCollectionStamp.class) != null) {
                return new DavCalendarCollection((CollectionItem) item, locator, this, entityFactory);
            } else {
                return new DavCollectionBase((CollectionItem) item, locator, this, entityFactory);
            }
        }

        if (item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            // don't expose modifications
            if (note.getModifies() != null) {
                return null;
            } else if (item.getStamp(EventStamp.class) != null) {
                return new DavEvent(note, locator, this, entityFactory);
            } else {
                return new DavTask(note, locator, this, entityFactory);
            }
        }

        if (item instanceof FreeBusyItem) {
            return new DavFreeBusy((FreeBusyItem) item, locator, this, entityFactory);
        }
        if (item instanceof AvailabilityItem) {
            return new DavAvailability((AvailabilityItem) item, locator, this, entityFactory);
        }

        return new DavFile((FileItem) item, locator, this, entityFactory);
    }

    // our methods

    protected WebDavResource createUidResource(DavResourceLocator locator, UriTemplate.Match match)
            throws CosmoDavException {
        String uid = match.get("uid");
        String path = match.get("*");
        Item item = path != null ? contentService.findItemByPath(path, uid) : contentService.findItemByUid(uid);
        return item != null ? createResource(locator, item) : null;
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator, UriTemplate.Match match)
            throws CosmoDavException {
        User user = userService.getUser(match.get("username"));
        return createUserPrincipalResource(locator, user);
    }

    protected WebDavResource createGroupPrincipalResource(DavResourceLocator locator, UriTemplate.Match match)
            throws CosmoDavException {
        Group group = userService.getGroup(match.get("groupname"));
        return createUserPrincipalResource(locator, group);
    }

    @Override
    public WebDavResource createUserPrincipalResource(DavResourceLocator locator, UserBase user) throws CosmoDavException {
        return user != null ? new DavUserPrincipal(user, locator, this, userIdentitySupplier) : null;
    }





    protected WebDavResource createUnknownResource(DavResourceLocator locator, String uri) throws CosmoDavException {
        Item item = contentService.findItemByPath(uri);
        return item != null ? createResource(locator, item) : null;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }

    public UserService getUserService() {
        return userService;
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }




    public ICalendarClientFilterManager getClientFilterManager() {
        return clientFilterManager;
    }

    public boolean isSchedulingEnabled() {
        return schedulingEnabled;
    }
}
