/*
 * Copyright 2008 Open Source Applications Foundation
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

import java.util.Date;

/**
 * Represents an item change.  Includes who, when and what changed.
 */
public class ItemChangeRecord {
    public enum Action { ITEM_ADDED, ITEM_CHANGED, ITEM_REMOVED }
    
    private Action action;
    private Date date;
    private String modifiedBy;
    private String itemUuid;
    private String itemDisplayName;
    
    public ItemChangeRecord() {}
    
    public Action getAction() {
        return action;
    }
    public void setAction(Action action) {
        this.action = action;
    }
    public Date getDate() {
        return date != null ? (Date)date.clone(): null;
    }
    public void setDate(Date date) {
        if(date != null){
            this.date = (Date)date.clone();
        }
    }
    public String getModifiedBy() {
        return modifiedBy;
    }
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    public String getItemUuid() {
        return itemUuid;
    }
    public void setItemUuid(String itemUuid) {
        this.itemUuid = itemUuid;
    }
    public String getItemDisplayName() {
        return itemDisplayName;
    }
    public void setItemDisplayName(String itemDisplayName) {
        this.itemDisplayName = itemDisplayName;
    }
    
    public static Action toAction(String action) {
        if("ItemAdded".equals(action)) {
            return Action.ITEM_ADDED;
        }
        else if("ItemRemoved".equals(action)) {
            return Action.ITEM_REMOVED;
        }
        else if("ItemChanged".equals(action) || "ItemUpdated".equals(action)) {
            return Action.ITEM_CHANGED;
        }
        
        throw new IllegalStateException("Unknown action " + action);
    }
}
