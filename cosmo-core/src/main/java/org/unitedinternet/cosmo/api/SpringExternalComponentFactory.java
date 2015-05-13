/*
 * SpringApiInterfaceImplProvider.java Apr 28, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class SpringExternalComponentFactory  implements  ExternalComponentFactory{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExternalComponentFactory.class);
    
    @Override
    public <T, R extends T> R instanceForDescritor(ExternalComponentDescriptor<R> desc) {
        Class<? extends R> clazz = desc.getImplementationClass();
        
        WebApplicationContext wac = WebApplicationContextHolder.get();
        if(wac == null){
            LOGGER.info("No Spring web application context available");
            return null;
        }
        
        return getBeanFromSpringContext(WebApplicationContextHolder.get(), clazz);
    }
    
    private static <T> T getBeanFromSpringContext(ApplicationContext ctx, Class<T> clazz){
        LOGGER.info("Searching bean of type [{}].", clazz.getName());
        try{
            T result = ctx.getBean(clazz);
            LOGGER.info("Found bean of type [{}].", clazz.getName());
            return result;
        }catch(BeansException be){
            LOGGER.info("Unable to find bean of type [{}].", clazz.getName());
            return null;
        }
    }
}