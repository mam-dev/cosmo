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

import java.io.InputStream;

import net.fortuna.ical4j.model.Calendar;

/**
 * Attribute that stores an icalendar (.ics) value.
 */
public interface ICalendarAttribute extends Attribute{

    public Calendar getValue();

    public void setValue(Calendar value);

    /**
     * Set Calendar value with string
     * @param calendar string
     */
    public void setValue(String value);

    /**
     * Set Calendar value with inputstream
     * @param calendar string
     */
    public void setValue(InputStream is);

}