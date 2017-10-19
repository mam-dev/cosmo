package org.unitedinternet.cosmo.model.hibernate;

import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_EXCLUDE_FREE_BUSY_ROLLUP;
import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_HUE;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Item;

/**
 * <code>CollectionItem</code> that models a subscription collection.
 * 
 * @author daniel grigore
 * @see CollectionSubscription
 */
@Entity
@SuppressWarnings("serial")
@DiscriminatorValue("subscription")
public class HibCollectionSubscriptionItem extends HibItem implements CollectionItem {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "proxyCollection",targetEntity = HibCollectionSubscription.class)
    private CollectionSubscription subscription;

    /**
     * Default constructor.
     */
    public HibCollectionSubscriptionItem() {
        super();
    }

    @Override
    public Item copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Item> getChildren() {
        return this.getTargetCollection().getChildren();
    }

    @Override
    public CollectionItemDetails getChildDetails(Item item) {
        return this.getTargetCollection().getChildDetails(item);
    }

    @Override
    public Item getChild(String uid) {
        return this.getTargetCollection().getChild(uid);
    }

    @Override
    public Item getChildByName(String name) {
        return this.getTargetCollection().getChildByName(name);
    }

    public CollectionItem getTargetCollection() {
        if (this.subscription == null) {
            throw new IllegalArgumentException("subscription is null");
        }
        return this.subscription.getTargetCollection();
    }
    
    public CollectionSubscription getSubscription() {
        return subscription;
    }
    
    public void setSubscription(CollectionSubscription subscription) {       
        this.subscription = subscription;
    }

    @Override
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv = HibBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if (bv == null) {
            return false;
        } else {
            return bv.booleanValue();
        }
    }

    @Override
    public void setExcludeFreeBusyRollup(boolean flag) {
        HibBooleanAttribute.setValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP, flag);
    }

    public Long getHue() {
        return HibIntegerAttribute.getValue(this, ATTR_HUE);
    }

    @Override
    public void setHue(Long value) {
        HibIntegerAttribute.setValue(this, ATTR_HUE, value);
    }

    @Override
    public int generateHash() {
        return this.getVersion();
    }

    @Override
    public String getEntityTag() {
        String ownEtag = super.getEntityTag();
        String targetEtag = this.subscription.getTargetCollection().getEntityTag();
        return encodeEntityTag(new String(ownEtag + "-" + targetEtag).getBytes());
    }
}
