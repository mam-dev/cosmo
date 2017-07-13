package org.unitedinternet.cosmo.dav.impl.parallel;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VFreeBusy;

public class FreeBusyFile extends CalDavFileBase{
	public FreeBusyFile(FreeBusyItem item, 	
						CalDavResourceLocator locator,
						CalDavResourceFactory calDavResourceFactory, 
						EntityFactory entityFactory) {
		super(item, locator, calDavResourceFactory, entityFactory);
	}
	
	
	public FreeBusyFile(CalDavResourceLocator locator,
			CalDavResourceFactory calDavResourceFactory, 
			EntityFactory entityFactory) {
		super(entityFactory.createNote(), locator, calDavResourceFactory, entityFactory);
}

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