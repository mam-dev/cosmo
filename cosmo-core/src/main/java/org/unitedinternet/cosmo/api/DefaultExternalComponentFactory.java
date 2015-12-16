/*
 * ApiInterfaceImplFactory.java Apr 24, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author corneliu dobrota
 *
 */
public class DefaultExternalComponentFactory implements ExternalComponentFactory{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExternalComponentFactory.class);
    
    private ThreadLocal<Map<Class<?>, Object>> cacheHolder = new ThreadLocal<Map<Class<?>, Object>>(){
    	protected Map<Class<?>, Object> initialValue(){
			return new HashMap<>();
    	}
    };
    
	public <T, R extends T> R instanceForDescriptor(ExternalComponentDescriptor<R> desc){
        try {
        	@SuppressWarnings("unchecked")
        	R result = (R)cacheHolder.get().get(desc.getImplementationClass());
        	if(result == null){
        		result = (R)desc.getImplementationClass().newInstance();
        		cacheHolder.get().put(desc.getImplementationClass(), result);
        	}
            LOGGER.info("Created instance of type [{}]", desc.getImplementationClass().getName());
            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Instantiation exception occured", e);
            return null;
        }
    }
}
