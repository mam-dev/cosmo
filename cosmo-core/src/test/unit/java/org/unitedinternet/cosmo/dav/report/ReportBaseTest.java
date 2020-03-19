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
package org.unitedinternet.cosmo.dav.report;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.report.mock.MockReport;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.w3c.dom.Document;

/**
 * Test case for <code>ReportBase</code>.
 * <p>
 */
public class ReportBaseTest extends BaseDavTestCase implements DavConstants {

    /**
     * Tests depth infinity.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDepthInfinity() throws Exception {
        DavHomeCollection home = testHelper.initializeHomeResource();

        CollectionItem coll1 = testHelper.makeAndStoreDummyCollection(testHelper.getHomeCollection());
        coll1.addStamp(new HibCalendarCollectionStamp(coll1));

        CollectionItem coll2 = testHelper.makeAndStoreDummyCollection(testHelper.getHomeCollection());

        MockReport report = new MockReport();
        report.init(home, makeReportInfo("freebusy1.xml", DEPTH_INFINITY));

        report.runQuery();

        // Verify report is recursively called on all collections

        // Should be 3 collection: home collection and two test collecitons
        // NOTE if scheduling is enabled, outbox/inbox will also be present
        Assert.assertEquals(3, report.calls.size());
        Assert.assertTrue(report.calls.contains(testHelper.getHomeCollection().getDisplayName()));
        Assert.assertTrue(report.calls.contains(coll1.getDisplayName()));
        Assert.assertTrue(report.calls.contains(coll2.getDisplayName()));
    }

    /**
     * Makes report info.
     * 
     * @param resource The resource.
     * @param depth    The depth.
     * @return The report info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private ReportInfo makeReportInfo(String resource, int depth) throws Exception {
        Document doc = testHelper.loadXml(resource);
        return new ReportInfo(doc.getDocumentElement(), depth);
    }
}
