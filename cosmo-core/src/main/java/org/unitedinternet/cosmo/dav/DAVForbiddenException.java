package org.unitedinternet.cosmo.dav;

public class DAVForbiddenException extends AbstractForbiddenException {
    public DAVForbiddenException(String message, String localName) {
        super(message, "DAV:", localName);
    }

}
