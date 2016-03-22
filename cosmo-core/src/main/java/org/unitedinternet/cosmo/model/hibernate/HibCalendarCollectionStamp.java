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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.hibernate.validator.Color;
import org.unitedinternet.cosmo.hibernate.validator.DisplayName;
import org.unitedinternet.cosmo.hibernate.validator.Timezone;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;


/**
 * Hibernate persistent CalendarCollectionStamp.
 */
@Entity
@DiscriminatorValue("calendar")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibCalendarCollectionStamp extends HibStamp implements ICalendarConstants, CalendarCollectionStamp {
    
    private static final long serialVersionUID = 4397868099410967516L;

    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_TIMEZONE = new HibQName(
            CalendarCollectionStamp.class, "timezone");
    
    public static final QName ATTR_CALENDAR_DESCRIPTION = new HibQName(
            CalendarCollectionStamp.class, "description");
    
    public static final QName ATTR_CALENDAR_LANGUAGE = new HibQName(
            CalendarCollectionStamp.class, "language");
    
    public static final QName ATTR_CALENDAR_COLOR = new HibQName(
            CalendarCollectionStamp.class, "color");
    
    public static final QName ATTR_CALENDAR_VISIBILITY = new HibQName(
            CalendarCollectionStamp.class, "visibility");
    
    public static final QName ATTR_CALENDAR_TARGET_URI = new HibQName(
            CalendarCollectionStamp.class, "targetUri");
   
    /** default constructor */
    public HibCalendarCollectionStamp() {
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "calendar";
    }
    
    public HibCalendarCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }

    public Stamp copy() {
        CalendarCollectionStamp stamp = new HibCalendarCollectionStamp();
        return stamp;
    }
        
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getDescription()
     */
    public String getDescription() {
        // description stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        // description stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_DESCRIPTION, description);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getLanguage()
     */
    public String getLanguage() {
        // language stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_LANGUAGE);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#setLanguage(java.lang.String)
     */
    public void setLanguage(String language) {
        // language stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_LANGUAGE, language);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getTimezoneCalendar()
     */
    @Timezone
    public Calendar getTimezoneCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return HibICalendarAttribute.getValue(getItem(), ATTR_CALENDAR_TIMEZONE);
    }
    
    @Override
    public void setTargetUri(String targetUri) {
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_TARGET_URI, targetUri);
    }
    
    @Override
    public String getTargetUri() {
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_TARGET_URI);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getTimezone()
     */
    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getTimezoneName()
     */
    public String getTimezoneName() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        return timezone.getComponents().getComponent(Component.VTIMEZONE).
            getProperties().getProperty(Property.TZID).getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#setTimezoneCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void setTimezoneCalendar(Calendar timezone) {
        // timezone stored as ICalendarAttribute on Item
        HibICalendarAttribute.setValue(getItem(), ATTR_CALENDAR_TIMEZONE, timezone);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getEventStamps()
     */
    public Set<EventStamp> getEventStamps() {
        Set<EventStamp> events = new HashSet<EventStamp>();
        for (Iterator<Item> i= ((CollectionItem) getItem()).getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            Stamp stamp = child.getStamp(EventStamp.class);
            if(stamp!=null) {
                events.add((EventStamp) stamp);
            }
        }
        return events;
    }

    
    /**
     * Return CalendarCollectionStamp from Item
     * @param item
     * @return CalendarCollectionStamp from Item
     */
    public static CalendarCollectionStamp getStamp(Item item) {
        return (CalendarCollectionStamp) item.getStamp(CalendarCollectionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }

    @Override
    @Color
    public String getColor() {
        // color stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_COLOR);
    }

    @Override
    public void setColor(String color) {
        // color stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_COLOR, color);
        
    }

    @Override
    public Boolean getVisibility() {
        // color stored as BooleanAttribute on Item
        return HibBooleanAttribute.getValue(getItem(), ATTR_CALENDAR_VISIBILITY);
    }

    @Override
    public void setVisibility(Boolean visibility) {
        // color stored as BooleanAttribute on Item
        HibBooleanAttribute.setValue(getItem(), ATTR_CALENDAR_VISIBILITY, visibility);
        
    }

    @Override
    @DisplayName
    public String getDisplayName() {
        return getItem().getDisplayName();
    }

    @Override
    public void setDisplayName(String displayName) {
        getItem().setDisplayName(displayName);
    }
}
