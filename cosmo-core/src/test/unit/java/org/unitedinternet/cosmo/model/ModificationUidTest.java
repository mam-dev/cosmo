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
package org.unitedinternet.cosmo.model;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;

/**
 * Test for ModificationUid class.
 *
 */
public class ModificationUidTest {
   
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    /**
     * Tests modification uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testModificationUid() throws Exception {
        Item parent = new MockNoteItem();
        parent.setUid("abc");
        Date date = new Date("20070101");
        
        ModificationUidImpl modUid = new ModificationUidImpl(parent, date);
        Assert.assertEquals("abc:20070101", modUid.toString());
        Assert.assertEquals(modUid, new ModificationUidImpl("abc:20070101"));
        
        date = new DateTime("20070101T100000");
        modUid = new ModificationUidImpl(parent, date);
        Assert.assertEquals("abc:20070101T100000", modUid.toString());
        Assert.assertEquals(modUid, new ModificationUidImpl("abc:20070101T100000"));
        
        date = new DateTime("20070101T100000", TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));
        modUid = new ModificationUidImpl(parent, date);
        Assert.assertEquals("abc:20070101T160000Z", modUid.toString());
       
        modUid = new ModificationUidImpl("abc:20070101T160000Z");
        Assert.assertEquals(parent.getUid(), modUid.getParentUid());
        Assert.assertTrue(modUid.getRecurrenceId() instanceof DateTime);
        Assert.assertTrue(((DateTime) modUid.getRecurrenceId()).isUtc());
        Assert.assertEquals("20070101T160000Z", modUid.getRecurrenceId().toString());
        
        try {
            new ModificationUidImpl("blah");
            Assert.fail("able to parse invalid date");
        } catch (ModelValidationException e) {
        }
        
        try {
            new ModificationUidImpl("blah:blah");
            Assert.fail("able to parse invalid date");
        } catch (ModelValidationException e) {
        }
        
        try {
            new ModificationUidImpl("blah:blahT");
            Assert.fail("able to parse invalid date");
        } catch (ModelValidationException e) {
        }
        
    }
}
