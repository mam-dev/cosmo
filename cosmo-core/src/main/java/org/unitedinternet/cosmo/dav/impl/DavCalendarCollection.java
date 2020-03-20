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
package org.unitedinternet.cosmo.dav.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.external.UuidExternalGenerator;
import org.unitedinternet.cosmo.dao.subscription.UuidSubscriptionGenerator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.MaxResourceSizeException;
import org.unitedinternet.cosmo.dav.caldav.TimeZoneExtractor;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.XCaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarColor;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarDescription;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarTimezone;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarVisibility;
import org.unitedinternet.cosmo.dav.caldav.property.GetCTag;
import org.unitedinternet.cosmo.dav.caldav.property.MaxResourceSize;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarData;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCollationSet;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscriptionItem;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo <code>CalendarCollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>CALDAV:calendar-description</code></li>
 * <li><code>CALDAV:calendar-timezone</code></li>
 * <li><code>CALDAV:calendar-supported-calendar-component-set</code> (protected)</li>
 * <li><code>CALDAV:supported-calendar-data</code> (protected)</li>
 * <li><code>CALDAV:max-resource-size</code> (protected)</li>
 * <li><code>CS:getctag</code> (protected)</li>
 * <li><code>XC:calendar-color</code></li>
 * <li><code>XC:calendar-visible</code></li>
 * </ul>
 *
 * @see DavCollection
 * @see CalendarCollectionItem
 */
