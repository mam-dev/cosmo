/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.hibernate;

import java.text.ParseException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ModificationUid;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

/**
 * Represents a uid for a NoteItem modification.
 * The uid of a NoteItem modification is composed of
 * the parent uid and the recurrenceId of the modification item.
 * If the recurrenceId of a modificatino contains timezone 
 * information it will be converted to UTC when the uid is
 * serialized to a String.
 * Examples:
 * <p>
 * <code>f2953300-6fcb-4547-ad7b-22e193b6903f:20070101<code><br/>
 * <code>f2953300-6fcb-4547-ad7b-22e193b6903f:20070101T100000<code><br/>
 * <code>f2953300-6fcb-4547-ad7b-22e193b6903f:20070101T100000Z<code>
 * </p>
 */
public class ModificationUidImpl implements ModificationUid {
    
    String parentUid = null;
    Date recurrenceId = null;
    
    /**
     * Construct modification uid from parent Item and recurrenceId
     * @param parent parent item
     * @param recurrenceId recurrenceId of modification
     */
    public ModificationUidImpl(Item parent, Date recurrenceId) {
        this.parentUid = parent.getUid();
        this.recurrenceId = recurrenceId;
    }

    public ModificationUidImpl(Item parent, String recurrenceId)
        throws ParseException {
        this(parent, fromStringToDate(recurrenceId));
    }

    /**
     * Construct modification uid from String.
     * @param modUid modification uid
     * @throws ModelValidationException
     */
    public ModificationUidImpl(String modUid) {
        String[] split = modUid.split(RECURRENCEID_DELIMITER);
        if(split.length!=2) {
            throw new ModelValidationException("invalid modification uid");
        }
        parentUid = split[0];
        try {
            recurrenceId = fromStringToDate(split[1]);
        } catch (ParseException e) {
            throw new ModelValidationException("invalid modification uid");
        }
    }
    
    public String toString() {
        return parentUid + RECURRENCEID_DELIMITER
                + fromDateToStringNoTimezone(recurrenceId);
    }
    
    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public Date getRecurrenceId() {
        return recurrenceId;
    }

    public void setRecurrenceId(Date recurrenceId) {
        this.recurrenceId = recurrenceId;
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof ModificationUidImpl) {
            ModificationUidImpl modUid = (ModificationUidImpl) obj;
            return ObjectUtils.equals(parentUid, modUid.getParentUid()) &&
                   ObjectUtils.equals(recurrenceId, modUid.getRecurrenceId());
        }
        return super.equals(obj);
    }
    
    public final int hashCode() {
        return new HashCodeBuilder().append(getParentUid())
                .append(recurrenceId).toHashCode();
    }

    /**
     * Converts an ical4j Date instance to a string representation.  If the instance
     * is a DateTime and has a timezone, then the string representation will be
     * UTC.
     * @param date date to format
     * @return string representation of date
     */
    public static String fromDateToStringNoTimezone(Date date) {
        if(date==null) {
            return null;
        }
        
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            // If DateTime has a timezone, then convert to UTC before
            // serializing as String.
            if(dt.getTimeZone()!=null) {
                // clone instance first to prevent changes to original instance
                DateTime copy = new DateTime(dt);
                copy.setUtc(true);
                return copy.toString();
            } else {
                return dt.toString();
            }
        } else {
            return date.toString();
        }
    }
    
    /**
     * Returns an ical4j Date instance for the following formats:
     * <p>
     * <code>20070101T073000Z</code><br/>
     * <code>20070101T073000</code><br/>
     * <code>20070101</code>
     * <p>
     * @param dateStr date string to parse
     * @return ical4j Date instance
     * @throws ParseException
     */
    public static Date fromStringToDate(String dateStr) throws ParseException {
        if(dateStr==null) {
            return null;
        }
        
        // handle date with no time first
        if(dateStr.indexOf("T") ==  -1) { 
            return new Date(dateStr);
        }
        // otherwise it must be a date time
        else {
            return new DateTime(dateStr);
        }
    }
}
