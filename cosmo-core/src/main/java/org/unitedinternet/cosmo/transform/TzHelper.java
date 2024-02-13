/*
 * TzHelper.java Feb 21, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.transform;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.DateProperty;

/**
 * Helper class containing methods for fixing inconsistencies time zone related issues.
 * 
 * @author corneliu dobrota
 * @author daniel grigore
 *
 */
public class TzHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TzHelper.class);

    private static final String MS_TIMEZONES_FILE = "msTimezones";
    private static final Map<String, String> MS_TIMEZONE_IDS = new HashMap<String, String>();
    private static final Map<String, String> MS_TIMEZONE_NAMES = new HashMap<String, String>();

    private static final TimeZoneRegistry TIMEZONE_REGISTRY = DefaultTimeZoneRegistryFactory.getInstance()
            .createRegistry();

    private static final String[] PROPERTIES_WITH_TIMEZONES = { Property.DTSTART, Property.DTEND, Property.EXDATE,
            Property.RDATE, Property.RECURRENCE_ID };

    static {
        initMsTimezones();
    }

    private static void initMsTimezones() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(TzHelper.class.getResourceAsStream(MS_TIMEZONES_FILE));
            while (scanner.hasNext()) {
                String[] arr = scanner.nextLine().split("=");
                String standardTzId = arr[1];
                String displayNameAndMsTzId[] = arr[0].split(";");
                MS_TIMEZONE_NAMES.put(displayNameAndMsTzId[0], standardTzId);
                MS_TIMEZONE_IDS.put(displayNameAndMsTzId[1], standardTzId);
            }
        } catch (Exception e) { // avoid NoClassDefFoundError
            LOG.error("Could not load MS timezones", e);
            throw new RuntimeException("Unable to load resource file " + MS_TIMEZONES_FILE, e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    public static void correctTzParameterFrom(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        VTimeZone vTimeZone = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
        if (vTimeZone != null && vTimeZone.getTimeZoneId() != null) {
            correctTzValueOf(vTimeZone.getTimeZoneId());
        }
        List<CalendarComponent> vEventList = calendar.getComponents(Component.VEVENT);
        for (CalendarComponent vEvent : vEventList) {
            correctTzParameterFrom(vEvent);
        }
    }

    private static void correctTzParameterFrom(CalendarComponent component) {
        for (String propertyName : PROPERTIES_WITH_TIMEZONES) {
            List<Property> props = component.getProperties(propertyName);
            for (Property p : props) {
                if (p instanceof DateProperty) {
                    correctTzParameterFrom((DateProperty) p);
                } else {
                    correctTzParameterFrom(p);
                }
            }
        }
    }

    private static void correctTzParameterFrom(Property property) {
        if (property.getParameter(Parameter.TZID) != null) {
            String newTimezoneId = getCorrectedTimezoneFromTzParameter(property);
            correctTzParameter(property, newTimezoneId);
        }
    }

    public static void correctTzParameterFrom(DateProperty property) {
        if (property.getValue() != null && property.getValue().endsWith("Z")) {
            property.getParameters().removeAll(Parameter.TZID);
            return;
        }
        if (!(property.getDate() instanceof DateTime)) {
            // DATE types don't contain time zone
            property.getParameters().removeAll(Parameter.TZID);
            return;
        }
        if (property.getParameter(Parameter.TZID) != null) {
            String newTimezone = getCorrectedTimezoneFromTzParameter(property);
            String value = property.getValue();
            correctTzParameter(property, newTimezone);
            if (newTimezone != null) {
                property.setTimeZone(TIMEZONE_REGISTRY.getTimeZone(newTimezone));
                try {
                    property.setValue(value);
                } catch (ParseException e) {
                    LOG.warn("Failed to reset property value", e);
                }
            } else {
                property.setUtc(true);
            }
        }
    }

    private static void correctTzParameter(Property property, String newTimezoneId) {
        property.getParameters().removeAll(Parameter.TZID);
        if (newTimezoneId != null) {
            property.getParameters().add(new TzId(newTimezoneId));
        }
    }

    private static String getCorrectedTimezoneFromTzParameter(Property property) {
        String tzIdValue = property.getParameter(Parameter.TZID).getValue();
        return getCorrectedTimeZoneIdFrom(tzIdValue);
    }

    private static void correctTzValueOf(net.fortuna.ical4j.model.property.TzId tzProperty) {
        String validTimezone = getCorrectedTimeZoneIdFrom(tzProperty.getValue());
        if (validTimezone != null) {
            tzProperty.setValue(validTimezone);
        }
    }

    /**
     * Gets a valid timezoneId for the specified timezoneValue or <code>null</code> in case the specified time zone
     * value does not match anything known.
     * 
     * @param value time zone value read from ICS file. The value can be a Microsoft time zone id or an invalid time
     *              zone value
     * @return a valid timezoneId for the specified timezoneValue or <code>null</code> in case the specified time zone
     *         value does not match anything known
     */
    private static String getCorrectedTimeZoneIdFrom(String value) {
        if (value != null) {
            value = value.contains("\"") ? value.replaceAll("\"", "") : value;
            if (TIMEZONE_REGISTRY.getTimeZone(value) != null) {
                return TIMEZONE_REGISTRY.getTimeZone(value).getID();
            }
            String nameCandidate = MS_TIMEZONE_NAMES.get(value);
            if (nameCandidate != null) {
                return TIMEZONE_REGISTRY.getTimeZone(nameCandidate) != null
                        ? TIMEZONE_REGISTRY.getTimeZone(nameCandidate).getID()
                        : nameCandidate;
            }
            return MS_TIMEZONE_IDS.get(value);
        }
        return null;
    }
}