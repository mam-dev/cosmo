/*
 * Copyright 2007 Open Source Applications Foundation
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ItemTombstone;

/**
 * When an Item is removed from a collection, a tombstone is attached
 * to the collection to track when this removal ocurred.
 */
public class MockItemTombstone extends MockTombstone implements ItemTombstone {
    
    private String itemUid = null;
    private String itemName = null;

    /**
     * Contructor.
     */
    public MockItemTombstone() {
    }
    
    /**
     * Constructor.
     * @param parent The collection item.
     * @param item The item.
     */
    public MockItemTombstone(CollectionItem parent, Item item) {
        super(parent);
        itemUid = item.getUid();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItemTombstone#getItemUid()
     */
    /**
     * Gets item uid.
     * @return The item uid.
     */
    public String getItemUid() {
        return this.itemUid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItemTombstone#setItemUid(java.lang.String)
     */
    /**
     * Sets item uid.
     * @param itemUid The item uid.
     */
    public void setItemUid(String itemUid) {
        this.itemUid = itemUid;
    }
    
    
    
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Equals method.
     * {@inheritDoc}
     * @param obj The object.
     * @return The boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ItemTombstone)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                itemUid, ((ItemTombstone) obj).getItemUid()).isEquals();
    }

    /**
     * HashCode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 27).appendSuper(super.hashCode())
                .append(itemUid.hashCode()).toHashCode();
    }
}
