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
package org.unitedinternet.cosmo.calendar;

import java.io.FileInputStream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test TimeZoneTranslator
 */
public class TimeZoneTranslatorTest {
    protected String baseDir = "src/test/unit/resources/testdata/testtimezones/";
    
    /**
     * Tests timezone translator.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTimeZoneTranslator() throws Exception {
        TimeZoneTranslator translator = TimeZoneTranslator.getInstance();
        TimeZone tz1 = getTimeZone("timezone1.ics");
        TimeZone tz2 = getTimeZone("timezone2.ics");
        TimeZone tz3 = getTimeZone("timezone3.ics");
        TimeZone tz4 = getTimeZone("timezone4.ics");
        
        TimeZone olsonTz = translator.translateToOlsonTz(tz1);
        Assert.assertNotNull(olsonTz);
        Assert.assertEquals("America/Los_Angeles", olsonTz.getID());
        
        olsonTz = translator.translateToOlsonTz(tz2);
        Assert.assertNotNull(olsonTz);
        Assert.assertEquals("America/Los_Angeles", olsonTz.getID());
        
        olsonTz = translator.translateToOlsonTz(tz3);
        Assert.assertNotNull(olsonTz);
        Assert.assertEquals("America/Chicago", olsonTz.getID());
        
        // bogus timezone should return null
        olsonTz = translator.translateToOlsonTz(tz4);
        Assert.assertNull(olsonTz);
    }
    
    /**
     * Tests translate lightning TZID.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTranslateLightningTZID() throws Exception {
        TimeZoneTranslator translator = TimeZoneTranslator.getInstance();
       
        // test Lightning TZID format
        TimeZone olsonTz = translator.translateToOlsonTz("/mozilla.org/20050126_1/America/Los_Angeles");
        
        Assert.assertEquals("America/Los_Angeles", olsonTz.getID());
    }
    
    /**
     * Gets timezone.
     * @param filename The file.
     * @return The timezone.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private TimeZone getTimeZone(String filename) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + filename);
        Calendar calendar = cb.build(fis);
        
        VTimeZone vtz = (VTimeZone) calendar.getComponents(Component.VTIMEZONE).get(0);
        return new TimeZone(vtz);
    }
        
    
}
