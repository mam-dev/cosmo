package org.unitedinternet.cosmo.dao.subscription;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.PathSegments;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
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
@Repository
public class ContentDaoSubscriptionImpl implements ContentDao {

    private final ContentDaoImpl contentDaoInternal;

    private final FreeBusyObfuscater freeBusyObfuscater;

    @PersistenceContext
    private EntityManager em;

    public ContentDaoSubscriptionImpl(ContentDaoImpl contentDaoInternal, FreeBusyObfuscater freeBusyObfuscater) {
        super();
        this.contentDaoInternal = contentDaoInternal;
        this.freeBusyObfuscater = freeBusyObfuscater;
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
    @Transactional(readOnly = true)
    public Item findItemByPath(String path) {
        PathSegments pathSegments = new PathSegments(path);
        String homeCollectionId = pathSegments.getHomeCollectionUid();
        String collectionId = pathSegments.getCollectionUid();
        String itemId = pathSegments.getEventUid();
        HibCollectionSubscriptionItem subscriptionItem = (HibCollectionSubscriptionItem) this.contentDaoInternal
                .findItemByPath(homeCollectionId + "/" + collectionId);
        if (itemId == null || itemId.trim().isEmpty()) {
            return obfuscate(subscriptionItem);
        }
        if (subscriptionItem != null) {
            CollectionItem target = subscriptionItem.getTargetCollection();
            return target != null ? obfuscate(subscriptionItem, target.getChildByName(itemId)) : null;
        }
        return null;
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
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(collection);
        if (subscriptionItem.getTargetCollection() != null) {
            this.contentDaoInternal.addItemToCollection(item, subscriptionItem.getTargetCollection());
        }
    }

    @Override
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(collection);
        if (subscriptionItem.getTargetCollection() != null) {
            this.contentDaoInternal.removeItemFromCollection(item, subscriptionItem.getTargetCollection());
        }
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
    @Transactional(readOnly = true)
    public Set<Item> findItems(ItemFilter filter) {
        if (filter.getParent() instanceof HibCollectionSubscriptionItem) {
            HibCollectionSubscriptionItem parent = (HibCollectionSubscriptionItem) filter.getParent();
            if (parent.getTargetCollection() == null) {
                return Collections.emptySet();
            }
            filter.setParent(parent.getTargetCollection());
            return obfuscate(parent, this.contentDaoInternal.findItems(filter));
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
        // Create content in sharer's calendar.
        CollectionItem parentCollection = subscriptionItem.getTargetCollection();
        if (parentCollection == null) {
            throw new CaldavExceptionForbidden("invalid subscription");
        }
        content.setOwner(parentCollection.getOwner());
        return this.contentDaoInternal.createContent(parentCollection, content);
    }

    private HibCollectionSubscriptionItem checkAndGetSubscriptionItem(CollectionItem parent) {
        if (!(parent instanceof HibCollectionSubscriptionItem)) {
            throw new CaldavExceptionForbidden("invalid subscription type " + parent.getClass());
        }
        return (HibCollectionSubscriptionItem) parent;
    }

    @Override
    public void createBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(parent);
        // Create contents in sharer's calendar.
        CollectionItem parentCollection = subscriptionItem.getTargetCollection();
        if (parentCollection == null) {
            throw new CaldavExceptionForbidden("invalid subscription");
        }
        for (ContentItem content : contents) {
            content.setOwner(parentCollection.getOwner());
        }
        this.contentDaoInternal.createBatchContent(parentCollection, contents);
    }

    @Override
    public void updateBatchContent(Set<ContentItem> contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        HibCollectionSubscriptionItem subscriptionItem = this.checkAndGetSubscriptionItem(parent);
        // Create contents in sharer's calendar.
        CollectionItem parentCollection = subscriptionItem.getTargetCollection();
        if (parentCollection == null) {
            throw new CaldavExceptionForbidden("invalid subscription");
        }
        this.contentDaoInternal.removeBatchContent(parentCollection, contents);
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

    @Override
    public long countItems(long ownerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countItems(long ownerId, long fromTimestamp) {
        throw new UnsupportedOperationException();
    }

    private HibCollectionSubscriptionItem obfuscate(HibCollectionSubscriptionItem parent) {
        this.obfuscate(parent, parent.getChildren());
        return parent;
    }

    private Set<Item> obfuscate(HibCollectionSubscriptionItem parent, Set<Item> items) {
        if (isFreeBusy(parent)) {
            for (Item item : items) {
                if (item instanceof ContentItem) {
                    this.freeBusyObfuscater.apply(parent.getOwner(), (ContentItem) item);
                }
            }
        }
        return items;
    }

    private Item obfuscate(HibCollectionSubscriptionItem parent, Item item) {
        if (isFreeBusy(parent) && item instanceof ContentItem) {
            this.freeBusyObfuscater.apply(parent.getOwner(), (ContentItem) item);
        }
        return item;
    }

    private static boolean isFreeBusy(HibCollectionSubscriptionItem subscriptionItem) {
        if (subscriptionItem == null) {
            return false;
        }
        if (subscriptionItem.getSubscription() == null) {
            return false;
        }
        Ticket ticket = subscriptionItem.getSubscription().getTicket();
        if (ticket == null) {
            return false;
        }
        return TicketType.FREE_BUSY.equals(ticket.getType());
    }

}
