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
package org.unitedinternet.cosmo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Defines server-wide constant attributes.
 */
public final class CosmoConstants {
    // cannot be instantiated
    private CosmoConstants() {
    }

    /**
     * The "friendly" name of the product used for casual identification.
     */
    public static final String PRODUCT_NAME;

    /**
     * The Cosmo release version number.
     */
    public static final String PRODUCT_VERSION;

    private static final String PRODUCT_SUFFIX = "//NONSGML//DE";

    /**
     * A string identifier for Cosmo used to distinguish it from other
     * software products.
     */
    public static final String PRODUCT_ID;

    /**
     * product Id key in cosmo.properties files, differs from brand to another
     * example : -//1&1 Mail & Media GmbH/WEB.DE Kalender Server//NONSGML//DE
     */

    private static final String PRODUCT_NAME_KEY = "calendar.server.productName";
    private static final String PRODUCT_VERSION_KEY = "calendar.server.Version";

    /**
     * The URL of the Cosmo product web site.
     */
    public static final String PRODUCT_URL = "http://cosmo.osafoundation.org/";


    /**
     * The Cosmo secham version.  This may or may not change when the
     * PRODUCT_VERSION changes.
     */
    public static final String SCHEMA_VERSION = "160";

    /**
     * The servlet context attribute which contains the Cosmo server
     * administrator's email address.
     */
    public static final String SC_ATTR_SERVER_ADMIN = "cosmo.server.admin";

    // read the product version from VERSION_FILE

    public static final Locale LANGUAGE_LOCALE;
    
    private static final String LOCALE_KEY = "application.locale";
    
    private static final String PROPERTIES_FILE = "/etc/application.properties"; 
    
    private static final Locale FALLBACK_LOCALE = Locale.GERMAN;
    
    static {
        Properties props = loadCosmoProperties();

        PRODUCT_NAME = props.getProperty(PRODUCT_NAME_KEY);
        PRODUCT_VERSION = props.getProperty(PRODUCT_VERSION_KEY);

        // form the product Id using current build version
        PRODUCT_ID = new StringBuilder(PRODUCT_NAME)
						                .append(" ").append(PRODUCT_VERSION).append(PRODUCT_SUFFIX)
						                .toString();

        String localeKey = props.getProperty(LOCALE_KEY);
        LANGUAGE_LOCALE = getLocaleFor(localeKey);
    }

    /**
     * @return Properties
     */
    private static Properties loadCosmoProperties() {
        
        Properties props = new Properties();
        
        try(InputStream is = CosmoConstants.class.getResourceAsStream(PROPERTIES_FILE)) {
            props.load(is);
        } catch (IOException e) {
			
		} 
        return props;
    }
    
    private static Locale getLocaleFor(String language){
        if(language == null){
            return FALLBACK_LOCALE;
        }
        
        for (String isoLanguage : Locale.getISOLanguages()){
            if(isoLanguage.equalsIgnoreCase(language)){
                return Locale.forLanguageTag(isoLanguage);
            }
        }
        
        return FALLBACK_LOCALE;
    }
}