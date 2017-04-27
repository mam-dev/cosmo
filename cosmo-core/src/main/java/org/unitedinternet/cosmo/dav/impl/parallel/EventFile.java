package org.unitedinternet.cosmo.dav.impl.parallel;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

public class EventFile extends CalDavFileBase{

	@Override
	public CalDavResource getParent() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	 public Calendar getCalendar() {
        Calendar calendar = new EntityConverter(null).convertNote((NoteItem)getItem());
        // run through client filter because unfortunatley
        // all clients don't adhere to the spec
       // getClientFilterManager().filterCalendar(calendar);
        return calendar;
    }

    public void setCalendar(Calendar calendar) throws CosmoDavException {
            
        ComponentList<VEvent> vevents = calendar.getComponents(Component.VEVENT);
        if (vevents.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain any VEVENTs");
        }

        StampUtils.getEventStamp(getItem()).setEventCalendar(calendar);
    }
}