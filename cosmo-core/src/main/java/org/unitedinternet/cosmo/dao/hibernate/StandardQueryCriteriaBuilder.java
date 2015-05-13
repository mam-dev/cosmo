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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitedinternet.cosmo.model.filter.PageCriteria;

/**
 * Interface for building Hibernate query criteria.
 *
 * @see Criteria
 * @see PageCriteria
 */
public class StandardQueryCriteriaBuilder<SortType extends Enum> implements QueryCriteriaBuilder<SortType> {

    private Class clazz;

    /**
     * Constructs a <code>StandardQueryCriteriaBuilder</code> that
     * will query objects of the given class.
     */
    public StandardQueryCriteriaBuilder(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * Returns a <code>Criteria</code> based on the given
     * <code>PageCriteria</code> that can be used to query the given
     * <code>Session</code>.
     */
    public Criteria buildQueryCriteria(Session session,
                                       PageCriteria<SortType> pageCriteria) {
        Criteria crit = session.createCriteria(clazz);

        // If page size is -1, that means get all users
        if (pageCriteria.getPageSize() > 0) {
            crit.setMaxResults(pageCriteria.getPageSize());

            int firstResult = 0;
            if (pageCriteria.getPageNumber() > 1) {
                firstResult = (pageCriteria.getPageNumber() - 1) *
                        pageCriteria.getPageSize();
            }
            crit.setFirstResult(firstResult);
        }

        for (Order order : buildOrders(pageCriteria)) {
            crit.addOrder(order);
        }

        Criterion orCriterion = null;
        for (String[] pair : pageCriteria.getOrCriteria()) {
            if (orCriterion == null) {
                orCriterion = Restrictions.ilike(pair[0], pair[1],
                        MatchMode.ANYWHERE);
            } else {
                orCriterion = Restrictions.or(orCriterion,
                        Restrictions.ilike(pair[0], pair[1],
                                MatchMode.ANYWHERE));
            }
        }
        if (orCriterion != null) {
            crit.add(orCriterion);
        }

        return crit;
    }

    /**
     * Returns a <code>List</code> of <code>Order</code> criteria
     * based on the sorting  attributes of the given
     * <code>PageCriteria</code>.
     * <p/>
     * This implementation simply adds one ascending or descending
     * <code>Order</code> as specified by
     * {@link PageCriteria#isSortAscending()}, sorting on the
     * attribute named by {@link PageCriteria#getSortTypeString()}.
     */
    protected List<Order> buildOrders(PageCriteria<SortType> pageCriteria) {
        List<Order> orders = new ArrayList<Order>();
        orders.add(pageCriteria.isSortAscending() ?
                Order.asc(pageCriteria.getSortTypeString()) :
                Order.desc(pageCriteria.getSortTypeString()));
        return orders;
    }
}
