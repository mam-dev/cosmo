package org.unitedinternet.cosmo.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to declare a <code>Collection/Event</code> operation handler.
 * 
 * @author daniel grigore
 * @see org.unitedinternet.cosmo.service.interceptors.*Handler
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor {

}
