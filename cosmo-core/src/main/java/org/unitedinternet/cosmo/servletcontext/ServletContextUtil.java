/*
 * ServletContextUtil.java Jan 22, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.servletcontext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextUtil.class);
	
    public static final String PROPERTIES_LOCATION = "propertiesLocation";
    
    public static Properties extractApplicationProperties(ServletContext servletContext) {
        String propertiesLocation = servletContext.getInitParameter(PROPERTIES_LOCATION);

        Properties props = new Properties();
        InputStream is = ServletContextUtil.class.getResourceAsStream(propertiesLocation); 
        
        if (propertiesLocation == null || is == null) {
            return props;
        }

        try {
            props.load(is);
        } catch (IOException e) {
            LOGGER.warn("Unable to load properties from location [{}]", propertiesLocation);
        }finally{
        	try {
				is.close();
			} catch (IOException e) {
				LOGGER.error("Colud not close input stream");
			}
        }
        return props;
    }
}
