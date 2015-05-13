/*
 * SearchService.java Jun 18, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.spi.search;

import org.unitedinternet.cosmo.spi.search.model.SearchQuery;
import org.unitedinternet.cosmo.spi.search.model.SearchResponse;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;

public interface SearchService {


    /**
     * 
     * @param user cosmo user
     * @param collection calendar
     * @param contentItem item: event or other entity
     */
    public void indexContentItem(User user, CollectionItem collection, ContentItem contentItem);
    /**
     * 
     * @param user cosmo user
     * @param collection calendar
     * @param contentItem item: event or other entity
     */
    public void deleteIndexForContentItem(User user, CollectionItem collection, Item contentItem);

    /**
     * Some search services need a refresh call to make the index visible before search.
     * @param userIdentifier cosmo user
     */
    public abstract void refresh(User user);

    /**
     * 
     * @param userIdentifier cosmo user
     * @param searchQuery contains search criteria
     * @return
     */
    public abstract SearchResponse searchEvent(User user, SearchQuery searchQuery);

    /**
     * Creates the user in search system.
     * @param userIdentifier cosmo user
     */
    public abstract void createUser(User user);

    /**
     * Removes user index from search system.
     * @param userIdentifier cosmo user
     */
    public abstract void deleteUser(User user);

    /**
     * Index all user documents.
     * @param user Cosmo user.
     */
    public void indexBulkContentItemForUser(User user);
}
