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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.abdera.i18n.text.UrlEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.ExistsException;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.property.Owner;
import org.unitedinternet.cosmo.dav.acl.property.PrincipalCollectionSet;
import org.unitedinternet.cosmo.dav.property.CreationDate;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.property.Uuid;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.util.PathUtil;
import org.w3c.dom.Element;

/**
 * <p>
 * Base class for dav resources that are backed by collections or items.
 * </p>
 * <p>
 * This class defines the following live properties:
 * </p>
 * <ul>
 * <li><code>DAV:getcreationdate</code> (protected)</li>
 * <li><code>DAV:displayname</code> (protected)</li>
 * <li><code>DAV:iscollection</code> (protected)</li>
 * <li><code>DAV:resourcetype</code> (protected)</li>
 * <li><code>DAV:owner</code> (protected)</li>
 * <li><code>DAV:principal-collection-set</code> (protected)</li>
 * <li><code>ticket:ticketdiscovery</code> (protected)</li>
 * <li><code>cosmo:uuid</code> (protected)</li>
 * </ul>
 * <p>
 * This class does not define any resource types.
 * </p>
 * @see Item
 */
public abstract class DavItemResourceBase extends DavResourceBase implements DavItemResource, TicketConstants {
   
    private static final Logger LOG = LoggerFactory.getLogger(DavItemResourceBase.class);
    private static final String DISPLAY_NAME_DEFAULT = "";

    private Item item;
    private DavCollection parent;
    private DavAcl acl;
    private EntityFactory entityFactory;

    static {
        registerLiveProperty(DavPropertyName.CREATIONDATE);
        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(OWNER);
        registerLiveProperty(PRINCIPALCOLLECTIONSET);
        registerLiveProperty(UUID);
    }

    public DavItemResourceBase(Item item, DavResourceLocator locator,
            DavResourceFactory factory, EntityFactory entityFactory)
            throws CosmoDavException {
        super(locator, factory);
        this.item = item;
        this.entityFactory = entityFactory;
        this.acl = makeAcl();
    }

    // WebDavResource methods

    public boolean exists() {
        return item != null && item.getUid() != null;
    }

    public String getDisplayName() {
        return item.getDisplayName();
    }

    public String getETag() {
        if (getItem() == null)
            return null;
        // an item that is about to be created does not yet have an etag
        if (StringUtils.isBlank(getItem().getEntityTag()))
            return null;
        return "\"" + getItem().getEntityTag() + "\"";
    }

    public long getModificationTime() {
        if (getItem() == null)
            return -1;
        if (getItem().getModifiedDate() == null)
            return new Date().getTime();
        return getItem().getModifiedDate().getTime();
    }

    public void setProperty(
            org.apache.jackrabbit.webdav.property.DavProperty<?> property)
            throws org.apache.jackrabbit.webdav.DavException {
        super.setProperty(property);
        updateItem();
    }

    public void removeProperty(DavPropertyName propertyName)
            throws org.apache.jackrabbit.webdav.DavException {
        super.removeProperty(propertyName);

        updateItem();
    }

    public WebDavResource getCollection() {
        try {
            return getParent();
        } catch (CosmoDavException e) {
            throw new RuntimeException(e);
        }
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination)
            throws org.apache.jackrabbit.webdav.DavException {
        if (!exists()) {
            throw new NotFoundException();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving resource {} to {}", getResourcePath(), destination.getResourcePath());
        }

        if (destination.exists()) {
            throw new ExistsException();
        }
        
        if(!(destination instanceof DavItemResourceBase)){
            throw new IllegalArgumentException("Required type for 'destination' is:[" + DavItemResourceBase.class.getName() + "]");
        }
        DavItemResourceBase destinationItemResource = (DavItemResourceBase) destination;

        try {

            DavItemResourceBase parentItemResource = (DavItemResourceBase) destinationItemResource
                    .getParent();
            CollectionItem newParent = (CollectionItem) parentItemResource
                    .getItem();
            if (!parentItemResource.exists() || newParent == null) {
                throw new ConflictException(
                        "One or more intermediate collections must be created");
            }
            CollectionItem oldParent = (CollectionItem) ((DavItemResourceBase) getParent())
                    .getItem();

            // update name
            getItem().setName(
                    PathUtil.getBasename(destination.getResourcePath()));

            // only move if parents are different
            if (!newParent.equals(oldParent)) {
                getContentService().moveItem(getItem(), oldParent, newParent);
            } else {
                // otherwise update name
                updateItem();
            }

        } catch (ItemNotFoundException e) {
            throw new ConflictException(
                    "One or more intermediate collections must be created");
        } catch (DuplicateItemNameException e) {
            throw new ExistsException();
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination,
            boolean shallow) throws org.apache.jackrabbit.webdav.DavException {
        if (!exists()) {
            throw new NotFoundException();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying resource {} to {}", getResourcePath(), destination.getResourcePath());
        }

        try {
            getContentService().copyItem(
                    item,
                    (CollectionItem) ((DavItemResourceBase) destination
                            .getCollection()).getItem(),
                    destination.getResourcePath(), !shallow);
        } catch (ItemNotFoundException e) {
            throw new ConflictException(
                    "One or more intermediate collections must be created");
        } catch (DuplicateItemNameException e) {
            throw new ExistsException();
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    // WebDavResource methods

    public DavCollection getParent() throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator = getResourceLocator()
                    .getParentLocator();
            try {
                parent = (DavCollection) getResourceFactory().resolve(
                        parentLocator);
            } catch (ClassCastException e) {
                throw new ForbiddenException("Resource "
                        + parentLocator.getPath() + " is not a collection");
            }
            if (parent == null)
                parent = new DavCollectionBase(parentLocator,
                        getResourceFactory(), entityFactory);
        }

        return parent;
    }

    public MultiStatusResponse updateProperties(DavPropertySet setProperties,
            DavPropertyNameSet removePropertyNames) throws CosmoDavException {
        MultiStatusResponse msr = super.updateProperties(setProperties,
                removePropertyNames);
        if (hasNonOK(msr)) {
            return msr;
        }

        updateItem();

        return msr;
    }

    // DavItemResource methods

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) throws CosmoDavException {
        this.item = item;
        loadProperties();
    }

