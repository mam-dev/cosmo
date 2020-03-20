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
package org.unitedinternet.cosmo.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ModificationUid;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.lock.LockManager;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryProcessor;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RecurrenceId;

/**
 * Standard implementation of <code>ContentService</code>.
 *
 * @see ContentService
 * @see ContentDao
 */
@Service
@Transactional
public class StandardContentService implements ContentService {
    
    private static final Logger LOG = LoggerFactory.getLogger(StandardContentService.class);        
   
    private final ContentDao contentDao;
        
    private final LockManager lockManager;
        
    private final TriageStatusQueryProcessor triageStatusQueryProcessor;
  
    private long lockTimeout = 100;
    
    public StandardContentService( @Autowired ContentDao contentDao,  @Autowired LockManager lockManager,
            @Autowired TriageStatusQueryProcessor triageStatusQueryProcessor) {
        super();
        this.contentDao = contentDao;
        this.lockManager = lockManager;
        this.triageStatusQueryProcessor = triageStatusQueryProcessor;
    }

    // ContentService methods

    /**
     * Get the root item for a user
     *
     * @param user
     */
    @Transactional(readOnly = true)
    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting root item for {}", user.getUsername());
        }
        return contentDao.getRootItem(user, forceReload);
    }

    /**
     * Get the root item for a user
     *
     * @param user
     */
    @Transactional(readOnly = true)
    public HomeCollectionItem getRootItem(User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting root item for {}", user.getUsername());
        }
        return contentDao.getRootItem(user);
    }

    /**
     * Searches for an item stamp by item uid. The implementation will hit directly the the DB. 
     * 
     * @param internalItemUid item internal uid
     * @param clazz stamp type
     * @return the item's stamp from the db
     */
    @Transactional(readOnly = true)
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid, Class<STAMP_TYPE> clazz){
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding item with uid {}", internalItemUid);
        }
        return contentDao.findStampByInternalItemUid(internalItemUid, clazz);
        
    }

    /**
     * Find an item with the specified uid.  If the uid is found, return
     * the item found.  If the uid represents a recurring NoteItem occurrence
     * (parentUid:recurrenceId), return a NoteOccurrence.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    @Transactional(readOnly = true)
    public Item findItemByUid(String uid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding item with uid {}", uid);
        }
        Item item = contentDao.findItemByUid(uid);
        
        // return item if found
        if(item!=null) {
            return item;
        }
        
        // Handle case where uid represents an occurrence of a recurring item.
        if(uid.indexOf(ModificationUid.RECURRENCEID_DELIMITER)!=-1) {
            ModificationUidImpl modUid;
            
            try {
                modUid = new ModificationUidImpl(uid);
            } catch (ModelValidationException e) {
                // If ModificationUid is invalid, item isn't present
                return null;
            }
            // Find the parent, and then verify that the recurrenceId is a valid occurrence date for the recurring item.
            NoteItem parent = (NoteItem) contentDao.findItemByUid(modUid.getParentUid());
            if(parent==null) {
                return null;
            }
            else {
                return getNoteOccurrence(parent, modUid.getRecurrenceId());
            }
        }
        
        return null;
    }

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     */
    @Transactional(readOnly = true)
    public Item findItemByPath(String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding item at path {}", path);
        }
        return contentDao.findItemByPath(path);
    }
    
    /**
     * Find content item by path relative to the identified parent
     * item.
     *
     * @throws NoSuchItemException if a item does not exist at the specified path
     * 
     */
    @Transactional(readOnly = true)
    public Item findItemByPath(String path, String parentUid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding item at path {} below parent {}", path , parentUid);
        }
        return contentDao.findItemByPath(path, parentUid);
    }
    
    /**
     * Find content item's parent by path. Path is of the format:
     * /username/parent1/parent2/itemname.  In this example,
     * the item at /username/parent1/parent2 would be returned.
     */
    @Transactional(readOnly = true)
    public Item findItemParentByPath(String path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding item's parent at path {}", path);
        }
        return contentDao.findItemParentByPath(path);
    }

   
    public void addItemToCollection(Item item, CollectionItem collection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding item {} to collection {}",item.getUid(),  collection.getUid());
        }
        
        contentDao.addItemToCollection(item, collection);
        contentDao.updateCollectionTimestamp(collection);
    }

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param existingParent existing source collection
     * @param targetParent existing destination collection
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     * @throws org.unitedinternet.cosmo.dao.ItemNotFoundException
     *         if parent item specified by path does not exist
     * @throws org.unitedinternet.cosmo.dao.DuplicateItemNameException
     *         if path points to an item with the same path
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and destination CollectionItem
     *         is lockecd.
     */    
    public void copyItem(Item item, CollectionItem targetParent, 
            String path, boolean deepCopy) {

        // Prevent HomeCollection from being copied
        if(item instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot copy home collection");
        }
        
        Item toItem = findItemByPath(path);
        if(toItem!=null) {
            throw new DuplicateItemNameException(null, path + " exists");
        }
        
        // Handle case of copying ContentItem (need to sync on dest collection)
        if(item != null && item instanceof ContentItem) {
            
            // Need to get exclusive lock to destination collection
            CollectionItem parent = 
                (CollectionItem) contentDao.findItemParentByPath(path);
            
            if(parent==null) {
                throw new IllegalArgumentException("path must match parent collection");
            }
            
            // Verify that destination parent in path matches newParent
            if(!parent.equals(targetParent)) {
                throw new IllegalArgumentException("targetParent must mach target path");
            }
           
            // if we can't get lock, then throw exception
            if (!lockManager.lockCollection(parent, lockTimeout)) {
                throw new CollectionLockedException(
                        "unable to obtain collection lock");
            }

            try {
                contentDao.copyItem(item, path, deepCopy);
            } finally {
                lockManager.unlockCollection(parent);
            }
        }
        else { 
            // no need to synchronize if not ContentItem
            contentDao.copyItem(item, path, deepCopy);
        }
    }
  
    /**
     * Move item from one collection to another
     * @param item item to move
     * @param oldParent parent to remove item from
     * @param newParent parent to add item to
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and source or destination 
     *         CollectionItem is lockecd.
     */    
    public void moveItem(Item item, CollectionItem oldParent, CollectionItem newParent) {
        
        // Prevent HomeCollection from being moved
        if(item instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot move home collection");
        }
        
        // Only need locking for ContentItem for now
        if(item instanceof ContentItem) {
            Set<CollectionItem> locks = acquireLocks(newParent, item);
            try {
                // add item to newParent
                contentDao.addItemToCollection(item, newParent);
                // remove item from oldParent
                contentDao.removeItemFromCollection(item, oldParent);
                
                // update collections involved
                for(CollectionItem parent : locks) {
                    contentDao.updateCollectionTimestamp(parent);
                }
                
            } finally {
                releaseLocks(locks);
            }
        } else {
            // add item to newParent
            contentDao.addItemToCollection(item, newParent);
            // remove item from oldParent
            contentDao.removeItemFromCollection(item, oldParent);
        }
    }
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if Item is a ContentItem and parent CollectionItem
     *         is locked
     */
    public void removeItem(Item item) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing item {}", item.getUid());
        }
        
        // Let service handle ContentItems (for sync purposes)
        if(item instanceof ContentItem) {
            removeContent((ContentItem) item);
        }
        else if(item instanceof CollectionItem) {
            removeCollection((CollectionItem) item);
        }
        else {
            contentDao.removeItem(item);
        }
    }
    
    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing item {} from collection {}", item.getUid(), collection.getUid());
        }
        
        contentDao.removeItemFromCollection(item, collection);
        contentDao.updateCollectionTimestamp(collection);
    }

    
    /**
     * Load all children for collection that have been updated since a given
     * timestamp. If no timestamp is specified, then return all children.
     * 
     * @param collection
     *            collection
     * @param timestamp
     *            timestamp
     * @return children of collection that have been updated since timestamp, or
     *         all children if timestamp is null
     */
    @Transactional(readOnly = true)
    public Set<ContentItem> loadChildren(CollectionItem collection, java.util.Date timestamp) {
        return contentDao.loadChildren(collection, timestamp);
    }

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
                                           CollectionItem collection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating collection {} in {}", collection.getName(), parent.getName());
        }
        
        return contentDao.createCollection(parent, collection);
    }

    /**
     * Create a new collection.
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
            CollectionItem collection, Set<Item> children) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating collection {} in {}", collection.getName(), parent.getName());
        }

        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of the children.  If all children are new
        // items, then no locks are obtained.
        Set<CollectionItem> locks = acquireLocks(children);
        
        try {
            // Create the new collection
            collection = contentDao.createCollection(parent, collection);
            
            Set<ContentItem> childrenToUpdate = new LinkedHashSet<ContentItem>();
            
            // Keep track of NoteItem modifications that need to be processed
            // after the master NoteItem.
            ArrayList<NoteItem> modifications = new ArrayList<NoteItem>(); 
            
            // Either create or update each item
            for (Item item : children) {
                if (item instanceof NoteItem) {
                    
                    NoteItem note = (NoteItem) item;
                    
                    // If item is a modification and the master note
                    // hasn't been created, then we need to process
                    // the master first.
                    if(note.getModifies()!=null) {
                        modifications.add(note);
                    }
                    else {
                        childrenToUpdate.add(note);
                    }
                }
            }
            
            // add modifications to end of set
            for(NoteItem mod: modifications) {
                childrenToUpdate.add(mod);
            }
            
            // update all children and collection
            collection = contentDao.updateCollection(collection, childrenToUpdate);
            
            // update timestamps on all collections involved
            for(CollectionItem lockedCollection : locks) {
               contentDao.updateCollectionTimestamp(lockedCollection);
            }
            
            // update timestamp on new collection
            collection = contentDao.updateCollectionTimestamp(collection);
            
            // get latest timestamp
            return collection;
            
        } finally {
           releaseLocks(locks);
        }
    }
    
    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if CollectionItem is locked
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        
        if (! lockManager.lockCollection(collection, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        
        try {
            return contentDao.updateCollection(collection);
        } finally {
            lockManager.unlockCollection(collection);
        }
    }

    
    /**
     * Update a collection and set of children.  The set of
     * children to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false to an existing Item.
     * 
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail immediately with a
     * <code>CollectionLockedException</code>.
     *
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     * @return updated collection
     * @throws CollectionLockedException if the collection is
     *         currently locked for an update.
     */
    public CollectionItem updateCollection(CollectionItem collection,
                                           Set<Item> updates) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating collection {}", collection.getUid());
        }

        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of updated items.
        Set<CollectionItem> locks = acquireLocks(collection, updates);
        
        try {
            Set<ContentItem> childrenToUpdate = new LinkedHashSet<ContentItem>();
            
            // Keep track of NoteItem modifications that need to be processed
            // after the master NoteItem.
            ArrayList<NoteItem> modifications = new ArrayList<NoteItem>(); 
            
            // Either create or update each item
            for (Item item : updates) {
                if (item instanceof NoteItem) {
                    
                    NoteItem note = (NoteItem) item;
                    
                    // If item is a modification and the master note
                    // hasn't been created, then we need to process
                    // the master first.
                    if(note.getModifies()!=null) {
                        modifications.add(note);
                    }
                    else {
                        childrenToUpdate.add(note);
                    }
                }
            }
            
            for(NoteItem mod: modifications) {
                // Only update modification if master has not been
                // deleted because master deletion will take care
                // of modification deletion.
                if(mod.getModifies().getIsActive()==true) {
                    childrenToUpdate.add(mod);
                }
            }
            checkDatesForEvents(childrenToUpdate);
            collection = contentDao.updateCollection(collection, childrenToUpdate);
            
            // update collections involved
            for(CollectionItem lockedCollection : locks) {
                lockedCollection = contentDao.updateCollectionTimestamp(lockedCollection);
                if(lockedCollection.getUid().equals(collection.getUid())) {
                    collection = lockedCollection;
                }
            }
            
            // get latest timestamp
            return collection;
            
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing collection {}", collection.getUid());
        }
        // Prevent HomeCollection from being removed (should only be removed when user is removed)
        if(collection instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        contentDao.removeCollection(collection);
    }

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
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating content item {} in {}", content.getName(), parent.getName());
        }
        checkDatesForEvent(content);
        // Obtain locks to all collections involved.
        Set<CollectionItem> locks = acquireLocks(parent, content);
        
        try {
            content = contentDao.createContent(parent, content);
            
            // update collections
            for(CollectionItem col : locks) {
                contentDao.updateCollectionTimestamp(col);
            }
            
            return content;
        } finally {
            releaseLocks(locks);
        }   
    }
    private void checkDatesForEvents(Collection<ContentItem> items){
        if(items == null){
            return;
        }
        for(ContentItem item : items){
            checkDatesForEvent(item);
        }
    }
    private void checkDatesForEvent(ContentItem item){
        if(!(item instanceof NoteItem)){
            return;
        }
        
        NoteItem noteItem = (NoteItem)item;
        Stamp stamp = noteItem.getStamp("event");
        
        if(!(stamp instanceof EventStamp)){
            return;
        }
        
        EventStamp eventStamp = (EventStamp) stamp;
        
        VEvent masterEvent = eventStamp.getMasterEvent();
        
        checkDatesForComponent(masterEvent);
        
        for(Component component : eventStamp.getExceptions()){
            checkDatesForComponent(component);
        }
    }
    
    private void checkDatesForComponent(Component component){
        if(component == null){
            return;
        }
        
        Property dtStart = component.getProperty(Property.DTSTART);
        Property dtEnd = component.getProperty(Property.DTEND);
        
        if( dtStart instanceof DtStart && dtStart.getValue()!= null 
            && dtEnd instanceof DtEnd && dtEnd.getValue() != null 
           && ((DtStart)dtStart).getDate().compareTo(((DtEnd)dtEnd).getDate()) > 0 ){
            throw new IllegalArgumentException("End date [" + dtEnd + " is lower than start date [" + dtStart + "]");
        }
        
    }
    
    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    @Override
    public void createContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating content items in {}", parent.getName());
        }
        
        checkDatesForEvents(contentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            for(ContentItem content : contentItems) {
                contentDao.createContent(parent, content);
            }
            
            contentDao.updateCollectionTimestamp(parent);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }

    @Override
    public void updateCollectionTimestamp(CollectionItem parent) {
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            contentDao.updateCollectionTimestamp(parent); 
            LOG.debug("collection timestamp updated");
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }

    @Override
    public void createBatchContentItems(CollectionItem parent, Set<ContentItem> contentItems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating content items in {}", parent.getName());
        }
        
        checkDatesForEvents(contentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            contentDao.createBatchContent(parent, contentItems);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }
    
    @Override
    public void updateBatchContentItems(CollectionItem parent, Set<ContentItem> contentItems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating content items in {}", parent.getName());
        }
        
        checkDatesForEvents(contentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            contentDao.updateBatchContent(contentItems);
            contentDao.updateCollectionTimestamp(parent);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }
    
    @Override
    public void removeBatchContentItems(CollectionItem parent, Set<ContentItem> contentItems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing content items in {}", parent.getName());
        }
        
        checkDatesForEvents(contentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            contentDao.removeBatchContent(parent, contentItems);
            contentDao.updateCollectionTimestamp(parent);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }

    
    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     * 
     * @param parent
     *            parents that new content items will be added to.
     * @param contentItems to update
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateContentItems(CollectionItem parent, Set<ContentItem> contentItems) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating content items");
        }
        checkDatesForEvents(contentItems);
        /*
         * Obtain locks to all collections involved. A collection is involved if it is the parent of one of updated
         * items.
         */
        Set<CollectionItem> locks = acquireLocks(contentItems);
        
        try {
            
           for(ContentItem content: contentItems) {
               if(content.getCreationDate()==null) {
                   contentDao.createContent(parent, content);
               }
               else if(Boolean.FALSE.equals(content.getIsActive())) {
                   contentDao.removeContent(content);
               }
               else {
                   contentDao.updateContent(content);
               }
           }
           
           // update collections
           for(CollectionItem collectionItem : locks) {
               contentDao.updateCollectionTimestamp(collectionItem);
           }
        } finally {
            releaseLocks(locks);
        }
    }
    

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem updateContent(ContentItem content) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating content item {}", content.getUid());
        }
        checkDatesForEvent(content);
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            content = contentDao.updateContent(content);
            
            // update collections
            for(CollectionItem parent : locks) {
                contentDao.updateCollectionTimestamp(parent);
            }
            
            return content;
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked           
     */
    public void removeContent(ContentItem content) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing content item {}", content.getUid());
        }
        
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            contentDao.removeContent(content);
            // update collections
            for(CollectionItem parent : locks) {
                contentDao.updateCollectionTimestamp(parent);
            }
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Find note items by triage status that belong to a collection.
     * @param collection collection
     * @param context the query context
     * @return set of notes that match the specified triage status label and
     *         belong to the specified collection
     */
    @Transactional(readOnly = true)
    public SortedSet<NoteItem> findNotesByTriageStatus(CollectionItem collection,
            TriageStatusQueryContext context) {
        return triageStatusQueryProcessor.processTriageStatusQuery(collection,
                context);
    }
    
    /**
     * Find note items by triage status that belong to a recurring note series.
     * @param note recurring note
     * @param context the query context
     * @return set of notes that match the specified triage status label and belong
     *         to the specified recurring note series
     */
    @Transactional(readOnly = true)
    public SortedSet<NoteItem> findNotesByTriageStatus(NoteItem note,
            TriageStatusQueryContext context) {
        return triageStatusQueryProcessor.processTriageStatusQuery(note,
                context);
    }
    
    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    @Transactional(readOnly = true)
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem) {
        return contentDao.findCollectionItems(collectionItem);
    }
    
    /**
     * Find items by filter.
     *
     * @param filter
     *            filter to use in search
     * @return set items matching specified
     *         filter.
     */
    @Transactional(readOnly = true)
    public Set<Item> findItems(ItemFilter filter) {
        return contentDao.findItems(filter);
    }

    /**
     * Creates a ticket on an item.
     *
     * @param item the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(Item item,
                             Ticket ticket) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating ticket on item {}", item.getUid());
        }
        contentDao.createTicket(item, ticket);
    }

    /**
     * Creates a ticket on an item.
     *
     * @param path the path of the item to be ticketed
     * @param ticket the ticket to be saved
     */
    public void createTicket(String path,
                             Ticket ticket) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating ticket on item at path {}", path);
        }
        Item item = contentDao.findItemByPath(path);
        if (item == null) {
            throw new IllegalArgumentException("item not found for path " + path);
        }
        contentDao.createTicket(item, ticket);
    }

    /**
     * Returns the identified ticket on the given item, or
     * <code>null</code> if the ticket does not exists. Tickets are
     * inherited, so if the specified item does not have the ticket
     * but an ancestor does, it will still be returned.
     *
     * @param item the ticketed item
     * @param key the ticket to return
     */
    @Transactional(readOnly = true)
    public Ticket getTicket(Item item,
                            String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting ticket {} for item {}", key, item.getUid());
        }
        return contentDao.getTicket(item, key);
    }
    
    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param ticket the ticket to remove
     */    
    public void removeTicket(Item item,
                             Ticket ticket) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing ticket {} on item {}",ticket.getKey(), item.getUid());
        }
        contentDao.removeTicket(item, ticket);
    }

    /**
     * Removes a ticket from an item.
     *
     * @param item the item to be de-ticketed
     * @param key the key of the ticket to remove
     */
    public void removeTicket(Item item,
                             String key) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing ticket {} on item {}", key, item.getUid());
        }
       
        if (item == null) {
            throw new IllegalArgumentException("item required");
        }
        
        Ticket ticket = contentDao.getTicket(item, key);
        if (ticket == null) {
            return;
        }
        contentDao.removeTicket(item, ticket);
    }


    /**
     * Given a set of items, aquire a lock on all parents
     */
    private Set<CollectionItem> acquireLocks(Set<? extends Item> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            for(Item child : children) {
                acquireLocks(locks, child);
            }
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    /**
     * Given a collection and a set of items, aquire a lock on the collection and
     * all 
     */
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Set<Item> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            if (! lockManager.lockCollection(collection, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            
            locks.add(collection);
            
            for(Item child : children) {
                acquireLocks(locks, child);
            }
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Item item) {
        HashSet<Item> items = new HashSet<Item>();
        items.add(item);
        
        return acquireLocks(collection, items);
    }
    
    private Set<CollectionItem> acquireLocks(Item item) {
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        try {
            acquireLocks(locks,item);
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private void acquireLocks(Set<CollectionItem> locks, Item item) {
        for(CollectionItem parent: item.getParents()) {
            if(locks.contains(parent)) {
                continue;
            }
            if (! lockManager.lockCollection(parent, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            locks.add(parent);
        }
        
        /*
         * Acquire locks on master item's parents, as an addition/deletion of a modifications item affects all the
         * parents of the master item.
         */ 
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null) {
                acquireLocks(locks, note.getModifies());
            }
        }
    }
    
    private void releaseLocks(Set<CollectionItem> locks) {
        for(CollectionItem lock : locks) {
            lockManager.unlockCollection(lock);
        }
    }
    
    private NoteOccurrence getNoteOccurrence(NoteItem parent, net.fortuna.ical4j.model.Date recurrenceId) {
        EventStamp eventStamp = StampUtils.getEventStamp(parent);
        
        // Parent must be a recurring event
        if(eventStamp==null || !eventStamp.isRecurring()) {
            return null;
        }
        RecurrenceId rid = null; 
        if(eventStamp.getEvent() != null && eventStamp.getEvent().getStartDate() != null){
        	DtStart startDate = eventStamp.getEvent().getStartDate();
        	rid = new RecurrenceId();
        	try {
				if(startDate.isUtc()){
					rid.setUtc(true);
				}else if(startDate.getTimeZone() != null){
					rid.setTimeZone(startDate.getTimeZone());
				}
				rid.setValue(recurrenceId.toString());
			} catch (ParseException e) {
				rid = null;
			}
        }
        net.fortuna.ical4j.model.Date recurrenceIdToUse = rid == null ? recurrenceId : rid.getDate();
        // Verify that occurrence date is valid
        RecurrenceExpander expander = new RecurrenceExpander();
        if(expander.isOccurrence(eventStamp.getEventCalendar(), recurrenceIdToUse)) {
            return NoteOccurrenceUtil.createNoteOccurrence(recurrenceIdToUse, parent);
        }
        
        return null;
    }

    @Override
    public void removeItemsFromCollection(CollectionItem collection) {
        if(collection instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        contentDao.removeItemsFromCollection(collection);
	contentDao.updateCollectionTimestamp(collection);
    }
}
