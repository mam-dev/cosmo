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
package org.unitedinternet.cosmo.dao.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.ModificationDao;
import org.unitedinternet.cosmo.model.CollectionModification;

/**
 * <p>
 * Persistence-level tests for {@link ModificationDaoImpl} against a real
 * Hibernate/JPA session backed by the embedded MariaDB instance provided by
 * {@link AbstractSpringDaoTestCase}.
 * </p>
 *
 * <p>
 * The RFC 6578 sync-collection integration tests run against the mock DAO
 * stack ({@code MockModificationDao}); this class is the counterpart that
 * verifies the production pieces they cannot see:
 * </p>
 * <ul>
 * <li>the {@code HibCollectionModification} entity is registered with
 * Hibernate and maps cleanly onto {@code cosmo_collection_modification};</li>
 * <li>ids assigned by the database AUTO_INCREMENT are strictly increasing,
 * which is what makes them usable as sync-token revisions;</li>
 * <li>{@link ModificationDaoImpl#findSince} semantics: exclusive lower bound,
 * oldest-first ordering, limit truncation, per-collection isolation.</li>
 * </ul>
 */
public class HibernateModificationDaoTest extends AbstractSpringDaoTestCase {

    @Autowired
    private ModificationDao modificationDao;

    private String uniqueCollectionUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Logging three changes of each type must persist all fields and
     * {@link ModificationDao#findSince} must return them ordered by ascending
     * revision regardless of insertion order of the queries.
     */
    @Test
    public void logAppendsAndFindSinceReturnsOrderedCompleteChanges() {
        String colUid = uniqueCollectionUid();

        modificationDao.log(colUid, "member-1", "one.ics",
                CollectionModification.MOD_TYPE_CREATED);
        modificationDao.log(colUid, "member-2", "two.ics",
                CollectionModification.MOD_TYPE_MODIFIED);
        modificationDao.log(colUid, "member-3", "three.ics",
                CollectionModification.MOD_TYPE_DELETED);

        List<CollectionModification> changes =
                modificationDao.findSince(colUid, 0L, -1);

        assertEquals(3, changes.size(),
                "every logged change must be found again");

        for (int i = 1; i < changes.size(); i++) {
            assertTrue(changes.get(i - 1).getId() < changes.get(i).getId(),
                    "changes must be ordered by strictly increasing revision");
        }

        CollectionModification created = changes.get(0);
        assertEquals(colUid, created.getCollectionUid());
        assertEquals("member-1", created.getMemberUid());
        assertEquals("one.ics", created.getMemberName());
        assertEquals(Character.valueOf(CollectionModification.MOD_TYPE_CREATED),
                Character.valueOf(created.getModType()));
        assertNotNull(created.getTimestamp(),
                "each record needs a wall-clock timestamp");
        assertNotNull(created.getId(), "server-assigned revision required");

        CollectionModification deleted = changes.get(2);
        assertEquals(Character.valueOf(CollectionModification.MOD_TYPE_DELETED),
                Character.valueOf(deleted.getModType()));
        assertEquals("three.ics", deleted.getMemberName(),
                "tombstones need the member name from deletion time");
    }

    /**
     * The since-revision is an exclusive lower bound: re-querying from the
     * first seen revision must not return that revision again, otherwise
     * clients would loop forever on the same change.
     */
    @Test
    public void findSinceExcludesTheSinceRevisionItself() {
        String colUid = uniqueCollectionUid();
        modificationDao.log(colUid, "m-1", "a.ics",
                CollectionModification.MOD_TYPE_CREATED);
        modificationDao.log(colUid, "m-2", "b.ics",
                CollectionModification.MOD_TYPE_CREATED);

        long first = modificationDao.findSince(colUid, 0L, -1).get(0).getId();

        List<CollectionModification> rest =
                modificationDao.findSince(colUid, first, -1);

        assertEquals(1, rest.size(),
                "querying from the first revision must yield exactly the newer change");
        assertTrue(rest.get(0).getId() > first,
                "no returned revision may be <= the since-revision");
    }

    /**
     * A positive limit truncates to the OLDEST pending changes (clients page
     * forward through the backlog); a negative limit means unlimited.
     */
    @Test
    public void limitTruncatesToOldestPendingChanges() {
        String colUid = uniqueCollectionUid();
        for (int i = 1; i <= 3; i++) {
            modificationDao.log(colUid, "m-" + i, i + ".ics",
                    CollectionModification.MOD_TYPE_CREATED);
        }

        List<CollectionModification> all =
                modificationDao.findSince(colUid, 0L, -1);
        List<CollectionModification> truncated =
                modificationDao.findSince(colUid, 0L, 2);

        assertEquals(3, all.size());
        assertEquals(2, truncated.size(),
                "positive limit must cap the result size");

        assertEquals(all.get(0).getId(), truncated.get(0).getId(),
                "truncation keeps the oldest pending change first");
        assertEquals(all.get(1).getId(), truncated.get(1).getId(),
                "truncation keeps pending changes in revision order");
    }

    /**
     * The change log is keyed by collection uid: entries of one collection
     * must never leak into another collection's incremental round.
     */
    @Test
    public void changeLogIsIsolatedPerCollection() {
        String colA = uniqueCollectionUid();
        String colB = uniqueCollectionUid();

        modificationDao.log(colA, "a-1", "a-one.ics",
                CollectionModification.MOD_TYPE_CREATED);
        modificationDao.log(colB, "b-1", "b-one.ics",
                CollectionModification.MOD_TYPE_CREATED);
        modificationDao.log(colA, "a-2", "a-two.ics",
                CollectionModification.MOD_TYPE_MODIFIED);

        List<CollectionModification> changesA =
                modificationDao.findSince(colA, 0L, -1);
        List<CollectionModification> changesB =
                modificationDao.findSince(colB, 0L, -1);

        assertEquals(2, changesA.size(), "collection A sees only its own rows");
        assertEquals(1, changesB.size(), "collection B sees only its own rows");
        for (CollectionModification change : changesA) {
            assertEquals(colA, change.getCollectionUid(),
                    "foreign collection rows must never be reported");
        }
    }

    /**
     * currentRevision() tracks the highest assigned revision and advances
     * monotonically - this is the value encoded into fresh sync tokens.
     */
    @Test
    public void currentRevisionTracksMaxAssignedRevision() {
        String colUid = uniqueCollectionUid();
        long before = modificationDao.currentRevision();

        modificationDao.log(colUid, "m-1", "one.ics",
                CollectionModification.MOD_TYPE_CREATED);

        long after = modificationDao.currentRevision();
        assertTrue(after > before,
                "logging a change must advance the global revision");

        List<CollectionModification> changes =
                modificationDao.findSince(colUid, 0L, -1);
        assertEquals(changes.get(changes.size() - 1).getId(), Long.valueOf(after),
                "currentRevision must equal the newest logged revision");
    }
}
