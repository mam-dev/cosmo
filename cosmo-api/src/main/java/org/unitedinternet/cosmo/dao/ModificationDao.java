/*
 * Copyright 2026 United Internet
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

import java.util.List;

import org.unitedinternet.cosmo.model.CollectionModification;

/**
 * <p>
 * Persistent per-collection change log backing RFC 6578
 * <code>DAV:sync-collection</code> incremental synchronization. Every
 * membership change of a collection appends exactly one
 * {@link CollectionModification} row whose server-assigned id is globally
 * monotonic.
 * </p>
 */
public interface ModificationDao {

    /**
     * Appends one change record to a collection's log.
     *
     * @param collectionUid uid of the collection whose membership changed
     * @param memberUid uid of the affected member item
     * @param memberName name of the affected member item at the time of the
     *        change (needed to report tombstones for deletions)
     * @param modType one of {@link CollectionModification#MOD_TYPE_CREATED},
     *        {@link CollectionModification#MOD_TYPE_MODIFIED} or
     *        {@link CollectionModification#MOD_TYPE_DELETED}
     */
    void log(String collectionUid, String memberUid, String memberName,
             char modType);

    /**
     * Returns the change records of a collection that were appended
     * <em>after</em> the given revision, ordered by ascending revision.
     *
     * @param collectionUid uid of the collection to query
     * @param sinceRevision exclusive lower bound (a client's previously seen
     *        token value)
     * @param limit maximum number of records to return; a negative value
     *        means no limit
     */
    List<CollectionModification> findSince(String collectionUid,
                                           long sinceRevision,
                                           int limit);

    /**
     * The highest revision ever assigned (0 when the log is empty). This is
     * the numeric part of the freshest sync token the server can issue.
     */
    long currentRevision();
}
