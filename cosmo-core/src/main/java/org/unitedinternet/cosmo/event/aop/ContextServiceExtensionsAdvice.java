/*
 * ContextServiceExtensionsAdvice.java Jun 13, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.event.aop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.service.interceptors.CalendarGetHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionCreateHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionDeleteHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionUpdateHandler;
import org.unitedinternet.cosmo.service.interceptors.EventAddHandler;
import org.unitedinternet.cosmo.service.interceptors.EventMoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventRemoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventUpdateHandler;

/**
 * 
 * Hook into event operations. External providers must add handlers for add/update/delete operations for improving
 * adding auxiliary operations.
 * 
 * @author ccoman, izidaru
 * 
 */
@Aspect
@Configuration
@Transactional
public class ContextServiceExtensionsAdvice {
   
    private static final Logger LOG = LoggerFactory.getLogger(ContextServiceExtensionsAdvice.class);

    @Autowired(required = false)
    private List<EventAddHandler> addHandlers = new ArrayList<>();

    @Autowired(required = false)
    private List<EventRemoveHandler> removeHandlers = new ArrayList<>();

    @Autowired(required = false)
    private List<EventUpdateHandler> updateHandlers = new ArrayList<>();

    @Autowired(required = false)
    private List<EventMoveHandler> moveHandlers = new ArrayList<>();

    // Feature activator.
    @Autowired(required = false)
    private List<CollectionCreateHandler> createHandlers = new ArrayList<>();
    // Deleting default calendar prohibited.
    @Autowired(required = false)
    private List<CollectionDeleteHandler> deleteHandlers = new ArrayList<>();

    @Autowired(required = false)
    private List<CollectionUpdateHandler> updateCollectionHandlers = new ArrayList<>();

    @Autowired(required = false)
    private List<CalendarGetHandler> calendarGetHandlers = new ArrayList<>();

    /**
     * Default constructor
     */
    public ContextServiceExtensionsAdvice() {

    }

