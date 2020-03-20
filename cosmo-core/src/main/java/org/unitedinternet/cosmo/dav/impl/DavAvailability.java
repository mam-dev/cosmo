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
package org.unitedinternet.cosmo.dav.impl;

import org.apache.commons.lang.StringUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.EntityFactory;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>AvailabilityItem</code> to the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavCalendarResource
 */
public class DavAvailability extends DavCalendarResource {
   
    /** */
    public DavAvailability(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createAvailability(), locator, factory, entityFactory);
    }

    /** */
    public DavAvailability(AvailabilityItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * <p>
     * Exports the item as a calendar object containing a single VAVAILABILITY
     * @return The calendar object.
     * </p>
     */
    public Calendar getCalendar() {
        AvailabilityItem availability = (AvailabilityItem) getItem();
        return availability.getAvailabilityCalendar();
    }

    /**
     * <p>
     * Imports a calendar object containing a VAVAILABILITY. 
     * @param cal The calendar imported.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     * </p>
     */
    public void setCalendar(Calendar cal) throws CosmoDavException {
        AvailabilityItem availability = (AvailabilityItem) getItem();
        
        availability.setAvailabilityCalendar(cal);
        
        Component comp = cal.getComponent(ICalendarConstants.COMPONENT_VAVAILABLITY);
        if (comp==null) {
            throw new UnprocessableEntityException("VCALENDAR does not contain a VAVAILABILITY");
        }

        String val = null;
        Property prop = comp.getProperty(Property.UID);
        if (prop != null) {
            val = prop.getValue();
        }
        if (StringUtils.isBlank(val)) {
            throw new UnprocessableEntityException("VAVAILABILITY does not contain a UID");
        }
        availability.setIcalUid(val);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
