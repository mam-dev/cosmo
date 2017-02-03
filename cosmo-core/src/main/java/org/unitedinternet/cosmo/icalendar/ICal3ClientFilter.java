/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.icalendar;

import java.util.ArrayList;
import java.util.Iterator;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.ExDate;

import org.unitedinternet.cosmo.CosmoException;

/**
 * ICalendar filter that tailors VEVENTs for
 * iCal 3 clients.  This includes fixing EXDATE
 * with multiple dates into multiple single EXDATE 
 * and removing redundant VALUE=DATE-TIME params
 * from date properties.
 */
public class ICal3ClientFilter implements ICalendarClientFilter{

    public void filterCalendar(Calendar calendar) {
       
        try {
            ComponentList events = calendar.getComponents(Component.VEVENT);
            for(Iterator i = events.iterator(); i.hasNext();) {
                VEvent event = (VEvent) i.next();
                // fix VALUE=DATE-TIME instances
                fixDateTimeProperties(event);
                // fix EXDATEs
                if(event.getRecurrenceId()==null) {
                    fixExDates(event);
                }
            }
        } catch (Exception e) {
            throw new CosmoException(e);
        } 
    }
    
    private void fixExDates(Component comp) throws Exception {
        PropertyList props = comp.getProperties(Property.EXDATE);
        ArrayList<Property> toAdd = new ArrayList<Property>();
        ArrayList<Property> toRemove = new ArrayList<Property>();
        
        for(Iterator i=props.iterator();i.hasNext();) {
            ExDate exDate = (ExDate) i.next();
            
            // ical likes a single exdate
            if(exDate.getDates().size()==1) {
                continue;
            }
            
            // remove exdate with multiple dates
            toRemove.add(exDate);
            
            // create single dates instead
            for(Iterator j=exDate.getDates().iterator();j.hasNext();) {
                ExDate singleEx = (ExDate) exDate.copy();
                singleEx.getDates().clear();
                Date date = (Date) j.next();
                singleEx.getDates().add(date);
                toAdd.add(singleEx);
            }
        }
        
        // remove exdates with multiple dates
        comp.getProperties().removeAll(toRemove);
        
        // Add all single exdates
        comp.getProperties().addAll(toAdd);
    }
    
    // Remove VALUE=DATE-TIME because it is redundant and for 
    // some reason ical doesn't like it
    private void fixDateTimeProperties(Component component) {
        PropertyList props = component.getProperties();
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property prop = it.next();
            if(prop instanceof DateProperty || prop instanceof DateListProperty) {
                Value v = (Value) prop.getParameter(Parameter.VALUE);
                if(Value.DATE_TIME.equals(v)) {
                    prop.getParameters().remove(v);
                }
            }
        }
    }

}
