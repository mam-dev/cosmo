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
package org.unitedinternet.cosmo.model;

import java.util.Set;

/**
 * Item that represents a collection of items.
 */
public interface CollectionItem extends Item {
    
    /**
     * Return active children items (those with isActive=true).
     * @return active children items
     */
    public Set<Item> getChildren();
    
    public CollectionItemDetails getChildDetails(Item item);

    /**
     * Return child item with matching uid
     * @return identified child item, or null if no child with that
     * uid exists
     */
    public Item getChild(String uid);

    public Item getChildByName(String name);

    public boolean isExcludeFreeBusyRollup();

    public void setExcludeFreeBusyRollup(boolean flag);

    public Long getHue();

    public void setHue(Long value);

    /**
     * Generate alternative hash code for collection.
     * This hash code will return a different value if
     * collection or any child items in the collection
     * has changed since the last hash code was generated.
     * @return
     */
    public int generateHash();

}