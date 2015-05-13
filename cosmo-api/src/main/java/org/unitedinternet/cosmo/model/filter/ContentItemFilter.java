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
package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.filter.FilterOrder.Order;


/**
 * Adds ContentItem specific criteria to ItemFilter.
 * Matches only ContentItem instances.
 */
public class ContentItemFilter extends ItemFilter {
   
    public static final FilterOrder ORDER_BY_TRIAGE_STATUS_RANK_ASC = new FilterOrder(
            "triageStatus.rank", Order.ASC);
    public static final FilterOrder ORDER_BY_TRIAGE_STATUS_RANK_DESC = new FilterOrder(
            "triageStatus.rank", Order.DESC);
    
    private FilterCriteria triageStatusCode = null;
    
    public ContentItemFilter() {}

    public FilterCriteria getTriageStatusCode() {
        return triageStatusCode;
    }

    /**
     * Match ContentItems with a specific triageStatus code.
     * Should be one of:
     * <p>
     *  <code> TriageStatus.CODE_DONE<br/>
     *  TriageStatus.CODE_NOW<br/>
     *  TriageStatus.CODE_LATER</code>
     *  <p>
     * @param triageStatus
     */
    public void setTriageStatusCode(FilterCriteria triageStatusCode) {
        this.triageStatusCode = triageStatusCode;
    }
}
