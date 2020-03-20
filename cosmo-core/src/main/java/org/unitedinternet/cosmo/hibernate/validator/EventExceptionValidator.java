/*
 * Copyright 2007 Open Source Applications Foundation
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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * Check if a Calendar object contains a valid VEvent exception.
 * 
 * @author randy
 *
 */
public class EventExceptionValidator implements ConstraintValidator<EventException, Calendar> {

    private static final Logger LOG = LoggerFactory.getLogger(EventExceptionValidator.class);

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

                // make sure we have a VEVENT with a recurrenceId
                comps = calendar.getComponents();
                if (comps == null) {
                    LOG.warn("Error validating event exception: {}", calendar.toString());
                    return false;
                }
            }
            if (comps != null) {
                comps = comps.getComponents(Component.VEVENT);
            }
            if (comps == null || comps.size() == 0) {
                LOG.warn("Error validating event exception: {}", calendar.toString());
                return false;
            }

            VEvent event = (VEvent) comps.get(0);
            if (event == null) {
                LOG.warn("Error validating event exception: {}", calendar.toString());
                return false;
            }

            RecurrenceId recurrenceId = event.getRecurrenceId();

            if (recurrenceId == null || recurrenceId.getValue() == null || "".equals(recurrenceId.getValue())) {
                LOG.warn("error validating event exception: {}", calendar.toString());
                return false;
            }

            return true;
        } catch (ValidationException ve) {
            LOG.warn("Event validation error", ve);
            LOG.warn("Error validating event: {}", calendar.toString());
        } catch (ParserException e) {
            LOG.warn("Parse error", e);
            LOG.warn("Error parsing event: {}", calendar.toString());
        } catch (IOException | RuntimeException e) {
            LOG.warn("", e);
        }
        return false;
    }

    public void initialize(EventException parameters) {
        // nothing to do
    }

}