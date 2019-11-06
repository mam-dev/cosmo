package org.unitedinternet.cosmo.dav.acl;

import org.unitedinternet.cosmo.dav.DAVForbiddenException;

public class NotAllowedPrincipalException extends DAVForbiddenException {
    //https://tools.ietf.org/html/rfc3744#section-8.1.1

    public NotAllowedPrincipalException(String message) {
        super(message, "allowed-principal");
    }
}
