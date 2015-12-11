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

import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZone;


/**
 * Stamp that can be added to CollectionItem that stores
 * CalendarCollection specific attributes.
 */
public interface CalendarCollectionStamp extends Stamp{

    public String getDescription();

    public void setDescription(String description);

    public String getLanguage();

    public void setLanguage(String language);

    /**
     * @return calendar object representing timezone
     */
    public Calendar getTimezoneCalendar();

    /**
     * @return timezone if present
     */
    public TimeZone getTimezone();

    /**
     * @return name of timezone if one is set
     */
    public String getTimezoneName();

    /**
     * Set timezone definition for calendar.
     * 
     * @param timezone
     *            timezone definition in ical format
     */
    public void setTimezoneCalendar(Calendar timezone);
    
    /**
     * Return a set of all EventStamps for the collection's children.
     * @return set of EventStamps contained in children
     */
    public Set<EventStamp> getEventStamps();
    
    

    public String getColor();


    public void setColor(String color);


    public Boolean getVisibility();


    public void setVisibility(Boolean visibility);


    public String getDisplayName();

    public void setDisplayName(String displayName) ;
    
    public String getTargetUri();
    
    public void setTargetUri(String targetUri);
}
