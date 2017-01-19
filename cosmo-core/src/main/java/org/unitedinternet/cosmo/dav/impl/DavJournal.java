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
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VJournal;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>NoteItem</code> to the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavCalendarResource
 */
public class DavJournal extends DavCalendarResource {

    
    /** */
    public DavJournal(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
    }

    /** */
    public DavJournal(NoteItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    // our methods

    /**
     * <p>
     * Exports the item as a calendar object containing a single VJOURNAL,
     * ignoring any stamps that may be associated with the item. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>UID: item's icalUid or uid</li>
     * <li>SUMMARY: item's displayName</li>
     * <li>DESCRIPTION: item's body</li>
     * </ul>
     * @return The calendar exported.
     */
    public Calendar getCalendar() {
        NoteItem note = (NoteItem) getItem();
        return new EntityConverter(null).convertNote(note);
    }

    /**
     * <p>
     * @param cal Imports a calendar object containing a VJOURNAL. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>display name: the VJOURNAL's SUMMARY (or the item's name, if the
     * SUMMARY is blank)</li>
     * <li>icalUid: the VJOURNAL's UID</li>
     * <li>body: the VJOURNAL's DESCRIPTION</li>
     * </ul>
     */
    public void setCalendar(Calendar cal)
        throws CosmoDavException {
        NoteItem note = (NoteItem) getItem();
      
        ComponentList<VJournal> vjournals = cal.getComponents(Component.VJOURNAL);
        if (vjournals.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain any VJOURNALS");
        }

        EntityConverter converter = new EntityConverter(getEntityFactory());
        converter.convertJournalCalendar(note, cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
