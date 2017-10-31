package org.unitedinternet.cosmo.dao.hibernate;

import java.util.List;

import org.unitedinternet.cosmo.dao.CollectionSubscriptionDao;
import org.unitedinternet.cosmo.model.CollectionSubscription;

/**
 * 
 * @author daniel grigore
 *
 */
public class CollectionSubscriptionDaoImpl extends AbstractDaoImpl implements CollectionSubscriptionDao {

    /**
     * Default one
     */
    public CollectionSubscriptionDaoImpl() {
        super();
    }

    @Override
    public void init() {
        // Nothing to do yet.
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    @Override
    public List<CollectionSubscription> findByTargetCollectionUid(String uid) {
        return this.getSession()
                .createQuery("SELECT s FROM HibCollectionSubscription s WHERE s.targetCollection.uid = :uid",
                        CollectionSubscription.class)
                .setParameter("uid", uid).getResultList();
    }

    @Override
    public void addOrUpdate(CollectionSubscription collectionSubscription) {
        this.getSession().saveOrUpdate(collectionSubscription);
        this.getSession().flush();
    }

    @Override
    public CollectionSubscription findByTicket(String ticketKey) {        
        List<CollectionSubscription> subscriptions = this.getSession()
                .createQuery("SELECT s FROM HibCollectionSubscription s WHERE s.ticket.key = :ticketKey",
                        CollectionSubscription.class)
                .setParameter("ticketKey", ticketKey).getResultList();
        if (!subscriptions.isEmpty()) {
            return subscriptions.get(0);
        }
        return null;
    }
    
    @Override
    public void delete(CollectionSubscription subscription) {
        this.getSession().delete(subscription);
        this.getSession().flush();
    }

}
