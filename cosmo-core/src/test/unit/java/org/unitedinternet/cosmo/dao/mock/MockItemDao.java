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
package org.unitedinternet.cosmo.dao.mock;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.ItemFilterEvaluater;
import org.unitedinternet.cosmo.model.filter.ItemFilterPostProcessor;
import org.unitedinternet.cosmo.model.mock.MockAuditableObject;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockItem;
import org.unitedinternet.cosmo.util.PathUtil;

/**
 * Mock implementation of <code>ItemDao</code> useful for testing.
 *
 * @see ItemDao
 * @see Item
 */
public class MockItemDao implements ItemDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(MockItemDao.class);

    private MockDaoStorage storage;

    /**
     * Constructor.
     * @param storage Tge mock dao storage.
     */
    public MockItemDao(MockDaoStorage storage) {
        this.storage = storage;
    }

    // ItemDao methods

    /**
     * Find an item with the specified uid. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param id
     *            id of item to find
     * @return item represented by uid
     */
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String id, Class<STAMP_TYPE> clazz) {
        return storage.getItemStampByUid(id, clazz);
    }
    
    /**
     * Find an item with the specified uid. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid) {
        return storage.getItemByUid(uid);
    }
    

    /**
     * Find an item with the specified path. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    public Item findItemByPath(String path) {
        return storage.getItemByPath(decode(path));
    }
    
    /**
     * Find the parent item of the item with the specified path. 
     * The return type will be one of CollectionItem, CalendarCollectionItem.
     *
     * @param path
     *            path of item
     * @return parent item of item represented by path
     */  
    public Item findItemParentByPath(String path) {
        return storage.getItemByPath(path).getParent();
    }

    @Override
    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        return getRootItem(user);
    }

    /**
     * Return the path to an item. The path has the format:
     * /username/parent1/parent2/itemname
     * 
     * @param item
     *            the item to calculate the path for
     * @return hierarchical path to item
     */
    public String getItemPath(Item item) {
        return storage.getItemPath(item);
    }

    /**
     * Return the path to an item. The path has the format:
     * /username/parent1/parent2/itemname
     * 
     * @param uid
     *            the uid of the item to calculate the path for
     * @return hierarchical path to item
     */
    public String getItemPath(String uid) {
        return storage.getItemPath(uid);
    }

    /**
     * Get the root item for a user
     * 
     * @param user The user.
     * @return Home collection item.
     */
    public HomeCollectionItem getRootItem(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getting root item for user {}", user.getUsername());
        }

        return getStorage().getRootItem(user.getUsername());
    }

    /**
     * Create the root item for a user.
     * @param user The user.
     * @return The home collection item.
     */
    public HomeCollectionItem createRootItem(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating root item for user {}", user.getUsername());
        }

        HomeCollectionItem rootCollection = storage.createRootItem(user);

        if (LOG.isDebugEnabled()) {
            LOG.debug("root item uid is {}", rootCollection.getUid());
        }

        return rootCollection;
    }

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     */
    public void copyItem(Item item, String path, boolean deepCopy) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        PathUtil.getParentPath(path);
        CollectionItem parent = (CollectionItem)
            storage.getItemByPath(PathUtil.getParentPath(path));
        if (parent == null) {
            throw new ItemNotFoundException("parent collection not found");
        }

        copyItem(item, PathUtil.getBasename(path), parent, deepCopy);
    }

    /**
     * Copy item.
     * @param item The item.
     * @param copyName The copy name.
     * @param parent The parent.
     * @param deepCopy DeepCopy.
     * @return The item.
     */
    private Item copyItem(Item item, String copyName, CollectionItem parent, boolean deepCopy) {
        Item copy = null;
        try {
            copy = item.getClass().newInstance();
        } catch (Exception e) {
            throw new CosmoException("unable to construct new instance of " + item.getClass(), e);
        }

        if (copyName == null) {
            copyName = item.getName();
        }
        copy.setName(copyName);
        copy.getParents().add(parent);
        copy.setOwner(item.getOwner());

        for (Map.Entry<QName, Attribute> entry : item.getAttributes().entrySet()) {
            copy.addAttribute(entry.getValue().copy());
        }

        // XXX: ignoring calendar indexes

        storage.storeItem(copy);

        if (deepCopy && (item instanceof CollectionItem)) {
            CollectionItem collection = (CollectionItem) item;
            for (Item child: collection.getChildren()) {
                copyItem(child, null, (CollectionItem) copy, true);
            }
        }

        return copy;
    }
    
  
    /**
     * Move item to the given path
     * @param fromPath item to move
     * @param toPath path to move item to
     */
    public void moveItem(String fromPath, String toPath) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     */
    public void removeItem(Item item) {
        if (item.getParent()!=null) {
            ((MockCollectionItem) item.getParent()).removeChild(item);
        }
        
        // update modifications
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if (note.getModifies() != null) {
                note.getModifies().removeModification(note);
            }
        }

        storage.removeItemByUid(item.getUid());
        storage.removeItemByPath(getItemPath(item));
        if (storage.getRootUid(item.getOwner().getUsername()).
            equals(item.getUid())) {
            storage.removeRootUid(item.getOwner().getUsername());
        }
    }

    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,  Ticket ticket) {
        item.addTicket(ticket);
        ((MockAuditableObject) ticket).setModifiedDate(new Date());
        storage.createTicket(item, ticket);
    }

    /**
     * Returns all tickets on the given item.
     *
     * @param item the item to be ticketed.
     * @return The tickets.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set getTickets(Item item) {
        return storage.findItemTickets(item);
    }

    /**
     * Finds tickets.
     * {@inheritDoc}
     * @param key The key.
     * @return The tickets.
     */
    public Ticket findTicket(String key) {
        return storage.findTicket(key);
    }
    
    
    /**
     * Returns the identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param item the ticketed item
     * @param key the ticket to return
     * @return The tickets.
     */
    public Ticket getTicket(Item item,  String key) {
        for(Ticket t : storage.findItemTickets(item)) {
            if (t.getKey().equals(key)) {
                return t;
            }
        }
        // the ticket might be on an ancestor, so check the parent
        if (item.getParent() != null) {
            return getTicket(storage.getItemByUid(item.getParent().getUid()),
                             key);
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return null;
    }

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */
    public void removeTicket(Item item, Ticket ticket) {
        @SuppressWarnings("rawtypes")
        Set itemTickets = storage.findItemTickets(item);
        if (itemTickets.contains(ticket)) {
            item.removeTicket(ticket);
            storage.removeTicket(item, ticket);
            return;
        }
        // the ticket might be on an ancestor, so check the parent
        if (item.getParent() != null) {
            removeTicket(storage.getItemByUid(item.getParent().getUid()),
                         ticket);
        }
        // this is the root item; the ticket simply doesn't exist
        // anywhere in the given path
        return;
    }

    /**
     * Finds item by path.
     * {@inheritDoc}
     * @param path The path.
     * @param parentUid The parent's id.
     * @return The item.
     */
    public Item findItemByPath(String path, String parentUid) {
        return null;
    }

    /**
     * Removes item by path.
     * {@inheritDoc}
     * @param path The path.
     */
    public void removeItemByPath(String path) {
        removeItem(findItemByPath(path));
    }

    /**
     * Removes item by uid.
     * {@inheritDoc}
     * @param uid The uid.
     */
    public void removeItemByUid(String uid) {
        removeItem(findItemByUid(uid));
    }
    
    /**
     * Adds item to collection.
     * {@inheritDoc}
     * @param item The item.
     * @param collection The collection.
     */
    public void addItemToCollection(Item item, CollectionItem collection) {
        ((MockCollectionItem) collection).addChild(item);
        ((MockItem) item).addParent(collection);
    }
    
    /**
     * Removes item from collection.
     * {@inheritDoc}
     * @param item The item.
     * @param collection The collection.
     */
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        ((MockItem) item).removeParent(collection);
        ((MockCollectionItem) collection).removeChild(item);
        if (item.getParents().size() == 0) {
            removeItem(item);
        }
    }
    
    /**
     * Refresh item.
     * {@inheritDoc}
     * @param item The item.
     */
    public void refreshItem(Item item) {
    }
    
    /**
     * Initializes item
     * {@inheritDoc}
     * @param item The item.
     */
    public void initializeItem(Item item) {
    }

    /**
     * Finds item.
     * {@inheritDoc}
     * @param filter The filter.
     * @return Set with item.
     */
    public Set<Item> findItems(ItemFilter filter) {
        ItemFilterEvaluater evaluater = new ItemFilterEvaluater();
        ItemFilterPostProcessor postProcessor = new ItemFilterPostProcessor();
        HashSet<Item> results = new HashSet<Item>();
        for (Item i : storage.getAllItems()) {
            if (evaluater.evaulate(i, filter)) {
                results.add(i);
            }
        }
        
        return postProcessor.processResults(results, filter);
    }
    
    /**
     * Finds items.
     * {@inheritDoc}
     * @param filters The filters.
     * @return The items.
     */
    public Set<Item> findItems(ItemFilter[] filters) {
        ItemFilterEvaluater evaluater = new ItemFilterEvaluater();
        ItemFilterPostProcessor postProcessor = new ItemFilterPostProcessor();
        HashSet<Item> allResults = new HashSet<Item>();
        
        for(ItemFilter f: filters) {
            HashSet<Item> results = new HashSet<Item>();
            for (Item i : storage.getAllItems()) {
                if (evaluater.evaulate(i, f)) {
                    results.add(i);
                }
            }
            
            allResults.addAll(postProcessor.processResults(results, f));
        }
        
        return allResults;
    }

    /**
     * Generates uid.
     * {@inheritDoc}
     * @return The uid.
     */
    public String generateUid() {
        return storage.calculateUid();
    }

    // Dao methods
    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    // our methods

    /**
     * Gets storage.
     * @return The mock dao storage.
     */
    public MockDaoStorage getStorage() {
        return storage;
    }

    /**
     * Finds root children.
     * @param user The user.
     * @return The root.
     */
    @SuppressWarnings("rawtypes")
    protected Set findRootChildren(User user) {
        return storage.findItemChildren(storage.getRootItem(user.getUid()));
    }
    
    private static String decode(String urlPath){
        try {
            return new URI(urlPath).getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem) {
        Set<CollectionItem> collections = new HashSet<CollectionItem>();
        if (collectionItem instanceof HomeCollectionItem) {
            HomeCollectionItem homeCollection = storage.getRootItem("test");
            for(Item item:homeCollection.getChildren()){
                if (item instanceof CollectionItem) {
                    collections.add((CollectionItem)item);
                }
            }
        }
        return collections;
    }
    
    @Override
    public long countItems(long ownerId, long fromTimestamp) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long countItems(long ownerId) {
        throw new UnsupportedOperationException();
    }
}
