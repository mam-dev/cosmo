/*
 * ApiInterfaceImplFinder.java Apr 24, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class TypesFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypesFinder.class);
    
    private Reflections reflections;
    
    public TypesFinder(){
        reflections = new Reflections(new FieldAnnotationsScanner(), 
                                        new MethodAnnotationsScanner(), 
                                        new TypeAnnotationsScanner(), 
                                        new SubTypesScanner());
    }
    public <T> Set<ExternalComponentDescriptor<? extends T>> findConcreteImplementationsByTypeAndMetadata(Class<T> type, 
                                                                                            Class<? extends Annotation> metadata){
        
        Set<Class<?>> annotatedTypes = reflections.getTypesAnnotatedWith(metadata);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(type);
        
        
        Set<ExternalComponentDescriptor<? extends T>> result = new HashSet<>();
        
        for(Class<? extends T> clazz : subTypes){
            if(annotatedTypes.contains(clazz) && !Modifier.isAbstract(clazz.getModifiers())){
                LOGGER.info("Found [{}] concrete class annotated with [{}]", clazz.getName(), metadata.getName());
                result.add(construct(clazz));
            }
        }
        return result;
    }
    
    private static <T> ExternalComponentDescriptor<T> construct(Class<T> clazz){
        return new ExternalComponentDescriptor<T>(clazz);
    }
    
    public Set<Method> getSettersAnnotatedWith(Class<? extends Annotation> annotation){
        Set<Method> allMethods = reflections.getMethodsAnnotatedWith(annotation);
        Set<Method> result = new HashSet<>();
        for(Method m : allMethods){
            if(m.getParameterTypes().length == 1 && m.getName().startsWith("set")){
                result.add(m);
            }
        }
        
        return result;
    }
    
    public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation){
        
        return reflections.getFieldsAnnotatedWith(annotation);
    }
}