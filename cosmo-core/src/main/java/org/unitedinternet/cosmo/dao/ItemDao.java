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
package org.unitedinternet.cosmo.dao;

import java.util.Set;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;

/**
 * Interface for DAO that provides base functionality for items stored in the server.
 *
 */
public interface ItemDao extends Dao {

    /**
     * Find an item with the specified uid. The return type will be one of ContentItem, CollectionItem, NoteItem.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid);

    /**
     * Searches for an item stamp by item uid. The implementation will hit directly the the DB.
     * 
     * @param internalItemUid
     *            item internal uid
     * @param clazz
     *            stamp type
     * @return the item's stamp from the db
     */
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid,
            Class<STAMP_TYPE> clazz);

    /**
     * Find an item with the specified path. The return type will be one of ContentItem, NoteItem, CollectionItem.
     *
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    @ExternalizableContent
    public Item findItemByPath(String path);

    /**
     * Find an item with the specified path, relative to a parent collection. The return type will be one of
     * ContentItem, NoteItem, CollectionItem.
     *
     * @param path
     *            path of item to find
     * @param parentUid
     *            uid of parent that path is relative to
     * @return item represented by path
     */
    @ExternalizableContent
    public Item findItemByPath(String path, String parentUid);

    /**
     * Find the parent item of the item with the specified path. The return type will be of type CollectionItem.
     *
     * @param path
     *            path of item
     * @return parent item of item represented by path
     */
    public Item findItemParentByPath(String path);

    /**
     * Get the root item for a user
     *
     * @param user
     *            The user for get the root item.
     * @param forceReload
     *            cleans the session before loading the item
     * @return home collection item.
     */
    public HomeCollectionItem getRootItem(User user, boolean forceReload);

    /**
     * Get the root item for a user
     *
     * @param user
     *            The user for get the root item.
     * @return home collection item.
     */
    public HomeCollectionItem getRootItem(User user);

    /**
     * Create the root item for a user.
     * 
     * @param user
     *            The user for create the root item.
     * @return Home collection item.
     */
    public HomeCollectionItem createRootItem(User user);

    /**
     * Copy an item to the given path
     * 
     * @param item
     *            item to copy
     * @param destPath
     *            destination path to copy item to
     * @param deepCopy
     *            true for deep copy, else shallow copy will be performed
     * @throws ItemNotFoundException
     *             if parent item specified by path does not exist
     * @throws DuplicateItemNameException
     *             if path points to an item with the same path
     */
    public void copyItem(Item item, String destPath, boolean deepCopy);

    /**
     * Move item to the given path
     * 
     * @param fromPath
     *            path of item to move
     * @param toPath
     *            path to move item to
     * @throws ItemNotFoundException
     *             if parent item specified by path does not exist
     * @throws DuplicateItemNameException
     *             if path points to an item with the same path
     */
    public void moveItem(String fromPath, String toPath);

    /**
     * Remove an item.
     *
     * @param item
     *            item to remove
     */
    public void removeItem(Item item);

    /**
     * Remove an item give the item's path
     * 
     * @param path
     *            path of item to remove
     */
    public void removeItemByPath(String path);

    /**
     * Remove an item given the item's uid
     * 
     * @param uid
     *            the uid of the item to remove
     */
    public void removeItemByUid(String uid);

    /**
     * Creates a ticket on an item.
     *
     * @param item
     *            the item to be ticketed
     * @param ticket
     *            the ticket to be saved
     */
    public void createTicket(Item item, Ticket ticket);

    /**
     * Returns all tickets on the given item.
     *
     * @param item
     *            the item to be ticketed
     * @return All tickets on the given item.
     */
    public Set<Ticket> getTickets(Item item);

    /**
     * Find ticket for ticket key.
     * 
     * @param key
     *            ticket key
     * @return Ticket corresponding to key
     */
    public Ticket findTicket(String key);

    /**
     * Returns the identified ticket on the given item, or <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket but an ancestor does, it will still be returned.
     *
     * @param item
     *            the ticketed item
     * @param key
     *            the ticket to return
     * @return The identified ticket on the goven item.
     */
    public Ticket getTicket(Item item, String key);

    /**
     * Removes a ticket from an item.
     *
     * @param item
     *            the item to be de-ticketed
     * @param ticket
     *            the ticket to remove
     */
    public void removeTicket(Item item, Ticket ticket);

    /**
     * Adds item to a collection.
     *
     * @param item
     *            the item
     * @param collection
     *            the collection to add to
     */
    @ExternalizableContent
    public void addItemToCollection(Item item, CollectionItem collection);

    /**
     * Remove item from a collection.
     *
     * @param item
     *            the item
     * @param collection
     *            the collection to remove from
     */
    @ExternalizableContent
    public void removeItemFromCollection(Item item, CollectionItem collection);

    /**
     * Refresh item with persistent state.
     *
     * @param item
     *            the item
     */
    public void refreshItem(Item item);

    /**
     * Initialize item, ensuring any proxied associations will be loaded.
     * 
     * @param item
     *            The item initialized.
     */
    public void initializeItem(Item item);

    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem
     *            parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem);

    /**
     * Find a set of items using an ItemFilter.
     * 
     * @param filter
     *            criteria to filter items by
     * @return set of items matching ItemFilter
     */
    @ExternalizableContent
    public Set<Item> findItems(ItemFilter filter);

    /**
     * Find a set of items using a set of ItemFilters. The set of items returned includes all items that match any of
     * the filters.
     * 
     * @param filters
     *            criteria to filter items by
     * @return set of items matching any of the filters
     */
    public Set<Item> findItems(ItemFilter[] filters);

    /**
     * Generates a unique ID. Provided for consumers that need to manipulate an item's UID before creating the item.
     * 
     * @return The id generated.
     */
    public String generateUid();

    /**
     * Counts the items for the specified owner that have been created after the specified time stamp.
     * 
     * @param ownerId
     * @param fromTimestamp
     * @return number items for the specified owner that have been created after the specified time stamp
     */
    public long countItems(long ownerId, long fromTimestamp);

    /**
     * Counts all the items of the specified ownerId.
     * 
     * @param ownerId
     * @return the number of items of the specified ownerId.
     */
    public long countItems(long ownerId);
}
