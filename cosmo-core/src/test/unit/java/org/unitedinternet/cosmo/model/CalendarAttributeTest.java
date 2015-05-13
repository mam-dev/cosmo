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
package org.unitedinternet.cosmo.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.mock.MockCalendarAttribute;

/**
 * Test CalendarAttribute
 */
public class CalendarAttributeTest {
   
    /**
     * Tests date with timezone.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDateWithTimezone() throws Exception {
        
        String test1 = "2002-10-10T00:00:00+05:00";
        String test2 = "2002-10-09T19:00:00Z";
        String test3 = "2002-10-10T00:00:00GMT+05:00";
        
        CalendarAttribute ca1 = new MockCalendarAttribute();
        CalendarAttribute ca2 = new MockCalendarAttribute();
        CalendarAttribute ca3 = new MockCalendarAttribute();
        
        ca1.setValue(test1);
        Calendar cal1 = ca1.getValue();
        
        ca2.setValue(test2);
        Calendar cal2 = ca2.getValue();
        
        ca3.setValue(test3);
        Calendar cal3 = ca3.getValue();
        
        Assert.assertEquals(cal1.getTimeZone().getID(), "GMT+05:00");
        Assert.assertEquals(cal2.getTimeZone().getID(), "GMT-00:00");
        Assert.assertEquals(cal3.getTimeZone().getID(), "GMT+05:00");
        Assert.assertEquals(cal1.getTime().getTime(), cal2.getTime().getTime());
        Assert.assertEquals(cal1.getTime().getTime(), cal3.getTime().getTime());
        Assert.assertEquals(test1, ca3.toString());
        
        Assert.assertFalse(cal1.equals(cal2));
        Assert.assertTrue(cal1.equals(cal3));
        
        Calendar cal4 = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal4.setTime(cal1.getTime());
        
        Assert.assertEquals(cal4.getTimeZone().getID(), cal1.getTimeZone().getID());
        Assert.assertEquals(cal4.getTime().getTime(), cal1.getTime().getTime());
        Assert.assertTrue(cal1.equals(cal4));
    }
    
}
