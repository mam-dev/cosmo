/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.calendar.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.Observance;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.RRule;

import org.unitedinternet.cosmo.CosmoException;

import com.ibm.icu.util.TimeZone;

/**
 * Contains utility methods for working with ical4j TimeZone and
 * VTimeZone objects, using icu4j built in timezone definitions.
 */
public class TimeZoneUtils {
    
    private static Set<String> allTimezoneIds = new HashSet<String>();
    public static final long ONE_DAY = 60*1000*60*24;
    
    // represents 1998
    static final long TIMEZONE_START_DATE = 883612800000L;
    
    // index all timezone ids from icu registry
    static
    {
        for(String id: TimeZone.getAvailableIDs()) {
            allTimezoneIds.add(id);
        }
        
        allTimezoneIds = Collections.unmodifiableSet(allTimezoneIds);
    }
    
    /**
     * @return all available timezone ids
     */
    public static Set<String> getTimeZoneIds() {
        return allTimezoneIds;
    }
    
    /**
     * Return ical4j TimeZone instance for timezone id.
     * @param id timezone id
     * @return ical4j TimeZone instance
     */
    public static net.fortuna.ical4j.model.TimeZone getTimeZone(String id) {
        if(!allTimezoneIds.contains(id)) {
            return null;
        } 
        
        VTimeZone vtz = getVTimeZone(id);
        if(vtz==null) {
            return null;
        }
        
        return new net.fortuna.ical4j.model.TimeZone(vtz);
    }
    
    /**
     * Return ical4j VTimeZone instance for timezone id
     * @param id timezone id
     * @return ical4j VTimeZone instance
     */
    public static VTimeZone getVTimeZone(String id) {
        if(!allTimezoneIds.contains(id)) {
            return null;
        } 
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(id);
        if(icuvtz==null) {
            return null;
        }
        
        try {
            StringWriter sw = new StringWriter();
            icuvtz.write(sw, TIMEZONE_START_DATE);
            VTimeZone vtz = (VTimeZone) CalendarUtils.parseComponent(sw.toString()); 
            fixIcuVTimeZone(vtz);
            return vtz;
        } catch (ParserException | IOException e) {
            throw new CosmoException(e);
        }
    }
    
    /**
     * ICU4J generates VTIMEZONE RRULEs with floating UNTIL, which results
     * in a bad VTIMEZONE because the UNTIL is converted to UTC, which will
     * be different depending on the default timezone of the server.  So
     * to fix this, always add a day to UNTIL to make sure the RRULE
     * doesn't fall short.  This should work for most timezones as timezones
     * don't usually change from day to day.  Hopefully this is fixed in
     * an icu4j update.
     * @param vtz
     */
    protected static void fixIcuVTimeZone(VTimeZone vtz) {
        for(Iterator<Observance> obIt = vtz.getObservances().iterator();obIt.hasNext();) {
            PropertyList<RRule> rruleList= obIt.next().getProperties(Property.RRULE);
            for(RRule rrule: rruleList) {
                Recur recur = rrule.getRecur();
                if(recur.getUntil()!=null) {
                    recur.getUntil().setTime(recur.getUntil().getTime() + ONE_DAY);
                }
            }
        }
    }
    
    
    /**
     * Return simple ical4j VTimeZone instance (including only relevant
     * rules for given date.
     * @param id timezone id
     * @param time time in milliseconds to base rules off of
     * @return ical4j VTimeZone instance
     */
    public static VTimeZone getSimpleVTimeZone(String id, long time) {
        if(!allTimezoneIds.contains(id)) {
            return null;
        }
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(id);
        if(icuvtz==null) {
            return null;
        }
        
        StringWriter sw = new StringWriter();
        
        try {
            icuvtz.writeSimple(sw, time);
            VTimeZone vtz = (VTimeZone) CalendarUtils.parseComponent(sw.toString()); 
            return vtz;
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }
    
    /**
     * Return equivalent Olson timezone id that corresponds to given 
     * VTIMEZONE definition
     * @param vtz VTIMEZONE
     * @return equivalent timezone id (null if no equivalent timezone found)
     */
    public static String getEquivalentTimeZoneId(VTimeZone vtz) {
        
        com.ibm.icu.util.VTimeZone icuvtz = com.ibm.icu.util.VTimeZone.create(new StringReader(vtz.toString()));
        for(String id: allTimezoneIds) {
            TimeZone tz = TimeZone.getTimeZone(id);
            if(icuvtz.hasSameRules(tz)) {
                return id;
            }
        }
        
        return null;
    }
}
