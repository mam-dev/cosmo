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
import net.fortuna.ical4j.validate.ValidationException;

/**
 * Check if a Calendar object contains a valid VFREEBUSY
 */
public class FreeBusyValidator implements ConstraintValidator<FreeBusy, Calendar> {

    private static final Logger LOG = LoggerFactory.getLogger(FreeBusyValidator.class);

    public boolean isValid(Calendar value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        Calendar calendar = null;
        try {
            calendar = (Calendar) value;

            // validate entire icalendar object
            calendar.validate(true);

            // additional check to prevent bad .ics
            CalendarUtils.parseCalendar(calendar.toString());

            // make sure we have a VFREEBUSY

            ComponentList<CalendarComponent> comps = calendar.getComponents(Component.VFREEBUSY);
            if (comps == null || comps.size() == 0) {
                LOG.warn("Error validating freebusy: {}", calendar.toString());
                return false;
            }

            return true;

        } catch (ValidationException ve) {
            LOG.warn("Freebusy validation error", ve);
            LOG.warn("Error validating freebusy: {}", calendar.toString());
        } catch (ParserException e) {
            LOG.warn("Parse error", e);
            LOG.warn("Error parsing freebusy: {}", calendar.toString());
        } catch (IOException | RuntimeException e) {
            LOG.warn("", e);
        }

        return false;
    }

    public void initialize(FreeBusy parameters) {
        // nothing to do
    }

}
