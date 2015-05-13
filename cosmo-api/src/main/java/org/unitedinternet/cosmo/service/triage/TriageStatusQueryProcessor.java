/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.service.triage;

import java.util.SortedSet;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;

/**
 * Defines API for processing a TriageStatus query.
 * A TriageStatus query is run against a collection or
 * a master recurring note and returns all NoteItems that 
 * belong to a specified  TriageStatus (NOW,DONE,LATER).  
 * 
 * The criteria that
 * determines if a NoteItem belongs to a certain status
 * is complicated and based on different rules depending
 * on the status queried.
 */
public interface TriageStatusQueryProcessor {
    
    /**
     * @return NoteItems from a collection that fall into a 
     * given triage status category.
     * @param collection collection to search
     * @param context the query context
     */
    public SortedSet<NoteItem>
        processTriageStatusQuery(CollectionItem collection,
                                 TriageStatusQueryContext context);
    
    /**
     * Return modification and occurrence NoteItems 
     * from a master recurring NoteItem that fall into a 
     * given triage status category.
     * @param note master recurring note to search
     * @param context the query context
     * @return the set of NoteItems that match the given parameters
     */
    public SortedSet<NoteItem>
        processTriageStatusQuery(NoteItem note, 
                                 TriageStatusQueryContext context);
}
