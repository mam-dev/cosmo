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

import java.io.InputStream;
import java.text.ParseException;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test InstanceList, the meat and potatoes of recurrence
 * expansion.
 */
public class InstanceListTest {

    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
            TimeZoneRegistryFactory.getInstance().createRegistry();

    /**
     * Tests floating recurring.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFloatingRecurring() throws Exception {
        Calendar calendar = getCalendar("floating_recurr_event.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20060101T140000");
        DateTime end = new DateTime("20060108T140000");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(5, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060102T140000", key);
        Assert.assertEquals("20060102T140000", instance.getStart().toString());
        Assert.assertEquals("20060102T150000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060103T140000", key);
        Assert.assertEquals("20060103T140000", instance.getStart().toString());
        Assert.assertEquals("20060103T150000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060104T140000", key);
        Assert.assertEquals("20060104T160000", instance.getStart().toString());
        Assert.assertEquals("20060104T170000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060105T140000", key);
        Assert.assertEquals("20060105T160000", instance.getStart().toString());
        Assert.assertEquals("20060105T170000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060106T140000", key);
        Assert.assertEquals("20060106T140000", instance.getStart().toString());
        Assert.assertEquals("20060106T150000", instance.getEnd().toString());
    }

    /**
     * Tests UTC Instance list.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUTCInstanceList() throws Exception {
        Calendar calendar = getCalendar("floating_recurr_event.ics");

        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/New_York");

        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(tz);

        DateTime start = new DateTime("20060101T190000Z");
        DateTime end = new DateTime("20060108T190000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(5, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060102T190000Z", key);
        Assert.assertEquals("20060102T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060102T200000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060103T190000Z", key);
        Assert.assertEquals("20060103T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060103T200000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060104T190000Z", key);
        Assert.assertEquals("20060104T210000Z", instance.getStart().toString());
        Assert.assertEquals("20060104T220000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060105T190000Z", key);
        Assert.assertEquals("20060105T210000Z", instance.getStart().toString());
        Assert.assertEquals("20060105T220000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060106T190000Z", key);
        Assert.assertEquals("20060106T190000Z", instance.getStart().toString());
        Assert.assertEquals("20060106T200000Z", instance.getEnd().toString());
    }

    /**
     * Tests UTC Instance List all day event.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUTCInstanceListAllDayEvent() throws Exception {

        Calendar calendar = getCalendar("allday_weekly_recurring.ics");

        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        DateTime start = new DateTime("20070103T090000Z");
        DateTime end = new DateTime("20070117T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070108T060000Z", key);
        Assert.assertEquals("20070108T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070109T060000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070115T060000Z", key);
        Assert.assertEquals("20070115T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070116T060000Z", instance.getEnd().toString());
    }

    /**
     * Tests UTC Instance list all day with ex Dates.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUTCInstanceListAllDayWithExDates() throws Exception {

        Calendar calendar = getCalendar("allday_recurring_with_exdates.ics");

        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070106T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070101T060000Z", key);
        Assert.assertEquals("20070101T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070102T060000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070102T060000Z", key);
        Assert.assertEquals("20070102T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070103T060000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070106T060000Z", key);
        Assert.assertEquals("20070106T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070107T060000Z", instance.getEnd().toString());
    }

    /**
     * Tests UTC Instance all day event with mods.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUTCInstanceListAllDayEventWithMods() throws Exception {

        Calendar calendar = getCalendar("allday_weekly_recurring_with_mods.ics");

        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070109T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070101T060000Z", key);
        Assert.assertEquals("20070101T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070102T060000Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070108T060000Z", key);
        Assert.assertEquals("20070109T060000Z", instance.getStart().toString());
        Assert.assertEquals("20070110T060000Z", instance.getEnd().toString());
    }

    /**
     * Tests instance list instance before start range.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testInstanceListInstanceBeforeStartRange() throws Exception {

        Calendar calendar = getCalendar("eventwithtimezone3.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070511T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070509T081500Z", key);
        Assert.assertEquals("20070509T031500", instance.getStart().toString());
        Assert.assertEquals("20070509T041500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070510T081500Z", key);
        Assert.assertEquals("20070510T031500", instance.getStart().toString());
        Assert.assertEquals("20070510T041500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070511T081500Z", key);
        Assert.assertEquals("20070511T031500", instance.getStart().toString());
        Assert.assertEquals("20070511T041500", instance.getEnd().toString());
    }

    /**
     * Tests floating with switching timezone instance list.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFloatingWithSwitchingTimezoneInstanceList() throws Exception {

        Calendar calendar = getCalendar("floating_recurr_event.ics");

        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        InstanceList instances = new InstanceList();
        instances.setTimezone(tz);

        DateTime start = new DateTime("20060102T220000Z");
        DateTime end = new DateTime("20060108T190000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(5, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060102T220000Z", key);
        Assert.assertEquals("20060102T140000", instance.getStart().toString());
        Assert.assertEquals("20060102T150000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060103T220000Z", key);
        Assert.assertEquals("20060103T140000", instance.getStart().toString());
        Assert.assertEquals("20060103T150000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060104T220000Z", key);
        Assert.assertEquals("20060104T160000", instance.getStart().toString());
        Assert.assertEquals("20060104T170000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060105T220000Z", key);
        Assert.assertEquals("20060105T160000", instance.getStart().toString());
        Assert.assertEquals("20060105T170000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20060106T220000Z", key);
        Assert.assertEquals("20060106T140000", instance.getStart().toString());
        Assert.assertEquals("20060106T150000", instance.getEnd().toString());
    }

    /**
     * Tests exadate with timezone.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExdateWithTimezone() throws Exception {

        Calendar calendar = getCalendar("recurring_with_exdates.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }

    /**
     * Tests exdate UTC.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExdateUtc() throws Exception {

        Calendar calendar = getCalendar("recurring_with_exdates_utc.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }

    /**
     * Tests exdate no timezone.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExdateNoTimezone() throws Exception {

        Calendar calendar = getCalendar("recurring_with_exdates_floating.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T040000");
        DateTime end = new DateTime("20070609T040000");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T051500", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070605T051500", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }

    /**
     * Property Name: RDATE
     * <p/>
     * Purpose: This property defines the list of DATE-TIME values for recurring events, to-dos, journal entries, or
     * time zone definitions. Value Type: The default value type for this property is DATE-TIME. The value type can be
     * set to DATE or PERIOD. Description: This property can appear along with the "RRULE" property to define an
     * aggregate set of repeating occurrences. When they both appear in a recurring component, the recurrence instances
     * are defined by the union of occurrences defined by both the "RDATE" and "RRULE".
     * <p/>
     * The recurrence dates, if specified, are used in computing the recurrence set. The recurrence set is the complete
     * set of recurrence instances for a calendar component. The recurrence set is generated by considering the initial
     * "DTSTART" property along with the "RRULE", "RDATE", and "EXDATE" properties contained within the recurring
     * component. The "DTSTART" property defines the first instance in the recurrence set. The "DTSTART" property value
     * SHOULD match the pattern of the recurrence rule, if specified. The recurrence set generated with a "DTSTART"
     * property value that doesn't match the pattern of the rule is undefined. The final recurrence set is generated by
     * gathering all of the start DATE-TIME values generated by any of the specified "RRULE" and "RDATE" properties, and
     * then excluding any start DATE-TIME values specified by "EXDATE" properties. This implies that start DATE-TIME
     * values specified by "EXDATE" properties take precedence over those specified by inclusion properties (i.e.,
     * "RDATE" and "RRULE"). Where duplicate instances are generated by the "RRULE" and "RDATE" properties, only one
     * recurrence is considered. Duplicate instances are ignored.
     * <p/>
     * Example: The following are examples of this property:
     * <p/>
     * RDATE:19970714T123000Z RDATE;TZID=America/New_York:19970714T083000
     * <p/>
     * RDATE;VALUE=PERIOD:19960403T020000Z/19960403T040000Z, 19960404T010000Z/PT3H
     * <p/>
     * RDATE;VALUE=DATE:19970101,19970120,19970217,19970421
     * 19970526,19970704,19970901,19971014,19971128,19971129,19971225
     *
     * @throws Exception - if something is wrong this exception is thrown.
     * @see http://tools.ietf.org/html/rfc5545 3.8.5.2. Recurrence Date-Times
     */
    @Test
    public void testRdateWithTimezone() throws Exception {

        Calendar calendar = getCalendar("recurring_with_rdates.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(7, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070515T101500Z", key);
        Assert.assertEquals("20070515T051500", instance.getStart().toString());
        Assert.assertEquals("20070515T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070516T101500Z", key);
        Assert.assertEquals("20070516T051500", instance.getStart().toString());
        Assert.assertEquals("20070516T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070517T101500Z", key);
        Assert.assertEquals("20070517T101500Z", instance.getStart().toString());
        Assert.assertEquals("20070517T131500Z", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070522T101500Z", key);
        Assert.assertEquals("20070522T051500", instance.getStart().toString());
        Assert.assertEquals("20070522T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070523T101500Z", key);
        Assert.assertEquals("20070523T051500", instance.getStart().toString());
        Assert.assertEquals("20070523T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070605T101500Z", key);
        Assert.assertEquals("20070605T051500", instance.getStart().toString());
        Assert.assertEquals("20070605T061500", instance.getEnd().toString());
    }

    /**
     * Text exrule with timezone.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testExruleWithTimezone() throws Exception {

        Calendar calendar = getCalendar("recurring_with_exrule.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070509T090000Z");
        DateTime end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070515T101500Z", key);
        Assert.assertEquals("20070515T051500", instance.getStart().toString());
        Assert.assertEquals("20070515T061500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
    }

    /**
     * Tests all day recurring.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAllDayRecurring() throws Exception {

        Calendar calendar = getCalendar("allday_recurring.ics");

        InstanceList instances = new InstanceList();

        // need to normalize to local timezone to get test to pass
        // in mutliple timezones
        DateTime start = new DateTime(new Date("20070101").getTime() + 1000 * 60);
        DateTime end = new DateTime(new Date("20070103").getTime() + 1000 * 60);
        start.setUtc(true);
        end.setUtc(true);

        //  This fails when run in Australia/Sydney default timezone
        //DateTime start = new DateTime("20070101T090000Z");
        //DateTime end = new DateTime("20070103T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070102", key);
        Assert.assertEquals("20070102", instance.getStart().toString());
        Assert.assertEquals("20070103", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070103", key);
        Assert.assertEquals("20070103", instance.getStart().toString());
        Assert.assertEquals("20070104", instance.getEnd().toString());
    }

    /**
     * Tests all day recurring with exdates.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAllDayRecurringWithExDates() throws Exception {

        Calendar calendar = getCalendar("allday_recurring_with_exdates.ics");

        InstanceList instances = new InstanceList();
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        instances.setTimezone(tz);

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070106T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070102", key);
        Assert.assertEquals("20070102", instance.getStart().toString());
        Assert.assertEquals("20070103", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070106", key);
        Assert.assertEquals("20070106", instance.getStart().toString());
        Assert.assertEquals("20070107", instance.getEnd().toString());
    }

    /**
     * Tests all day reccuring with modes.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAllDayRecurringWithMods() throws Exception {

        Calendar calendar = getCalendar("allday_weekly_recurring_with_mods.ics");

        InstanceList instances = new InstanceList();
        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        instances.setTimezone(tz);

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070109T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(2, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070101", key);
        Assert.assertEquals("20070101", instance.getStart().toString());
        Assert.assertEquals("20070102", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070108", key);
        Assert.assertEquals("20070109", instance.getStart().toString());
        Assert.assertEquals("20070110", instance.getEnd().toString());
    }

    public void testDatesCompare() {

        try {
            Date d1 = new Date("20070105");
            Date d2 = new Date("20070105T010000");
            System.out.println("InstanceListTest.testDatesCompare()" + d1.getTime());
            System.out.println("InstanceListTest.testDatesCompare()" + d2.getTime());
            System.out.println("InstanceListTest.testDatesCompare()" + d1.before(d2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Tests all day recurring with timezone.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAllDayRecurringWithTimeZone() throws Exception {
        /*
         *
         *  Property Name: DTSTAMP   
         *  Purpose: The property indicates the date/time that the instance of
         *  the iCalendar object was created.
         *  
         *   DTSTART;VALUE=DATE:20070101 
         *   DTEND;VALUE=DATE:20070102 
         *   DTSTAMP:20070516T181406Z  
         *   RRULE:FREQ=DAILY;
         */
        Calendar calendar = getCalendar("allday_recurring.ics");

        InstanceList instances = new InstanceList();

        TimeZone tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Melbourne");//+10
        instances.setTimezone(tz);

        /*
         * This range in UTC translates to 20070103T010000 to 20070105T010000 in Australia/Melbourne local time.
         */
        DateTime start = new DateTime("20070102T140000Z");
        DateTime end = new DateTime("20070104T160000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070103", key);
        Assert.assertEquals("20070103", instance.getStart().toString());
        Assert.assertEquals("20070104", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070104", key);
        Assert.assertEquals("20070104", instance.getStart().toString());
        Assert.assertEquals("20070105", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070105", key);
        Assert.assertEquals("20070105", instance.getStart().toString());
        Assert.assertEquals("20070106", instance.getEnd().toString());
    }

    /**
     * Tests instance start before range.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testInstanceStartBeforeRange() throws Exception {

        Calendar calendar = getCalendar("recurring_with_exdates.ics");

        InstanceList instances = new InstanceList();

        // make sure startRange is after the startDate of an occurrence,
        // in this case the occurrence is at 20070529T101500Z
        DateTime start = new DateTime("20070529T110000Z");
        DateTime end = new DateTime("20070530T051500Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(1, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070529T101500Z", key);
        Assert.assertEquals("20070529T051500", instance.getStart().toString());
        Assert.assertEquals("20070529T061500", instance.getEnd().toString());
    }

    /**
     * Tests complicate recurring with timezone.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComplicatedRecurringWithTimezone() throws Exception {

        Calendar calendar = getCalendar("complicated_recurring.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(4, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070102T161500Z", key);
        Assert.assertEquals("20070102T101500", instance.getStart().toString());
        Assert.assertEquals("20070102T111500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070104T161500Z", key);
        Assert.assertEquals("20070104T101500", instance.getStart().toString());
        Assert.assertEquals("20070104T111500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070116T161500Z", key);
        Assert.assertEquals("20070116T101500", instance.getStart().toString());
        Assert.assertEquals("20070116T111500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070118T161500Z", key);
        Assert.assertEquals("20070118T101500", instance.getStart().toString());
        Assert.assertEquals("20070118T111500", instance.getEnd().toString());
    }

    /**
     * Tests complicated recurring all day.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComplicatedRecurringAllDay() throws Exception {

        Calendar calendar = getCalendar("complicated_allday_recurring.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20071201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(5, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070105", key);
        Assert.assertEquals("20070105", instance.getStart().toString());
        Assert.assertEquals("20070106", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070202", key);
        Assert.assertEquals("20070202", instance.getStart().toString());
        Assert.assertEquals("20070203", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070302", key);
        Assert.assertEquals("20070302", instance.getStart().toString());
        Assert.assertEquals("20070303", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070406", key);
        Assert.assertEquals("20070406", instance.getStart().toString());
        Assert.assertEquals("20070407", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070504", key);
        Assert.assertEquals("20070504", instance.getStart().toString());
        Assert.assertEquals("20070505", instance.getEnd().toString());
    }

    /**
     * Tests recurring with until.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRecurringWithUntil() throws Exception {

        Calendar calendar = getCalendar("recurring_until.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20070101T090000Z");
        DateTime end = new DateTime("20070201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(3, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070102T161500Z", key);
        Assert.assertEquals("20070102T101500", instance.getStart().toString());
        Assert.assertEquals("20070102T111500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070103T161500Z", key);
        Assert.assertEquals("20070103T101500", instance.getStart().toString());
        Assert.assertEquals("20070103T111500", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20070104T161500Z", key);
        Assert.assertEquals("20070104T101500", instance.getStart().toString());
        Assert.assertEquals("20070104T111500", instance.getEnd().toString());
    }

    /**
     * Tests recurrence expander by day.
     *
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRecurrenceExpanderByDay() throws Exception {

        Calendar calendar = getCalendar("recurring_by_day.ics");

        InstanceList instances = new InstanceList();

        DateTime start = new DateTime("20080720T170000Z");
        DateTime end = new DateTime("20080726T200000Z");

        addToInstanceList(calendar, instances, start, end);

        Assert.assertEquals(5, instances.size());

        Iterator<String> keys = instances.keySet().iterator();

        String key = null;
        Instance instance = null;

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20080721T180000Z", key);
        Assert.assertEquals("20080721T110000", instance.getStart().toString());
        Assert.assertEquals("20080721T113000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20080722T180000Z", key);
        Assert.assertEquals("20080722T110000", instance.getStart().toString());
        Assert.assertEquals("20080722T113000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20080723T180000Z", key);
        Assert.assertEquals("20080723T110000", instance.getStart().toString());
        Assert.assertEquals("20080723T113000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20080724T180000Z", key);
        Assert.assertEquals("20080724T110000", instance.getStart().toString());
        Assert.assertEquals("20080724T113000", instance.getEnd().toString());

        key = keys.next();
        instance = (Instance) instances.get(key);

        Assert.assertEquals("20080725T180000Z", key);
        Assert.assertEquals("20080725T110000", instance.getStart().toString());
        Assert.assertEquals("20080725T113000", instance.getEnd().toString());
    }

    /**
     * Adds to instance list.
     *
     * @param calendar  The calendar.
     * @param instances The instances.
     * @param start     The start.
     * @param end       The end.
     */
    private static void addToInstanceList(Calendar calendar,
                                          InstanceList instances, Date start, Date end) {
        ComponentList<VEvent> vevents = calendar.getComponents().getComponents(VEvent.VEVENT);
	Iterator<VEvent> it = vevents.iterator();
        boolean addedMaster = false;
        while (it.hasNext()) {
            VEvent event = (VEvent)it.next();
            if (event.getRecurrenceId() == null) {
                addedMaster = true;
                instances.addComponent(event, start, end);
            } else {
                Assert.assertTrue(addedMaster);
                instances.addOverride(event, start, end);
            }
        }
    }

    /**
     * Gets calendar.
     *
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream("instancelist/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        Calendar calendar = cb.build(in);
        return calendar;
    }

}
