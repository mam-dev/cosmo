/*
 * EventRemoveHandler.java Jun 25, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.interceptors;

import java.util.Set;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Interface for event remove handler.
 * @author ccoman
 *
 */
public interface EventRemoveHandler {
    
    /**
     * Intercepter executed before removing an item from a collection.
     * 
     * @param parent CollectionItem
     * @param items Set<Item> to remove
     */
    public void beforeRemove(CollectionItem parent,  Set<Item> items);
	
    /**
     * Intercepter executed after removing an item from a collection.

     * @param parent CollectionItem
     * @param items Set<Item> to remove
     */
    public void afterRemove(CollectionItem parent, Set<Item> items);
}