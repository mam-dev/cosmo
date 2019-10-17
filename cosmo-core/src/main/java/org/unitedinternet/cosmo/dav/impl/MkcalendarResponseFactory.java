package org.unitedinternet.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponse;
import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponseFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MkcalendarResponseFactory implements CreateCollectionResponseFactory, CaldavConstants {
    public static final String XML_MKCALENDAR_RESPONSE = "mkcalendar-response";
    @Override
    public CreateCollectionResponse get(String responseDescription) {
        return new AbstractMkcolResponse(responseDescription) {
            @Override
            protected Element createRootResponseElement(Document document) {
                return DomUtil.createElement(document, XML_MKCALENDAR_RESPONSE, NAMESPACE_CALDAV);
            }
        };

    }
}
