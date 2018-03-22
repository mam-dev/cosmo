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
package org.unitedinternet.cosmo.calendar.data;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;

import org.unitedinternet.cosmo.CosmoConstants;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.Instance;
import org.unitedinternet.cosmo.calendar.InstanceList;

/**
 * This is a filter object that allows filtering a {@link Calendar} by
 * component type or property. It follows the model defined by the
 * CalDAV <calendar-data> XML element including support of the
 * no-value attribute. When no-value is used, a property is written
 * out up to the ':' in the stream, and then no more (i.e. the value
 * is skipped).
 *
 * Heavily based on code written by Cyrus Daboo.
 */
public class OutputFilter {

    private String componentName;
    private boolean allSubComponents;
    private Map<String, OutputFilter> subComponents;
    private boolean allProperties;
    private HashMap<String, Object> properties;
    private Period expand;
    private Period limit;
    private Period limitfb;

    /**
     * Constructor.
     * @param name The name.
     */
    public OutputFilter(String name) {
        componentName = name;
    }

    /**
     * Filter.
     * @param calendar The calendar.
     * @param builder The string buffer.
     */
    public void filter(Calendar calendar, final StringBuilder builder) {
        // If expansion of recurrence is required what we have to do
        // is create a whole new calendar object with the new expanded
        // components in it and then write that one out.
        if (getExpand() != null) {
            calendar = createExpanded(calendar);
        }

        // If limit of recurrence set is required, we have to remove those
        // overriden components in recurring components that do not
        // overlap the given time period.  Create a new calendar in order
        // to preserve the original.
        else if (getLimit() != null) {
            calendar = createLimitedRecurrence(calendar);
        }

        builder.append(Calendar.BEGIN).
            append(':').
            append(Calendar.VCALENDAR).
            append("\n");

        filterProperties(calendar.getProperties(), builder);
        filterSubComponents(calendar.getComponents(), builder);

        builder.append(Calendar.END).
            append(':').
            append(Calendar.VCALENDAR).
            append("\n");
    }

    /**
     * Creates limited recurrence.
     * @param calendar The calendar.
     * @return The calendar.
     */
    private Calendar createLimitedRecurrence(Calendar calendar) {
        // Create a new calendar with the same top-level properties as current
        Calendar newCal = new Calendar();
        newCal.getProperties().addAll(calendar.getProperties());
       
        InstanceList instances = new InstanceList();
        ComponentList<CalendarComponent> overrides = new ComponentList<>();
        
        // Limit range
        Period period = getLimit();
        
        // Filter override components based on limit range
        for (CalendarComponent comp: calendar.getComponents()) {
            // Only care about VEVENT, VJOURNAL, VTODO
            if ((comp instanceof VEvent) ||
                (comp instanceof VJournal) ||
                (comp instanceof VToDo)) {
                // Add master component to result Calendar
                if (comp.getProperties().
                    getProperty(Property.RECURRENCE_ID) == null) {
                    newCal.getComponents().add(comp);
                    // seed the InstanceList with master component
                    instances.addComponent((CalendarComponent)comp, period.getStart(),
                                           period.getEnd());
                }
                // Keep track of overrides, we'll process later
                else {
                    overrides.add(comp);
                }
            } else {
                newCal.getComponents().add(comp);
            }
        }
        
        // Add override components to InstanceList.
        // Only add override if it changes anything about the InstanceList.
        for (CalendarComponent comp : overrides) {
            if (instances.addOverride(comp, period.getStart(), period.getEnd())) {
                newCal.getComponents().add(comp);
            }
        }
        
        return newCal;
    }
    
