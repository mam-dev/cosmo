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

import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Base class for WebDAV reports.
 * </p>
 */
public abstract class ReportBase implements Report, ExtendedDavConstants {
    private static final Logger LOG = LoggerFactory.getLogger(ReportBase.class);

    private WebDavResource resource;
    private ReportInfo info;
    private HashSet<WebDavResource> results;

    // Report methods

    /**
     * Puts the report into a state where it can be run. Parses the given
     * report info by calling {@link #parseReport(ReportInfo)}.
     */
    public void init(org.apache.jackrabbit.webdav.DavResource resource,
                     ReportInfo info)
            throws CosmoDavException {
        this.resource = (WebDavResource) resource;
        this.info = info;
        this.results = new HashSet<WebDavResource>();
        parseReport(info);
    }

    /**
     * Executes the report and writes the output to the response.
     * Calls {@link runQuery())} to execute the report and
     * {@link #output(DavServletResponse)} to write the result.
     */
    public void run(DavServletResponse response)
            throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running report {} against {} ", getType().getReportName(), resource.getResourcePath());
        }
        runQuery();
        output(response);
    }

    // our methods

    /**
     * Parses information from the given report info needed to execute
     * the report. For example, a report request might include a query
     * filter that constrains the set of resources to be returned from
     * the report.
     */
    protected abstract void parseReport(ReportInfo info)
            throws CosmoDavException;

    /**
     * <p>
     * Executes the report query and stores the result.
     * Calls the following methods:
     * </p>
     * <ol>
     * <li> {@link #doQuerySelf(WebDavResource)} on the targeted resource </li>
     * <li> {@link #doQueryChildren(DavCollection)} if the targeted resource is
     * a collection and the depth is 1 or Infinity </li>
     * <li> {@link #doQueryDescendents(DavCollection)} if the targeted resource
     * is a collection and the depth is Infinity</li>
     * </ol>
     */
    protected void runQuery()
            throws CosmoDavException {
        doQuerySelf(resource);

        if (info.getDepth() == DEPTH_0) {
            return;
        }

        if (!(resource instanceof DavCollection)) {
            throw new BadRequestException("Report may not be run with depth " +
                    info.getDepth() + " against a non-collection resource");
        }
        DavCollection collection = (DavCollection) resource;

        doQueryChildren(collection);
        if (info.getDepth() == DEPTH_1) {
            return;
        }
        
        doQueryDescendents(collection);
    }

    /**
     * Writes the report result to the response.
     */
    protected abstract void output(DavServletResponse response)
            throws CosmoDavException;

    /**
     * Performs the report query on the specified resource.
     */
    protected abstract void doQuerySelf(WebDavResource resource)
            throws CosmoDavException;

    /**
     * Performs the report query on the specified collection's children.
     */
    protected abstract void doQueryChildren(DavCollection collection)
            throws CosmoDavException;

    /**
     * Performs the report query on the descendents of the specified collection.
     * Should recursively call the method against each of the children of the
     * provided collection that are themselves collections.
     */
    protected void doQueryDescendents(DavCollection collection)
            throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Querying descendents of {}", collection.getResourcePath());
        }
        for (DavResourceIterator i = collection.getCollectionMembers(); i.hasNext(); ) {
            WebDavResource member = (WebDavResource) i.nextResource();
            if (member.isCollection()) {
                DavCollection dc = (DavCollection) member;
                doQuerySelf(dc);
                doQueryChildren(dc);
                doQueryDescendents(dc);
            }
        }
    }

    protected static Element getReportElementFrom(ReportInfo reportInfo) {
        if (reportInfo == null) {
            return null;
        }
        Document document;
        try {
            document = DomUtil.createDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        return reportInfo.toXml(document);
    }

    public WebDavResource getResource() {
        return resource;
    }

    public ReportInfo getInfo() {
        return info;
    }

    public Set<WebDavResource> getResults() {
        return results;
    }
}
