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

import java.util.Date;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;

/**
 * Mock CollectionItemDetails
 */
public class MockCollectionItemDetails implements CollectionItemDetails {

    private CollectionItem collection = null;
    private Item item = null;
    private Date createDate = new Date();
    
    /**
     * Constructor.
     * @param collection The collection item.
     * @param item The item.
     */
    public MockCollectionItemDetails(CollectionItem collection, Item item) {
        this.collection = collection;
        this.item = item;
    }
    
    /**
     * Gets collection.
     * {@inheritDoc}
     * @return collection item.
     */
    public CollectionItem getCollection() {
        return collection;
    }
    /**
     * Gets item.
     * {@inheritDoc}
     * @return The item.
     */
    public Item getItem() {
       return item;
    }
    /**
     * Gets timestamp.
     * {@inheritDoc}
     * @return The date.
     */
    public Date getTimestamp() {
        return createDate;
    }
}
