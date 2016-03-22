/*
 * TooLargeExternalContentException.java Jan 25, 2016
 * 
 * Copyright (c) 2016 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.ext;

@SuppressWarnings("serial")
public class ExternalContentTooLargeException extends ExternalContentRuntimeException {

    public ExternalContentTooLargeException(String message) {
        super(message);
    }
}
