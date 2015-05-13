/*
 * Copyright 2005-2007 Open Source Applications Foundation
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

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

/**
 * @author cyrusdaboo
 * 
 * This class represents an instance of a (possibly) recurring component.
 */
public class Instance {

    private Component comp;
    private Date start;
    private Date end;
    private Date rid;
    private boolean overridden;
    private boolean future;

    /**
     * @param comp The component.
     * @param end The date.
     * @param start The date.
     */
    public Instance(Component comp, Date start, Date end) {
        this(comp, start, end, start, false, false);
    }

    /**
     * @param comp The component.
     * @param start The date. 
     * @param end The date.
     * @param rid The date.
     * @param future Boolean.
     * @param overridden Boolean.
     */
    public Instance(Component comp, Date start, Date end, Date rid, boolean overridden, boolean future) {
        this.comp = comp;
        this.start = start;
        this.end = end;
        this.rid = copyNormalisedDate(rid);
        this.overridden = overridden;
        this.future = future;
    }

    /**
     * @return Returns the component.
     */
    public Component getComp() {
        return comp;
    }

    /**
     * @return Returns the start.
     */
    public Date getStart() {
        return start;
    }

    /**
     * @return Returns the end.
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @return Returns the rid.
     */
    public Date getRid() {
        return rid;
    }

    /**
     * @return Returns the overridden.
     */
    public boolean isOverridden() {
        return overridden;
    }

    /**
     * @return Returns the future.
     */
    public boolean isFuture() {
        return future;
    }

   
    /**
     * Copy a Date/DateTime and normalise to UTC if its not floating.
     * 
     * @param date The date.
     * @return The date.
     */
    private Date copyNormalisedDate(Date date) {
        if (date instanceof DateTime) {
            DateTime dt = new DateTime(date);
            if (!dt.isUtc() && dt.getTimeZone() != null) {
                dt.setUtc(true);
            }
            return dt;
        } else {
            return new Date(date);
        }
    }
}
