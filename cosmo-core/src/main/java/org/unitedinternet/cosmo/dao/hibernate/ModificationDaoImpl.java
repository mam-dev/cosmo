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

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.ModificationDao;
import org.unitedinternet.cosmo.model.CollectionModification;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionModification;

/**
 * <p>
 * Hibernate implementation of the persistent collection change log. Rows are
 * never mutated after insertion; the AUTO_INCREMENT primary key provides the
 * global monotonic revision used by sync tokens.
 * </p>
 */
@Repository
public class ModificationDaoImpl implements ModificationDao {

    @PersistenceContext
    protected EntityManager em;

    public void log(String collectionUid, String memberUid,
                    String memberName, char modType) {
        em.persist(new HibCollectionModification(collectionUid, memberUid,
                memberName, modType));
        em.flush();
    }

    public List<CollectionModification> findSince(String collectionUid,
                                                  long sinceRevision,
                                                  int limit) {
        TypedQuery<HibCollectionModification> query = em.createQuery(
                "select m from HibCollectionModification m "
                + "where m.collectionUid = :collectionUid and m.id > :since "
                + "order by m.id asc",
                HibCollectionModification.class);
        query.setParameter("collectionUid", collectionUid);
        query.setParameter("since", Long.valueOf(sinceRevision));
        if (limit >= 0) {
            query.setMaxResults(limit);
        }
        List<HibCollectionModification> result = query.getResultList();
        return List.copyOf(result);
    }

    public long currentRevision() {
        List<Long> revisions = em.createQuery(
                "select max(m.id) from HibCollectionModification m",
                Long.class).getResultList();
        if (revisions.isEmpty() || revisions.get(0) == null) {
            return 0L;
        }
        return revisions.get(0).longValue();
    }
}
