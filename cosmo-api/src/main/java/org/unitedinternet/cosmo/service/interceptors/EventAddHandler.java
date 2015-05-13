
/*
 * EventAddHandler.java Jun 13, 2012
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
 * Interface for event add handler.
 * @author ccoman
 *
 */
public interface EventAddHandler {
    
    /**
     * This method contains the code inserted before an event to be added.
     * 
     * @param parent CollectionItem
     * @param contentItems Set<ContentItem>
     */
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems);
    
    /**
     * This method contains the code inserted after an event to be added.
     * 
     * @param parent CollectionItem
     * @param contentItems Set<ContentItem>
     */
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems);
    
}
