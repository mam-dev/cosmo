package org.unitedinternet.cosmo.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that tells that a method invocation can be handled by external calendar providers in certain
 * conditions.
 * 
 * @author daniel grigore
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExternalizableContent {

}
