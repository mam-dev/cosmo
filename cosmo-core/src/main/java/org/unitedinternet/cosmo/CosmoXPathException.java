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
public class CosmoXPathException extends CosmoException {

    private static final long serialVersionUID = -6046924363669277199L;
    /**
     * Constructor.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoXPathException(Throwable cause) {
        super(cause);
    }
    /**
     * 
     * @param message The message exception.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public CosmoXPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