    /**
     * Creates expanded.
     * @param calendar The calendar.
     * @return The calendar.
     */
    private Calendar createExpanded(Calendar calendar) {
        // Create a new calendar with the same top-level properties as this one
        Calendar newCal = new Calendar();
        newCal.getProperties().addAll(calendar.getProperties());

        // Now look at each component and determine whether expansion is
        // required
        InstanceList instances = new InstanceList();
        ComponentList<CalendarComponent> overrides = new ComponentList<>();
        Component master = null;
        for (CalendarComponent comp :calendar.getComponents()) {
            if ((comp instanceof VEvent) ||
                (comp instanceof VJournal) ||
                (comp instanceof VToDo)) {
                // See if this is the master instance
                if (((CalendarComponent)comp).getProperties().
                    getProperty(Property.RECURRENCE_ID) == null) {
                    master = (CalendarComponent)comp;
                    instances.addComponent(master, getExpand().getStart(),
                                           getExpand().getEnd());
                } else {
                    overrides.add(comp);
                }
            } else if (!(comp instanceof VTimeZone))  {
                // Create new component and convert properties to UTC
                try {
                    CalendarComponent newcomp = (CalendarComponent)comp.copy();
                    componentToUTC((CalendarComponent)newcomp);
                    newCal.getComponents().add(newcomp);
                } catch (Exception e) {
                    throw new CosmoException("Error copying component", e);
                }
            }
        }

        for (Object comp : overrides) {
            instances.addComponent((CalendarComponent)comp, getExpand().getStart(), getExpand().getEnd());
        }

        // Create a copy of the master with recurrence properties removed
        boolean isRecurring = false;
        Component masterCopy = null;
        try {
            masterCopy = master.copy();
        } catch (Exception e) {
            throw new CosmoException("Error copying master component", e);
        }
        Iterator<Property> i = (Iterator<Property>)
            masterCopy.getProperties().iterator();
        while (i.hasNext()) {
            Property prop = i.next();
            if ((prop instanceof RRule) ||
                (prop instanceof RDate) ||
                (prop instanceof ExRule) ||
                (prop instanceof ExDate)) {
                i.remove();
                isRecurring = true;
            }
        }

        // Expand each instance within the requested range
        TreeSet<String> sortedKeys = new TreeSet<String>(instances.keySet());
        for (String ikey : sortedKeys) {
            Instance instance = (Instance) instances.get(ikey);

            // Make sure this instance is within the requested range
            // FIXME: Need to handle floating date/times.  Right now
            // floating times will use the server timezone.
            if ((getExpand().getStart().compareTo(instance.getEnd()) >= 0) ||
                (getExpand().getEnd().compareTo(instance.getStart()) <= 0)) {
                continue;
            }
            
            // Create appropriate copy
            Component copy = null;
            try {
                copy = instance.getComp() == master ?
                    masterCopy.copy() :
                    instance.getComp().copy();
                componentToUTC(copy);
            } catch (URISyntaxException | ParseException | IOException e) {
                throw new CosmoException("Error copying component", e);
            }

            // Adjust the copy to match the actual instance info
            if (isRecurring) {
                // Add RECURRENCE-ID, replacing existing if present
                RecurrenceId rid = (RecurrenceId) copy.getProperties()
                    .getProperty(Property.RECURRENCE_ID);
                if (rid != null) {
                    copy.getProperties().remove(rid);
                }
                rid = new RecurrenceId(instance.getRid());
                copy.getProperties().add(rid);

                // Adjust DTSTART (in UTC)
                DtStart olddtstart = (DtStart)
                    copy.getProperties().getProperty(Property.DTSTART);
                if (olddtstart != null) {
                    copy.getProperties().remove(olddtstart);
                }
                DtStart newdtstart = new DtStart(instance.getStart());
                Date newdtstartDate = newdtstart.getDate(); 
                if ( newdtstartDate instanceof DateTime &&
                    (((DateTime)newdtstartDate).getTimeZone() != null)) {
                    newdtstart.setUtc(true);
                }
                copy.getProperties().add(newdtstart);

                // If DTEND present, replace it (in UTC)
                DtEnd olddtend = (DtEnd)
                    copy.getProperties().getProperty(Property.DTEND);
                if (olddtend != null) {
                    copy.getProperties().remove(olddtend);
                    DtEnd newdtend = new DtEnd(instance.getEnd());
                    Date newdtwenddate = newdtend.getDate(); 
                    if (newdtwenddate instanceof DateTime &&
                        (((DateTime)newdtwenddate).getTimeZone() != null)) {
                        newdtend.setUtc(true);
                    }
                    copy.getProperties().add(newdtend);
                }
            }

            // Now have a valid expanded instance so add it
            newCal.getComponents().add((CalendarComponent)copy);
        }

        return newCal;
    }

