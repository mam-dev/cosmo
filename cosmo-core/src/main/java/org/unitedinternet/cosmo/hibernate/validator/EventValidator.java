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
package org.unitedinternet.cosmo.hibernate.validator;


import java.io.IOException;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * Check if a Calendar object contains a valid VEvent
 * @author randy
 *
 */
@org.springframework.stereotype.Component
public class EventValidator implements ConstraintValidator<Event, Calendar> {
    
    private static final Log LOG = LogFactory.getLog(EventValidator.class);
    
    @Autowired
    private ValidationConfig validationConfig;
    
    
    
    public boolean isValid(Calendar value, ConstraintValidatorContext context) {
        Calendar calendar = null;
        ComponentList<CalendarComponent> comps = null;
        try {
            calendar = (Calendar) value;
            
            // validate entire icalendar object
            if (calendar != null) {
                calendar.validate(true);
                // additional check to prevent bad .ics
                CalendarUtils.parseCalendar(calendar.toString());
                
                // make sure we have a VEVENT
                comps = calendar.getComponents();
                if(comps==null) {
                    LOG.warn("error validating event: " + calendar.toString());
                    return false;
                }
            }
            if (comps != null) {
                comps = comps.getComponents(Component.VEVENT);
            }
            if(comps==null || comps.size()==0) {
                LOG.warn("error validating event: " + calendar.toString());
                return false;
            }
            
            VEvent event = (VEvent) comps.get(0);
            
            if(event == null || !PropertyValidator.isEventValid(event, validationConfig)) {
                LOG.warn("error validating event: " + calendar.toString());
                return false;
            }
            
            return true;
            
        } catch(ValidationException ve) {
            LOG.warn("event validation error", ve);
            LOG.warn("error validating event: " + calendar.toString() );
        } catch(ParserException e) {
            LOG.warn("parse error", e);
            LOG.warn("error parsing event: " + calendar.toString() );
        } catch (IOException | RuntimeException e) {
            LOG.warn("Exception occured while parsing calendar", e);
        }
        
        return false;
    }

    @Override
    public void initialize(Event constraintAnnotation) {
   
    }
    
    
    private static enum PropertyValidator{
        SUMMARY(Property.SUMMARY){

            @Override
            protected boolean isValid(VEvent event, ValidationConfig config) {
                
                return isTextPropertyValid(event.getProperty(prop), config.summaryMinLength, config.summaryMaxLength);
            }
            
        },
        DESCRIPTION(Property.DESCRIPTION){

            @Override
            protected boolean isValid(VEvent event, ValidationConfig config) {
                return isTextPropertyValid(event.getProperty(prop), config.descriptionMinLength, config.descriptionMaxLength);
            }
            
        },
        LOCATION(Property.LOCATION){

            @Override
            protected boolean isValid(VEvent event, ValidationConfig config) {
                return isTextPropertyValid(event.getProperty(prop), config.locationMinLength, config.locationMaxLength);
            }
            
        },
        RECURRENCE_RULE(Property.RRULE){

            @Override
            protected boolean isValid(VEvent event, ValidationConfig config) {
                                
                List<? extends Property> rrules = event.getProperties(prop);
                if(rrules == null){
                    return true;
                }
                for(Property p : rrules){
                    RRule rrule = (RRule)p; 
                    if(! isRRuleValid(rrule, config)){
                        return false;
                    }
                }
                
                return true;
            }
            
            private boolean isRRuleValid(RRule rrule, ValidationConfig config){
                if(rrule == null){
                    return true;
                }
                
                if(rrule.getRecur() == null || rrule.getRecur().getFrequency() == null){
                    return false;
                }
                
                String recurFrequency = rrule.getRecur().getFrequency();
                if(!config.allowedRecurrenceFrequencies.contains(recurFrequency)){
                    return false;
                }
                
                return true;
            }
            
        }, 
        
        ATTENDEES(Property.ATTENDEE){
            @Override
            protected boolean isValid(VEvent event, ValidationConfig config) {
                List<?> attendees = event.getProperties(prop);
                int attendeesSize = attendees == null ? 0 : attendees.size();
                
                return attendeesSize < config.attendeesMaxSize;
            }
            
        };
        private static final String[] PROPERTIES_WITH_TIMEZONES = {Property.DTSTART, Property.DTEND, Property.EXDATE, Property.RDATE, Property.RECURRENCE_ID}; 
        private static TimeZoneRegistry timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry(); 
        
        String prop;
        
        PropertyValidator(String propertyToValidate){
            this.prop = propertyToValidate;
        }
        
        protected abstract boolean isValid(VEvent event, ValidationConfig config);
        
        private static boolean isTextPropertyValid(Property prop, int minLength, int maxLength){
            
            if(prop == null && minLength == 0){
                return true;
            }else if(prop == null){
                return false;
            }
            String value = prop.getValue();
            int valueSize = value == null ? 0 :  value.length();
            if(valueSize < minLength || valueSize > maxLength){
                return false;
            }
            
            return true;
        }
        private static boolean isEventValid(VEvent event, ValidationConfig config){
        	if (null == config) {
        		LOG.error("ValidationConfig cannot be null");
        		return false; //TODO - validation config should not be null
        	}
            DtStart startDate = event.getStartDate();
            DtEnd endDate = event.getEndDate(true);
            if(startDate == null || 
                startDate.getDate() == null ||
                endDate != null && startDate.getDate().after(endDate.getDate())){
                
                return false;
            }
            
            
            for(PropertyValidator validator : values()){
                if(!validator.isValid(event, config)){
                    return false; 
                }
            }
            
            return areTimeZoneIdsValid(event);
        }
        
        private static boolean areTimeZoneIdsValid(VEvent event){
            for(String propertyName : PROPERTIES_WITH_TIMEZONES){
                List<Property> props = event.getProperties(propertyName);
                for(Property p : props){
                    if(p != null && p.getParameter(Parameter.TZID) != null){
                        String tzId = p.getParameter(Parameter.TZID).getValue();
                        if(tzId != null && timeZoneRegistry.getTimeZone(tzId) == null){
                            LOG.warn("Unknown TZID [" + tzId + "] for event " + event);
                            return false;
                            
                        }
                    }
                }
            }
            return true;
        }
    }
    
    public void setValidationConfig(ValidationConfig validationConfig){
    	this.validationConfig = validationConfig;
    }
}