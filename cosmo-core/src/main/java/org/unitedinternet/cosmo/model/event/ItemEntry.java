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
package org.unitedinternet.cosmo.model.event;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Abstract event log entry that contains information
 * about an item and an associated collection.
 */
public abstract class ItemEntry extends EventLogEntry {
    
    private Item item;
    private CollectionItem collection;
    
    public ItemEntry(Item item, CollectionItem collection) {
        this.item = item;
        this.collection = collection;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public CollectionItem getCollection() {
        return collection;
    }

    public void setCollection(CollectionItem collection) {
        this.collection = collection;
    }
}
