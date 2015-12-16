/*
 * SpringApiInterfaceImplProvider.java Apr 28, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class SpringExternalComponentFactory  implements  ExternalComponentFactory{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExternalComponentFactory.class);
    private Map<Class<?>, Object> unwrappedSpringBeans;
    @Override
    public <T, R extends T> R instanceForDescriptor(ExternalComponentDescriptor<R> desc) {
        Class<? extends R> clazz = desc.getImplementationClass();
        
        
        WebApplicationContext wac = WebApplicationContextHolder.get();
        if(wac == null){
            LOGGER.info("No Spring web application context available");
            return null;
        }
        
        return getBeanFromSpringContext(WebApplicationContextHolder.get(), clazz);
    }
    
    @SuppressWarnings("unchecked")
	private  <T> T getBeanFromSpringContext(ApplicationContext ctx, Class<T> clazz){
        LOGGER.info("Searching bean of type [{}].", clazz.getName());
        	registerUnWrappedBeansFromContextIfNecessary(ctx);
            
        	T result = (T)unwrappedSpringBeans.get(clazz);
        	
            logResult(clazz, result);
            
            return result;
    }

	private static <T>  void logResult(Class<T> clazz, T result) {
		String template = (result == null ? "Couldn't find ": "Found") + " bean of type [{}].";
		LOGGER.info(template, clazz.getName());
	}

	private void registerUnWrappedBeansFromContextIfNecessary(ApplicationContext ctx) {
		if(unwrappedSpringBeans != null){
			return;
		}
		
		unwrappedSpringBeans = new HashMap<>();
		ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory)ctx.getAutowireCapableBeanFactory();
		
		Iterator<String> beanNamesIterator = beanFactory.getBeanNamesIterator();
		
		Set<String> beanNames = new HashSet<>();
		Set<String> beanNamesToSkip = new HashSet<>();
		while(beanNamesIterator.hasNext()){
			String beanName = beanNamesIterator.next();
	
			//avoid getting web scoped beans which will cause an error
			collectUneligibleBeanName(beanFactory, beanNamesToSkip, beanName);
			
			collectEligibleBeanName(beanNames, beanName);
		}
		beanNames.removeAll(beanNamesToSkip);
		
		unwrappedSpringBeans = new HashMap<>();
		
		for(String bn : beanNames){
			Object bean = beanFactory.getBean(bn);
			
			try {
				if(bean != null && 
					AopUtils.isAopProxy(bean) &&
					bean instanceof Advised &&
					((Advised)bean).getTargetSource() != null &&
					((Advised)bean).getTargetSource().getTarget() != null){
					bean = ((Advised)bean).getTargetSource().getTarget();
				}
			} catch (Exception e) {
				throw new RuntimeException(e); 
			}
		
			if(bean != null){
				unwrappedSpringBeans.put(bean.getClass(), bean);
				if(beanFactory.isFactoryBean(bn)){
					Object factoryBean = beanFactory.getBean("&" + bn);
					unwrappedSpringBeans.put(factoryBean.getClass(), factoryBean);
				}
			}
		}
		
		
	}

	private void collectEligibleBeanName(Set<String> beanNames, String beanName) {
		if(!ScopedProxyUtils.isScopedTarget(beanName)){
			beanNames.add(beanName);
		}
	}

	private void collectUneligibleBeanName(ConfigurableListableBeanFactory beanFactory, Set<String> beanNamesToSkip, String beanName) {
		String uneligibleForDecorationBeanName = skipBeanNameForDecoration(beanFactory, beanName);
		
		if(uneligibleForDecorationBeanName != null){
			beanNamesToSkip.add(uneligibleForDecorationBeanName);
		}
	}

	private String skipBeanNameForDecoration(ConfigurableListableBeanFactory beanFactory, String beanName) {
		
		if(beanName != null && beanName.startsWith("scopedTarget.")){
			return beanName.replace("scopedTarget.", "");
		}
		if( !beanFactory.containsBeanDefinition(beanName) || 
				!beanFactory.containsSingleton(beanName)){
			return beanName;
		}
		
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
		if(beanDefinition.isAbstract()){
			return beanName;
		}
		
		return null;
		
	}
}