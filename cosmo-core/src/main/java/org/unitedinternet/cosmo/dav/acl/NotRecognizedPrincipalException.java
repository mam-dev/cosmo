package org.unitedinternet.cosmo.dav.acl;

import org.unitedinternet.cosmo.dav.DAVForbiddenException;

public class NotRecognizedPrincipalException extends DAVForbiddenException {
    public NotRecognizedPrincipalException(String message) {
        super(message, "recognized-principal");
    }
}
