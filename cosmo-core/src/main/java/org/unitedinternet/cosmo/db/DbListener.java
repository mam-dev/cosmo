/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.db;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitedinternet.cosmo.servlet.ServletContextListenerDelegate;

/**
 * A <code>ServletContextListener</code> that performs maintenance tasks on the Cosmo database when the application
 * starts up and shuts down.
 *
 * This class simply hooks into the servlet context lifecycle. It delegates the real database work to a series of helper
 * classes.
 *
 * Typical maintenance tasks include schema creation, schema migration and population of seed data.
 *
 * @see ServletContextListener
 */

public class DbListener implements ServletContextListener {

    private static final String BEAN_DB_INITIALIZER = "dbInitializer";
    private static final String DELEGATES_BEAN_NAME = "servletContextListenersDelegate";

    private List<ServletContextListenerDelegate> delegates;

    /**
     * Resolves dependencies using the Spring <code>WebApplicationContext</code> and performs startup maintenance tasks.
     */
    @SuppressWarnings("unchecked")
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

        DbInitializer initializer = beanForName(BEAN_DB_INITIALIZER, wac, DbInitializer.class);

        initializer.initialize();

        delegates = beanForName(DELEGATES_BEAN_NAME, wac, List.class);

        for (ServletContextListenerDelegate delegate : delegates) {
            delegate.contextInitialized(sce);
        }
    }

    private <T> T beanForName(String beanName, WebApplicationContext wac, Class<T> clazz) {
        return (T) wac.getBean(beanName, clazz);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        for (ServletContextListenerDelegate delegate : delegates) {
            delegate.contextDestroyed(sce);
        }
    }
}
