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
package org.unitedinternet.cosmo.dav.impl;

import org.junit.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.impl.mock.MockCalendarResource;

/**
 * Test case for <code>DavCalendarResource</code>.
 */
public class DavCalendarResourceTest extends BaseDavTestCase {
    
    /**
     * Tests CaldavReportTypes.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCaldavReportTypes() throws Exception {
        MockCalendarResource test = new MockCalendarResource(null, null, testHelper.getEntityFactory());

        assert(test.getReportTypes().contains(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY));
        assert(test.getReportTypes().contains(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET));
        assert(test.getReportTypes().contains(QueryReport.REPORT_TYPE_CALDAV_QUERY));
    }
}