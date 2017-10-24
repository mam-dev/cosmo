package org.unitedinternet.cosmo.model.hibernate;

import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_EXCLUDE_FREE_BUSY_ROLLUP;
import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_HUE;

import java.util.Collections;
import java.util.Set;

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

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "proxyCollection", targetEntity = HibCollectionSubscription.class)
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
        if (this.getTargetCollection() == null) {
            return Collections.emptySet();
        }
        return this.getTargetCollection().getChildren();
    }

    @Override
    public CollectionItemDetails getChildDetails(Item item) {
        if (this.getTargetCollection() == null) {
            return null;
        }
        return this.getTargetCollection().getChildDetails(item);
    }

    @Override
    public Item getChild(String uid) {
        if (this.getTargetCollection() == null) {
            return null;
        }
        return this.getTargetCollection().getChild(uid);
    }

    @Override
    public Item getChildByName(String name) {
        if (this.getTargetCollection() == null) {
            return null;
        }
        return this.getTargetCollection().getChildByName(name);
    }

    /**
     * Gets the target collection associated with this CollectionSubscriptionItem or <code>null</code>. Note that if the
     * sharer deletes the collection then the associated target collection becomes <code>null</code>
     * 
     * @return the target collection associated with this CollectionSubscription or <code>null</code>.
     */
    public CollectionItem getTargetCollection() {
        if (this.subscription == null) {
            return null;
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
        String targetEtag = this.getTargetCollection() != null ? this.getTargetCollection().getEntityTag() : "";
        return encodeEntityTag(new String(ownEtag + "-" + targetEtag).getBytes());
    }
}
