/*
 * DisplayNameValidator.java Nov 18, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.hibernate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DisplayNameValidator implements  ConstraintValidator<DisplayName, String> {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 64;
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void initialize(DisplayName constraintAnnotation) {
       //nothing to do 
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null ? true : value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH;
    }

}
