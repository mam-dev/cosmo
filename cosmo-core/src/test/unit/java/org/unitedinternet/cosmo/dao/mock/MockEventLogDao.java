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
package org.unitedinternet.cosmo.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitedinternet.cosmo.dao.EventLogDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ItemChangeRecord;
import org.unitedinternet.cosmo.model.ItemChangeRecord.Action;
import org.unitedinternet.cosmo.model.event.EventLogEntry;
import org.unitedinternet.cosmo.model.event.ItemAddedEntry;
import org.unitedinternet.cosmo.model.event.ItemEntry;
import org.unitedinternet.cosmo.model.event.ItemRemovedEntry;
import org.unitedinternet.cosmo.model.event.ItemUpdatedEntry;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockEventLogDao implements EventLogDao {
    ArrayList<EventLogEntry> allEntries = new ArrayList<EventLogEntry>();

    /**
     * Adds event log entries.
     * {@inheritDoc}
     * @param entries The list with events.
     */
    public void addEventLogEntries(List<EventLogEntry> entries) {
        for(EventLogEntry entry: entries) {
            addEventLogEntry(entry);
        }
    }

    /**
     * Adds event log entry.
     * {@inheritDoc}
     * @param entry The event log entry.
     */
    public void addEventLogEntry(EventLogEntry entry) {
        if (entry.getDate() == null) {
            entry.setDate(new Date());
        }
        allEntries.add(entry);
    }

    /**
     * Finds changes for collection.
     * {@inheritDoc}
     * @return The list with the items.
     */
    public List<ItemChangeRecord> findChangesForCollection(
            CollectionItem collection, Date start, Date end) {
       ArrayList<ItemChangeRecord> records = new ArrayList<ItemChangeRecord>();
       
       for(EventLogEntry entry: allEntries) {
           
           // match date
           if (entry.getDate().before(start) || entry.getDate().after(end)) {
               continue;
           }
           
           ItemEntry itemEntry = (ItemEntry) entry;
           
           // match collection
           if (!collection.equals(itemEntry.getCollection())) {
               continue;
           }
           
           ItemChangeRecord record = new ItemChangeRecord();
           record.setDate(entry.getDate());
           
           if(entry instanceof ItemAddedEntry) {
               record.setAction(Action.ITEM_ADDED);
           } else if(entry instanceof ItemRemovedEntry) {
               record.setAction(Action.ITEM_REMOVED);
           } else if(entry instanceof ItemUpdatedEntry) {
               record.setAction(Action.ITEM_CHANGED);
           } else {
               throw new IllegalStateException("unrecognized entry type");
           }
           
           record.setItemUuid(itemEntry.getItem().getUid());
           record.setItemDisplayName(itemEntry.getItem().getDisplayName());
           setModifiedBy(record, itemEntry);
           records.add(record);
       }
       
       return records;
    }
    
    /**
     * Sets modification.
     * @param record The item change record.
     * @param entry The item entry.
     */
    private void setModifiedBy(ItemChangeRecord record, ItemEntry entry) {
        Item item = entry.getItem();
        if (item instanceof ContentItem) {
            record.setModifiedBy(((ContentItem) item).getLastModifiedBy());
        }
        
        if (record.getModifiedBy() == null) {
            if (entry.getUser() != null) {
                record.setModifiedBy(entry.getUser().getEmail());
            }
            else {
                record.setModifiedBy("ticket: anonymous");
            }
        }
    }
    
    /**
     * Destroy.
     * {@inheritDoc}
     */
    public void destroy() {
    }

    /**
     * Init.
     * {@inheritDoc}
     */
    public void init() {
    }

}
