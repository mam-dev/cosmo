package org.unitedinternet.cosmo.service.interceptors;

import org.unitedinternet.cosmo.model.Item;

/**
 * Handler that gets called after the calendar is retrieved.
 * 
 * @author daniel grigore
 *
 */
public interface CalendarGetHandler {

    /**
     * Join point that allows decorative operations after calendar is retrieved.
     * 
     * @param item
     *            calendar item
     */
    void afterGet(Item item);
}
