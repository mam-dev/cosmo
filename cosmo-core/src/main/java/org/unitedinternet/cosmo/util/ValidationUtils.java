package org.unitedinternet.cosmo.util;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.validate.ValidationException;
import net.fortuna.ical4j.validate.ValidationResult;

/**
 * 
 * @author daniel grigore
 *
 */
public class ValidationUtils {

    public static void verifyResult(ValidationResult result) throws ValidationException {
        if (result != null && result.hasErrors()) {
            throw new ValidationException("there are validation error in result");
        }
    }

    public static void addRequired(Calendar calendar) {
        addRequiredVjournal(calendar);
    }

    private static void addRequiredVjournal(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        VJournal vJournal = calendar.getComponent(Component.VJOURNAL);
        if (vJournal == null) {
            return;
        }
        // See: https://github.com/orgs/ical4j/discussions/710
        if (vJournal.getStartDate() == null) {
            vJournal.getProperties().add(new DtStart(new Date(System.currentTimeMillis())));
        }
    }
}
