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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VFreeBusy;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>FreeBusyItem</code> to the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavFile
 */
public class DavFreeBusy extends DavCalendarResource {

    
    /** */
    public DavFreeBusy(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createFreeBusy(), locator, factory, entityFactory);
    }

    /** */
    public DavFreeBusy(FreeBusyItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * <p>
     * Exports the item as a calendar object containing a single VFREEBUSY
     * @return Calendar.
     * </p>
     */
    public Calendar getCalendar() {
        FreeBusyItem freeBusy = (FreeBusyItem) getItem();
        return freeBusy.getFreeBusyCalendar();
    }

    /**
     * <p>
     * Imports a calendar object containing a VFREEBUSY. 
     * </p>
     * @return The calendar imported.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public void setCalendar(Calendar cal) throws CosmoDavException {
        FreeBusyItem freeBusy = (FreeBusyItem) getItem();
        
        VFreeBusy vfb = (VFreeBusy) cal.getComponent(Component.VFREEBUSY);
        if (vfb==null) {
            throw new UnprocessableEntityException("VCALENDAR does not contain a VFREEBUSY");
        }

        EntityConverter converter = new EntityConverter(getEntityFactory());
        converter.convertFreeBusyCalendar(freeBusy, cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
