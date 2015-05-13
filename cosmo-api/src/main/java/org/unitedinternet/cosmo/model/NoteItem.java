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

import java.io.Reader;
import java.util.Date;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;


/**
 * Extends {@link ICalendarItem} to represent a Note item.
 * A note is the basis for a pim item.
 */
public interface NoteItem extends ICalendarItem{

    // Property accessors
    public String getBody();

    public void setBody(String body);

    public void setBody(Reader body);

    public Date getReminderTime();

    public void setReminderTime(Date reminderTime);

    /**
     * Return the Calendar object containing a VTODO component.
     * @return calendar
     */
    public Calendar getTaskCalendar();

    /**
     * Set the Calendar object containing a VOTODO component.
     * This allows non-standard icalendar properties to be stored 
     * with the task.
     * @param calendar
     */
    public void setTaskCalendar(Calendar calendar);

    public Set<NoteItem> getModifications();

    public void addModification(NoteItem mod);

    public boolean removeModification(NoteItem mod);

    public void removeAllModifications();

    public NoteItem getModifies();

    public void setModifies(NoteItem modifies);

}
