/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.acl.report;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>DAV:principal-search-property-set</code> report that
 * provides a mechanism for finding out with properties can be used to
 * search for resources with the <code>DAV:principal-property-search</code>
 * report.
 * </p>
 * <p>
 * This report will always return only <code>DAV:displayname</code>, since
 * user principals do not have any other live properties that are useful for
 * searching on.
 * </p>
 * <p>
 * As per RFC 3744, the report must be specified with depth 0. The report
 * must be targeted at a collection.
 * </p>
 */
public class PrincipalSearchPropertySetReport extends ReportBase
    implements AclConstants {
   
    public static final ReportType REPORT_TYPE_PRINCIPAL_SEARCH_PROPERTY_SET =
        ReportType.register("principal-search-property-set", NAMESPACE,
                            PrincipalSearchPropertySetReport.class);

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_PRINCIPAL_SEARCH_PROPERTY_SET;
    }

    // ReportBase methods
    
    public boolean isMultiStatusReport() {
        return false;
    }
    /**
     * Validates the report info.
     */
    protected void parseReport(ReportInfo info)
        throws CosmoDavException {
        if (! getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType().getReportName());
        }

        if (! getResource().isCollection()) {
            throw new BadRequestException(getType() + " report must target a collection");
        }
        if (info.getDepth() != DEPTH_0) {
            throw new BadRequestException(getType() + " report must be made with depth 0");
        }
        if (DomUtil.hasContent(getReportElementFrom(info))) {
            throw new BadRequestException("DAV:principal-search-property-set must be empty");
        }
    }

    /**
     * Does nothing.
     */
    protected void runQuery()
        throws CosmoDavException {}

    protected void output(DavServletResponse response)
        throws CosmoDavException {
        try {
            response.sendXmlResponse(new PrincipalSearchPropertySet(), 200);
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    /**
     * Does nothing.
     */
    protected void doQuerySelf(WebDavResource resource)
        throws CosmoDavException {}

    /**
     * Does nothing.
     */
    protected void doQueryChildren(DavCollection collection)
        throws CosmoDavException {}

    // our methods

    public static class PrincipalSearchPropertySet implements XmlSerializable {

        public Element toXml(Document document) {
            Element root = DomUtil.
                createElement(document, "principal-search-property-set",
                              NAMESPACE);

            Element psp = DomUtil.
                createElement(document, "principal-search-property",
                              NAMESPACE);
            root.appendChild(psp);

            Element prop = DomUtil.createElement(document, "prop", NAMESPACE);
            psp.appendChild(prop);

            prop.appendChild(DavPropertyName.DISPLAYNAME.toXml(document));

            // XXX I18N
            Element desc =
                DomUtil.createElement(document, "description", NAMESPACE);
            DomUtil.setAttribute(desc, "lang", NAMESPACE_XML, "en_US");
            DomUtil.setText(desc, "Display name");
            psp.appendChild(desc);

            return root;
        }
    }

    @Override
    public Element toXml(Document document) {
    	return new PrincipalSearchPropertySet().toXml(document);
    }
}
