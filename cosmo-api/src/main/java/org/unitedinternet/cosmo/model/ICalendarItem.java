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
 * Extends {@link Item} to represent an abstract
 * item that is backed by an icalendar component.
 */
public interface ICalendarItem extends ContentItem{

    public String getIcalUid();

    /**
     * Set the icalendar uid for this icalendar item.  The icalUid
     * is separate from the uid.  A uid is unique across all items.
     * The icalUid only has to be unique within a collection.
     * @param icalUid
     */
    public void setIcalUid(String icalUid);
}