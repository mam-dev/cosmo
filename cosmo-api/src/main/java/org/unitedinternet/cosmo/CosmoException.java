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
public class CosmoException extends RuntimeException {
    /**
     * Declare serial version UID
     */
    private static final long serialVersionUID = 4019648364692469900L;

    /**
     * Implicit constructor.
     */
    public CosmoException() {
        
    }
    /**
     * Constructor.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoException(Throwable cause) {
        super(cause);
    }

    /**
     * 
     * @param message The message exception.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoException(String message, Throwable cause) {
        super(message, cause);
    }
}
