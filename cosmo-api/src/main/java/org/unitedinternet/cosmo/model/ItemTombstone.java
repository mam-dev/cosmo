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

/**
 * When an Item is removed from a collection, a tombstone is attached
 * to the collection to track when this removal ocurred.  The item
 * uid is stored with the tombstone to track which item was removed.
 */
public interface ItemTombstone extends Tombstone{

    /**
     * @return uid of item removed
     */
    public String getItemUid();

    /**
     * @param itemUid uid of item removed
     */
    public void setItemUid(String itemUid);
    
    
    /**
     * @param itemName name of the item external exposed as ical UID
     */
    public void setItemName(String itemName);
    
    /**
     * 
     * @return itemName of <code>Item</code>
     */
    public String getItemName();

}