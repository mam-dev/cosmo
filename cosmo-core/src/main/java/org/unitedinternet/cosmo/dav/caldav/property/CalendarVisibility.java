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
package org.unitedinternet.cosmo.dav.caldav.property;

import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.XCaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;

/**
 * Represents the <code>XC:calendar-visibility</code> property.
 */
public class CalendarVisibility extends StandardDavProperty
    implements CaldavConstants {

    /**
     * Constructor.
     * @param visibility The calendar visibility property.
     */
    public CalendarVisibility(boolean visibility) {
        super(XCaldavConstants.CALENDAR_VISIBLE, visibility);
    }
}
