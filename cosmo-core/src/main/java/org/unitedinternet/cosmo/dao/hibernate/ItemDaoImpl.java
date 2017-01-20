/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StatelessSession;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.security.core.token.TokenService;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibItemTombstone;
import org.unitedinternet.cosmo.util.VersionFourGenerator;


/**
 * Implementation of ItemDao using Hibernate persistent objects.
 */
public abstract class ItemDaoImpl extends AbstractDaoImpl implements ItemDao {

    private static final Log LOG = LogFactory.getLog(ItemDaoImpl.class);

    private VersionFourGenerator idGenerator = null;
    private TokenService ticketKeyGenerator = null;
    private ItemPathTranslator itemPathTranslator = null;
    private ItemFilterProcessor itemFilterProcessor = null;

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByPath(java.lang.String)
     */
    public Item findItemByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemByPath(path);
            return dbItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByPath(java.lang.String, java.lang.String)
     */
    public Item findItemByPath(String path, String parentUid) {
        try {
            Item parent = findItemByUid(parentUid);
            if (parent == null) {
                return null;
            }
            Item item = itemPathTranslator.findItemByPath(path, (CollectionItem) parent);
            return item;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemParentByPath(java.lang.String)
     */
    public Item findItemParentByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemParent(path);
            return dbItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findEventStampFromDbByUid(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid,
            Class<STAMP_TYPE> clazz) {
        try (StatelessSession session = this.openStatelessSession()){

            List<Stamp> stamps = (List<Stamp>) session.createNamedQuery("item.stamps.by.uid")
                    .setParameter("uid", internalItemUid)
                    .setHint(AvailableSettings.JPA_SHARED_CACHE_STORE_MODE, null)
                    .setHint(AvailableSettings.JPA_SHARED_CACHE_RETRIEVE_MODE, null)
                    .getResultList();
            for (Stamp stamp : stamps) {
                if (clazz.isInstance(stamp)) {
                    return clazz.cast(stamp);
                }
            }
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByUid(java.lang.String)
     */
    public Item findItemByUid(String uid) {
        try {
            // prevent auto flushing when looking up item by uid
            getSession().setHibernateFlushMode(FlushMode.MANUAL);
            return (Item) getSession().byNaturalId(HibItem.class).using("uid", uid).load();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void removeItem(Item item) {
        try {

            if (item == null) {
                throw new IllegalArgumentException("item cannot be null");
            }

            if (item instanceof HomeCollectionItem) {
                throw new IllegalArgumentException("cannot remove root item");
            }

            removeItemInternal(item);
            getSession().flush();

        } catch (ObjectNotFoundException onfe) {
            throw new ItemNotFoundException("item not found");
        } catch (ObjectDeletedException ode) {
            throw new ItemNotFoundException("item not found");
        } catch (UnresolvableObjectException uoe) {
            throw new ItemNotFoundException("item not found");
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        if(forceReload){
            getSession().clear();
        }
        return getRootItem(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#getRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HomeCollectionItem getRootItem(User user) {
        try {
            return findRootItem(getBaseModelObject(user).getId());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#createRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HomeCollectionItem createRootItem(User user) {
        try {

            if (user == null) {
                throw new IllegalArgumentException("invalid user");
            }

            if (findRootItem(getBaseModelObject(user).getId()) != null) {
                throw new CosmoException("user already has root item", new CosmoException());
            }

            HomeCollectionItem newItem = new HibHomeCollectionItem();

            newItem.setOwner(user);
            newItem.setName(user.getUsername());
            //do not set this, it might be sensitive or different than name
            //newItem.setDisplayName(newItem.getName()); 
            setBaseItemProps(newItem);
            getSession().save(newItem);
            getSession().flush();
            return newItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void addItemToCollection(Item item, CollectionItem collection) {
        try {
            addItemToCollectionInternal(item, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeItemFromCollection(Item item, CollectionItem collection) {
        try {
            removeItemFromCollectionInternal(item, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<Ticket> getTickets(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        try {
            getSession().refresh(item);
            return item.getTickets();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Ticket findTicket(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        try {
            // prevent auto flushing when looking up ticket
            getSession().setHibernateFlushMode(FlushMode.MANUAL);
            Query<Ticket> query = getSession().createNamedQuery("ticket.by.key", Ticket.class)
                    .setParameter("key", key);
            query.setCacheable(true);
            query.setFlushMode(FlushMode.MANUAL);
            List<Ticket> ticketList = query.getResultList();
            return ticketList.size() > 0 ? ticketList.get(0) : null;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void createTicket(Item item, Ticket ticket) {
        try {
            if (ticket == null) {
                throw new IllegalArgumentException("ticket cannot be null");
            }

            if (item == null) {
                throw new IllegalArgumentException("item cannot be null");
            }

            User owner = ticket.getOwner();
            if (owner == null) {
                throw new IllegalArgumentException("ticket must have owner");
            }

            if (ticket.getKey() == null) {
                ticket.setKey(ticketKeyGenerator.allocateToken("").getKey());
            }

            ticket.setCreated(new Date());
            getSession().update(item);
            item.addTicket(ticket);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public Ticket getTicket(Item item, String key) {
        try {
            getSession().refresh(item);
            return getTicketRecursive(item, key);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeTicket(Item item, Ticket ticket) {
        try {
            getSession().update(item);
            item.removeTicket(ticket);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByPath(java.lang.String)
     */
    public void removeItemByPath(String path) {
        try {
            Item item = itemPathTranslator.findItemByPath(path);
            if (item == null) {
                throw new ItemNotFoundException("item at " + path
                        + " not found");
            }
            removeItem(item);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByUid(java.lang.String)
     */
    public void removeItemByUid(String uid) {
        try {
            Item item = findItemByUid(uid);
            if (item == null) {
                throw new ItemNotFoundException("item with uid " + uid
                        + " not found");
            }
            removeItem(item);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    public void copyItem(Item item, String destPath, boolean deepCopy) {
        try {
            String copyName = itemPathTranslator.getItemName(destPath);

            if (copyName == null || "".equals(copyName)) {
                throw new IllegalArgumentException("path must include name");
            }

            if (item instanceof HomeCollectionItem) {
                throw new IllegalArgumentException("cannot copy root collection");
            }

            CollectionItem newParent = (CollectionItem) itemPathTranslator.findItemParent(destPath);

            if (newParent == null) {
                throw new ItemNotFoundException("parent collection not found");
            }

            verifyNotInLoop(item, newParent);

            Item newItem = copyItemInternal(item, newParent, deepCopy);
            newItem.setName(copyName);
            getSession().flush();

        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#moveItem(java.lang.String, java.lang.String)
     */
    public void moveItem(String fromPath, String toPath) {
        try {

            // Get current item
            Item item = itemPathTranslator.findItemByPath(fromPath);

            if (item == null) {
                throw new ItemNotFoundException("item " + fromPath + " not found");
            }

            if (item instanceof HomeCollectionItem) {
                throw new IllegalArgumentException("cannot move root collection");
            }

            // Name of moved item
            String moveName = itemPathTranslator.getItemName(toPath);

            if (moveName == null || "".equals(moveName)) {
                throw new IllegalArgumentException("path must include name");
            }

            // Parent of moved item
            CollectionItem parent = (CollectionItem) itemPathTranslator.findItemParent(toPath);

            if (parent == null) {
                throw new ItemNotFoundException("parent collecion not found");
            }

            // Current parent
            CollectionItem oldParent = (CollectionItem) itemPathTranslator.findItemParent(fromPath);

            verifyNotInLoop(item, parent);

            item.setName(moveName);
            if (!parent.getUid().equals(oldParent.getUid())) {
                ((HibCollectionItem) parent).removeTombstone(item);

                // Copy over existing CollectionItemDetails
                ((HibItem) item).addParent(parent);

                // Remove item from old parent collection
                getHibItem(oldParent).addTombstone(new HibItemTombstone(oldParent, item));
                ((HibItem) item).removeParent(oldParent);
            }

            getSession().flush();

        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#refreshItem(org.unitedinternet.cosmo.model.Item)
     */
    public void refreshItem(Item item) {
        try {
            getSession().refresh(item);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#initializeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void initializeItem(Item item) {
        try {
            LOG.info("initialize Item : "+item.getUid());
            // initialize all the proxied-associations, to prevent
            // lazy-loading of this data
            Hibernate.initialize(item.getAttributes());
            Hibernate.initialize(item.getStamps());
            Hibernate.initialize(item.getTombstones());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem){
        try {
            Set<CollectionItem> children = new HashSet<>();
            Query<CollectionItem> hibQuery = getSession()
                    .createNamedQuery("collections.children.by.parent", CollectionItem.class)
                    .setParameter("parent", collectionItem);

            List<CollectionItem> results = hibQuery.getResultList();
            children.addAll(results);            
            return children;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    /**
     * Find a set of items using an ItemFilter.
     *
     * @param filter criteria to filter items by
     * @return set of items matching ItemFilter
     */
    public Set<Item> findItems(ItemFilter filter) {
        try {
            return itemFilterProcessor.processFilter(filter);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /**
     * Find a set of items using a set of ItemFilters.  The set of items
     * returned includes all items that match any of the filters.
     *
     * @param filters criteria to filter items by
     * @return set of items matching any of the filters
     */
    public Set<Item> findItems(ItemFilter[] filters) {
        try {
            HashSet<Item> returnSet = new HashSet<Item>();
            for (ItemFilter filter : filters) {
                returnSet.addAll(itemFilterProcessor.processFilter(filter));
            }
            return returnSet;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /**
     * Generates a unique ID. Provided for consumers that need to
     * manipulate an item's UID before creating the item.
     */
    public String generateUid() {
        return idGenerator.nextStringIdentifier();
    }

    /**
     * Set the unique ID generator for new items
     *
     * @param idGenerator
     */
    public void setIdGenerator(VersionFourGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public VersionFourGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * Set the unique key generator for new tickets
     *
     * @param ticketKeyGenerator
     */
    public void setTicketKeyGenerator(TokenService ticketKeyGenerator) {
        this.ticketKeyGenerator = ticketKeyGenerator;
    }

    public TokenService getTicketKeyGenerator() {
        return ticketKeyGenerator;
    }

    public ItemPathTranslator getItemPathTranslator() {
        return itemPathTranslator;
    }

    /**
     * Set the path translator. The path translator is responsible for
     * translating a path to an item in the database.
     *
     * @param itemPathTranslator
     */
    public void setItemPathTranslator(ItemPathTranslator itemPathTranslator) {
        this.itemPathTranslator = itemPathTranslator;
    }


    public ItemFilterProcessor getItemFilterProcessor() {
        return itemFilterProcessor;
    }

    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#destroy()
     */
    public abstract void destroy();

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#init()
     */
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }

        if (ticketKeyGenerator == null) {
            throw new IllegalStateException("ticketKeyGenerator is required");
        }

        if (itemPathTranslator == null) {
            throw new IllegalStateException("itemPathTranslator is required");
        }

        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

    }

    protected Item copyItemInternal(Item item, CollectionItem newParent, boolean deepCopy) {

        Item item2 = item.copy();
        item2.setName(item.getName());

        // copy base Item fields
        setBaseItemProps(item2);

        ((HibItem) item2).addParent(newParent);

        // save Item before attempting deep copy
        getSession().save(item2);
        getSession().flush();

        // copy children if collection and deepCopy = true
        if (deepCopy == true && item instanceof CollectionItem) {
            CollectionItem collection = (CollectionItem) item;
            for (Item child : collection.getChildren()) {
                copyItemInternal(child, (CollectionItem) item2, true);
            }
        }

        return item2;
    }

    /**
     * Checks to see if a parent Item is currently a child of a target item. If
     * so, then this would put the hierarchy into a loop and is not allowed.
     *
     * @param item
     * @param newParent
     * @throws org.unitedinternet.cosmo.dao.ModelValidationException if newParent is child of item
     */
    protected void verifyNotInLoop(Item item, CollectionItem newParent) {
        // need to verify that the new parent is not a child
        // of the item, otherwise we get a loop
        if (getBaseModelObject(item).getId().equals(getBaseModelObject(newParent).getId())) {
            throw new ModelValidationException(newParent,
                    "Invalid parent - will cause loop");
        }

        // If item is not a collection then all is good
        if (!(item instanceof CollectionItem)) {
            return;
        }

        CollectionItem collection = (CollectionItem) item;
        getSession().refresh(collection);

        for (Item nextItem : collection.getChildren()) {
            verifyNotInLoop(nextItem, newParent);
        }
    }

    /**
     * Verifies that name is unique in collection, meaning no item exists
     * in collection with the same item name.
     *
     * @param item       item name to check
     * @param collection collection to check against
     * @throws org.unitedinternet.cosmo.dao.DuplicateItemNameException if item with same name exists
     *                                    in collection
     */
    protected void verifyItemNameUnique(Item item, CollectionItem collection) {
        Query<Long> query = getSession().createNamedQuery("itemId.by.parentId.name", Long.class);
        query.setParameter("name", item.getName()).setParameter("parentid",
                ((HibItem) collection).getId());
        List<Long> results = query.getResultList();
        if (results.size() > 0) {
            throw new DuplicateItemNameException(item, "item name " + item.getName() +
                    " already exists in collection " + collection.getUid());
        }
    }

    /**
     * Find the DbItem with the specified dbId
     *
     * @param dbId dbId of DbItem to find
     * @return DbItem with specified dbId
     */
    protected Item findItemByDbId(Long dbId) {
        return (Item) getSession().get(Item.class, dbId);
    }

    // Set server generated item properties
    protected void setBaseItemProps(Item item) {
        if (item.getUid() == null) {
            item.setUid(idGenerator.nextStringIdentifier());
        }
        if (item.getName() == null) {
            item.setName(item.getUid());
        }
        if (item instanceof ICalendarItem) {
            ICalendarItem ical = (ICalendarItem) item;
            if (ical.getIcalUid() == null) {
                ical.setIcalUid(item.getUid());
                EventStamp es = HibEventStamp.getStamp(ical);
                if (es != null) {
                    es.setIcalUid(ical.getIcalUid());
                }
            }
        }
        for (Ticket ticket : item.getTickets()) {
            if (ticket.getOwner() == null) {
                ticket.setOwner(item.getOwner());
            }
            if (ticket.getKey() == null) {
                ticket.setKey(ticketKeyGenerator.allocateToken("").getKey());
            }
            if (ticket.getTimeout() == null) {
                ticket.setTimeout(Ticket.TIMEOUT_INFINITE);
            }
            ticket.setCreated(new Date());
        }
    }

    protected Item findItemByParentAndName(Long userDbId, Long parentDbId, String name) {
        Query<Item> query = null;
        if (parentDbId != null) {
            query = getSession().createNamedQuery("item.by.ownerId.parentId.name", Item.class)
                    .setParameter("ownerid", userDbId).setParameter("parentid", parentDbId).setParameter("name", name);

        } else {
            query = getSession().createNamedQuery("item.by.ownerId.nullParent.name", Item.class)
                    .setParameter("ownerid", userDbId).setParameter("name", name);
        }
        query.setFlushMode(FlushMode.MANUAL);
        List<Item> itemList = query.getResultList();
        return itemList.size() > 0 ? itemList.get(0) : null;
    }

    protected Item findItemByParentAndNameMinusItem(Long userDbId, Long parentDbId, String name, Long itemId) {
        Query<Item> query = null;
        if (parentDbId != null) {
            query = getSession().createNamedQuery("item.by.ownerId.parentId.name.minusItem", Item.class)
                    .setParameter("itemid", itemId).setParameter("ownerid", userDbId)
                    .setParameter("parentid", parentDbId).setParameter("name", name);
        } else {
            query = getSession().createNamedQuery("item.by.ownerId.nullParent.name.minusItem", Item.class)
                    .setParameter("itemid", itemId).setParameter("ownerid", userDbId).setParameter("name", name);
        }
        query.setFlushMode(FlushMode.MANUAL);
        List<Item> itemList = query.getResultList();
        return itemList.size() > 0 ? itemList.get(0) : null;
    }

    protected HomeCollectionItem findRootItem(Long dbUserId) {
        Query<HomeCollectionItem> query = getSession()
                .createNamedQuery("homeCollection.by.ownerId", HomeCollectionItem.class)
                .setParameter("ownerid", dbUserId);
        query.setCacheable(true);
        query.setFlushMode(FlushMode.MANUAL);
        List<HomeCollectionItem> itemList = query.getResultList();
        return itemList.size() > 0 ? itemList.get(0) : null;
    }

    protected void checkForDuplicateUid(Item item) {
        // verify uid not in use
        if (item.getUid() != null) {
            // Lookup item by uid
            Query<Long> query = getSession().createNamedQuery("itemid.by.uid", Long.class).setParameter("uid",
                    item.getUid());
            query.setFlushMode(FlushMode.MANUAL);
            List<Long> idList = query.getResultList();
            // if uid is in use throw exception
            if (idList.size() > 0) {
                throw new UidInUseException(item.getUid(), "uid " + item.getUid() + " already in use");
            }
        }
    }

    protected Ticket getTicketRecursive(Item item, String key) {
        if (item == null) {
            return null;
        }

        for (Ticket ticket : item.getTickets()) {
            if (ticket.getKey().equals(key)) {
                return ticket;
            }
        }

        for (Item parent : item.getParents()) {
            Ticket ticket = getTicketRecursive(parent, key);
            if (ticket != null) {
                return ticket;
            }
        }

        return null;
    }

    protected void attachToSession(Item item) {
        if (getSession().contains(item)) {
            return;
        }
        getSession().lock(item, LockMode.NONE);
    }

    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {

        getSession().update(collection);
        getSession().update(item);

        // do nothing if item doesn't belong to collection
        if (!item.getParents().contains(collection)) {
            return;
        }

        getHibItem(collection).addTombstone(new HibItemTombstone(collection, item));
        ((HibItem) item).removeParent(collection);

        // If the item belongs to no collection, then it should
        // be purged.
        if (item.getParents().size() == 0) {
            removeItemInternal(item);
        }
    }

    protected void addItemToCollectionInternal(Item item,
                                               CollectionItem collection) {
        verifyItemNameUnique(item, collection);
        getSession().update(item);
        getSession().update(collection);
        ((HibCollectionItem) collection).removeTombstone(item);
        ((HibItem) item).addParent(collection);
    }

    protected void removeItemInternal(Item item) {
        getSession().delete(item);
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

    protected HibItem getHibItem(Item item) {
        return (HibItem) item;
    }

    protected HibCollectionItem getHibCollectionItem(CollectionItem item) {
        return (HibCollectionItem) item;
    }

}
