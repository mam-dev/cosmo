/*
 * CalendarClientsAdapter.java Nov 14, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.ProdId;

/**
 * Class used to fix problems in ics provided by clients.
 * @author izidaru
 *
 */
public class CalendarClientsAdapter {

    public static void adaptTimezoneCalendarComponent(Calendar calendar) {
        //ios 7 doesn't send product id on create calendar method
        if(calendar.getProductId() == null){
            calendar.getProperties().add(new ProdId("UNKNOWN_PRODID"));
        }
        
    }

}
