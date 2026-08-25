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
package org.unitedinternet.cosmo.dao.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.unitedinternet.cosmo.dao.ModificationDao;
import org.unitedinternet.cosmo.model.CollectionModification;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionModification;

/**
 * <p>
 * In-memory implementation of the persistent collection change log used by
 * the mock DAO test stack. Mirrors {@code ModificationDaoImpl}'s semantics:
 * ids come from a monotonically increasing counter (never reused), rows are
 * immutable once stored.
 * </p>
 */
public class MockModificationDao implements ModificationDao {

    private final AtomicLong revisionCounter = new AtomicLong(0);
    private final TreeMap<Long, HibCollectionModification> modifications =
            new TreeMap<Long, HibCollectionModification>();

    public synchronized void log(String collectionUid, String memberUid,
                                 String memberName, char modType) {
        long id = revisionCounter.incrementAndGet();
        HibCollectionModification modification =
                new HibCollectionModification(collectionUid, memberUid,
                        memberName, modType);
        modification.setId(Long.valueOf(id));
        modifications.put(Long.valueOf(id), modification);
    }

    public synchronized List<CollectionModification> findSince(
            String collectionUid, long sinceRevision, int limit) {
        List<CollectionModification> result =
                new ArrayList<CollectionModification>();
        NavigableMap<Long, HibCollectionModification> tail =
                modifications.tailMap(Long.valueOf(sinceRevision), false);
        for (HibCollectionModification modification : tail.values()) {
            if (!modification.getCollectionUid().equals(collectionUid)) {
                continue;
            }
            if (limit >= 0 && result.size() >= limit) {
                break;
            }
            result.add(modification);
        }
        return result;
    }

    public synchronized long currentRevision() {
        return revisionCounter.get();
    }
}
