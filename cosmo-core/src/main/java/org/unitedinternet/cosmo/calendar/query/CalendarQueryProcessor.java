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
package org.unitedinternet.cosmo.calendar.query;

import java.util.Set;

import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.User;

/**
 * <p>
 * A component that accepts queries formulated against the iCalendar data
 * model and processes them against the Cosmo data model. A query processor
 * supports the following types of queries:
 * </p>
 * <dl>
 * <dt>General calendar query</dt>
 * <dd>Finds items that match one or more iCalendar components, properties
 * and/or parameters. The items may be required to occur within a specified
 * time period. Query criteria are expressed using a
 * {@link CalendarFilter}.</dd>
 * <dt>Free-busy query</dt>
 * <dd>Returns a description of the free-busy periods for items that occur
 * within a specified time period.</dd>
 * </dl>
 * <p>
 * Calendar queries will only ever match instances of {@link ICalendarItem}.
 * </p>
 */
public interface CalendarQueryProcessor { 
 
    /**
     * <p>
     * Executes a general calendar query against a collection. Returns all
     * members that match the provided filter.
     * </p>
     * @param collection The collection.
     * @param filter The calendar filter.
     * @return All members that match the provided filter.
     */
    Set<ICalendarItem> filterQuery(CollectionItem collection, CalendarFilter filter);

    /**
     * <p>
     * Executes a general calendar query against an item. Returns true if the
     * item matches the provided filter.
     * </p>
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return True if the item matches the provided filter.
     */
    boolean filterQuery(ICalendarItem item, CalendarFilter filter);
    
    /**
     * <p>
     * Executes a free-busy query against a User. Returns a
     * <code>VFREEBUSY</code> component containing the aggregate free-busy
     * periods for every member of every collection that has an occurrence
     * during the given period.
     * </p>
     * @param user The user.
     * @param period The period.
     * @return VFreeBusy.
     */
    VFreeBusy freeBusyQuery(User user, Period period);

    /**
     * <p>
     * Executes a free-busy query against a collection. Returns a
     * <code>VFREEBUSY</code> component containing the aggregate free-busy
     * periods for every member of the collection that has an occurrence
     * during the given period.
     * </p>
     * @param collection The collection item.
     * @param period The period
     * @return VFeeBusy.
     */
    VFreeBusy freeBusyQuery(CollectionItem collection, Period period);

    /**
     * <p>
     * Executes a free-busy query against an item. Returns a
     * <code>VFREEBUSY</code> component containing the free-busy periods for
     * the item during the given period.
     * </p>
     * @param item The ICalendar item.
     * @param period The period.
     * @return VFreeBusy.
     */
    VFreeBusy freeBusyQuery(ICalendarItem item, Period period);
}
