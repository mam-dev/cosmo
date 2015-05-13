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

import java.util.Calendar;

/**
 * Attribute that stores Calendar value.
 */
public interface CalendarAttribute extends Attribute{

    // Property accessors
    public Calendar getValue();

    public void setValue(Calendar value);

    /**
     * Set Calendar value with a date string
     * that is in one of the following formats:
     * 
     * 2002-10-10T00:00:00+05:00
     * 2002-10-09T19:00:00Z
     * 2002-10-10T00:00:00GMT+05:00
     * 
     * @param date string
     */
    public void setValue(String value);

}