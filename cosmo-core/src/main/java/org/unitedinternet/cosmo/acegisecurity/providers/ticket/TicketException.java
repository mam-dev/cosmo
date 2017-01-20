/*
 * TicketException.java Feb 5, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.acegisecurity.providers.ticket;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception in authentication with token
 * @author iulia zidaru
 *
 */
@SuppressWarnings("serial")
public class TicketException extends AuthenticationException {

    public TicketException(String msg) {
        super(msg);
    }

    public TicketException(String msg, Throwable t) {
        super(msg, t);
    }

}
