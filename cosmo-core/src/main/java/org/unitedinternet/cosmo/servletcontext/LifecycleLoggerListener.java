/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.servletcontext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitedinternet.cosmo.CosmoConstants;

/**
 * A {@link javax.servlet.ServletContextListener} that logs
 * information about Cosmo servlet context lifecycle events.
 */
public class LifecycleLoggerListener implements ServletContextListener {
    private static final Log LOG =
        LogFactory.getLog(LifecycleLoggerListener.class);

    /**
     * Logs an info message when the servlet context is starting.
     */
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info(CosmoConstants.PRODUCT_NAME + " " +
                 CosmoConstants.PRODUCT_VERSION + " starting");
    }

    /**
     * Logs an info message when the servlet context is shutting
     * down.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info(CosmoConstants.PRODUCT_NAME + " " +
                 CosmoConstants.PRODUCT_VERSION + " stopping");
        java.beans.Introspector.flushCaches();
        LogFactory.releaseAll();
    }
}
