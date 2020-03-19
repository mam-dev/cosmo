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
package org.unitedinternet.cosmo.security.aop;

import java.util.Date;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.aop.OrderedAdvice;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscriptionItem;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.util.SecurityHelper;
import org.unitedinternet.cosmo.service.triage.TriageStatusQueryContext;

/**
 * Security Advice for determining access to service methods. By default service methods are not secured. To secure a
 * method, add a pointcut that matches the method. Any method that is not secured will be logged as insecure.
 */
@Aspect
@Configuration
public class SecurityAdvice extends OrderedAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAdvice.class);

    private final CosmoSecurityManager securityManager;    
    private SecurityHelper securityHelper;
    
    
    public SecurityAdvice(CosmoSecurityManager securityManager, ContentDao contentDao, UserDao userDao) {
        super();
        this.securityManager = securityManager;        
        this.securityHelper = new SecurityHelper(contentDao, userDao);
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.getRootItem(..)) &&" + "args(user)")
    public Object checkGetRootItem(ProceedingJoinPoint pjp, User user) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkGetRootItem(user)");
        }
        if (!securityHelper.hasUserAccess(securityManager.getSecurityContext(), user)) {
            throw new CosmoSecurityException("principal does not have access to user " + user.getUid());
        }
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findItemByUid(..)) &&" + "args(uid)")
    public Object checkFindItemByUid(ProceedingJoinPoint pjp, String uid) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindItemByUid(uid)");
        }
        Item item = (Item) pjp.proceed();
        if (item != null && !securityHelper.hasReadAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.READ);
        }

        return item;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findItemByPath(..)) &&" + "args(path)")
    public Object checkFindItemByPath(ProceedingJoinPoint pjp, String path) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindItemByPath(path)");
        }

        Item item = (Item) pjp.proceed();
        if (item != null && !securityHelper.hasReadAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.READ);
        }

        return item;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findItemByPath(..)) &&"
            + "args(path, parentUid)")
    public Object checkFindItemByPathAndParent(ProceedingJoinPoint pjp, String path, String parentUid)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindItemByPathAndParent(path,parentUid)");
        }

        Item item = (Item) pjp.proceed();
        if (item != null && !securityHelper.hasReadAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.READ);
        }

        return item;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findItemParentByPath(..)) &&" + "args(path)")
    public Object checkFindItemParentByPath(ProceedingJoinPoint pjp, String path) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindItemParentByPath(path)");
        }

        Item item = (Item) pjp.proceed();
        if (item != null && !securityHelper.hasReadAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.READ);
        }

        return item;
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.addItemToCollection(..)) &&"
            + "args(item, collection)")
    public Object checkAddItemToCollection(ProceedingJoinPoint pjp, Item item, CollectionItem collection)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkAddItemToCollection(item, collection)");
        }
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(collection);
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.WRITE);
        }
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItem(..)) &&" + "args(item)")
    public Object checkRemoveItem(ProceedingJoinPoint pjp, Item item) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveItem(item)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.WRITE);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeItemFromCollection(..)) &&"
            + "args(item, collection)")
    public Object checkRemoveItemFromCollection(ProceedingJoinPoint pjp, Item item, CollectionItem collection)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveItemFromCollection(item, collection)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(collection);
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.WRITE);
        }
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.loadChildren(..)) &&"
            + "args(collection, date)")
    public Object checkLoadChildren(ProceedingJoinPoint pjp, CollectionItem collection, Date date) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkLoadChildren(collection, date)");
        }

        if (!securityHelper.hasReadAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.READ);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createCollection(..)) &&"
            + "args(parent, collection)")
    public Object checkCreateCollection(ProceedingJoinPoint pjp, CollectionItem parent, CollectionItem collection)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCreateCollection(parent, collection)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createCollection(..)) &&"
            + "args(parent, collection, children)")
    public Object checkCreateCollection(ProceedingJoinPoint pjp, CollectionItem parent, CollectionItem collection,
            Set<Item> children) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCreateCollection(parent, collection, children)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }

        for (Item child : children) {
            // existing items
            if (child.getCreationDate() != null
                    && !securityHelper.hasWriteAccess(securityManager.getSecurityContext(), child)) {
                throwItemSecurityException(child, Permission.WRITE);
            }
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateCollection(..)) &&"
            + "args(collection, children)")
    public Object checkUpdateCollection(ProceedingJoinPoint pjp, CollectionItem collection, Set<Item> children)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkUpdateCollection(collection, children)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.WRITE);
        }

        for (Item child : children) {
            // existing items
            if (child.getCreationDate() != null
                    && !securityHelper.hasWriteAccess(securityManager.getSecurityContext(), child)) {
                throwItemSecurityException(child, Permission.WRITE);
            }
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeCollection(..)) &&" + "args(collection)")
    public Object checkRemoveCollection(ProceedingJoinPoint pjp, CollectionItem collection) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveCollection(collection)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.WRITE);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateCollection(..)) &&" + "args(collection)")
    public Object checkUpdateCollection(ProceedingJoinPoint pjp, CollectionItem collection) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkUpdateCollection(collection)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.WRITE);
        }

        return pjp.proceed();
    }
    
    //Batch operations start
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object checkCreateBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {        
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(parent);        
        return pjp.proceed();
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object checkUpdateBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {        
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(parent);        
        return pjp.proceed();
    }
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeBatchContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object checkRemoveBatchContentItems(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {        
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(parent);        
        return pjp.proceed();
    }

    // Batch operations end
    
    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.copyItem(..)) &&"
            + "args(item, targetParent, path, deepCopy)")
    public Object checkCopyItem(ProceedingJoinPoint pjp, Item item, CollectionItem targetParent, String path,
            boolean deepCopy) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCopyItem(item, targetParent, path, deepCopy)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), targetParent)) {
            throwItemSecurityException(targetParent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(targetParent);
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.moveItem(..)) &&"
            + "args(item, oldParent, newParent)")
    public Object checkMoveItem(ProceedingJoinPoint pjp, Item item, CollectionItem oldParent, CollectionItem newParent)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkMoveItem(item, oldParent, newParent)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), item)) {
            throwItemSecurityException(item, Permission.WRITE);
        }
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), oldParent)) {
            throwItemSecurityException(oldParent, Permission.WRITE);
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), newParent)) {
            throwItemSecurityException(newParent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(newParent);
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContent(..)) &&"
            + "args(parent, content)")
    public Object checkCreateContent(ProceedingJoinPoint pjp, CollectionItem parent, ContentItem content)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCreateContent(parent, content)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(parent);
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object checkCreateContentItems(ProceedingJoinPoint pjp, CollectionItem parent, Set<ContentItem> contentItems)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCreateContent(parent, contentItems)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), parent)) {
            throwItemSecurityException(parent, Permission.WRITE);
        }
        this.checkWriteAccessToTargetCollection(parent);
        return pjp.proceed();
    }

    /**
     * Checks if specified collection is of type <code>subscription</code> and then checks access to the target
     * collection.
     * 
     * @param collection
     * @throws ItemSecurityException
     *             in case user does not have access to target collection.
     */
    private void checkWriteAccessToTargetCollection(CollectionItem collection) {
        if (collection instanceof HibCollectionSubscriptionItem) {
            HibCollectionSubscriptionItem subscription = (HibCollectionSubscriptionItem) collection;
            if (subscription.getTargetCollection() == null || !securityHelper.hasWriteAccess(securityManager.getSecurityContext(),
                    subscription.getTargetCollection())) {
                throwItemSecurityException(subscription, Permission.WRITE);
            }
        }
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateContent(..)) &&" + "args(content)")
    public Object checkUpdateContent(ProceedingJoinPoint pjp, ContentItem content) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkUpdateContent(content)");
        }

        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), content)) {
            throwItemSecurityException(content, Permission.WRITE);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.updateContentItems(..)) &&"
            + "args(parent, contentItems)")
    public Object checkUpdateContentItems(ProceedingJoinPoint pjp, CollectionItem parent,
            Set<ContentItem> contentItems) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkUpdateContentItems(parent, contentItems)");
        }

        CosmoSecurityContext context = securityManager.getSecurityContext();
        for (ContentItem content : contentItems) {
            // existing items
            if (content.getCreationDate() != null) {
                if (!securityHelper.hasWriteAccess(context, content)) {
                    throwItemSecurityException(content, Permission.WRITE);
                }
            }
            // new items
            else {
                // NoteMods require write access to the master (which should be checked)
                if (isNoteMod(content)) {
                    NoteItem mod = (NoteItem) content;
                    NoteItem master = mod.getModifies();

                    // if master is included in set, ignore, otherwise
                    // check master
                    if (contentItems.contains(master)) {
                        continue;
                    } else if (!securityHelper.hasWriteAccess(context, master)) {
                        throwItemSecurityException(master, Permission.WRITE);
                    }
                } else {
                    // item is new so check access to parent
                    if (!securityHelper.hasWriteAccess(context, parent)) {
                        throwItemSecurityException(parent, Permission.WRITE);
                    }
                }
            }
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeContent(..)) &&" + "args(content)")
    public Object checkRemoveContent(ProceedingJoinPoint pjp, ContentItem content) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveContent(content)");
        }
        if (!securityHelper.hasWriteAccess(securityManager.getSecurityContext(), content)) {
            throwItemSecurityException(content, Permission.WRITE);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findItems(..)) &&" + "args(filter)")
    public Object checkFindItems(ProceedingJoinPoint pjp, ItemFilter filter) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindItems(filter)");
        }
        if (!securityHelper.hasAccessToFilter(securityManager.getSecurityContext(), filter)) {
            throw new CosmoSecurityException("principal does not have access to use filter " + filter.toString());
        }
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findNotesByTriageStatus(..)) &&"
            + "args(collection, context)")
    public Object checkFindNotesByTriageStatus(ProceedingJoinPoint pjp, CollectionItem collection,
            TriageStatusQueryContext context) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindNotesByTriageStatus(collection, context)");
        }

        if (!securityHelper.hasReadAccess(securityManager.getSecurityContext(), collection)) {
            throwItemSecurityException(collection, Permission.READ);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.findNotesByTriageStatus(..)) &&"
            + "args(note, context)")
    public Object checkFindNotesByTriageStatus(ProceedingJoinPoint pjp, NoteItem note, TriageStatusQueryContext context)
            throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkFindNotesByTriageStatus(note, context)");
        }

        if (!securityHelper.hasReadAccess(securityManager.getSecurityContext(), note)) {
            throwItemSecurityException(note, Permission.READ);
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.createTicket(..)) &&" + "args(item, ticket)")
    public Object checkCreatTicket(ProceedingJoinPoint pjp, Item item, Ticket ticket) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkCreatTicket(item, ticket)");
        }
        if (!securityHelper.hasWriteTicketAccess(securityManager.getSecurityContext(), item)) {
            throw new CosmoSecurityException("principal does not have access to add tickets to item " + item.getUid());
        }
        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeTicket(..)) &&" + "args(item, ticket)")
    public Object checkRemoveTicket(ProceedingJoinPoint pjp, Item item, Ticket ticket) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveTicket(item, ticket)");
        }

        if (!securityHelper.hasWriteTicketAccess(securityManager.getSecurityContext(), item)) {
            throw new CosmoSecurityException(
                    "principal does not have access to remove tickets from item " + item.getUid());
        }

        return pjp.proceed();
    }

    @Around("execution(* org.unitedinternet.cosmo.service.ContentService.removeTicket(..)) &&" + "args(item, key)")
    public Object checkRemoveTicketKey(ProceedingJoinPoint pjp, Item item, String key) throws Throwable {
        if (LOG.isDebugEnabled()) {
            LOG.debug("in checkRemoveTicketKey(item, key)");
        }

        if (!securityHelper.hasWriteTicketAccess(securityManager.getSecurityContext(), item)) {
            throw new CosmoSecurityException(
                    "principal does not have access to remove tickets from item " + item.getUid());
        }
        return pjp.proceed();
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    private boolean isNoteMod(Item item) {
        if (item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            return note.getModifies() != null;
        }

        return false;
    }

    private void throwItemSecurityException(Item item, int permission) {
        throw new ItemSecurityException(item, "principal does not have access to item " + item.getUid(), permission);
    }
}
