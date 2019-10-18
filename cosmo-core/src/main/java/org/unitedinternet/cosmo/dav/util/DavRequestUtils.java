package org.unitedinternet.cosmo.dav.util;

import org.apache.jackrabbit.webdav.DavException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;

public class DavRequestUtils {

    /** Checks if a request has a body */
    public static boolean hasBody(DavRequest request) throws CosmoDavException {
        boolean hasBody = false;
        try {
            hasBody = request.getRequestDocument() != null;
        } catch (IllegalArgumentException e) {
            // parse error indicates that there was a body to parse
            hasBody = true;
        } catch (DavException e) {
            throw new CosmoDavException(e);
        }
        return hasBody;
    }
}
