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
package org.unitedinternet.cosmo.dao;

import java.util.Date;
import java.util.List;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ItemChangeRecord;
import org.unitedinternet.cosmo.model.event.EventLogEntry;

/**
 * Interface for DAO that provides base functionality for adding
 * event log entries.
 *
 */
public interface EventLogDao extends Dao {

    /**
     *  Add event log entry
      * @param entry entry to add
      */
     public void addEventLogEntry(EventLogEntry entry);
    
   /**
    *  Add event log entries
     * @param entries entries to add
     */
    public void addEventLogEntries(List<EventLogEntry> entries);
    
    /**
     * Find changes to collection.
     * @param collection collection
     * @param start start date to query
     * @param end end date to query
     * @return list of ItemChangeRecords representing all changes to target
     *         collection during time period
     */
    public List<ItemChangeRecord> findChangesForCollection(CollectionItem collection, Date start, Date end);
    
}
