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
package org.unitedinternet.cosmo.model;

import java.util.ArrayList;
import java.util.List;

import org.unitedinternet.cosmo.model.filter.PageCriteria;

/**
 * A paginated section of a list that knows how many elements are in the total list,
 * and knows its pagination criteria (page size, page number, sort order, sort type).
 * 
 * @author EdBindl (heavily modified by TravisVachon)
 * 
 */
public class ArrayPagedList<T, SortType extends Enum> extends ArrayList<T> implements PagedList<T, SortType> {
    
    private static final long serialVersionUID = 8265962506011999456L;

    /**
     * The size of the total unpaginated list.
     */
    int total;

    /**
     * Holds the Pagination information for the <code>List</code>.
     */
    transient PageCriteria<SortType> pageCriteria;

    
    /**
     * Creates an ArrayPaged list that adheres to the supplied PageCriteria. If
     * an invalid page number is supplied, the list will be filled with a
     * pageNumber of 1 and will keep the supplied pageSize
     * 
     * @param pageCriteria -
     *            Pagination Criteria to paginate the list
     * @param list -
     *            the total unpaginated list to be paginated
     * 
     */
    public ArrayPagedList(PageCriteria<SortType> pageCriteria, List<T> list) {
        super(list);
        this.total = list.size();
        this.pageCriteria = pageCriteria;
        
    }

    /**
     * Creates an <code>ArrayPagedList</code> based on the supplied
     * sublist. The sublist is assumed to have already been paged, and
     * the supplied page criteria is not used.
     * 
     * @param pageCriteria - criteria for pagination of the list
     * @param sublist - the elements of the original list that have
     *            been deemed by some external code to be part of the
     *            page indicated by the page criteria
     * @param total the total number of elements in the original list
     * 
     */
    public ArrayPagedList(PageCriteria<SortType> pageCriteria,
                              List<T> list,
                              int total) {
        super(list);
        this.pageCriteria = pageCriteria;
        this.total = total;
    }

    /**
     */
    public PageCriteria<SortType> getPageCriteria() {
        return pageCriteria;
    }

    /**
     */
    public int getTotal() {
        return this.total;
    }

    /**
     */
    public void setTotal(int total) {
        this.total = total;
    }

    public int getLastPageNumber() {
        if (pageCriteria.getPageSize() == PageCriteria.VIEW_ALL){
            return 1;
            
        } else {
            int result = getTotal() / getPageCriteria().getPageSize();
            if (getTotal() % getPageCriteria().getPageSize() > 0) result++;
            return result;
        }
    }

    /**
     */
    public void setPageCriteria(PageCriteria<SortType> pageCriteria) {
        this.pageCriteria = pageCriteria;
    }
    
    /**
     */
    public List<T> getList(){
        return this;        
    }
    
    /**
     */
    public void setList(List<T> items){
        this.clear();
        this.addAll(items);
    }

}
