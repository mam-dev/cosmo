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
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
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
    private ExternalComponentInstanceProvider externalComponentInstanceProvider;
    
    private TypesFinder typesFinder;

    public ExternalComponentDecorator(ExternalComponentInstanceProvider externalComponentInstanceProvider, TypesFinder typesFinder) {
        this.externalComponentInstanceProvider = externalComponentInstanceProvider;
        this.typesFinder = typesFinder;
    }

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        decorateByFields();
        decorateBySetters();
    }

    private void decorateByFields() {
        Set<FieldBasedServiceOwnerDescriptor> descriptors = typesFinder.getFieldsAnnotatedWith(Provided.class);
        
        for(FieldBasedServiceOwnerDescriptor desc : descriptors){
        	Class<?> fieldDeclaringClass = desc.getOwnerType();
        
            Object managedComponent = getManagedComponentFor(fieldDeclaringClass);
            
            if(managedComponent == null || 
            		!isPublicApiInterface(desc.getServiceType())){
                continue;
            }
            Field field = desc.getField();
            try {
                field.setAccessible(true);
                Object toBeInjected = this.getBean(field.getType(), field.getAnnotation(Provided.class));
                field.set(managedComponent, unwrapIfNecessary(toBeInjected, field.getAnnotation(Provided.class)));
                LOGGER.info("Set field [{}] of [{}].", field.getName(), field.getDeclaringClass().getName());
            } catch (BeansException | IllegalArgumentException | IllegalAccessException e) {
            	LOGGER.error("Exception occured", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void decorateBySetters() {
        Set<SetterBasedServiceOwnerDescriptor> setterDescriptors = typesFinder.getSettersAnnotatedWith(Provided.class);
        
        for(SetterBasedServiceOwnerDescriptor desc : setterDescriptors){
        	Class<?> setterDeclaringClass = desc.getOwnerType();
        	Method setter = desc.getSetter();
            Object managedComponent = getManagedComponentFor(setterDeclaringClass);
            
            if(managedComponent == null || !isPublicApiInterface(setter.getParameterTypes()[0])){
                continue;
            }
            
            try {
                setter.setAccessible(true);
                Provided provided = setter.getAnnotation(Provided.class);                
                Object toBeSet = this.getBean(setter.getParameterTypes()[0], provided);
                setter.invoke(managedComponent, unwrapIfNecessary(toBeSet, provided));
                LOGGER.info("Invoked setter [{}] of [{}].", setter.getName(), setter.getDeclaringClass().getName());
            } catch (BeansException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            	LOGGER.error("Exception occured", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    private Object getBean(Class<?> clazz, Provided annotation) {
        if (!annotation.beanName().isEmpty()) {
            return this.applicationContext.getBean(annotation.beanName(), clazz);
        }
        return this.applicationContext.getBean(clazz);
    }
    
    private static Object unwrapIfNecessary(Object obj, Provided annotation){
    	if(!annotation.unwrapIfProxied()){
    		return obj;
    	}
    	
    	if(AopUtils.isAopProxy(obj) && obj instanceof Advised) {
			try {
				return ((Advised)obj).getTargetSource().getTarget();
			} catch (Exception e) {
				LOGGER.error("Exception occured", e);
				throw new RuntimeException(e);
			}
    	}
    	
    	return obj;
    }
    private Object getManagedComponentFor(Class<?> clazz){
    	return externalComponentInstanceProvider.instanceForClass(clazz);
    }



    private static boolean isPublicApiInterface(Class<?> clazz) {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}