    /**
     * Method called when events are imported.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object createBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {

        if (LOG.isDebugEnabled()) {
            LOG.debug("in createContent(parent, contentItems)");
        }

        return handleCreateContentsItemsInternal(pjp, parent, contentItems);
    }

    /**
     * Method called when an event is added.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object createContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in createContent(parent, contentItems)");
        }

        return handleCreateContentsItemsInternal(pjp, parent, contentItems);
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContent(..)) &&"
            + "args(parent, content)")
    public Object createContent(ProceedingJoinPoint pjp, CollectionItem parent, ContentItem content) throws Throwable {
        Set<ContentItem> contentItems = new HashSet<ContentItem>();
        contentItems.add(content);

        if (LOG.isDebugEnabled()) {
            LOG.debug("in createContent(parent, contentItems)");
        }

        return handleCreateContentsItemsInternal(pjp, parent, contentItems);
    }

    /**
     * @param pjp
     * @param parent
     * @param contentItems
     * @return
     * @throws Throwable
     */
    private Object handleCreateContentsItemsInternal(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {
        Object returnVal = null;
        // nothing to do
        if (addHandlers == null || addHandlers.size() == 0) {
            return pjp.proceed();
        }

        for (EventAddHandler eventAdd : addHandlers) {
            eventAdd.beforeAdd(parent, contentItems);
        }

        returnVal = pjp.proceed();

        for (EventAddHandler eventAdd : addHandlers) {
            eventAdd.afterAdd(parent, contentItems);
        }
        return returnVal;
    }

    /**
     * Method called when an event is removed.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItemFromCollection(..)) &&"
            + "args(item, collection)")
    public Object removeItemFromCollection(ProceedingJoinPoint pjp, Item item, CollectionItem collection)
            throws Throwable {
        Set<Item> items = new HashSet<Item>();
        items.add(item);

        if (LOG.isDebugEnabled()) {
            LOG.debug("in removeItemFromCollection(item, collection)");
        }

        return removeItemsFromCollectionInternal(pjp, collection, items);
    }

    /**
     * Method called when an event is removed.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object removeBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in removeItemFromCollection(item, collection)");
        }
        Set<Item> items = new HashSet<Item>();
        items.addAll(contentItems);

        return removeItemsFromCollectionInternal(pjp, parent, items);
    }

    /**
     * Method called when an event is removed.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItemsFromCollection(..)) &&"
            + "args(collection)")
    public Object removeItemsFromCollection(ProceedingJoinPoint pjp, CollectionItem collection) throws Throwable {
        Set<Item> items = collection.getChildren();

        if (LOG.isDebugEnabled()) {
            LOG.debug("in removeItemsFromCollection(item)");
        }

        return removeItemsFromCollectionInternal(pjp, collection, items);
    }

    /**
     * @param pjp
     * @param collection
     * @param items
     * @return
     * @throws Throwable
     */
    private Object removeItemsFromCollectionInternal(ProceedingJoinPoint pjp, CollectionItem collection,
            Set<Item> items) throws Throwable {
        Object returnVal = null;

        // nothing to do
        if (removeHandlers == null || removeHandlers.size() == 0) {
            return pjp.proceed();
        }

        for (EventRemoveHandler eventRemove : removeHandlers) {
            eventRemove.beforeRemove(collection, items);
        }

        returnVal = pjp.proceed();

        for (EventRemoveHandler eventRemove : removeHandlers) {
            eventRemove.afterRemove(collection, items);
        }

        return returnVal;
    }

    /**
     * Method called when an event is updated.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object updateContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {

        if (LOG.isDebugEnabled()) {
            LOG.debug("in updateContentItems(parents, contentItems)");
        }

        return updateContentItemsIternal(pjp, parent, contentItems);
    }

    /**
     * Method called when an event is updated.
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object updateBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in updateContentItems(parents, contentItems)");
        }

        return updateContentItemsIternal(pjp, parent, contentItems);
    }

    /**
     * @param pjp
     *            ProceedingJoinPoint
     * @param parent
     *            Set<CollectionItem>
     * @param contentItems
     *            Set<ContentItem>
     * @return Object
     * @throws Throwable
     *             Exception
     */
    private Object updateContentItemsIternal(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {
        Object returnVal = null;
        // nothing to do
        if (updateHandlers == null || updateHandlers.size() == 0) {
            return pjp.proceed();
        }

        for (EventUpdateHandler eventUpdate : updateHandlers) {
            eventUpdate.beforeUpdate(parent, contentItems);
        }

        returnVal = pjp.proceed();

        for (EventUpdateHandler eventUpdate : updateHandlers) {
            eventUpdate.afterUpdate(parent, contentItems);
        }
        return returnVal;
    }

    /**
     * If mkcalendar feature is not activated, then an exception is thrown, else it go on.
     * 
     * @throws Throwable
     */
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createCollection(..)) &&"
            + "args(parent, collection)")
    public CollectionItem createCollection(ProceedingJoinPoint pjp, CollectionItem parent, CollectionItem collection)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In  ContextServiceExtensionsAdvice: createCollection(parent, collection, children)");
        }

        for (CollectionCreateHandler handler : this.createHandlers) {
            handler.beforeCreateCollection(collection);
        }
        CollectionItem toReturn = (CollectionItem) pjp.proceed();
        for (CollectionCreateHandler handler : this.createHandlers) {
            handler.afterCreateCollection(collection);
        }
        return toReturn;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeCollection(..)) &&" + "args(collection)")
    public CollectionItem removeCollection(ProceedingJoinPoint pjp, CollectionItem collection) throws Throwable {
        Object returnVal = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("In  ContextServiceExtensionsAdvice: removeCollection(item) {}", collection.getUid());
        }

        for (CollectionDeleteHandler collectionDelete : deleteHandlers) {
            collectionDelete.beforeDeleteCollection(collection.getName());
        }

        returnVal = pjp.proceed();

        for (CollectionDeleteHandler collectionDelete : deleteHandlers) {
            collectionDelete.afterDeleteCollection(collection.getName());
        }

        return (CollectionItem) returnVal;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateCollection(..)) &&" + "args(collection)")
    public CollectionItem updateCollection(ProceedingJoinPoint pjp, CollectionItem collection) throws Throwable {
        Object returnVal = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("In  ContextServiceExtensionsAdvice: removeCollection(item) {}", collection.getUid());
        }

        for (CollectionUpdateHandler collectionUpdate : updateCollectionHandlers) {
            collectionUpdate.beforeUpdateCollection(collection);
        }

        returnVal = pjp.proceed();
        return (CollectionItem) returnVal;
    }

    @AfterReturning(pointcut = "execution(org.unitedinternet.cosmo.model.Item org.unitedinternet.cosmo.service.ContentService.findItemBy*(..))", returning = "item")
    public void afterFindItemBy(Item item) throws Throwable {
        for (CalendarGetHandler handler : this.calendarGetHandlers) {
            handler.afterGet(item);
        }
    }

    @Before(value = "execution(void org.unitedinternet.cosmo.service.ContentService.moveItem(..)) && "
            + "args(item, oldParent, newParent)")
    public void afterMoveItem(Item item, CollectionItem oldParent, CollectionItem newParent) {
        for (EventMoveHandler handler : this.moveHandlers) {
            handler.beforeMove(item, oldParent, newParent);
        }
    }

    public void setAddHandlers(List<EventAddHandler> addHandlers) {
        this.addHandlers = addHandlers;
    }

    public void setRemoveHandlers(List<EventRemoveHandler> removeHandlers) {
        this.removeHandlers = removeHandlers;
    }

    public void setUpdateHandlers(List<EventUpdateHandler> updateHandlers) {
        this.updateHandlers = updateHandlers;
    }

    public void setCreateHandlers(List<CollectionCreateHandler> createHandlers) {
        this.createHandlers = createHandlers;
    }

    public void setDeleteHandlers(List<CollectionDeleteHandler> deleteHandlers) {
        this.deleteHandlers = deleteHandlers;
    }

    public void setUpdateCollectionHandlers(List<CollectionUpdateHandler> updateCollectionHandlers) {
        this.updateCollectionHandlers = updateCollectionHandlers;
    }

    public void setCalendarGetHandlers(List<CalendarGetHandler> calendarGetHandlers) {
        this.calendarGetHandlers = calendarGetHandlers;
    }

    public void setMoveHandlers(List<EventMoveHandler> moveHandlers) {
        this.moveHandlers = moveHandlers;
    }

}
