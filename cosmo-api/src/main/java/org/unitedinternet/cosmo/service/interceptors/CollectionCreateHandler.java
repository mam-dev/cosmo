/*
 * EventUpdateHandler.java Jun 25, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.interceptors;

import org.unitedinternet.cosmo.model.CollectionItem;



/**
 * Interface for collection create handler.
 * @author ccoman
 *
 */
public interface CollectionCreateHandler {
    /**
     * This method contains the code inserted before a collection is created..
     * @param collection 
     */
    public void beforeCreateCollection(CollectionItem collection);

}
