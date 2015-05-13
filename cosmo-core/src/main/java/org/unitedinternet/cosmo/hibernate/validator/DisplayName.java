/*
 * DisplayName.java Nov 18, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.hibernate.validator;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 
 * @author cdobrota
 *
 */
@Target(METHOD) 
@Retention(RUNTIME)
@Constraint(validatedBy = DisplayNameValidator.class)
@Documented
public @interface DisplayName {
    String message() default "Display name length must be between 1 and 64 characters.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
