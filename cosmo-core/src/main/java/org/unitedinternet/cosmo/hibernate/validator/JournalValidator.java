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
 * Check if a Calendar object contains a valid VJOURNAL
 */
public class JournalValidator implements ConstraintValidator<Journal, Calendar> {

    private static final Logger LOG = LoggerFactory.getLogger(JournalValidator.class);

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

            // make sure we have a VJOURNAL
            ComponentList<CalendarComponent> comps = calendar.getComponents();
            if (comps == null) {
                LOG.warn("Error validating journal: {}", calendar.toString());
                return false;
            }

            comps = comps.getComponents(Component.VJOURNAL);
            if (comps == null || comps.size() == 0) {
                LOG.warn("Error validating journal: {}", calendar.toString());
                return false;
            }

            return true;

        } catch (ValidationException ve) {
            LOG.warn("Journal validation error", ve);
            LOG.warn("Error validating journal: {}", calendar.toString());
        } catch (ParserException e) {
            LOG.warn("Parse error", e);
            LOG.warn("Error parsing journal: {}", calendar.toString());
        } catch (IOException | RuntimeException e) {
            LOG.warn("", e);
        }
        return false;
    }

    public void initialize(Journal parameters) {
        // nothing to do
    }

}
