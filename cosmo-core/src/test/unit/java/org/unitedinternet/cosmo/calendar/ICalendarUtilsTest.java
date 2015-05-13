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
package org.unitedinternet.cosmo.calendar;


import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test ICalendarUtils
 */
public class ICalendarUtilsTest {
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
	
    /**
     * Tests normalize UTC dateTime to date.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNormalizeUTCDateTimeToDate() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        DateTime dt = new DateTime("20070201T070000Z");
        
        Assert.assertEquals("20070201", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());
        
        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        Assert.assertEquals("20070131", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());
        
        tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");
        Assert.assertEquals("20070201", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());
    }
    
    /**
     * Tests compare dates.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCompareDates() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        
        DateTime dt = new DateTime("20070201T070000Z");
        Date toTest = new Date("20070201");
        
        Assert.assertEquals(-1, ICalendarUtils.compareDates(toTest, dt, tz));
        
        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        Assert.assertEquals(1, ICalendarUtils.compareDates(toTest, dt, tz));
    }
    
    /**
     * Tests pin floating time.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPinFloatingTime() throws Exception {
        TimeZone tz1 = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
       
        Assert.assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new Date("20070101"), tz1).toString());
        Assert.assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz1).toString());
        
        
        TimeZone tz2 = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        Assert.assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new Date("20070101"), tz1).toString());
        Assert.assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz1).toString());
    
        Assert.assertTrue(ICalendarUtils.pinFloatingTime(
                new Date("20070101"), tz1).before(
                ICalendarUtils.pinFloatingTime(new Date("20070101"),
                        tz2)));
        Assert.assertTrue(ICalendarUtils.pinFloatingTime(
                new DateTime("20070101T000000"), tz1).before(
                ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"),
                        tz2)));
    }
    
    /**
     * Tests convert to UTC.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConvertToUTC() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
       
        Assert.assertEquals("20070101T060000Z", ICalendarUtils.convertToUTC(new Date("20070101"), tz).toString());
        Assert.assertEquals("20070101T160000Z", ICalendarUtils.convertToUTC(new DateTime("20070101T100000"), tz).toString());
        
        
        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        Assert.assertEquals("20070101T080000Z", ICalendarUtils.convertToUTC(new Date("20070101"), tz).toString());
        Assert.assertEquals("20070101T180000Z", ICalendarUtils.convertToUTC(new DateTime("20070101T100000"), tz).toString());
        
    }
}