public class DavCalendarCollection extends DavCollectionBase implements CaldavConstants, ICalendarConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavCalendarCollection.class);
    
    private static final Set<String> DEAD_PROPERTY_FILTER = new HashSet<String>();

    static {
        registerLiveProperty(CALENDARDESCRIPTION);
        registerLiveProperty(CALENDARTIMEZONE);
        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);
        registerLiveProperty(MAXRESOURCESIZE);
        registerLiveProperty(GET_CTAG);
        registerLiveProperty(XCaldavConstants.CALENDAR_COLOR);
        registerLiveProperty(XCaldavConstants.CALENDAR_VISIBLE);

        DEAD_PROPERTY_FILTER.add(CalendarCollectionStamp.class.getName());
    }

    /** */
    public DavCalendarCollection(CollectionItem collection, DavResourceLocator locator, DavResourceFactory factory,
            EntityFactory entityFactory) throws CosmoDavException {
        super(collection, locator, factory, entityFactory);
    }

    /** */
    public DavCalendarCollection(DavResourceLocator locator, DavResourceFactory factory, EntityFactory entityFactory)
            throws CosmoDavException {
        this(entityFactory.createCollection(), locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createCalendarCollectionStamp((CollectionItem) getItem()));
    }

    // Jackrabbit WebDavResource

    /** */
    public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
    }

    /** */
    public void move(org.apache.jackrabbit.webdav.DavResource destination)
            throws org.apache.jackrabbit.webdav.DavException {
        validateDestination(destination);
        super.move(destination);
    }

    /** */
    public void copy(org.apache.jackrabbit.webdav.DavResource destination, boolean shallow)
            throws org.apache.jackrabbit.webdav.DavException {
        validateDestination(destination);
        super.copy(destination, shallow);
    }

    // DavCollection

    public boolean isCalendarCollection() {
        return true;
    }

    // our methods

    /**
     * Returns the member resources in this calendar collection matching the given filter.
     */
    public Set<DavCalendarResource> findMembers(CalendarFilter filter) throws CosmoDavException {
        Set<DavCalendarResource> members = new HashSet<DavCalendarResource>();

        CollectionItem collection = (CollectionItem) getItem();
        for (ContentItem memberItem : getCalendarQueryProcesor().filterQuery(collection, filter)) {
            WebDavResource resource = memberToResource(memberItem);
            if (resource != null) {
                members.add((DavCalendarResource) resource);
            }
        }

        return members;
    }

    /**
     * Returns the member collection resources in this calendar collection.
     * 
     */
    public Set<CollectionItem> findCollectionMembers(DavCollectionBase collectionBase) throws CosmoDavException {
        CollectionItem collection = (CollectionItem) collectionBase.getItem();
        return getContentService().findCollectionItems(collection);
    }

    /**
     * Returns a VFREEBUSY component containing the freebusy periods for the calendar collection for the specified time
     * range.
     * 
     * @param period
     *            time range for freebusy information
     * @return VFREEBUSY component containing FREEBUSY periods for specified timerange
     */
    public VFreeBusy generateFreeBusy(Period period) {

        VFreeBusy vfb = this.getCalendarQueryProcesor().freeBusyQuery((CollectionItem) getItem(), period);

        return vfb;
    }

    /**
     * @return The default timezone for this calendar collection, if one has been set.
     */
    public VTimeZone getTimeZone() {
        Calendar obj = getCalendarCollectionStamp().getTimezoneCalendar();
        if (obj == null) {
            return null;
        }
        return (VTimeZone) obj.getComponents().getComponent(Component.VTIMEZONE);
    }

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = super.getResourceTypes();
        rt.add(RESOURCE_TYPE_CALENDAR);
        return rt;
    }

    public CalendarCollectionStamp getCalendarCollectionStamp() {
        return StampUtils.getCalendarCollectionStamp(getItem());
    }

    /** */
    protected void populateItem(InputContext inputContext) throws CosmoDavException {
        super.populateItem(inputContext);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();

        try {
            cc.setDescription(getItem().getName());
            // XXX: language should come from the input context
        } catch (DataSizeException e) {
            throw new MaxResourceSizeException(e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        if (cc.getDescription() != null) {
            properties.add(new CalendarDescription(cc.getDescription(), cc.getLanguage()));
        }
        if (cc.getTimezoneCalendar() != null) {
            properties.add(new CalendarTimezone(cc.getTimezoneCalendar().toString()));
        }

        // add CS:getctag property, which is the collection's entitytag
        // if it exists
        Item item = getItem();
        if (item != null && item.getEntityTag() != null) {
            properties.add(new GetCTag(item.getEntityTag()));
        }

        properties.add(new SupportedCalendarComponentSet());
        properties.add(new SupportedCollationSet());
        properties.add(new SupportedCalendarData());
        properties.add(new MaxResourceSize());

        if (cc.getVisibility() != null) {
            properties.add(new CalendarVisibility(cc.getVisibility()));
        }

        if (cc.getColor() != null) {
            properties.add(new CalendarColor(cc.getColor()));
        }

        if (cc.getDisplayName() != null) {
            properties.add(new DisplayName(cc.getDisplayName()));
        }
    }

    /**
     * The CALDAV:supported-calendar-component-set property is used to specify restrictions on the calendar component
     * types that calendar object resources may contain in a calendar collection. Any attempt by the client to store
     * calendar object resources with component types not listed in this property, if it exists, MUST result in an
     * error, with the CALDAV:supported-calendar-component precondition (Section 5.3.2.1) being violated. Since this
     * property is protected, it cannot be changed by clients using a PROPPATCH request.
     */
    protected void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
        super.setLiveProperty(property, create);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name + " requires a value");
        }

        if (!(create && name.equals(SUPPORTEDCALENDARCOMPONENTSET)) && (name.equals(SUPPORTEDCALENDARCOMPONENTSET)
                || name.equals(SUPPORTEDCALENDARDATA) || name.equals(MAXRESOURCESIZE) || name.equals(GET_CTAG))) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(property.getValueText());
            cc.setLanguage(property.getLanguage());
            return;
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezoneCalendar(TimeZoneExtractor.extract(property));
        }
        if (name.equals(XCaldavConstants.CALENDAR_COLOR)) {
            cc.setColor(property.getValueText());
        }
        if (name.equals(XCaldavConstants.CALENDAR_VISIBLE)) {
            cc.setVisibility(Boolean.parseBoolean(property.getValueText()));
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
        super.removeLiveProperty(name);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) || name.equals(SUPPORTEDCALENDARDATA)
                || name.equals(MAXRESOURCESIZE) || name.equals(GET_CTAG)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(CALENDARDESCRIPTION)) {
            cc.setDescription(null);
            cc.setLanguage(null);
            return;
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezoneCalendar(null);
            return;
        }
        if (name.equals(XCaldavConstants.CALENDAR_COLOR)) {
            cc.setColor(null);
        }
        if (name.equals(XCaldavConstants.CALENDAR_VISIBLE)) {
            cc.setVisibility(null);
        }
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        Set<String> copy = new HashSet<String>();
        copy.addAll(super.getDeadPropertyFilter());
        copy.addAll(DEAD_PROPERTY_FILTER);
        return copy;
    }

    /** */
    protected void saveContent(DavItemContent member) throws CosmoDavException {
        if (!(member instanceof DavCalendarResource)) {
            throw new IllegalArgumentException("member not DavCalendarResource");
        }

        if (member instanceof DavEvent) {
            saveEvent(member);
        } else {
            try {
                super.saveContent(member);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            }
        }
    }

    private void saveEvent(DavItemContent member) throws CosmoDavException {

        ContentItem content = (ContentItem) member.getItem();
        EventStamp event = StampUtils.getEventStamp(content);
        EntityConverter converter = new EntityConverter(getEntityFactory());
        Set<ContentItem> toUpdate = new LinkedHashSet<ContentItem>();

        try {
            // convert icalendar representation to cosmo data model
            toUpdate.addAll(converter.convertEventCalendar((NoteItem) content, event.getEventCalendar()));
        } catch (ModelValidationException e) {
            throw new InvalidCalendarResourceException(e.getMessage());
        }

        if (event.getCreationDate() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("updating event " + member.getResourcePath());
            }

            try {
                getContentService().updateContentItems(content.getParents().iterator().next(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating event " + member.getResourcePath());
            }

            try {
                getContentService().createContentItems((CollectionItem) getItem(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        }

        member.setItem(content);
    }

    /** */
    protected void removeContent(DavItemContent member) throws CosmoDavException {
        if (!(member instanceof DavCalendarResource)) {
            throw new IllegalArgumentException("member not DavCalendarResource");
        }

        ContentItem content = (ContentItem) member.getItem();
        CollectionItem parent = (CollectionItem) getItem();

        // XXX: what exceptions need to be caught?
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing event " + member.getResourcePath());
        }

        try {
            if (content instanceof NoteItem) {
                getContentService().removeItemFromCollection(content, parent);
            } else {
                getContentService().removeContent(content);
            }
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    private void validateDestination(org.apache.jackrabbit.webdav.DavResource destination) throws CosmoDavException {
        if (destination instanceof WebDavResource
                && ((WebDavResource) destination).getParent() instanceof DavCalendarCollection) {
            throw new InvalidCalendarLocationException(
                    "Parent collection of destination must not be a calendar collection");
        }
    }

    @Override
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        if (!isGeneratedUid(getItem())) {
            return super.getCurrentPrincipalPrivileges();
        }
        Set<DavPrivilege> privileges = new HashSet<>();
        privileges.add(DavPrivilege.READ);
        Item item = this.getItem();
        if (item instanceof HibCollectionSubscriptionItem) {
            HibCollectionSubscriptionItem subscriptionItem = (HibCollectionSubscriptionItem) item;
            CollectionSubscription subscription = subscriptionItem.getSubscription();
            if (subscription != null) {
                Ticket ticket = subscription.getTicket();
                if (ticket != null && ticket.isReadWrite()) {
                    privileges.add(DavPrivilege.WRITE);
                }
            }
        }

        return privileges;
    }

    @Override
    protected DavAcl getAcl() {
        if (!isGeneratedUid(getItem())) {
            return super.getAcl();
        }
        DavAcl result = new DavAcl();
        DavAce owner = new DavAce.PropertyAce(OWNER);
        owner.getPrivileges().addAll(this.getCurrentPrincipalPrivileges());
        owner.setProtected(true);
        result.getAces().add(owner);

        return result;
    }

    private static boolean isGeneratedUid(Item item) {
        if (!(item instanceof CollectionItem)) {
            return false;
        }
        String uid = item.getUid();
        return UuidExternalGenerator.get().containsUuid(uid) || UuidSubscriptionGenerator.get().containsUuid(uid);
    }
}
