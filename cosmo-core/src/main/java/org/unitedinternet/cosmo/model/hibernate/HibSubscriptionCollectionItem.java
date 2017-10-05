package org.unitedinternet.cosmo.model.hibernate;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;


@Entity
@SuppressWarnings("serial")
@DiscriminatorValue("subscriptioncollection")
public class HibSubscriptionCollectionItem extends HibItem implements CollectionItem {
    
    // CollectionItem specific attributes
    //TODO Move to common place
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
            new HibQName(CollectionItem.class, "excludeFreeBusyRollup");
    
    //TODO Move to common place
    public static final QName ATTR_HUE =
            new HibQName(CollectionItem.class, "hue");
    
    
    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy = "proxyCollection")
    private HibCollectionSubscription subscription;
    
    /**
     * Default constructor.
     */
    public HibSubscriptionCollectionItem() {
        super();
    }

    @Override
    public Item copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Item> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionItemDetails getChildDetails(Item item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item getChild(String uid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item getChildByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv =  HibBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if(bv==null) {
            return false;
        }
        else {
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

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#setHue(java.lang.Long)
     */
    public void setHue(Long value) {
        HibIntegerAttribute.setValue(this, ATTR_HUE, value);
    }

    @Override
    public int generateHash() {
        return this.getVersion();
    }
    
}
