package org.unitedinternet.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponse;
import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponseFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MkcolResponseFactory implements CreateCollectionResponseFactory, DavConstants {

    public static final String XML_MKCOL_RESPONSE = "mkcol-response";
    @Override
    public CreateCollectionResponse get(String responseDescription) {
        return new AbstractMkcolResponse(responseDescription) {
            @Override
            protected Element createRootResponseElement(Document document) {
                return DomUtil.createElement(document, XML_MKCOL_RESPONSE, NAMESPACE);
            }
        };
    }
}
