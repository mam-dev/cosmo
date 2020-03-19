package org.unitedinternet.cosmo.dao.external;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.PathSegments;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.ext.ContentSource;
import org.unitedinternet.cosmo.ext.ExternalContentRuntimeException;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.filter.EqualsExpression;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.filter.StampFilter;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * <code>ContentDao</code> that fetches calendar content from external providers.
 * 
 * @author daniel grigore
 *
 */
@Repository
public class ContentDaoExternal implements ContentDao {

    private static final Logger LOG = LoggerFactory.getLogger(ContentDaoExternal.class);

    private final ContentSource contentSource;

    private final ContentDaoImpl contentDaoInternal;

    private final ItemFilterProcessor itemFilterProcessor;

    public ContentDaoExternal(ContentSource contentSource, final ContentDaoImpl contentDaoInternal,
            ItemFilterProcessor itemFilterProcessor) {
        this.contentSource = contentSource;
        this.contentDaoInternal = contentDaoInternal;
        this.itemFilterProcessor = itemFilterProcessor;
    }

    @Override
    public Item findItemByPath(String path) {
        PathSegments extPath = new PathSegments(path);

        String homeCollectionUid = validHomeCollectionUidFrom(extPath);

        String collectionUid = validCollectionUidFrom(extPath);

        CollectionItem collectionItem = (CollectionItem) this.contentDaoInternal
                .findItemByPath(homeCollectionUid + "/" + collectionUid);

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

        Set<NoteItem> itemsFromUri = this.getContent(targetUri, collectionItem);
        if (itemsFromUri != null) {
            String eventUid = extPath.getEventUid();
            if (eventUid == null || eventUid.trim().isEmpty()) {
                return new ExternalCollectionItem(collectionItem, itemsFromUri);
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
                Set<NoteItem> noteItems = this.getContent(targetUri, calendarItem);
                if (noteItems != null) {
                    this.postFilter(noteItems, filter);
                    List<Item> itemsList = new ArrayList<Item>(noteItems);
                    // Processes the recurring events
                    items.addAll(this.itemFilterProcessor.processResults(itemsList, filter));
                }
            }
        }
        return items;
    }

    /**
     * Gets a set of <code>NoteItem</code> from specified <code>targetUri</code> as children of the specified
     * <code>parent</code> ready to be displayed.
     * 
     * @param targetUri
     * @param parent
     * @return a set of <code>NoteItem</code> from specified <code>targetUri</code> as children of the specified
     *         <code>parent</code> ready to be displayed
     */
    private Set<NoteItem> getContent(String targetUri, CollectionItem parent) {
        Set<NoteItem> noteItems = this.contentSource.getContent(targetUri);
        if (noteItems != null) {
            for (NoteItem item : noteItems) {
                HibNoteItem hibItem = (HibNoteItem) item;
                hibItem.setOwner(parent.getOwner());
                hibItem.addParent(parent);
                String eTag = hibItem.getEntityTag();
                if (eTag == null || eTag.trim().isEmpty()) {
                    hibItem.setEntityTag(hibItem.calculateEntityTag());
                }
            }
        }
        return noteItems;
    }

    /**
     * Applies the specified filter to the list of items. Any non-recurring item that does not comply with the filter is
     * removed from list.
     * 
     * @param noteItems
     * @param filter
     */
    private void postFilter(Set<NoteItem> noteItems, ItemFilter filter) {
    	Object eventUid = null;
    	
    	if(filter instanceof NoteItemFilter && ((NoteItemFilter) filter).getIcalUid() instanceof EqualsExpression){
    		eventUid = ((EqualsExpression)((NoteItemFilter) filter).getIcalUid()).getValue();
    	}
    	
    	
        List<StampFilter> stampFilters = filter.getStampFilters();
        
        
        for (Iterator<NoteItem> iterator = noteItems.iterator(); iterator.hasNext();) {
            NoteItem item = iterator.next();
            BaseEventStamp eventStamp = (BaseEventStamp) item.getStamp(BaseEventStamp.class);
            
            if(eventUid != null && !Objects.equals(eventUid, item.getIcalUid())){
            	iterator.remove();
            	continue;
            }
            
            if (eventStamp == null || eventStamp.isRecurring()) {
                continue;
            }
            if (isOutOfRangeSafely(eventStamp, stampFilters)) {
                iterator.remove();
            }
        }
    }

    /**
     * Safely computes if the specified event is outside the date range defined by the stamp filters. In case any
     * exception is thrown then the event is considered to be in range.
     * 
     * @param eventStamp
     * @param stampFilters
     * @return <code>true</code> if the specified event is outside the range defined by the specified filters.
     */
    private static boolean isOutOfRangeSafely(BaseEventStamp eventStamp, List<StampFilter> stampFilters) {
    	if(stampFilters == null || stampFilters.isEmpty()){
    		return false;
    	}
        try {
            return isOutOfRange(eventStamp, stampFilters);
        } catch (Exception e) {
            LOG.error("Unable to compute isOutOfRange.", e);
            return false;
        }
    }

    private static boolean isOutOfRange(BaseEventStamp eventStamp, List<StampFilter> stampFilters)
            throws ParseException {
        for (StampFilter stampFilter : stampFilters) {
            if (!(stampFilter instanceof EventStampFilter)) {
                continue;
            }
            VEvent vEvent = null;
            if (eventStamp instanceof EventStamp) {
                vEvent = ((EventStamp) eventStamp).getMasterEvent();
            } else if (eventStamp instanceof EventExceptionStamp) {
                vEvent = ((EventExceptionStamp) eventStamp).getExceptionEvent();
            } else {
                throw new ExternalContentRuntimeException("Unknown event stamp class: " + eventStamp.getClass());
            }

            EventStampFilter eventFilter = (EventStampFilter) stampFilter;
            Date dtStart = vEvent.getStartDate().getDate();
            Date dtEnd = vEvent.getEndDate().getDate();
            Date filterStartDate = new DateTime(eventFilter.getUTCStart());
            Date filterEndDate = new DateTime(eventFilter.getUTCEnd());
            if (dtStart.after(filterEndDate) || dtStart.equals(filterEndDate) || dtEnd.before(filterStartDate)
                    || dtEnd.equals(filterStartDate)) {
                return true;
            }
        }
        return false;
    }

    /* All below methods should not be called for external providers */

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
        throw new CaldavExceptionForbidden("Readonly calendar.");
    }

    @Override
    public CollectionItem updateCollection(CollectionItem collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     */
    @Override
    public ContentItem createContent(CollectionItem parent, ContentItem content) {
        throw new CaldavExceptionForbidden("Readonly calendar.");
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
    
    @Override
    public long countItems(long ownerId) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long countItems(long ownerId, long fromTimestamp) {
        throw new UnsupportedOperationException();
    }
}
