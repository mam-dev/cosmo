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
package org.unitedinternet.cosmo.service;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;

/**
 * Interface for services that manage access to user content.
 */
public interface ContentService {

    /**
     * Get the root item for a user
     *
     * @param user The givern user.
     * @return The root item for a user.
     */
    public HomeCollectionItem getRootItem(User user);

    /**
     * Get the root item for a user
     *
     * @param user The givern user.
     * @param forceReload if true, cleans hibernate session before loading.
     * @return The root item for a user.
     */
    public HomeCollectionItem getRootItem(User user, boolean forceReload);

    /**
     * Searches for an item stamp by item uid. The implementation will hit directly the the DB. 
     * @param internalItemUid item internal uid
     * @param clazz stamp type
     * @return the item's stamp from the db
     */
    <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid, Class<STAMP_TYPE> clazz);
    
    /**
     * Find an item with the specified uid. The return type will be one of
     * ContentItem, CollectionItem, CalendarCollectionItem, CalendarItem.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid);

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     * @param path The given path.
     * @return The content item.
     *
     */
    public Item findItemByPath(String path);
    
    /**
     * Find content item by path relative to the identified parent
     * item.
     * @param path The pathe relative.
     * @param parentUid Identified parent item.
     * @return The content item,.
     *
     */
    public Item findItemByPath(String path,
                               String parentUid);
    
    /**
     * Find content item's parent by path. Path is of the format:
     * /username/parent1/parent2/itemname. 
     * @param path The given path.
     * @return In this example,
     * the item at /username/parent1/parent2 would be returned.
     *
     */
    public Item findItemParentByPath(String path);

    /**
     * Add an item to a collection.
     * 
     * @param item
     *            item to add to collection
     * @param collection
     *            collection to add item to
     */
    public void addItemToCollection(Item item, CollectionItem collection);

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param targetParent existing source collection
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     * @throws org.osaf.cosmo.dao.hibernate.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.osaf.cosmo.dao.hibernate.DuplicateItemNameException
     *         if path points to an item with the same path
     */
    public void copyItem(Item item, CollectionItem targetParent, 
            String path, boolean deepCopy);
  
    /**
     * Move item from one collection to another
     * @param item item to move
     * @param oldParent parent to remove item from
     * @param newParent parent to add item to
     */
    public void moveItem(Item item, CollectionItem oldParent, CollectionItem newParent);
    
    /**
     * Remove an item. Removes item from all collections.
     * 
     * @param item
     *            item to remove
     */
    public void removeItem(Item item);
    
    /**
     * Remove an item from a collection.  The item will be removed if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(Item item, CollectionItem collection);

   
    /**
     * Load all children for collection that have been updated since a
     * given timestamp.  If no timestamp is specified, then return all 
     * children.
     * @param collection collection
     * @param timestamp timestamp
     * @return children of collection that have been updated since 
     *         timestamp, or all children if timestamp is null
     */
    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp);

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection);

    /**
     * Create a new collection with an initial set of items.
     * The initial set of items can include new items and
     * existing items.  New items will be created and associated
     * to the new collection and existing items will be updated
     * and associated to the new collection.  
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @param children
     *            collection children
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection,
                                           Set<Item> children);
    
    /**
     * Update a collection and set child items.  The set of
     * child items to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false for an existing Item.  When an item is marked
     * for removal, it is removed from the collection and
     * removed from the server only if the item has no parent
     * collections.
     * 
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection,
                                           Set<Item> children);
    
    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection);

    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection);
    
    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     * 
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content);

    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void createContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems);

    /**
     * 
     * @param parent parent collection of content items.
     */
    public void updateCollectionTimestamp(CollectionItem parent);
    
    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void createBatchContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems);

    /**
     * updates existing content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to update
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateBatchContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems);
    
    /**
     * remove existing content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to remove
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void removeBatchContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems);
    
    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     * 
     * @param parent
     *            parent that new content items will be added to.
     * @param contentItems to update
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateContentItems(CollectionItem parent, Set<ContentItem> contentItems);
    
    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    public ContentItem updateContent(ContentItem content);
    
    
    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     */
    public void removeContent(ContentItem content);

    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem);
    
    /**
     * Find items by filter.
     *
     * @param filter
     *            filter to use in search
     * @return set items matching specified
     *         filter.
     */
    public Set<Item> findItems(ItemFilter filter);
    
   
    /**
     * Find note items by triage status that belong to a collection.
     * @param collection collection
     * @param context the query context
     * @return set of notes that match the specified triage status label and
     *         belong to the specified collection
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(CollectionItem collection,
            TriageStatusQueryContext context);
    
    /**
     * Find note items by triage status that belong to a recurring note series.
     * @param note recurring note
     * @param context the query context
     * @return set of notes that match the specified triage status label and belong
     *         to the specified recurring note series
     */
    public SortedSet<NoteItem> findNotesByTriageStatus(NoteItem note,
            TriageStatusQueryContext context);
    
    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,
                             Ticket ticket);

    /**
     * Creates a ticket on an item.
     *
     * @param path the path of the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(String path,
                             Ticket ticket);

   
    /**
     * @return The identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param item the ticketed item
     * @param key the ticket to return
     */
    public Ticket getTicket(Item item,
                            String key);

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */
    public void removeTicket(Item item,
                             Ticket ticket);

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param key the key of the ticket to remove
     */
    public void removeTicket(Item item,
                             String key);
    
    /**
     * Remove items from a collection.
     * @param collection The collection contains all items which will be removed. 
     */
    public void removeItemsFromCollection(CollectionItem collection);

}
