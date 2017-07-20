package org.unitedinternet.cosmo.service.interceptors;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Handler called when an item is moved from one parent to another.
 * 
 * @author daniel grigore
 *
 */
public interface EventMoveHandler {

    void beforeMove(Item item, CollectionItem oldParent, CollectionItem newParent);
}
