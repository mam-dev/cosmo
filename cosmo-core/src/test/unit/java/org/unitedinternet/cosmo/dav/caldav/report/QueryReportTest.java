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
package org.unitedinternet.cosmo.dav.caldav.report;


import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.mock.MockCalendarResource;
import org.unitedinternet.cosmo.dav.report.BaseReportTestCase;

/**
 * Test case for <code>QueryReport</code>.
 */
public class QueryReportTest extends BaseReportTestCase {

    /**
     * Tests wrong type.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("query");

        QueryReport report = new QueryReport();
        try {
            report.init(dcc, makeReportInfo("freebusy1.xml", DEPTH_1));
            Assert.fail("Non-query report info initalized");
        } catch (Exception e) {}
    }

    /**
     * Tests query self calendar resource.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQuerySelfCalendarResource() throws Exception {
        MockCalendarResource test = (MockCalendarResource)
            makeTarget(MockCalendarResource.class);
        test.setMatchFilters(true);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            Assert.fail("Self query failed for calendar resource");
        }
        Assert.assertTrue("Calendar resource not found in results",
                   report.getResults().contains(test));
    }

    /**
     * Tests query self non calendar resource.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQuerySelfNonCalendarResource() throws Exception {
        WebDavResource test = makeTarget(DavFile.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
            Assert.fail("Self query succeeded for non-calendar resource");
        } catch (UnprocessableEntityException e) {}
    }

    /**
     * Tests query self calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQuerySelfCalendarCollection() throws Exception {
        WebDavResource test = makeTarget(DavCalendarCollection.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            Assert.fail("Self query failed for calendar collection");
        }
    }

    /**
     * Tests query self non calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQuerySelfNonCalendarCollection() throws Exception {
        WebDavResource test = makeTarget(DavCollectionBase.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQuerySelf(test);
        } catch (Exception e) {
            Assert.fail("Self query failed for non-calendar collection");
        }
    }

    /**
     * Tests query children calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQueryChildrenCalendarCollection() throws Exception {
        DavCollection test = (DavCollection)
            makeTarget(DavCalendarCollection.class);
        QueryReport report = makeReport("query1.xml", DEPTH_1, test);
        try {
            report.doQueryChildren(test);
        } catch (Exception e) {
            Assert.fail("Children query failed for calendar collection");
        }
    }

    /**
     * Tests query children non calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testQueryChildrenNonCalendarCollection() throws Exception {
        DavCollection test = (DavCollection)
            makeTarget(DavCollectionBase.class);
        QueryReport report = makeReport("query1.xml", DEPTH_0, test);
        try {
            report.doQueryChildren(test);
        } catch (Exception e) {
            Assert.fail("Children query failed for non-calendar collection");
        }
    }

    /**
     * Makes report.
     * @param reportXml Report xml.
     * @param depth Depth.
     * @param target Target/
     * @return The query report.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private QueryReport makeReport(String reportXml, int depth,  WebDavResource target)
        throws Exception {
        return (QueryReport)
            super.makeReport(QueryReport.class, reportXml, depth, target);
    }
}
