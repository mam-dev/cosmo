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

import java.util.Date;

/**
 * Represents something that was removed from an item.
 */
public interface Tombstone {

    /**
     * Time that tombstone was created.  This is effectively 
     * the time that the object was removed from the item.
     * @return time tombstone was created
     */
    public Date getTimestamp();

    /**
     * @param timestamp time tombstone created
     */
    public void setTimestamp(Date timestamp);

    /**
     * @return Item that tombstone is associated with.
     */
    public Item getItem();

    /**
     * @param item Item that tombstone is associated with.
     */
    public void setItem(Item item);

}