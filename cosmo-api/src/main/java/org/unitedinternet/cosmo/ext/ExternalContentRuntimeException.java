package org.unitedinternet.cosmo.ext;

/**
 * 
 * @author daniel grigore
 *
 */
@SuppressWarnings("serial")
public class ExternalContentRuntimeException extends RuntimeException {

    public ExternalContentRuntimeException() {
        super();
    }

    public ExternalContentRuntimeException(String message) {
        super(message);
    }

    public ExternalContentRuntimeException(Throwable cause) {
        super(cause);
    }
}
