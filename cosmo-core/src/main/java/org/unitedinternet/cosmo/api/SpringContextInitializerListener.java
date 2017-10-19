/*
 * SpringContextInitializerListener.java Apr 30, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class SpringContextInitializerListener implements ServletContextListener{
    private final static  String CONTEXT_PARAM_NAME = "comsoContextFile";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextInitializerListener.class);
    
    private volatile ContextLoader contextLoader;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            AbstractRefreshableWebApplicationContext wac = (AbstractRefreshableWebApplicationContext) WebApplicationContextUtils
                    .getWebApplicationContext(sce.getServletContext());
            if (wac == null) {
                contextLoader = new ContextLoader();
                createSpringApplicationContext(sce.getServletContext());
            } else {
                WebApplicationContextHolder.set(wac);
                enhanceExistingSpringWebApplicationContext(sce, wac);
            }
        } catch (Exception t) {
            LOGGER.error("Exception occured", t);
        }
    }

    private ApplicationContext createSpringApplicationContext(ServletContext servletContext){
        
        LOGGER.info("Creating Spring application context...");
        
        String cosmoContextLocation = servletContext.getInitParameter(CONTEXT_PARAM_NAME);
        servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM, cosmoContextLocation);
        AbstractApplicationContext applicationContext = (AbstractApplicationContext)contextLoader.initWebApplicationContext(servletContext);
        
        applicationContext.start();
        
        LOGGER.info("Creating Spring application context started.");
        
        return applicationContext;
    }
    
    private void enhanceExistingSpringWebApplicationContext(ServletContextEvent sce, WebApplicationContext wac) {
        
        LOGGER.info("Enhancing existing Spring application context...");
        
        String cosmoContextLocation = sce.getServletContext().getInitParameter(CONTEXT_PARAM_NAME);
        
        @SuppressWarnings("resource")
        GenericXmlApplicationContext cosmoAppCtxt = new GenericXmlApplicationContext();
        cosmoAppCtxt.setEnvironment((ConfigurableEnvironment) wac.getEnvironment());
        cosmoAppCtxt.load(cosmoContextLocation);
        cosmoAppCtxt.refresh();
        cosmoAppCtxt.start();

        //make beans that are required from web components (as delegating filter proxy) accesible
        ((AbstractRefreshableWebApplicationContext)wac).getBeanFactory().setParentBeanFactory(cosmoAppCtxt.getBeanFactory());
        LOGGER.info("Enhanced existing Spring application context started");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(contextLoader!= null){
            contextLoader.closeWebApplicationContext(sce.getServletContext());
        }
    }
}