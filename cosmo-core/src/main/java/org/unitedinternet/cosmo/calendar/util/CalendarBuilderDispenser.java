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
/**
 * 
 */
package org.unitedinternet.cosmo.calendar.util;

import net.fortuna.ical4j.data.CalendarBuilder;

/**
 * Utility for managing a per-thread singleton for
 * CalendarBuilder.  Constructing a CalendarBuilder every
 * time one is needed can be expensive if a lot of 
 * time zones are involved in the calendar data.
 */
public class CalendarBuilderDispenser {
    
    private static class ThreadLocalCalendarBuilder extends ThreadLocal {
        public Object initialValue() {
            return new CalendarBuilder();
        }
        
        public CalendarBuilder getBuilder() {
            return (CalendarBuilder) super.get();
        }
    }

    private static ThreadLocalCalendarBuilder builder = new ThreadLocalCalendarBuilder();

    /**
     * Return the CalendarBuilder singelton for the current thread
     * @return the calendarBuilder singleton.
     */
    public static CalendarBuilder getCalendarBuilder() {
        return builder.getBuilder();
    }
}