    /**
     * @param comp The component.
     */
    private void componentToUTC(Component comp) {
        // Do to each top-level property
        for (Property prop : (List<Property>) comp.getProperties()) {
            if (prop instanceof DateProperty) {
                DateProperty dprop = (DateProperty) prop;
                Date date = dprop.getDate();
                if (date instanceof DateTime &&
                    (((DateTime) date).getTimeZone() != null)) {
                    dprop.setUtc(true);
                }
            }
        }

        // Do to each embedded component
        ComponentList<? extends CalendarComponent> subcomps = null;
        if (comp instanceof VEvent) {
            subcomps = ((VEvent) comp).getAlarms() ;
        }
        else if (comp instanceof VToDo) {
            subcomps = ((VToDo)comp).getAlarms();
        }

        if (subcomps != null) {
            for (CalendarComponent subcomp :  subcomps) {
                componentToUTC(subcomp);
            }
        }
    }

    private void filterProperties(PropertyList<Property> properties,
                                  StringBuilder buffer) {
        if (isAllProperties()) {
            buffer.append(properties.toString());
            return;
        }

        if (! hasPropertyFilters()) {
            return;
        }

        for (Property property : properties) {
            PropertyMatch pm = testPropertyValue(property.getName());
            if (pm.isMatch()) {
                if (pm.isValueExcluded()) {
                    chompPropertyValue(property, buffer);
                }
                else {
                    buffer.append(property.toString());
                }
            }
        }
    }

    /**
     * 
     * @param property The property.
     * @param buffer The string buffer.
     */
    private void chompPropertyValue(Property property, StringBuilder buffer) {
        buffer.append(property.getName()).
            append(property.getParameters()).
            append(':').
            append("\n");
    }

    /**
     * Filter sub component.
     * @param subComponents The component list.
     * @param buffer The string buffer.
     */ 
    private void filterSubComponents(ComponentList<?> subComponents,
                                     StringBuilder buffer) {
        if (isAllSubComponents() && getLimit() != null) {
            buffer.append(subComponents.toString());
            return;
        }

        if (! (hasSubComponentFilters() || isAllSubComponents())) {
            return;
        }

        for (Component component : subComponents) {
            if (getLimit() != null && component instanceof VEvent &&
                    ! includeOverride((VEvent) component)) {
                continue;
            }

            if (isAllSubComponents()) {
                buffer.append(component.toString());
            }
            else {
                OutputFilter subfilter = getSubComponentFilter(component);
                if (subfilter != null) {
                    subfilter.filterSubComponent(component, buffer);
                }
            }
        }
    }

