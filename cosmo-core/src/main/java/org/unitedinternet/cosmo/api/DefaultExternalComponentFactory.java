/*
 * ApiInterfaceImplFactory.java Apr 24, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author corneliu dobrota
 *
 */
public class DefaultExternalComponentFactory implements ExternalComponentFactory{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExternalComponentFactory.class);
    
    public <T, R extends T> R instanceForDescriptor(ExternalComponentDescriptor<R> desc){
        try {
            R result = (R)desc.getImplementationClass().newInstance();
            LOGGER.info("Created instance of type [{}]", desc.getImplementationClass().getName());
            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Instantiation exception occured", e);
            return null;
        }
    }
}
