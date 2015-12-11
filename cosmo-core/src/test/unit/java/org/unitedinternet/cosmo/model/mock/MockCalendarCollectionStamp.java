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
package org.unitedinternet.cosmo.model.mock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitedinternet.cosmo.hibernate.validator.Timezone;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;

/**
 * Represents a Calendar Collection.
 */
@SuppressWarnings("serial")
public class MockCalendarCollectionStamp extends MockStamp
        implements java.io.Serializable, ICalendarConstants, CalendarCollectionStamp {

    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_TIMEZONE = new MockQName(CalendarCollectionStamp.class, "timezone");

    public static final QName ATTR_CALENDAR_DESCRIPTION = new MockQName(CalendarCollectionStamp.class, "description");

    public static final QName ATTR_CALENDAR_LANGUAGE = new MockQName(CalendarCollectionStamp.class, "language");

    public static final QName ATTR_CALENDAR_COLOR = new MockQName(CalendarCollectionStamp.class, "color");

    public static final QName ATTR_CALENDAR_VISIBILITY = new MockQName(CalendarCollectionStamp.class, "visibility");

    public static final QName ATTR_CALENDAR_DISPLAY_NAME = new MockQName(CalendarCollectionStamp.class, "displayName");

    public static final QName ATTR_CALENDAR_TARGET_URI = new MockQName(CalendarCollectionStamp.class, "targetUri");

    /** default constructor */
    public MockCalendarCollectionStamp() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getType()
     */
    /**
     * Gets type.
     * 
     * @return The type.
     */
    public String getType() {
        return "calendar";
    }

    /**
     * Constructor.
     * 
     * @param collection
     *            The collection item.
     */
    public MockCalendarCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }

    /**
     * Copy. {@inheritDoc}
     * 
     * @return stamp
     */
    public Stamp copy() {
        CalendarCollectionStamp stamp = new MockCalendarCollectionStamp();
        return stamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getDescription()
     */
    /**
     * Gets description.
     * 
     * @return The description.
     */
    public String getDescription() {
        // description stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_DESCRIPTION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#setDescription(java.lang.String)
     */
    /**
     * Sets description.
     * 
     * @param description
     *            - The description.
     * 
     */
    public void setDescription(String description) {
        // description stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_DESCRIPTION, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getLanguage()
     */
    /**
     * Gets language.
     * 
     * @return The language.
     */
    public String getLanguage() {
        // language stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_LANGUAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#setLanguage(java.lang.String)
     */
    /**
     * Sets language.
     * 
     * @param language
     *            The language
     */
    public void setLanguage(String language) {
        // language stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_LANGUAGE, language);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezoneCalendar()
     */
    /**
     * Gets timezone.
     * 
     * @return calendar.
     */
    @Timezone
    public Calendar getTimezoneCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(getItem(), ATTR_CALENDAR_TIMEZONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezone()
     */
    /**
     * Gets timezone.
     * 
     * @return timezone.
     */
    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezoneName()
     */
    /**
     * Gets timezone name.
     * 
     * @return The timezone.
     */
    public String getTimezoneName() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        return timezone.getComponents().getComponent(Component.VTIMEZONE).getProperties().getProperty(Property.TZID)
                .getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#setTimezoneCalendar(net.fortuna.ical4j.model
     * .Calendar)
     */
    /**
     * Sets timezone calendar.
     * 
     * @param timezone
     *            The timezone.
     */
    public void setTimezoneCalendar(Calendar timezone) {
        // timezone stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(getItem(), ATTR_CALENDAR_TIMEZONE, timezone);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.CalendarCollectionStamp#getEventStamps()
     */
    /**
     * Gets event stamps.
     * 
     * @return The events.
     */
    public Set<EventStamp> getEventStamps() {
        Set<EventStamp> events = new HashSet<EventStamp>();
        for (Iterator<Item> i = ((CollectionItem) getItem()).getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            Stamp stamp = child.getStamp(EventStamp.class);
            if (stamp != null) {
                events.add((EventStamp) stamp);
            }
        }
        return events;
    }

    /**
     * Return CalendarCollectionStamp from Item
     * 
     * @param item
     *            The item
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
    public String getColor() {
        // color stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_COLOR);
    }

    @Override
    public void setColor(String color) {
        // color stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_COLOR, color);

    }

    @Override
    public Boolean getVisibility() {
        // color stored as BooleanAttribute on Item
        return MockBooleanAttribute.getValue(getItem(), ATTR_CALENDAR_VISIBILITY);
    }

    @Override
    public void setVisibility(Boolean visibility) {
        // color stored as BooleanAttribute on Item
        MockBooleanAttribute.setValue(getItem(), ATTR_CALENDAR_VISIBILITY, visibility);

    }

    @Override
    public String getDisplayName() {
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(String displayName) {
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_DISPLAY_NAME, displayName);

    }

    @Override
    public String getTargetUri() {
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_TARGET_URI);
    }

    @Override
    public void setTargetUri(String targetUri) {
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_TARGET_URI, targetUri);
    }
}
