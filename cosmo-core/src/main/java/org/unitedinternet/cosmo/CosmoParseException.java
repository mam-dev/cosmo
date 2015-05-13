/*
 * CosmoIOException.java Apr 18, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo;

/**
 * An instance of {@link java.lang.RuntimeException}.
 * @author ccoman
 */
public class CosmoParseException extends CosmoException {

    private static final long serialVersionUID = -2746128836750233032L;
    /**
     * Constructor.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoParseException(Throwable cause) {
        super(cause);
    }
    /**
     * 
     * @param message The message exception.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
