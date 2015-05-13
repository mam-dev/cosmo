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
 * Represents the relationship between an item and a collection.
 * An item can belong to many collection.  This relationship
 * also has extra information suck as a timestamp of when the
 * item was added to a collection.
 */
public interface CollectionItemDetails {
    
    /**
     * @return collection
     */
    public CollectionItem getCollection();
    
    /**
     * @return item
     */
    public Item getItem();
    
    /**
     * @return date item was added to collection
     */
    public Date getTimestamp();
}
