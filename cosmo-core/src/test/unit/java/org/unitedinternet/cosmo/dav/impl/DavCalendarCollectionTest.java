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

import java.util.Set;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCollationSet;
import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCalendarCollection</code>.
 */
public class DavCalendarCollectionTest extends BaseDavTestCase
    implements ExtendedDavConstants,CaldavConstants  {
    
    /**
     * Tests supported collation set property.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSupportedCollationSetProperty() throws Exception {
        CollectionItem col = testHelper.makeAndStoreDummyCalendarCollection();
        
        DavHomeCollection home = testHelper.initializeHomeResource();

        DavCollection dcc =
            (DavCalendarCollection) testHelper.findMember(home, col.getName());
        
        SupportedCollationSet prop = 
            (SupportedCollationSet) dcc.getProperty(SUPPORTEDCOLLATIONSET);
        
        Assert.assertNotNull(prop);
        Assert.assertTrue(prop.isProtected());
        Set<String> collations = prop.getCollations();
        Assert.assertNotNull(collations);
        Assert.assertTrue(collations.size()==2);
        for (String c: collations) {
            Assert.assertTrue(CalendarUtils.isSupportedCollation(c));
        }
    }

    /**
     * SetUp
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testHelper.logIn();
    }
}
