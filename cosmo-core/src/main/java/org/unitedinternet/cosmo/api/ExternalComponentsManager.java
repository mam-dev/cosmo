/*
 * ExternalComponentsManager.java May 5, 2015
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
public class ExternalComponentsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalComponentsManager.class);
    
    private Map<ExternalComponentDescriptor<?>, Object> components = new HashMap<>();
    
    public <T, R extends T> R getComponent(ExternalComponentDescriptor<R> desc){
        @SuppressWarnings("unchecked")
        R r = (R)components.get(desc);
        return r;
    }
    
    public <T, R extends T>  void add(ExternalComponentDescriptor<R> desc, R instance){
        components.put(desc, instance);
    }
    
    public Object forClass(Class<?> clazz){
        Object result = components.get(new ExternalComponentDescriptor<>(clazz));
        LOGGER.info("Found [{}] for type [{}]", result, clazz.getName());
        return result;
    }
}
