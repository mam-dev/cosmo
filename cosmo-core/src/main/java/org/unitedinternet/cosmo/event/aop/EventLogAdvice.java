/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.event.aop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.unitedinternet.cosmo.aop.OrderedAdvice;
import org.unitedinternet.cosmo.dao.EventLogDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.event.EventLogEntry;
import org.unitedinternet.cosmo.model.event.ItemAddedEntry;
import org.unitedinternet.cosmo.model.event.ItemEntry;
import org.unitedinternet.cosmo.model.event.ItemRemovedEntry;
import org.unitedinternet.cosmo.model.event.ItemUpdatedEntry;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

/**
 * Advice for generating event log entries for
 * updates to ContentItems.
 */
@Aspect
public class EventLogAdvice extends OrderedAdvice {

    private boolean enabled = true;
    private CosmoSecurityManager securityManager = null;
    private EventLogDao eventLogDao = null;
  
    private static final Log LOG =
        LogFactory.getLog(EventLogAdvice.class);
    
    public void init() {
        if(eventLogDao==null) {
            throw new IllegalStateException("eventLogDao must not be null");
        }
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.addItemToCollection(..)) &&"
            + "args(item, collection)")
    public Object addItemToCollection(ProceedingJoinPoint pjp,
            Item  item, CollectionItem collection) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in addItemToCollection(item, collection)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        // for now only care about content items
        if(!(item instanceof ContentItem)) {
            return pjp.proceed();
        }
        
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntry(createItemAddedEntry(collection, item));
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItem(..)) &&"
            + "args(item)")
    public Object removeItem(ProceedingJoinPoint pjp,
            Item  item) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in removeItem(item)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        // for now only care about content items
        if(!(item instanceof ContentItem)) {
            return pjp.proceed();
        }
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        
        for(CollectionItem parent: item.getParents()) {
            entries.add(createItemRemovedEntry(parent, item));
        }
        
        Object returnValue = pjp.proceed();
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnValue;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItemFromCollection(..)) &&"
            + "args(item, collection)")
    public Object removeItemFromCollection(ProceedingJoinPoint pjp,
            Item  item, CollectionItem collection) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in removeItemFromCollection(item, collection)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        // for now only care about content items
        if(!(item instanceof ContentItem)) {
            return pjp.proceed();
        }
        
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntry(createItemRemovedEntry(collection, item));
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createCollection(..)) &&"
            + "args(parent, collection, children)")
    public Object createCollection(ProceedingJoinPoint pjp,
            CollectionItem parent, CollectionItem collection, Set<Item> children) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in createCollection(parent, collection, children)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
       
        // create update and added record for each item
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        for(Item child: children) {
            
            // for now only care about content items
            if(!(child instanceof ContentItem)) {
                continue;
            }
            
            // existing items are updated in existing collections
            if(child.getCreationDate()!=null) {
                entries.addAll(createItemUpdatedEntries(child));
            } 
            
            // added to new collection
            entries.add(createItemAddedEntry(collection, child));
        }
        
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateCollection(..)) &&"
            + "args(collection, children)")
    public Object updateCollection(ProceedingJoinPoint pjp,
            CollectionItem collection, Set<Item> children) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in updateCollection(collection, children)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        for(Item child: children) {
            
            // for now only care about content items
            if(!(child instanceof ContentItem)) {
                continue;
            }
            
            // removed items
            if(Boolean.FALSE.equals(child.getIsActive())) {
               entries.add(createItemRemovedEntry(collection, child));
            } else {
                // existing items are updated
                if(child.getCreationDate()!=null) {
                    entries.addAll(createItemUpdatedEntries(child));
                    // if item isn't in collection it is gets 
                    // and "added" entry
                    if(!child.getParents().contains(collection)) {
                        entries.add(createItemAddedEntry(collection, child));
                    }
                } else {
                    entries.add(createItemAddedEntry(collection, child));
                }
            }
        }
        
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnVal;
    }
    
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContent(..)) &&"
            + "args(parent, content)")
    public Object createContent(ProceedingJoinPoint pjp,
            CollectionItem parent, ContentItem content) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in createContent(parent, content)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
       
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntry(createItemAddedEntry(parent, content));
     
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object createContentItems(ProceedingJoinPoint pjp,
            CollectionItem parent, Set<ContentItem> contentItems) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in createContent(parent, contentItems)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        for(Item child: contentItems) {
            entries.add(createItemAddedEntry(parent, child));
        }
        
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateContent(..)) &&"
            + "args(content)")
    public Object updateContent(ProceedingJoinPoint pjp,
            ContentItem content) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in updateContent(content)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
       
        Object returnVal = pjp.proceed();
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();
        entries.addAll(createItemUpdatedEntries(content));
        eventLogDao.addEventLogEntries(entries);
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateContentItems(..)) &&"
            + "args(parents, contentItems)")
    public Object updateContentItems(ProceedingJoinPoint pjp,
            Set<CollectionItem> parents, Set<ContentItem> contentItems) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in updateContentItems(parents, contentItems)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();    
        for(ContentItem content: contentItems) {
           if(Boolean.FALSE.equals(content.getIsActive())) {
               for(CollectionItem parent: content.getParents()) {
                   entries.add(createItemRemovedEntry(parent, content));
               }
           } else {
               if(content.getCreationDate()==null) {
                   for(CollectionItem parent: parents) {
                       entries.add(createItemAddedEntry(parent, content));
                   }
               }
               else {
                   entries.addAll(createItemUpdatedEntries(content));
               }
           }
               
        }
       
        Object returnVal = pjp.proceed();
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnVal;
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeContent(..)) &&"
            + "args(content)")
    public Object removeContent(ProceedingJoinPoint pjp,
            ContentItem content) throws Throwable {
        if(LOG.isDebugEnabled()) {
            LOG.debug("in removeContent(content)");
        }
        if (!enabled) {
            return pjp.proceed();
        }
        
        HashSet<CollectionItem> parents = new HashSet<CollectionItem>();
        parents.addAll(content.getParents());
        
        Object returnValue = pjp.proceed();
        
        ArrayList<EventLogEntry> entries = new ArrayList<EventLogEntry>();    
        for(CollectionItem parent: parents) {
            entries.add(createItemRemovedEntry(parent, content));
        }
        
        eventLogDao.addEventLogEntries(entries);
        
        return returnValue;
    }
    
    protected ItemAddedEntry createItemAddedEntry(CollectionItem collection, Item item) {
        ItemAddedEntry entry = new ItemAddedEntry(item, collection);
        setBaseEntryProps(entry);
        return entry;
    }
    
    protected List<ItemUpdatedEntry> createItemUpdatedEntries(Item item) {
        ArrayList<ItemUpdatedEntry> entries = new ArrayList<ItemUpdatedEntry>();
        for(CollectionItem parent: item.getParents()) {
            ItemUpdatedEntry entry = new ItemUpdatedEntry(item, parent);
            setBaseEntryProps(entry);
            entries.add(entry);
        }

        return entries;
    }
    
    protected ItemRemovedEntry createItemRemovedEntry(CollectionItem collection, Item item) {
        ItemRemovedEntry entry = new ItemRemovedEntry(item, collection);
        setBaseEntryProps(entry);
        return entry;
    }
    
    public void setEventLogDao(EventLogDao eventLogDao) {
        this.eventLogDao = eventLogDao;
    }

    protected void setBaseEntryProps(ItemEntry entry) {
        CosmoSecurityContext context = securityManager.getSecurityContext();
        if(context.getUser()!=null) {
            entry.setUser(context.getUser());
        }
        else {
            entry.setTicket(context.getTicket());
        }
    }
   
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
