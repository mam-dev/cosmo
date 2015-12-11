package org.unitedinternet.cosmo.dao.external;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.ext.ContentSource;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

/**
 * <code>ContentDao</code> that fetches calendar content from external providers.
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoExternal implements ContentDao {

    private final ContentSource contentSource;

    public ContentDaoExternal(ContentSource contentSource) {
        this.contentSource = contentSource;
    }

    @Override
    public Item findItemByPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item findItemByPath(String path, String parentUid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Item> findItems(ItemFilter filter) {
        Set<Item> items = new HashSet<>();
        if (filter != null && filter.getParent() != null) {
            CollectionItem calendarItem = filter.getParent();
            CalendarCollectionStamp stamp = (CalendarCollectionStamp) calendarItem
                    .getStamp(CalendarCollectionStamp.class);
            if (stamp != null) {
                String targetUri = stamp.getTargetUri();
                if (this.contentSource.isContentFrom(targetUri)) {
                    Set<NoteItem> noteItems = this.contentSource.getContent(targetUri);
                    this.postProcess(noteItems);
                    items.addAll(noteItems);
                }
            }
        }
        return items;
    }

    /**
     * Post process external <code>NoteItem</code>-s to make them ready to be displayed.
     * 
     * @param noteItems
     *            note items to be processed.
     */
    private void postProcess(Set<NoteItem> noteItems) {
        for (NoteItem item : noteItems) {
            HibNoteItem hibItem = (HibNoteItem) item;
            String eTag = hibItem.getEntityTag();
            if (eTag == null || eTag.trim().isEmpty()) {
                hibItem.setEntityTag(UUID.randomUUID().toString());
            }
        }
    }

    /* All below methods should not be called for external providers */

    @Override
    public void init() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item findItemByUid(String uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseEventStamp findEventStampFromDbByUid(String uid) {
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
