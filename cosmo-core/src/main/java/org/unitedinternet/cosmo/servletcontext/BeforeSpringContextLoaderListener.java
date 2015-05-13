/*
 * BeforeSpringContextLoaderListener.java Jan 22, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.servletcontext;

import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The class is used to load listeners which have to be forced to execute before Spring context is loaded
 * (like Resteasy Bootstrap)
 * @author izidaru
 *
 */
public class BeforeSpringContextLoaderListener implements ServletContextListener {
    private static final String COSMO_PROPERTY_DELEGATE_LISTENER = "cosmo.extensions.BeforeSpringContextLoaderListener";
    private ServletContextListener delegate;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties applicationProperties = ServletContextUtil.
                extractApplicationProperties(sce.getServletContext());

        String delegateClass = applicationProperties.getProperty(COSMO_PROPERTY_DELEGATE_LISTENER);
        if(delegateClass != null){
            try {
                Class<?> delegateCls = Class.forName(delegateClass);
                BeforeSpringContextLoaderListener.class.getClassLoader().loadClass(delegateClass);
                delegate = (ServletContextListener) delegateCls.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to initialize delegate listener: " + delegateClass + 
                        " defined in " + COSMO_PROPERTY_DELEGATE_LISTENER, e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Unable to initialize delegate listener: " + delegateClass + 
                        " defined in " + COSMO_PROPERTY_DELEGATE_LISTENER, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to initialize delegate listener: " + delegateClass + 
                        " defined in " + COSMO_PROPERTY_DELEGATE_LISTENER, e);
            }
        }
        
        if(delegate != null){
            delegate.contextInitialized(sce);
        }
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(delegate != null){
            delegate.contextDestroyed(sce);
        }
    }

}
