package org.unitedinternet.cosmo.util;

import static net.fortuna.ical4j.model.Property.DTEND;
import static net.fortuna.ical4j.model.Property.DTSTART;
import static net.fortuna.ical4j.model.Property.EXDATE;
import static net.fortuna.ical4j.model.Property.RECURRENCE_ID;
import static net.fortuna.ical4j.model.Property.RRULE;
import static net.fortuna.ical4j.model.Property.UID;

import java.util.Arrays;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

/**
 * 
 * @author daniel grigore
 *
 */
public final class FreeBusyUtil {

    private static final String FREE_BUSY_X_PROPERTY = Property.EXPERIMENTAL_PREFIX + "FREE-BUSY";

    private static final List<String> FREE_BUSY_ALLOWED_PROPERTIES = Arrays
            .asList(new String[] { UID, DTSTART, DTEND, RRULE, RECURRENCE_ID, EXDATE });

    public static final String FREE_BUSY_TEXT = "Busy";

    private FreeBusyUtil() {

    }

    public static Calendar getFreeBusyCalendar(Calendar original, String productId) {
        return getFreeBusyCalendar(original, productId, FREE_BUSY_TEXT);
    }

    /**
     * Obfuscates the specified calendar by removing unnecessary properties and replacing text fields with specified
     * text.
     * 
     * @param original
     *            calendar to be obfuscated
     * @param productId
     *            productId to be set for the copy calendar.
     * @param freeBusyText
     * @return obfuscated calendar.
     */
    public static Calendar getFreeBusyCalendar(Calendar original, String productId, String freeBusyText) {
        // Make a copy of the original calendar
        Calendar copy = new Calendar();
        copy.getProperties().add(new ProdId(productId));
        copy.getProperties().add(Version.VERSION_2_0);
        copy.getProperties().add(CalScale.GREGORIAN);
        copy.getProperties().add(new XProperty(FREE_BUSY_X_PROPERTY, Boolean.TRUE.toString()));

        ComponentList<CalendarComponent> events = original.getComponents(Component.VEVENT);
        for (Component event : events) {
            copy.getComponents().add(getFreeBusyEvent((VEvent) event, freeBusyText));
        }
        return copy;
    }

    private static VEvent getFreeBusyEvent(VEvent vEvent, String freeBusyText) {

        try {
            VEvent freeBusyEvent = new VEvent();
            freeBusyEvent.getProperties().add(new Summary(freeBusyText));
            for (String propertyName : FREE_BUSY_ALLOWED_PROPERTIES) {
                Property property = vEvent.getProperty(propertyName);
                if (property != null) {
                    freeBusyEvent.getProperties().add(property);
                }
            }
            return freeBusyEvent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
