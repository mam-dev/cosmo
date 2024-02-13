package org.unitedinternet.cosmo.util;

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
}
