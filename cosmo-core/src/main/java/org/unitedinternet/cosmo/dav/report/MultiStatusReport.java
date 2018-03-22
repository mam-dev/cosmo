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
package org.unitedinternet.cosmo.dav.report;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for WebDAV reports that return multistatus responses.
 */
public abstract class MultiStatusReport extends ReportBase {

    private MultiStatus multistatus = new MultiStatus();
    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;

    // Report methods

    public final boolean isMultiStatusReport() {
        return true;
    }

    // our methods

    /**
     * Generates and writes the multistatus response.
     */
    protected void output(DavServletResponse response) throws CosmoDavException {
        try {
            buildMultistatus();
            response.sendXmlResponse(multistatus, 207);
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    public final void buildMultistatus() throws CosmoDavException {

        DavPropertyNameSet resultProps = this.createResultPropSpec();
        for (WebDavResource resource : this.getResults()) {
            MultiStatusResponse response = this.buildMultiStatusResponse(resource, resultProps);
            multistatus.addResponse(response);
        }
    }

    protected DavPropertyNameSet createResultPropSpec() {
        return new DavPropertyNameSet(propfindProps);
    }

    /**
     * Returns a <code>MultiStatusResponse</code> describing the specified resource including the specified properties.
     */
    protected MultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props)
            throws CosmoDavException {
        if (props.isEmpty()) {
            String href = resource.getResourceLocator().getHref(resource.isCollection());
            return new MultiStatusResponse(href, 200);
        }
	return new MultiStatusResponse(resource, props, this.propfindType);
    }

    protected MultiStatus getMultiStatus() {
        return multistatus;
    }

    public final Element toXml(Document document) {
        try {
            runQuery();
        } catch (CosmoDavException e) {
            throw new RuntimeException(e);
        }

        return multistatus.toXml(document);
    }

    public int getPropFindType() {
        return propfindType;
    }

    public void setPropFindType(int type) {
        this.propfindType = type;
    }

    public DavPropertyNameSet getPropFindProps() {
        return propfindProps;
    }

    public void setPropFindProps(DavPropertyNameSet props) {
        this.propfindProps = props;
    }
}
