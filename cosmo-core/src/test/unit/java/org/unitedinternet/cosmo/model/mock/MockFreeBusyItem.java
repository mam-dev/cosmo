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

import net.fortuna.ical4j.model.Calendar;

import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Extends {@link ICalendarItem} to represent a VFREEBUSY item.
 */
public class MockFreeBusyItem extends MockICalendarItem implements FreeBusyItem {

    /**
     * Constructor.
     */
    public MockFreeBusyItem() {
    }

    /**
     * Copy.
     * {@inheritDoc}
     * @return The item.
     */
    @Override
    public Item copy() {
        FreeBusyItem copy = new MockFreeBusyItem();
        copyToItem(copy);
        return copy;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFreeBusyItem#getFreeBusyCalendar()
     */
    /**
     * Gets free busy calendar.
     * @return The calendar.
     */
    public Calendar getFreeBusyCalendar() {
        return getCalendar();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFreeBusyItem#setFreeBusyCalendar(net.fortuna.ical4j.model.Calendar)
     */
    /**
     * Sets free busy calendar.
     * @param calendar The calendar.
     */
    public void setFreeBusyCalendar(Calendar calendar) {
        setCalendar(calendar);
    }
    
}
