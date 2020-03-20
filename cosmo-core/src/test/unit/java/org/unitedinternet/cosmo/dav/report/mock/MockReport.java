/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.report.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.report.ReportBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Mock used for testing reports.
 * </p>
 * <p>
 * When the report is run, each resource that is queried is added to the
 * {@link calls} list so that a test case can verify they were called.
 * </p>
 *
 */
public class MockReport extends ReportBase { 
    
    private static final Logger LOG = LoggerFactory.getLogger(MockReport.class);

    // keep track of which resource were passed to doQuery()
    public List<String> calls = new ArrayList<String>();

    /**
     * Output.
     * {@inheritDoc}
     * @param response Dav servlet response.
     * @throws CosmodavException - if something is wrong this exception is thrown.
     */
    protected void output(DavServletResponse response)
        throws CosmoDavException {
        // XXX
    }

    /**
     * Parses report.
     * {@inheritDoc}
     * @param infi Report info.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    protected void parseReport(ReportInfo info)
        throws CosmoDavException {
        // XXX
    }    

    /**
     * Adds the queried resource to {@link #calls}.
     * @param resource Dav resource.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    protected void doQuerySelf(WebDavResource resource) throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("querying {}", resource.getResourcePath());
        }
        calls.add(resource.getDisplayName());
    }

    /**
     * Does nothing.
     * @param collection Dav collection.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
     protected void doQueryChildren(DavCollection collection) throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("querying children of {}", collection.getResourcePath());
        }
    }

     /**
      * Gets type.
      * {@inheritDoc}
      * @return Report type.
      */
    public ReportType getType() {
        // XXX
        return null;
    }

    /**
     * Is multi status report.
     * {@inheritDoc}
     * @return boolean.
     */
    public boolean isMultiStatusReport() {
        return false;
    }

    @Override
    public Element toXml(Document document) {
        return null;
    }

}
