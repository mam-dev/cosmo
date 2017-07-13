package org.unitedinternet.cosmo.dav.impl.parallel;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VToDo;

public class TaskFile extends CalDavFileBase{
	 // our methods

    public TaskFile(NoteItem item, 
    				CalDavResourceLocator locator,
    				CalDavResourceFactory calDavResourceFactory, 
    				EntityFactory entityFactory) {
		super(item, locator, calDavResourceFactory, entityFactory);
	}
    
    public TaskFile(CalDavResourceLocator locator,
					CalDavResourceFactory calDavResourceFactory, 
					EntityFactory entityFactory) {
    	this(entityFactory.createNote(), locator, calDavResourceFactory, entityFactory);
    }

	/**
     * <p>
     * Exports the stamp as a calendar object containing a single VTODO.
     * Sets the following properties:
     * </p>
     * <ul>
     * <li>UID: item's icalUid or uid</li>
     * <li>SUMMARY: item's displayName</li>
     * <li>DESCRIPTION: item's body</li>
     * </ul>
     * @return The calendar exported.
     */
    public Calendar getCalendar() {
        return new EntityConverter(null).convertNote((NoteItem)getItem());
    }

    /**
     * <p>
     * Imports a calendar object containing a VTODO. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>display name: the VTODO's SUMMARY (or the item's name, if the
     * SUMMARY is blank)</li>
     * <li>icalUid: the VTODO's UID</li>
     * <li>body: the VTODO's DESCRIPTION</li>
     * <li>reminderTime: if the VTODO has a DISPLAY VALARM
     *     the reminderTime will be set to the trigger time</li>
     * </ul>
     * @param cal The calendar imported.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public void setCalendar(Calendar cal)throws CosmoDavException {
        NoteItem note = (NoteItem) getItem();
        
        ComponentList<VToDo> vtodos = cal.getComponents(Component.VTODO);
        if (vtodos.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain any VTODOS");
        }

        EntityConverter converter = new EntityConverter(getEntityFactory());
        converter.convertTaskCalendar(note, cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
