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

import java.util.List;

import org.unitedinternet.cosmo.model.filter.PageCriteria;

/**
 * An interface for a class that holds a <code>PageCriteria</code>,
 * <code>List</code> that meets the <code>PageCriteria</code> and the size
 * of the total unpaginated<code>List</code>.
 * 
 * @author EdBindl
 * 
 */
public interface PagedList<T, SortType extends Enum> extends List<T>{
    
    /**
     * The request attribute in which this action places a List of
     * Users: <code>Users</code>
     */
    public static final String ATTR_USERS = "Users";
    
    /**
     * The request attribute in which this action places the total number of
     * pages
     */
    public static final String ATTR_NUM_PAGES = "NumPages";
    
    /**
     * The request attribute in which this action places the current page number
     */
    public static final String ATTR_CURRENT_PAGE = "CurrentPage";
    
    /**
     * Returns the Pagination criteria for the list.
     */
    public PageCriteria<SortType> getPageCriteria();

    /**
     * Sets the Pagination criteria for the list.
     * 
     * @param pageCriteria
     */
    public void setPageCriteria(PageCriteria<SortType> pageCriteria);

    /**
     * Returns the size of the total unpaginated list.
     */
    public int getTotal();
    
    /**
     * Returns the number of the last page.
     */
    public int getLastPageNumber();
    
    /**
     * Sets the size of the total unpaginated list.
     * @param total
     */
    public void setTotal(int total);
    
    /**
     * Returns the list meeting the Pagination Criteria
     */
    public List<T> getList();
    
    /**
     * Sets the list meeting the Pagination Criteria
     * 
     * @param items
     */
    public void setList(List<T> items);
}
