/*
 * CaldavExceptionForbidden.java Jan 13, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

/**
 * Exception extension for CalDAV. Errors MUST be properly returned to client. Otherwise, clients will retry to make the
 * request(which will fail again) producing unnecessary load.
 * 
 * @author izidaru
 *
 */
@SuppressWarnings("serial")
public class CaldavExceptionForbidden extends RuntimeException {

    public CaldavExceptionForbidden(String message) {
        super(message);
    }

}
