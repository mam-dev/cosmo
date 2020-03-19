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
package org.unitedinternet.cosmo.dav.impl;


import org.apache.jackrabbit.webdav.property.DavProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCollectionBase</code>.
 */
public class DavCollectionBaseTest extends BaseDavTestCase  implements ExtendedDavConstants {
   
    /**
     * Tests deaf freeBusy rollup property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeadFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        boolean found = false;
        for (String name : dc.getDeadPropertyFilter()) {
            if (name.equals(CollectionItem.class.getName())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("exclude-free-busy-rollup not found in dead property filter",
                   found);
    }

    /**
     * Tests get include freeBusy rollup property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetIncludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        @SuppressWarnings("rawtypes")
        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        Assert.assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        Assert.assertTrue("exclude-free-busy-rollup property not false", ! flag);
    }

    /**
     * Tests get exclude free busy rollup porperty.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        @SuppressWarnings("rawtypes")
        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        Assert.assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        Assert.assertTrue("exclude-free-busy-rollup property not true", flag);
    }

    /**
     * Tests set inclide free busy rollup property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSetIncludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        ExcludeFreeBusyRollup efbr = new ExcludeFreeBusyRollup(false);
        dc.setLiveProperty(efbr, false);

        Assert.assertTrue("set exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /**
     * Tests set exclude free busy rollup property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        ExcludeFreeBusyRollup efbr = new ExcludeFreeBusyRollup(true);
        dc.setLiveProperty(efbr, false);

        Assert.assertTrue("set exclude-free-busy-rollup property is false",
                   testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /**
     * Tests remove exclude free busy rollup property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        dc.removeLiveProperty(EXCLUDEFREEBUSYROLLUP);

        Assert.assertTrue("removed exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /**
     * Tests caldav report types.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCaldavReportTypes() throws Exception {
        DavCollectionBase test = new DavCollectionBase(null, null, testHelper.getEntityFactory());

        assert(test.getReportTypes().contains(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY));
        assert(test.getReportTypes().contains(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET));
        assert(test.getReportTypes().contains(QueryReport.REPORT_TYPE_CALDAV_QUERY));
    }

    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testHelper.logIn();
    }
}
