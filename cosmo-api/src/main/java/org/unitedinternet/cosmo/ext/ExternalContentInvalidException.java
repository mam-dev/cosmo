/*
 * InvalidExternalContentException.java Jan 25, 2016
 * 
 * Copyright (c) 2016 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.ext;

/**
 * Thrown when content read from external url is invalid or could not be parsed.
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 */
@SuppressWarnings("serial")
public class ExternalContentInvalidException extends ExternalContentRuntimeException {

    public ExternalContentInvalidException() {

    }

    public ExternalContentInvalidException(Throwable cause) {
        super(cause);
    }
}
