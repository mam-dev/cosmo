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
package org.unitedinternet.cosmo.model.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ItemTombstone;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Tombstone;

/**
 * Extends {@link Item} to represent a collection of items
 */

public class MockCollectionItem extends MockItem implements CollectionItem {

    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
        new MockQName(CollectionItem.class, "excludeFreeBusyRollup");
    
    public static final QName ATTR_HUE =
        new MockQName(CollectionItem.class, "hue");

    private Set<CollectionItemDetails> childDetails = new HashSet<CollectionItemDetails>(0);
    
    /**
     * Constructor.
     */
    public MockCollectionItem() {
    };

    /**
     * Adds child.
     * @param item The item.
     */
    public void addChild(Item item) {
        MockCollectionItemDetails cid = new MockCollectionItemDetails(this, item);
        childDetails.add(cid);
    }
    
    /**
     * Removes.
     * @param item The item.
     */
    public void removeChild(Item item) {
        CollectionItemDetails cid = getChildDetails(item);
        if (cid != null) {
            childDetails.remove(cid);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildren()
     */
    /**
     * Gets children.
     * @return set item.
     */
    public Set<Item> getChildren() {
        Set<Item> children = new HashSet<Item>();
        for (CollectionItemDetails cid: childDetails) {
            children.add(cid.getItem());
        }
        
        return Collections.unmodifiableSet(children);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildDetails(org.unitedinternet.cosmo.model.Item)
     */
    /**
     * Gets child details.
     * @param item The item.
     * @return collection item details.
     */
    public CollectionItemDetails getChildDetails(Item item) {
        for (CollectionItemDetails cid: childDetails) {
            if (cid.getItem().equals(item)) {
                return cid;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    /**
     * Gets child.
     * @param uid The id.
     * @return The item.
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
    /**
     * Gets child by name.
     * @param name 
     * @return item
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#isExcludeFreeBusyRollup()
     */
    /**
     * Is exclude free busy rollup.
     * @return boolean.
     */
    public boolean isExcludeFreeBusyRollup() {
        Boolean bv =  MockBooleanAttribute.getValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP);
        if (bv == null) {
            return false;
        }
        else {
            return bv.booleanValue();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#setExcludeFreeBusyRollup(boolean)
     */
    /**
     * Sets exclude free busy rollup.
     * @param flag The flag.
     */
    public void setExcludeFreeBusyRollup(boolean flag) {
        MockBooleanAttribute.setValue(this, ATTR_EXCLUDE_FREE_BUSY_ROLLUP, flag);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#getHue()
     */
    /** 
     * GetHue.
     * @return hue.
     */
    public Long getHue() {
        return MockIntegerAttribute.getValue(this, ATTR_HUE);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#setHue(java.lang.Long)
     */
    /**
     * Sets hue.
     * @param value The value.
     */
    public void setHue(Long value) {
        MockIntegerAttribute.setValue(this, ATTR_HUE, value);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#removeTombstone(org.unitedinternet.cosmo.model.copy.Item)
     */
    /**
     * Removes tombstone.
     * @param item The item.
     * @return boolean.
     */
    public boolean removeTombstone(Item item) {
        for(Iterator<Tombstone> it = getTombstones().iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof ItemTombstone) {
                if(((ItemTombstone) ts).getItemUid().equals(item.getUid())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#generateHash()
     */
    /**
     * Generates hash.
     * @return The hash. 
     */
    public int generateHash() {
        return getVersion();
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The item
     */
    public Item copy() {
        CollectionItem copy = new MockCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
