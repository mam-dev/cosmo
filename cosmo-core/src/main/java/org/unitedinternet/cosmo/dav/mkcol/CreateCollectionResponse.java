package org.unitedinternet.cosmo.dav.mkcol;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

public interface CreateCollectionResponse extends DavConstants, XmlSerializable {
    Status[] getStatus();

    void add(DavPropertyName propertyName, int status);
    void add(DavPropertyName propertyName);


    public static boolean hasNonOK(CreateCollectionResponse msr) {
        if (msr == null || msr.getStatus() == null) {
            return false;
        }

        for (Status status : msr.getStatus()) {

            if (status != null) {
                int statusCode = status.getStatusCode();

                if (statusCode != 200) {
                    return true;
                }
            }
        }
        return false;
    }
}
