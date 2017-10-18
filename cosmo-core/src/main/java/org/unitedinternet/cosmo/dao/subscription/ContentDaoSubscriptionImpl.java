package org.unitedinternet.cosmo.dao.subscription;

import java.util.Date;
import java.util.Set;

import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.PathSegments;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscriptionItem;

/**
 * <code>ContentDao</code> implementation that manages the operations for <code>SubscriptionItem</code> collections.
 * 
 * @author daniel grigore
 * 
 * @see HibCollectionSubscriptionItem
 * @see CollectionSubscription
 */
public class ContentDaoSubscriptionImpl implements ContentDao {

    private final ContentDao contentDaoInternal;

    public ContentDaoSubscriptionImpl(ContentDao contentDaoInternal) {
        this.contentDaoInternal = contentDaoInternal;
    }

    @Override
    public Item findItemByUid(String uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid,
            Class<STAMP_TYPE> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item findItemByPath(String path) {
        PathSegments pathSegments = new PathSegments(path);
        String homeCollectionId = pathSegments.getHomeCollectionUid();
        String collectionId = pathSegments.getCollectionUid();
        String itemId = pathSegments.getEventUid();
        HibCollectionSubscriptionItem subscriptionItem = (HibCollectionSubscriptionItem) this.contentDaoInternal
                .findItemByPath(homeCollectionId + "/" + collectionId);
        if (itemId == null || itemId.trim().isEmpty()) {
            return subscriptionItem;
        }
        CollectionItem target = subscriptionItem.getTargetCollection();
        return target.getChildByName(itemId);
    }

    @Override
    public Item findItemByPath(String path, String parentUid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item findItemParentByPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HomeCollectionItem getRootItem(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HomeCollectionItem createRootItem(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyItem(Item item, String destPath, boolean deepCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveItem(String fromPath, String toPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItem(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItemByPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItemByUid(String uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createTicket(Item item, Ticket ticket) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Ticket> getTickets(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ticket findTicket(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ticket getTicket(Item item, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTicket(Item item, Ticket ticket) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addItemToCollection(Item item, CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(collection);
        this.contentDaoInternal.removeItemFromCollection(item, subscriptionItem.getTargetCollection());
    }

    @Override
    public void refreshItem(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initializeItem(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Item> findItems(ItemFilter filter) {
        if (filter.getParent() instanceof HibCollectionSubscriptionItem) {
            HibCollectionSubscriptionItem parent = (HibCollectionSubscriptionItem) filter.getParent();
            filter.setParent(parent.getTargetCollection());
        }
        return this.contentDaoInternal.findItems(filter);
    }

    @Override
    public Set<Item> findItems(ItemFilter[] filters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String generateUid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionItem createCollection(CollectionItem parent, CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionItem updateCollection(CollectionItem collection, Set<ContentItem> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionItem updateCollection(CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentItem createContent(CollectionItem parent, ContentItem content) {
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(parent);
        // Create collection in sharer's calendar.         
        CollectionItem parentCollection = subscriptionItem.getTargetCollection();
        content.setOwner(parentCollection.getOwner());
        return this.contentDaoInternal.createContent(parentCollection, content);
    }
    
    private HibCollectionSubscriptionItem checkAndGetSubscriptionItem(CollectionItem parent) {
        if (!(parent instanceof HibCollectionSubscriptionItem)) {
            throw new CaldavExceptionForbidden("invalid subscription type " + parent.getClass());
        }
        HibCollectionSubscriptionItem subscriptionItem = (HibCollectionSubscriptionItem) parent;
        if (subscriptionItem.getTargetCollection() == null) {
            throw new CaldavExceptionForbidden("invalid subscription collection with uid " + subscriptionItem.getUid());
        }
        return subscriptionItem;
    }

    @Override
    public void createBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBatchContent(Set<ContentItem> contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentItem updateContent(ContentItem content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeContent(ContentItem content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUserContent(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCollection(CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ContentItem> loadChildren(CollectionItem collection, Date timestamp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItemsFromCollection(CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

}
