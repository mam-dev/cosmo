/*
 * CosmoApiImplFinder.java Apr 24, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author corneliu dobrota
 *
 */
@Component
public class ExternalComponentInstanceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalComponentInstanceProvider.class);
    
    private TypesFinder typesFinder; 
    private ExternalComponentFactoryChain externalComponentFactoryChain;
    
    @Autowired
    public ExternalComponentInstanceProvider(TypesFinder typesFinder,
    		ExternalComponentFactoryChain externalComponentFactoryChain){
        
        this.typesFinder = typesFinder; 
        this.externalComponentFactoryChain = externalComponentFactoryChain;
    }
    
    public <T> Set<? extends T> getImplInstancesAnnotatedWith(Class<? extends Annotation> metadata, Class<T> superType){
        LOGGER.info("Searching for types [{}] annotated with [{}]", superType.getName(), metadata.getName());
        
        Set<ExternalComponentDescriptor<? extends T>> descriptions = typesFinder.findConcreteImplementationsByTypeAndMetadata(superType ,metadata);
        
        Set<T> result = new HashSet<>(1);
        
        LOGGER.info("Found [{}] type(s) [{}] annotated with [{}]",  descriptions.size(), superType.getName(), metadata.getName());
        
        for(ExternalComponentDescriptor<? extends T> description : descriptions){
            
            T instance = externalComponentFactoryChain.instanceForDescriptor(description);
            
            result.add(instance);
        }
        return result;
    }
    
    public <T> T instanceForClass(Class<T> clazz){
    	return externalComponentFactoryChain.instanceForDescriptor(new ExternalComponentDescriptor<T>(clazz));
    }
}
