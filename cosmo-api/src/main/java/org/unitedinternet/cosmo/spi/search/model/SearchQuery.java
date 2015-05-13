/*
 * SearchQuery.java Jun 18, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.spi.search.model;

import net.fortuna.ical4j.model.Component;

import org.unitedinternet.cosmo.model.filter.EventStampFilter;

/**
 * Encapsulates search query parameters.
 * @author izidaru
 *
 */
public class SearchQuery {
    public enum SearchType {
        EXACT, CONTAINS
    }
    
    String searchTerm;
    SearchType searchType;
    EventStampFilter timeRangeFilter;
    String component = Component.VEVENT;
    
    public SearchQuery(String searchTerm, SearchType searchType) {
        super();
        this.searchTerm = searchTerm;
        this.searchType = searchType;
    }
    
    
    
    public SearchQuery(String searchTerm, SearchType searchType, EventStampFilter timeRangeFilter) {
        super();
        this.searchTerm = searchTerm;
        this.searchType = searchType;
        this.timeRangeFilter = timeRangeFilter;
    }



    public String getSearchTerm() {
        return searchTerm;
    }
   
    public SearchType getSearchType() {
        return searchType;
    }

    public EventStampFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }



    public String getComponent() {
        return component;
    }
    
    
}
