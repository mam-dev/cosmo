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

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;

/**
 * Test cal date.
 * @author ccoman
 *
 */
public class ICalDateTest {
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    /**
     * Tests parse date time.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseDateTime() throws Exception {
        String str =
            ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";

        DateTime dt = new ICalDate(str).getDateTime();
        Assert.assertNotNull("null datetime", dt);
        Assert.assertEquals("incorrect datetime", "20021010T120000", dt.toString());

        TimeZone dtTz = dt.getTimeZone();
        Assert.assertNotNull("null datetime timezone", dtTz);
        Assert.assertEquals("incorrect datetime timezone", "America/Los_Angeles",
                     dtTz.getID());
    }
    /**
     * Tests parse date.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseDate() throws Exception {
        String str = ";VALUE=DATE;X-OSAF-ANYTIME=TRUE:20021010";

        ICalDate icd = new ICalDate(str);
        Assert.assertNotNull("null date", icd.getDate());
        Assert.assertEquals("incorrect date", "20021010", icd.getDate().toString());
        Assert.assertTrue("not anytime", icd.isAnyTime());
    }
    
    /**
     * Tests parse date list.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseDateList() throws Exception {
        String str = ";VALUE=DATE:20021010,20021011,20021012";

        DateList dl = new ICalDate(str).getDateList();
        Assert.assertNotNull("null date list", dl);
        Assert.assertEquals("wrong number of dates", 3, dl.size());

        Assert.assertNull("not null timezone", dl.getTimeZone());

        Assert.assertEquals("not date", Value.DATE, dl.getType());

        Assert.assertEquals("incorrect date", "20021010", dl.get(0).toString());
        Assert.assertEquals("incorrect date", "20021011", dl.get(1).toString());
        Assert.assertEquals("incorrect date", "20021012", dl.get(2).toString());
    }
    
    /**
     * Tests parse date.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseDateNoParams() throws Exception {
        DateTime dt = new ICalDate("20021010T120000").getDateTime();
        Assert.assertNotNull("not datetime", dt);
        Assert.assertNull("timezone where none should be", dt.getTimeZone());
    }

    /**
     * Tests parse error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseErrors() throws Exception {
        try {
            new ICalDate(";TZID=deadbeef:20021010T120000");
            Assert.fail("converted with bad TZID deadbeef");
        } catch (UnknownTimeZoneException e) {}

        try {
            new ICalDate(";VALUE=deadbeef:20021010T120000");
            Assert.fail("converted with bad VALUE deadbeef");
        } catch (IllegalArgumentException e) {}

        try {
            new ICalDate("deadbeef");
            Assert.fail("converted with bad text deadbeef");
        } catch (ParseException e) {}
    }
    
    /**
     * Tests date time to string.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDateTimeToString() throws Exception {
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        DateTime dt = new DateTime("20021010T120000", tz);

        String test =
            ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";
        String result = new ICalDate(dt).toString();
        Assert.assertEquals(test, result);
    }

    /**
     * Tests date to string.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDateToString() throws Exception {
        Date d = new Date("20021010");

        String test = ";VALUE=DATE;X-OSAF-ANYTIME=TRUE:20021010";
        String result = new ICalDate(d, true).toString();
        Assert.assertEquals(test, result);
    }

    /**
     * Tests date list to String.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDateListToString() throws Exception {
        DateList dl = new DateList(Value.DATE);
        dl.add(new Date("20021010"));
        dl.add(new Date("20021011"));
        dl.add(new Date("20021012"));

        String test = ";VALUE=DATE:20021010,20021011,20021012";
        String result = new ICalDate(dl).toString();
        Assert.assertEquals(test, result);
        
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        dl = new DateList(Value.DATE_TIME, tz);
        dl.add(new DateTime("20021010T100000", tz));
        dl.add(new DateTime("20021011T100000", tz));
        dl.add(new DateTime("20021012T100000", tz));
        
        test = ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T100000,20021011T100000,20021012T100000";
        result = new ICalDate(dl).toString();
        Assert.assertEquals(test, result);
    }
}
