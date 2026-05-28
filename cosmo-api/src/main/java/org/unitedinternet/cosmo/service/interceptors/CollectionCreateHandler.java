/*
 * CollectionCreateHandler.java Jun 25, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.interceptors;

import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * 
 * @author cristina coman
 *
 */
public interface CollectionCreateHandler {

    default void beforeCreateCollection(CollectionItem collection) {
    }

    default void afterCreateCollection(CollectionItem collection) {
    }
}
