package org.unitedinternet.cosmo.dav.impl.parallel;

import org.apache.commons.lang3.StringUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

public class AvailabilityFile extends CalDavFileBase {
	public AvailabilityFile(Item item, CalDavResourceLocator locator, CalDavResourceFactory calDavResourceFactory,
			EntityFactory entityFactory) {
		super(item, locator, calDavResourceFactory, entityFactory);
	}

	public Calendar getCalendar() {
		AvailabilityItem availability = (AvailabilityItem) getItem();
		return availability.getAvailabilityCalendar();
	}

	/**
	 * <p>
	 * Imports a calendar object containing a VAVAILABILITY.
	 * 
	 * @param cal
	 *            The calendar imported.
	 * @throws CosmoDavException
	 *             - if something is wrong this exception is thrown.
	 *             </p>
	 */
	public void setCalendar(Calendar cal) throws CosmoDavException {
		AvailabilityItem availability = (AvailabilityItem) getItem();

		availability.setAvailabilityCalendar(cal);

		Component comp = cal.getComponent(ICalendarConstants.COMPONENT_VAVAILABLITY);
		if (comp == null) {
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