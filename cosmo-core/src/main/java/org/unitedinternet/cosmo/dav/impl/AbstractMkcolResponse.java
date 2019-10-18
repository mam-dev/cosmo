package org.unitedinternet.cosmo.dav.impl;

import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;


/**
 *  https://tools.ietf.org/html/rfc5689
 *
 * Successfull MKCOL response
 *  <?xml version="1.0" encoding="utf-8" ?>
   <D:mkcol-response xmlns:D="DAV:"
                 xmlns:C="urn:ietf:params:xml:ns:caldav">
     <D:propstat>
       <D:prop>
         <D:resourcetype/>
         <D:displayname/>
       </D:prop>
       <D:status>HTTP/1.1 200 OK</D:status>
     </D:propstat>
    </D:mkcol-response>

 ** Unsuccessful MKCOL response
 *
 *    <D:mkcol-response xmlns:D="DAV:">
     <D:propstat>
       <D:prop>
         <D:resourcetype/>
       </D:prop>
       <D:status>HTTP/1.1 403 Forbidden</D:status>
       <D:error><D:valid-resourcetype /></D:error>
       <D:responsedescription>Resource type is not
       supported by this server</D:responsedescription>
     </D:propstat>
     <D:propstat>
       <D:prop>
         <D:displayname/>
       </D:prop>
       <D:status>HTTP/1.1 424 Failed Dependency</D:status>
     </D:propstat>
   </D:mkcol-response>
 Taken from RFC
 */
public abstract class AbstractMkcolResponse implements XmlSerializable, DavConstants, CreateCollectionResponse {

    /** <D:responsedescription **/
    private final String responseDescription;


    /** Contains all statuses */
    private HashMap<Integer, DavPropertyNameSet> statusMap = new HashMap<Integer, DavPropertyNameSet>();

    public Status[] getStatus() {
        Status [] sts = new Status[statusMap.size()];
        Iterator<Integer> iter = statusMap.keySet().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            Integer statusKey = iter.next();
            sts[i] = new Status(statusKey);
        }
        return sts;
    }

    public AbstractMkcolResponse(String responseDescription) {
        this.responseDescription = responseDescription;
    }


    public String getResponseDescription() {
        return responseDescription;
    }

    /** Creates/obtains a new DavPropertyNameSet for <D:prop> */
    private PropContainer getPropContainer(int status) {
        DavPropertyNameSet propContainer = statusMap.get(status);
        if (propContainer == null) {
            propContainer = new DavPropertyNameSet();
            statusMap.put(status, propContainer);
    }
        return propContainer;
    }

    /*  Get property names present in this response for the given status code */
    public DavPropertyNameSet getPropertyNames(int status) {
        return statusMap.getOrDefault(status, new DavPropertyNameSet());
    }

    /**
     * Add a property name to this response.
     * @param propertyName
     * @param status
     */
    public void add(DavPropertyName propertyName, int status) {
        PropContainer propCont = getPropContainer(status);
        propCont.addContent(propertyName);
    }

    public void add(DavPropertyName propertyName) {
        PropContainer status200 = getPropContainer(DavServletResponse.SC_OK);
        status200.addContent(propertyName);
    }

    protected abstract Element createRootResponseElement(Document document);

    @Override
    public Element toXml(Document document) {
        Element response = createRootResponseElement(document);

        for (Integer statusKey : statusMap.keySet()) {
            Status st = new Status(statusKey);
            PropContainer propCont = statusMap.get(statusKey);
            if (!propCont.isEmpty()) {
                Element propstat = DomUtil.createElement(document, XML_PROPSTAT, NAMESPACE);
                propstat.appendChild(propCont.toXml(document));
                propstat.appendChild(st.toXml(document));
                response.appendChild(propstat);
            }
        }
            // add the optional '<responsedescription>' element
            String description = getResponseDescription();
            if (description != null) {
                Element desc = DomUtil.createElement(document, XML_RESPONSEDESCRIPTION, NAMESPACE);
                DomUtil.setText(desc, description);
                response.appendChild(desc);
            }
            return response;
        }
}
