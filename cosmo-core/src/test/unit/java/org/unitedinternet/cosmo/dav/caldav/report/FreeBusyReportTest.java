/*
 * Copyright 2006 Open Source Applications Foundation
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

import org.apache.jackrabbit.webdav.DavException;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.report.BaseReportTestCase;
import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * Test case for <code>FreeBusyReport</code>.
 */
public class FreeBusyReportTest extends BaseReportTestCase {

    /**
     * Tests wrong type.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("freebusy");

        FreeBusyReport report = new FreeBusyReport();
        try {
            report.init(dcc, makeReportInfo("multiget1.xml", DEPTH_1));
            Assert.fail("Non-freebusy report info initalized");
        } catch (Exception e) {
        }
    }

    /**
     * Tests included collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIncludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, home);

        report.runQuery();
    }
    
    /**
     * Tests excluded collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExcludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, home);

        try {
            report.runQuery();
            Assert.fail("free-busy report targeted at excluded collection should not have succeeded but did");
        } catch (DavException e) {
            Assert.assertEquals("free-busy report targeted at excluded collection did not return 403", 403, e.getErrorCode());
        }
    }

    /**
     * Tests excluded parent collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExcludedParentCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        CollectionItem coll = testHelper.
            makeAndStoreDummyCollection(testHelper.getHomeCollection());
        coll.setExcludeFreeBusyRollup(false);

        DavHomeCollection home = testHelper.initializeHomeResource();
        DavCollection dc =
            (DavCollection) testHelper.findMember(home, coll.getName());

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, dc);

        try {
            report.runQuery();
            Assert.fail("free-busy report targeted at collection with excluded parent should not have succeeded but did");
        } catch (DavException e) {
            Assert.assertEquals("free-busy report targeted at collection with excluded parent "
                    + "did not return 403", 403, e.getErrorCode());
        }
    }

    /**
     * Makes report.
     * @param reportXml Raport xml.
     * @param depth depth.
     * @param target Dav resource.
     * @return Free busy report.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private FreeBusyReport makeReport(String reportXml, int depth, WebDavResource target)
        throws Exception {
        return (FreeBusyReport)
            super.makeReport(FreeBusyReport.class, reportXml, depth, target);
    }
}
