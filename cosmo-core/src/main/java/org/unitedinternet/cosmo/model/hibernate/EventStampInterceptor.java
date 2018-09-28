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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.Serializable;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.calendar.util.Dates;
import org.unitedinternet.cosmo.model.EventStamp;

/**
 * Hibernate Interceptor that updates BaseEventStamp timeRangeIndexes.
 */
@Component
public class EventStampInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 5339230223113722458L;

    @Override
    public boolean onFlushDirty(Object object, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        if(! (object instanceof HibBaseEventStamp)) {
            return false;
        }
        
        // calculate time-range-index
        HibBaseEventStamp es = (HibBaseEventStamp) object;
        HibEventTimeRangeIndex index = calculateEventStampIndexes(es);
        
        if(index==null) {
            return false;
        }
        
        // update modifiedDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "timeRangeIndex".equals( propertyNames[i] ) ) {
                currentState[i] = index;
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean onSave(Object object, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        
        if(! (object instanceof HibBaseEventStamp)) {
            return false;
        }
        
        // calculate time-range-index
        HibBaseEventStamp es = (HibBaseEventStamp) object;
        HibEventTimeRangeIndex index = calculateEventStampIndexes(es);
        
        if(index==null) {
            return false;
        }
        
        // update modifiedDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "timeRangeIndex".equals( propertyNames[i] ) ) {
                state[i] = index;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Update the TimeRangeIndex property of the BaseEventStamp.
     * For recurring events, this means calculating the first start date
     * and the last end date for all occurences.
     */
    protected HibEventTimeRangeIndex calculateEventStampIndexes(HibBaseEventStamp eventStamp) {
        Date startDate = eventStamp.getStartDate();
        Date endDate = eventStamp.getEndDate();
        
        // Handle "missing" endDate
        if(endDate==null && eventStamp instanceof HibEventExceptionStamp ) {
            // For "missing" endDate, get the duration of the master event
            // and use with the startDate of the modification to calculate
            // the endDate of the modification
            HibEventExceptionStamp exceptionStamp = (HibEventExceptionStamp) eventStamp;
            EventStamp masterStamp = exceptionStamp.getMasterStamp();
            
            // Make sure master EventStamp exists
            if(masterStamp!=null) {
                Dur duration = masterStamp.getDuration();
                if(duration!=null) {
                    endDate = Dates.getInstance(duration.getTime(startDate), startDate);
                }
            }
        }
        
        
        boolean isRecurring = false;
        
        if (eventStamp.isRecurring()) {
            isRecurring = true;
            RecurrenceExpander expander = new RecurrenceExpander();
            Date[] range = expander
                    .calculateRecurrenceRange(eventStamp.getEventCalendar());
            startDate = range[0];
            endDate = range[1];
        } else {
            // If there is no end date, then its a point-in-time event
            if (endDate == null) {
                endDate = startDate;
            }
        }
        
        boolean isFloating = false;
        
        // must have start date
        if(startDate==null) {
            return null;
        }
        
        // A floating date is a DateTime with no timezone, or
        // a Date
        if(startDate instanceof DateTime) {
            DateTime dtStart = (DateTime) startDate;
            if(dtStart.getTimeZone()==null && !dtStart.isUtc()) {
                isFloating = true;
            }
        } else {
            // Date instances are really floating because you can't pin
            // the a date like 20070101 to an instant without first
            // knowing the timezone
            isFloating = true;
        }
        
        HibEventTimeRangeIndex timeRangeIndex = new HibEventTimeRangeIndex();
        timeRangeIndex.setStartDate(fromDateToStringNoTimezone(startDate));
        
        
        // A null endDate equates to infinity, which is represented by
        // a String that will always come after any date when compared.
        if(endDate!=null) {
            timeRangeIndex.setEndDate(fromDateToStringNoTimezone(endDate));
        }
        else {
            timeRangeIndex.setEndDate(HibEventStamp.TIME_INFINITY);
        }
        
        timeRangeIndex.setIsFloating(isFloating);
        timeRangeIndex.setIsRecurring(isRecurring);
        
        return timeRangeIndex;
    }
    
    private String fromDateToStringNoTimezone(Date date) {
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

}