    public void saveTicket(Ticket ticket) throws CosmoDavException {
        if (LOG.isDebugEnabled())
            LOG.debug("adding ticket for " + item.getName());

        // automatically add freebusy privilege along with read
        if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ))
            ticket.getPrivileges().add(Ticket.PRIVILEGE_FREEBUSY);

        getContentService().createTicket(item, ticket);
    }

    public void removeTicket(Ticket ticket) throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing ticket " + ticket.getKey() + " on "
                    + item.getName());
        }

        getContentService().removeTicket(item, ticket);
    }

    public Ticket getTicket(String id) {
        for (Iterator<Ticket> i = item.getTickets().iterator(); i.hasNext();) {
            Ticket t = (Ticket) i.next();
            if (t.getKey().equals(id))
                return t;
        }
        return null;
    }

    public Set<Ticket> getTickets() {
        return getSecurityManager().getSecurityContext().findVisibleTickets(
                item);
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    // our methods

    protected ContentService getContentService() {
        return getResourceFactory().getContentService();
    }

    protected CalendarQueryProcessor getCalendarQueryProcesor() {
        return getResourceFactory().getCalendarQueryProcessor();
    }

    protected ICalendarClientFilterManager getClientFilterManager() {
        return getResourceFactory().getClientFilterManager();
    }

    /**
     * Sets the properties of the item backing this resource from the given
     * input context.
     */
    protected void populateItem(InputContext inputContext)
            throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populating item for " + getResourcePath());
        }

        if (item.getUid() == null) {
            try {
                item.setName(UrlEncoding.decode(PathUtil.getBasename(getResourcePath()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CosmoDavException(e);
            }
            if (item.getDisplayName() == null){
                if (item instanceof ContentItem) {
                    item.setDisplayName(DISPLAY_NAME_DEFAULT);
                } else {
                    item.setDisplayName(item.getName());
                }
            }
        }

        /*
         * If we don't know specifically who the user is, then the owner of the resource becomes the person who issued
         * the ticket
         */

        // Only initialize owner once
        if (item.getOwner() == null) {
            User owner = getSecurityManager().getSecurityContext().getUser();
            if (owner == null) {
                Ticket ticket = getSecurityManager().getSecurityContext()
                        .getTicket();
                owner = ticket.getOwner();
            }
            item.setOwner(owner);
        }

        if (item.getUid() == null) {
            item.setClientCreationDate(Calendar.getInstance().getTime());
            item.setClientModifiedDate(item.getClientCreationDate());
        }
    }

    /**
     * Sets the attributes the item backing this resource from the given
     * property set.
     */
    protected MultiStatusResponse populateAttributes(DavPropertySet properties) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Populating attributes for {}", getResourcePath());
        }

        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);
        if (properties == null) {
            return msr;
        }

        org.apache.jackrabbit.webdav.property.DavProperty<?> property = null;
        List<DavPropertyName> df = new ArrayList<DavPropertyName>();
        CosmoDavException error = null;
        DavPropertyName failed = null;
        for (DavPropertyIterator i = properties.iterator(); i.hasNext();) {
            try {
                property = i.nextProperty();
                setResourceProperty((WebDavProperty) property, true);
                df.add(property.getName());
                msr.add(property.getName(), 200);
            } catch (CosmoDavException e) {
                // we can only report one error message in the
                // responsedescription, so even if multiple properties would
                // fail, we return 424 for the second and subsequent failures
                // as well
                if (error == null) {
                    error = e;
                    failed = property.getName();
                } else {
                    df.add(property.getName());
                }
            }
        }

        if (error == null) {
            return msr;
        }

        /*
         * Replace the other response with a new one, since we have to change the response code for each of the
         * properties that would have been set successfully
         */
        msr = new MultiStatusResponse(getHref(), error.getMessage());
        for (DavPropertyName n : df)
            msr.add(n, 424);
        msr.add(failed, error.getErrorCode());

        return msr;
    }

    /**
     * Returns the resource's access control list. The list contains the
     * following ACEs:
     * 
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code></li>
     * <li> <code>DAV:owner</code>: allow <code>DAV:all</code></li>
     * <li>owner of each parent collection: allow <code>DAV:all</code></li>
     * <li> <code>DAV:all</code>: allow
     * <code>DAV:read-current-user-privilege-set</code></li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code></li>
     * </ol>
     * 
     * <p>
     * TODO: Include administrative users in the ACL, probably with a group
     * principal.<br/>
     * TODO: Include tickets, both those granted on the resource itself and
     * those inherited from ancestor resources.<br/>
     * </p>
     */
    protected DavAcl getAcl() {
        return acl;
    }

    private DavAcl makeAcl() {
        DavAcl acl = new DavAcl();

        DavAce unauthenticated = new DavAce.UnauthenticatedAce();
        unauthenticated.setDenied(true);
        unauthenticated.getPrivileges().add(DavPrivilege.ALL);
        unauthenticated.setProtected(true);
        acl.getAces().add(unauthenticated);

        DavAce owner = new DavAce.PropertyAce(OWNER);
        owner.getPrivileges().add(DavPrivilege.READ);
        owner.setProtected(true);
        acl.getAces().add(owner);

        for (CollectionItem parent : item.getParents()) {
            if (parent.getOwner().equals(item.getOwner())){
                continue;
            }
            try {
                DavResourceLocatorFactory f = getResourceLocator().getFactory();
                DavResourceLocator l = f.createPrincipalLocator(
                        getResourceLocator().getContext(), parent.getOwner());
                DavAce parentOwner = new DavAce.PropertyAce(OWNER);
                parentOwner.getPrivileges().add(DavPrivilege.ALL);
                parentOwner.setProtected(true);
                parentOwner.setInherited(l.getHref(false));
                acl.getAces().add(parentOwner);
            } catch (CosmoDavException e) {
                LOG.warn("Could not create principal locator for parent collection owner: '{}' - skipping ACE",
                        parent.getOwner().getUsername());
            }
        }

        DavAce allAllow = new DavAce.AllAce();
        allAllow.getPrivileges().add(
                DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
        allAllow.setProtected(true);
        acl.getAces().add(allAllow);

        DavAce allDeny = new DavAce.AllAce();
        allDeny.setDenied(true);
        allDeny.getPrivileges().add(DavPrivilege.ALL);
        allDeny.setProtected(true);
        acl.getAces().add(allDeny);

        return acl;
    }

    /**
     * <p>
     * Extends the superclass method.
     * </p>
     * <p>
     * If the principal is a user, returns {@link DavPrivilege#READ} and
     * {@link DavPrivilege@WRITE}. This is a shortcut that assumes the security
     * layer has only allowed access to the owner of the home collection
     * specified in the URL used to access this resource. Eventually this method
     * will check the ACL for all ACEs corresponding to the current principal
     * and return the privileges those ACEs grant.
     * </p>
     * <p>
     * If the principal is a ticket, returns the dav privileges corresponding to
     * the ticket's privileges, since a ticket is in effect its own ACE.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (!privileges.isEmpty()) {
            return privileges;
        }

        // XXX eventually we will want to find the aces for the user and
        // add each of their granted privileges
        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            privileges.add(DavPrivilege.READ);
            privileges.add(DavPrivilege.WRITE);
            privileges.add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
            privileges.add(DavPrivilege.READ_FREE_BUSY);
            return privileges;
        }

        Ticket ticket = getSecurityManager().getSecurityContext().getTicket();
        if (ticket != null) {
            privileges.add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);

            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_READ)){
                privileges.add(DavPrivilege.READ);
            }
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_WRITE)){
                privileges.add(DavPrivilege.WRITE);
            }
            if (ticket.getPrivileges().contains(Ticket.PRIVILEGE_FREEBUSY)){
                privileges.add(DavPrivilege.READ_FREE_BUSY);
            }

            return privileges;
        }

        return privileges;
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        if (item == null) {
            return;
        }

        properties.add(new CreationDate(item.getCreationDate()));
        properties.add(new LastModified(item.getModifiedDate()));
        properties.add(new Etag(getETag()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Owner(getResourceLocator(), item.getOwner()));
        properties.add(new PrincipalCollectionSet(getResourceLocator()));
        properties.add(new Uuid(item.getUid()));
    }

    protected void setLiveProperty(WebDavProperty property, boolean create)
            throws CosmoDavException {
        if (item == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name
                    + " requires a value");
        }

        if (name.equals(DavPropertyName.CREATIONDATE)
                || name.equals(DavPropertyName.GETLASTMODIFIED)
                || name.equals(DavPropertyName.GETETAG)
                || name.equals(DavPropertyName.RESOURCETYPE)
                || name.equals(DavPropertyName.ISCOLLECTION)
                || name.equals(OWNER) || name.equals(PRINCIPALCOLLECTIONSET)
                || name.equals(UUID)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(DavPropertyName.DISPLAYNAME)) {
            item.setDisplayName(property.getValueText());
        }
    }

    protected void removeLiveProperty(DavPropertyName name)
            throws CosmoDavException {
        if (item == null) {
            return;
        }

        if (name.equals(DavPropertyName.CREATIONDATE)
                || name.equals(DavPropertyName.GETLASTMODIFIED)
                || name.equals(DavPropertyName.GETETAG)
                || name.equals(DavPropertyName.DISPLAYNAME)
                || name.equals(DavPropertyName.RESOURCETYPE)
                || name.equals(DavPropertyName.ISCOLLECTION)
                || name.equals(OWNER) || name.equals(PRINCIPALCOLLECTIONSET)
                || name.equals(UUID)) {
            throw new ProtectedPropertyModificationException(name);
        }

        getProperties().remove(name);
    }

    /**
     * Returns a list of names of <code>Attribute</code>s that should not be
     * exposed through DAV as dead properties.
     */
    protected abstract Set<String> getDeadPropertyFilter();

    protected void loadDeadProperties(DavPropertySet properties) {
        for (Iterator<Map.Entry<QName, Attribute>> i = item.getAttributes()
                .entrySet().iterator(); i.hasNext();) {
            Map.Entry<QName, Attribute> entry = i.next();

            // skip attributes that are not meant to be shown as dead
            // properties
            if (getDeadPropertyFilter().contains(entry.getKey().getNamespace())) {
                continue;
            }

            DavPropertyName propName = qNameToPropName(entry.getKey());

            // ignore live properties, as they'll be loaded separately
            if (isLiveProperty(propName)) {
                continue;
            }

            // XXX: language
            Object propValue = entry.getValue().getValue();
            properties.add(new StandardDavProperty(propName, propValue, false));
        }
    }

    protected void setDeadProperty(WebDavProperty property)
            throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setting dead property " + property.getName() + " on "
                    + getResourcePath() + " to " + property.getValue());
        }

        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property "
                    + property.getName() + " requires a value");
        }

        try {
            QName qname = propNameToQName(property.getName());
            Element value = (Element) property.getValue();
            Attribute attr = item.getAttribute(qname);

            // first check for existing attribute otherwise add
            if (attr != null) {
                attr.setValue(value);
            } else {
                item.addAttribute(entityFactory
                        .createXMLAttribute(qname, value));
            }
        } catch (DataSizeException e) {
            throw new ForbiddenException(e.getMessage());
        }
    }

    protected void removeDeadProperty(DavPropertyName name)
            throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing property " + name + " on " + getResourcePath());
        }

        item.removeAttribute(propNameToQName(name));
    }

    abstract protected void updateItem() throws CosmoDavException;

    private QName propNameToQName(DavPropertyName name) {
        if (name == null) {
            final String msg = "name cannot be null";
            throw new IllegalArgumentException(msg);
        }

        Namespace ns = name.getNamespace();
        String uri = ns != null ? ns.getURI() : "";

        return entityFactory.createQName(uri, name.getName());
    }

    private DavPropertyName qNameToPropName(QName qname) {
        // no namespace at all
        if ("".equals(qname.getNamespace())) {
            return DavPropertyName.create(qname.getLocalName());
        }

        Namespace ns = Namespace.getNamespace(qname.getNamespace());

        return DavPropertyName.create(qname.getLocalName(), ns);
    }

    public static boolean hasNonOK(MultiStatusResponse msr) {
        if (msr == null || msr.getStatus() == null) {
            return false;
        }

        for (Status status : msr.getStatus()) {

            if (status != null) {
                int statusCode = status.getStatusCode();

                if (statusCode != 200) {
                    return true;
                }
            }
        }
        return false;
    }
}