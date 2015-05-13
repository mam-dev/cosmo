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

import net.fortuna.ical4j.model.Calendar;

import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

/**
 * Extends {@link Item} to represent an abstract
 * item that is backed by an icalendar component.
 */
public abstract class MockICalendarItem extends MockContentItem implements ICalendarItem {

    public static final QName ATTR_ICALENDAR = new MockQName(
            ICalendarItem.class, "icalendar");
    
    
    private String icalUid = null;
    
    /**
     * Contructor.
     */
    public MockICalendarItem() {
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalenarItem#getIcalUid()
     */
    /**
     * Gets ical uid.
     * @return The ical uid.
     */
    public String getIcalUid() {
        return icalUid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalenarItem#setIcalUid(java.lang.String)
     */
    /**
     * Sets ical uid.
     * @param icalUid The ical uid.
     */
    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalenarItem#getFullCalendar()
     */
    /**
     * Gets full calendar.
     * @return calendar.
     */
    public Calendar getFullCalendar() {
        return getCalendar();
    }
    
    /**
     * Return the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     * @return calendar
     */
    protected Calendar getCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(this, ATTR_ICALENDAR);
    }
    
    /**
     * Set the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     * @param calendar
     */
    protected void setCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(this, ATTR_ICALENDAR, calendar);
    }
    
    /**
     * Copy to item.
     * @param item The item.
     * {@inheritDoc}
     */
    @Override
    protected void copyToItem(Item item) {
        
        if (!(item instanceof ICalendarItem)) {
            return;
        }
        
        super.copyToItem(item);
        
        // copy icalUid
        ICalendarItem icalItem = (ICalendarItem) item;
        icalItem.setIcalUid(getIcalUid());
    }
    
}