    /**
     * Includes override.
     * @param event The event.
     * @return The boolean.
     */
    private boolean includeOverride(VEvent event) {
        // Policy: if event has a Recurrence-ID property then
        // include it if:
        //
        // a) If start/end are within limit range
        // b) else if r-id + duration is within the limit range
        // c) else if r-id is before limit start and
        // range=thisandfuture
        // d) else if r-id is after limit end and range=thisandprior
        //

        RecurrenceId rid = event.getRecurrenceId();
        if (rid == null) {
            return true;
        }

        Range range = (Range) rid.getParameter(Parameter.RANGE);
        DtStart dtstart = ((VEvent)event).getStartDate();
        DtEnd dtend = ((VEvent)event).getEndDate();
        DateTime start = new DateTime(dtstart.getDate());
        DateTime end = null;
        if (dtend != null) {
            end = new DateTime(dtend.getDate());
        } else {
            Dur duration = new Dur(0, 0, 0, 0);
            end = (DateTime) org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        }

        Period p = new Period(start, end);
        if (! p.intersects(getLimit())) {
            Dur duration = new Dur(start, end);
            start = new DateTime(rid.getDate());
            end = (DateTime) org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
            p = new Period(start, end);
            if (! p.intersects(getLimit())) {
                if (Range.THISANDFUTURE.equals(range)) {
                    if (start.compareTo(getLimit().getEnd()) >= 0) {
                        return false;
                    }
                } else if (Range.THISANDPRIOR.equals(range)) {
                    if (start.compareTo(getLimit().getStart()) < 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            } 
        }

        return true;
    }

    /**
     * Filter sub component.
     * @param subComponent The component list.
     * @param buffer The StringBuilder.
     */
    private void filterSubComponent(Component subComponent, StringBuilder buffer) {
        buffer.append(Component.BEGIN).
            append(':').
            append(subComponent.getName()).
            append("\n");

        filterProperties(subComponent.getProperties(), buffer);
        filterSubComponents(ICalendarUtils.getSubComponents(subComponent), buffer);

        buffer.append(Component.END).
            append(':').
            append(subComponent.getName()).
            append("\n");
    }

    /**
     * Test component.
     * @param comp The component.
     * @return The result of this test.
     */
    public boolean testComponent(Component comp) {
        return componentName.equalsIgnoreCase(comp.getName());
    }

    /**
     * Test subcomponent.
     * @param subcomp The component.
     * @return The result.
     */
    public boolean testSubComponent(Component subcomp) {
        if (allSubComponents) {
            return true;
        }

        if (subComponents == null) {
            return false;
        }

        if (subComponents.containsKey(subcomp.getName().toUpperCase(CosmoConstants.LANGUAGE_LOCALE))) {
            return true;
        }

        return false;
    }

    /**
     * Test property value.
     * @param name The name.
     * @return The property match.
     */
    public PropertyMatch testPropertyValue(String name) {
        if (allProperties) {
            return new PropertyMatch(true, false);
        }

        if (properties == null) {
            return new PropertyMatch(false, false);
        }

        Boolean presult = (Boolean) properties.get(name.toUpperCase(CosmoConstants.LANGUAGE_LOCALE));
        if (presult == null) {
            return new PropertyMatch(false, false);
        }

        return new PropertyMatch(true, presult.booleanValue());
    }

    public String getComponentName() {
        return componentName;
    }

    /**
     * Is all sub components.
     * @return The result.
     */
    public boolean isAllSubComponents() {
        return allSubComponents;
    }

    /**
     * Sets all sub component.
     */
    public void setAllSubComponents() {
        allSubComponents = true;
        subComponents = null;
    }

    /**
     * Adds sub component.
     * @param filter The output filter.
     */
    public void addSubComponent(OutputFilter filter) {
        if (subComponents == null) {
            subComponents = new HashMap<>();
        }
        subComponents.put(filter.getComponentName().toUpperCase(CosmoConstants.LANGUAGE_LOCALE), filter);
    }

    /**
     * 
     * @return The result.
     */
    public boolean hasSubComponentFilters() {
        return subComponents != null;
    }

    /**
     * Gets sub component filter.
     * @param subcomp The component.
     * @return The output filter.
     */
    public OutputFilter getSubComponentFilter(Component subcomp) {
        if (subComponents == null) {
            return null;
        }
        return (OutputFilter)
            subComponents.get(subcomp.getName().toUpperCase(CosmoConstants.LANGUAGE_LOCALE));
    }

    /**
     * Is all properties.
     * @return The result.
     */
    public boolean isAllProperties() {
        return allProperties;
    }

    /**
     * Sets all properties.
     */
    public void setAllProperties() {
        allProperties = true;
        properties = null;
    }

    /**
     * Adds property.
     * @param name The name.
     * @param noValue The boolean.
     */
    public void addProperty(String name, boolean noValue) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name.toUpperCase(CosmoConstants.LANGUAGE_LOCALE), Boolean.valueOf(noValue));
    }

    /**
     * Has property filters.
     * @return The result.
     */
    public boolean hasPropertyFilters() {
        return properties != null;
    }

    /**
     * Gets expand.
     * @return The period.
     */
    public Period getExpand() {
        return expand;
    }

    /**
     * Sets period.
     * @param expand The perios.
     */
    public void setExpand(Period expand) {
        this.expand = expand;
    }

    /**
     * Gets limit.
     * @return The period.
     */
    public Period getLimit() {
        return limit;
    }

    /**
     * Sets limit.
     * @param limit The limit period.
     */
    public void setLimit(Period limit) {
        this.limit = limit;
    }

    /**
     * Gets limitfb.
     * @return The limitfb.
     */
    public Period getLimitfb() {
        return limitfb;
    }

    /**
     * Sets limitfb.
     * @param limitfb The limitfb.
     */
    public void setLimitfb(Period limitfb) {
        this.limitfb = limitfb;
    }

    /**
     * PropertyMatch.
     *
     */
    public static class PropertyMatch {
        private boolean match;
        private boolean valueExcluded;

        /**
         * Constructor.
         * @param match The match.
         * @param valueExcluded The value excluded.
         */
        public PropertyMatch(boolean match, boolean valueExcluded) {
            this.match = match;
            this.valueExcluded = valueExcluded;
        }

        /**
         * Verification.
         * @return The result.
         */
        public boolean isMatch() {
            return match;
        }

        /**
         * Verify if the value is excluded.
         * @return The result.
         */
        public boolean isValueExcluded() {
            return valueExcluded;
        }
    }
}
