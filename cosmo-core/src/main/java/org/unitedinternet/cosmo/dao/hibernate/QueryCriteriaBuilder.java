/*
 * Copyright 2006 Open Source Applications Foundation
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

import org.hibernate.Criteria;
import org.hibernate.Session;

import org.unitedinternet.cosmo.model.filter.PageCriteria;

/**
 * Interface for building Hibernate query criteria.
 *
 * @see Criteria
 * @see PageCriteria
 */
public interface QueryCriteriaBuilder<SortType extends Enum> {

    /**
     * Returns a <code>Criteria</code> based on the given
     * <code>PageCriteria</code> that can be used to query the given
     * <code>Session</code>.
     */
    Criteria buildQueryCriteria(Session session,
                                       PageCriteria<SortType> pageCriteria);

}
