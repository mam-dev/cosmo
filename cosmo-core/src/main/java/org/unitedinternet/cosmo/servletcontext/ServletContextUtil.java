/*
 * ServletContextUtil.java Jan 22, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.servletcontext;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.unitedinternet.cosmo.CosmoIOException;

public class ServletContextUtil {
    public static final String PROPERTIES_LOCATION = "propertiesLocation";
    
    public static Properties extractApplicationProperties(ServletContext servletContext) {
        String propertiesLocation = servletContext
                .getInitParameter(PROPERTIES_LOCATION);

        if (propertiesLocation == null) {
            return null;
        }

        Properties props = new Properties();

        try {
            props.load(ServletContextUtil.class.getResourceAsStream(propertiesLocation));
        } catch (IOException e) {
            throw new CosmoIOException("Could not load " + propertiesLocation, e);
        }
        return props;
    }
}
