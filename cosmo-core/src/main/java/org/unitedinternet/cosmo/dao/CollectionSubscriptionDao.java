package org.unitedinternet.cosmo.dao;

import java.util.List;

import org.unitedinternet.cosmo.model.CollectionSubscription;

/**
 * 
 * @author daniel grigore
 *
 */
public interface CollectionSubscriptionDao extends Dao {

    List<CollectionSubscription> findByTargetCollectionUid(String uid);
    
    public void addOrUpdate(CollectionSubscription collectionSubscription);
    
    CollectionSubscription findByTicket(String ticketKey);
    
    void delete(CollectionSubscription subscription);
}
