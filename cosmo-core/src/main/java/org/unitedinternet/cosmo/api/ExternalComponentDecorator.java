/*
 * ApiInterfaceImplServiceDecorator.java Apr 24, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.unitedinternet.cosmo.metadata.Provided;
/**
 * 
 * @author corneliu dobrota
 *
 */
public class ExternalComponentDecorator implements ApplicationListener<ContextStartedEvent>, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalComponentDecorator.class);
    
    private ApplicationContext applicationContext;
    private ExternalComponentsManager manager;

    private TypesFinder typesFinder;

    public ExternalComponentDecorator(ExternalComponentsManager manager, TypesFinder typesFinder) {
        this.manager = manager;
        this.typesFinder = typesFinder;
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        decorateByFields();
        decorateBySetters();
    }

    private void decorateByFields() {
        Set<Field> annotatedFields = typesFinder.getFieldsAnnotatedWith(Provided.class);
        
        for(Field field : annotatedFields){
            Object managedComponent = getManagedInstanceFor(field.getDeclaringClass());
            if(managedComponent == null || !isPublicApiInterface(field.getType())){
                continue;
            }
            
            try {
                field.setAccessible(true);
                field.set(managedComponent, applicationContext.getBean(field.getType()));
                LOGGER.info("Set field [{}] of [{}].", field.getName(), field.getDeclaringClass().getName());
            } catch (BeansException | IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void decorateBySetters() {
        Set<Method> setters = typesFinder.getSettersAnnotatedWith(Provided.class);
        
        for(Method setter : setters){
            Object managedComponent = getManagedInstanceFor(setter.getDeclaringClass());
            if(managedComponent == null || !isPublicApiInterface(setter.getParameterTypes()[0])){
                continue;
            }
            
            try {
                setter.setAccessible(true);
                setter.invoke(managedComponent, applicationContext.getBean(setter.getParameterTypes()[0]));
                LOGGER.info("Invoked setter [{}] of [{}].", setter.getName(), setter.getDeclaringClass().getName());
            } catch (BeansException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException();
            }
        }
    }
    
    private Object getManagedInstanceFor(Class<?> clazz){
        Object locallyManaged = manager.forClass(clazz);
        
        if(locallyManaged == null){
            locallyManaged = applicationContext.getBean(clazz);
        }
        return locallyManaged;
    }



    private static boolean isPublicApiInterface(Class<?> clazz) {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
