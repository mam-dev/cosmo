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
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.StampTombstone;

/**
 * When an Stamp is removed from an item, a tombstone is attached
 * to the item to track when this removal ocurred.
 */
public class MockStampTombstone extends MockTombstone implements StampTombstone {
    
    private String stampType = null;

    /**
     * Contructor.
     */
    public MockStampTombstone() {
    }
    
    /**
     * Constructor.
     * @param item The item.
     * @param stamp The stamp.
     */
    public MockStampTombstone(Item item, Stamp stamp) {
        super(item);
        stampType = stamp.getType();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceStampTombstone#getStampType()
     */
    /**
     * Gets stamp type.
     * @return The stamp type.
     */
    public String getStampType() {
        return this.stampType;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceStampTombstone#setStampType(java.lang.String)
     */
    /**
     * Sets stamp type.
     * @param stampType The stamp type.
     */
    public void setStampType(String stampType) {
        this.stampType = stampType;
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param obj The object.
     * @return The boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StampTombstone)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                stampType, ((StampTombstone) obj).getStampType()).isEquals();
    }

    /**
     * Hashcode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 25).appendSuper(super.hashCode())
                .append(stampType.hashCode()).toHashCode();
    }
    
    
}
