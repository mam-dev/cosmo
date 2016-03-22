package org.unitedinternet.cosmo.dao.external;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.ext.ContentSource;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import com.google.common.base.Strings;

/**
 * <code>ContentDao</code> that fetches calendar content from external providers.
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoExternal implements ContentDao {

    private static final Log LOG = LogFactory.getLog(ContentDaoExternal.class);

    private final ContentSource contentSource;

    private final ContentDao contentDaoInternal;

    public ContentDaoExternal(ContentSource contentSource, final ContentDao contentDaoInternal) {
        this.contentSource = contentSource;
        this.contentDaoInternal = contentDaoInternal;
    }

    @Override
    public Item findItemByPath(String path) {
        PathSegments extPath = new PathSegments(path);

        String homeCollectionUid = validHomeCollectionUidFrom(extPath);

        String collectionUid = validCollectionUidFrom(extPath);

        Item collectionItem = this.contentDaoInternal.findItemByPath(homeCollectionUid + "/" + collectionUid);

        if (collectionItem == null) {
            throw new IllegalArgumentException(
                    "Could not find collection for path: " + homeCollectionUid + "/" + collectionUid);
        }

        CalendarCollectionStamp stamp = (CalendarCollectionStamp) collectionItem
                .getStamp(CalendarCollectionStamp.class);

        if (stamp == null) {
            throw new IllegalStateException("Found calendar without stamp for path [" + path + "]");
        }

        String targetUri = stamp.getTargetUri();

        Set<NoteItem> itemsFromUri = this.contentSource.getContent(targetUri);
        if (itemsFromUri != null) {
            for (NoteItem item : itemsFromUri) {
                item.setOwner(collectionItem.getOwner());
            }

            String eventUid = extPath.getEventUid();

            if (Strings.isNullOrEmpty(eventUid)) {
                return new ExternalCollectionItem((CollectionItem) collectionItem, itemsFromUri);
            }
            for (NoteItem noteItem : itemsFromUri) {
                if (eventUid.equals(noteItem.getName())) {
                    return noteItem;
                }
            }
        }
        return null;
    }

    private static String validCollectionUidFrom(PathSegments extPath) {
        String collectionUid = extPath.getCollectionUid();

        if (collectionUid == null || collectionUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection path cannot be null or empty.");
        }
        return collectionUid;
    }

    private static String validHomeCollectionUidFrom(PathSegments extPath) {
        String homeCollectionUid = extPath.getHomeCollectionUid();

        if (homeCollectionUid == null || homeCollectionUid.trim().isEmpty()) {
            throw new IllegalArgumentException("Home path path cannot be null or empty.");
        }
        return homeCollectionUid;
    }

    @Override
    public Item findItemByPath(String path, String parentUid) {
        LOG.info("EXTERNAL Delegating call to internal for parentUid: " + parentUid + " and path: " + path);
        return this.contentDaoInternal.findItemByPath(path, parentUid);
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
                Set<NoteItem> noteItems = this.contentSource.getContent(targetUri);
                if (noteItems != null) {
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
    public <STAMP_TYPE extends Stamp> STAMP_TYPE findStampByInternalItemUid(String internalItemUid, Class<STAMP_TYPE> clazz){
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
