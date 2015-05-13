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

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.w3c.dom.Document;

/**
 * Test case for <code>MultigetReport</code>.
 * <p>
 */
public class MultigetReportTest extends BaseDavTestCase {

    /**
     * Tests wrong type.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");

        MultigetReport report = new MultigetReport();
        try {
            report.init(dcc, makeReportInfo("freebusy1.xml"));
            Assert.fail("Freebusy report initalized");
        } catch (CosmoDavException e) {}
    }

    /**
     * Tests no href.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNoHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");

        MultigetReport report = new MultigetReport();
        try {
            report.init(dcc, makeReportInfo("multiget1.xml"));
            Assert.fail("Report with no hrefs initalized");
        } catch (BadRequestException e) {}
    }

    /**
     * Tests resource too many href.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testResourceTooManyHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");
        DavEvent de = testHelper.initializeDavEvent(dcc, "event");

        MultigetReport report = new MultigetReport();
        try {
            report.init(de, makeReportInfo("multiget2.xml"));
            Assert.fail("Report against resource with more than one href initalized");
        } catch (BadRequestException e) {}
    }

    /**
     * Tests relative href.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRelativeHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");

        MultigetReport report = new MultigetReport();
        report.init(dcc, makeReportInfo("multiget2.xml"));
    }

    /**
     * Tests absolute hrefs.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAbsoluteHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");

        MultigetReport report = new MultigetReport();
        report.init(dcc, makeReportInfo("multiget3.xml"));
    }

    /**
     * Tests resource relative hrefs.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testResourceRelativeHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");
        DavEvent de = testHelper.initializeDavEvent(dcc, "event");

        MultigetReport report = new MultigetReport();
        report.init(de, makeReportInfo("multiget4.xml"));
    }

    /**
     * Tests resource absolute hrefs.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testResourceAbsoluteHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");
        DavEvent de = testHelper.initializeDavEvent(dcc, "event");

        MultigetReport report = new MultigetReport();
        report.init(de, makeReportInfo("multiget5.xml"));
    }

    /**
     * Tests incorrect hrefs.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIncorrectHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");

        MultigetReport report = new MultigetReport();
        try {
            report.init(dcc, makeReportInfo("multiget6.xml"));
            Assert.fail("Report with mislocated href parsed");
        } catch (BadRequestException e) {}
    }

    /**
     * Tests incorrect resource hrefs.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIncorrectResourceHrefs() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("multiget");
        DavEvent de = testHelper.initializeDavEvent(dcc, "event");

        MultigetReport report = new MultigetReport();
        try {
            report.init(de, makeReportInfo("multiget6.xml"));
            Assert.fail("Report with mislocated href parsed");
        } catch (BadRequestException e) {}
    }
    
    /**
     * Makes report info.
     * @param resource The resource.
     * @return The report info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private ReportInfo makeReportInfo(String resource)
        throws Exception {
        Document doc = testHelper.loadXml(resource);
        return new ReportInfo(doc.getDocumentElement(), DEPTH_1);
    }
}
