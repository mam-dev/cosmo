package org.unitedinternet.cosmo.dao.hibernate;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.CollectionSubscriptionDao;
import org.unitedinternet.cosmo.model.CollectionSubscription;

/**
 * 
 * @author daniel grigore
 *
 */
@Repository
public class CollectionSubscriptionDaoImpl implements CollectionSubscriptionDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * Default one
     */
    public CollectionSubscriptionDaoImpl() {
        super();
    }

    @Override
    public List<CollectionSubscription> findByTargetCollectionUid(String uid) {
        return this.em.createQuery("SELECT s FROM HibCollectionSubscription s WHERE s.targetCollection.uid = :uid",
                CollectionSubscription.class).setParameter("uid", uid).getResultList();
    }

    @Override
    public void addOrUpdate(CollectionSubscription collectionSubscription) {
        this.em.persist(collectionSubscription);        
    }

    @Override
    public CollectionSubscription findByTicket(String ticketKey) {
        List<CollectionSubscription> subscriptions = this.em
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
        this.em.remove(subscription);
        this.em.flush();
    }
}
