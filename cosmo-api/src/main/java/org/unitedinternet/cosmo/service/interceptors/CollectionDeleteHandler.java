/*
 * EventUpdateHandler.java Jun 25, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.interceptors;



/**
 * Interface for collection delete handler.
 * @author ccoman
 *
 */
public interface CollectionDeleteHandler {
    /**
     * This method contains the code inserted before a collection is deleted.
     */
    public void beforeDeleteCollection(String calendarName);
    /**
     * This method contains the code inserted after a collection is deleted.
     */
    public void afterDeleteCollection(String calendarName);
}
