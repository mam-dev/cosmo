/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model.hibernate;

import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_EXCLUDE_FREE_BUSY_ROLLUP;
import static org.unitedinternet.cosmo.model.hibernate.CollectionItemConstants.ATTR_HUE;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ItemTombstone;

/**
 * Hibernate persistent CollectionItem.
 */
@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem implements CollectionItem {

    /**
     * 
     */
    private static final long serialVersionUID = 2873258323314048223L;
  

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="primaryKey.collection", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE }) 
    private Set<CollectionItemDetails> childDetails = new HashSet<CollectionItemDetails>(0);

    private transient Set<Item> children = null;

    public HibCollectionItem() {
    };

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildren()
     */
    public Set<Item> getChildren() {
        if(children!=null) {
            return children;
        }

        children = new HashSet<Item>();
        for(CollectionItemDetails cid: childDetails) {
            children.add(cid.getItem());
        }

        return children;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildDetails(org.unitedinternet.cosmo.model.Item)
     */
    public CollectionItemDetails getChildDetails(Item item) {
        for(CollectionItemDetails cid: childDetails) {
            if(cid.getItem().getUid().equals(item.getUid()) && 
                    cid.getItem().getName().equals(item.getName())&& 
                    cid.getItem().getOwner().getUid().equals(item.getOwner().getUid()) ) {
                return cid;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    public Item getChild(String uid) {
        for (Item child : getChildren()) {
            if (child.getUid().equals(uid)) {
                return child;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    public Item getChildByName(String name) {
        for (Item child : getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#isExcludeFreeBusyRollup()
     */
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv =  HibBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if(bv==null) {
            return false;
        }
        else {
            return bv.booleanValue();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    public void setExcludeFreeBusyRollup(boolean flag) {
        HibBooleanAttribute.setValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP, flag);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getHue()
     */
    public Long getHue() {
        return HibIntegerAttribute.getValue(this, ATTR_HUE);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#setHue(java.lang.Long)
     */
    public void setHue(Long value) {
        HibIntegerAttribute.setValue(this, ATTR_HUE, value);
    }

    /**
     * Remove ItemTombstone with an itemUid equal to a given Item's uid
     * @param item
     * @return true if a tombstone was removed
     */
    public boolean removeTombstone(Item item) {
        ItemTombstone ts = new HibItemTombstone(this, item);
        return tombstones.remove(ts);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#generateHash()
     */
    public int generateHash() {
        return getVersion();
    }

    public Item copy() {
        CollectionItem copy = new HibCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
