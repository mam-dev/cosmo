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
    
    public Set<SetterBasedServiceOwnerDescriptor> getSettersAnnotatedWith(Class<? extends Annotation> annotation){
        Set<Method> allMethods = reflections.getMethodsAnnotatedWith(annotation);
        Set<SetterBasedServiceOwnerDescriptor> result = new HashSet<>();
        for(Method method : allMethods){
            if(method.getParameterTypes().length == 1 && method.getName().startsWith("set")){
            	Class<?> setterDeclaringClass = method.getDeclaringClass();
            	Set<Class<?>> setterDeclaringClasses = new HashSet<>(1);
            	collectConcreteTypesFor(setterDeclaringClass, setterDeclaringClasses);
            	for(Class<?> c : setterDeclaringClasses){
            		result.add(new SetterBasedServiceOwnerDescriptor(c, method.getParameterTypes()[0], method));
            	}
            }
        }
        
        return result;
    }
    
    public Set<FieldBasedServiceOwnerDescriptor> getFieldsAnnotatedWith(Class<? extends Annotation> annotation){
        Set<Field> fields = reflections.getFieldsAnnotatedWith(annotation);
        Set<FieldBasedServiceOwnerDescriptor> result = new HashSet<>();
        
        for(Field field : fields){
        	result.add(new FieldBasedServiceOwnerDescriptor(field.getDeclaringClass(), field.getType(), field));
        }
        return result;
    }
    
    private <T> void  collectConcreteTypesFor(Class<T> clazz, Set<Class<?>> set){
    	if(!Modifier.isAbstract(clazz.getModifiers())){
    		set.add(clazz);
    		return;
    	}
    	
    	Set<Class<? extends T >> subTypes = reflections.getSubTypesOf(clazz);
    	for(Class<? extends T> c : subTypes){
    		collectConcreteTypesFor(c, set);
    	}
    }
}