/*
 * CaldavExceptionExtMkCalendarForbidden.java Dec 30, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

/**
 * Exception extension for caldav.
 * Errors MUST be properly returned to client. Otherwise, clients will retry to
 * make the request(which will fail again) producing unnecessary load.
 * 
 * @author izidaru
 *
 */
public class CaldavExceptionExtMkCalendarForbidden extends CaldavExceptionForbidden{

    public CaldavExceptionExtMkCalendarForbidden(String message) {
        super(message);
    }

    
}
