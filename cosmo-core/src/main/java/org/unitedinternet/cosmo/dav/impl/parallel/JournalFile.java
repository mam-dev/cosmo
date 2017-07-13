package org.unitedinternet.cosmo.dav.impl.parallel;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VJournal;

public class JournalFile extends CalDavFileBase{
	  public JournalFile(Item item, 
		  				CalDavResourceLocator calDavResourceLocator,
		  				CalDavResourceFactory calDavResourceFactory, 
		  				EntityFactory entityFactory) {
		super(item, calDavResourceLocator, calDavResourceFactory, entityFactory);
	}
	  
	  public JournalFile(CalDavResourceLocator calDavResourceLocator,
				CalDavResourceFactory calDavResourceFactory, 
				EntityFactory entityFactory) {
		  super(entityFactory.createNote(), calDavResourceLocator, calDavResourceFactory, entityFactory);
}

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
