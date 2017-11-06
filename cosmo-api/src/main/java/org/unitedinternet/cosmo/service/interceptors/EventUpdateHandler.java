/*
 * EventUpdateHandler.java Jun 25, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.interceptors;

import java.util.Set;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;

/**
 * Interface for event update handler.
 * @author ccoman
 *
 */
public interface EventUpdateHandler {

    /**
     * This method contains the code inserted before an event to be updated.
     * Update a collection and set of children.  The set of
     * children to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false to an existing Item.
     * 
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail immediately with a
     * <code>CollectionLockedException</code>.
     *
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     */
    public void beforeUpdate(CollectionItem parent, Set<ContentItem> contentItems);
    
    /**
     * This method contains the code inserted after an event to be updated.
     * Update a collection and set of children.  The set of
     * children to be updated can include updates to existing
     * children, new children, and removed children.  A removal
     * of a child Item is accomplished by setting Item.isActive
     * to false to an existing Item.
     * 
     * The collection is locked at the beginning of the update. Any
     * other update that begins before this update has completed, and
     * the collection unlocked, will fail immediately with a
     * <code>CollectionLockedException</code>.
     *
     * @param collection
     *             collection to update
     * @param children
     *             children to update
     */
    public void afterUpdate(CollectionItem parent, Set<ContentItem> contentItems);
}
