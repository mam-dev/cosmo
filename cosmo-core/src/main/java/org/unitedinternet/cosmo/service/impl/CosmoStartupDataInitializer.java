/*
 * DBInitializerDefaultProperties.java Nov 9, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.ServerProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ServerPropertyService;
import org.unitedinternet.cosmo.service.UserService;

/**
 * Cosmo specific database initialization which adds default server properties(schema version) 
 * and cosmo root
 * 
 * @author ccoman
 *
 */
public class CosmoStartupDataInitializer {

 private static final Log LOG = LogFactory.getLog(CosmoStartupDataInitializer.class);
    
    public static final String PREF_KEY_LOGIN_URL = "Login.Url";
    
    private ServerPropertyService serverPropertyService;
    
    private EntityFactory entityFactory;
    
    private String rootLoginUrl;
    
    private UserService userService;
    
    private void addServerProperties() {
        serverPropertyService.setServerProperty(
                ServerProperty.PROP_SCHEMA_VERSION,
                "160");
    }
    
    private void addOverlord() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding overlord");
        }

        User overlord = entityFactory.createUser();
        overlord.setUsername(User.USERNAME_OVERLORD);
        overlord.setFirstName("Cosmo");
        overlord.setLastName("Administrator");
        overlord.setPassword("cosmo");
        overlord.setEmail("root@localhost");
        overlord.setAdmin(Boolean.TRUE);

        Preference loginUrlPref =
            entityFactory.createPreference(PREF_KEY_LOGIN_URL, rootLoginUrl);
        overlord.addPreference(loginUrlPref);

        userService.createUser(overlord);
    }

    /* (non-Javadoc)
	 * @see org.unitedinternet.cosmo.service.StartupDataInitializer#initializeStartupData()
	 */
    public void initializeStartupData() {
        addServerProperties();
        addOverlord();
    }
    
    public void setServerPropertyService(ServerPropertyService serverPropertyService) {
        this.serverPropertyService = serverPropertyService;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public void setRootLoginUrl(String rootLoginUrl) {
        this.rootLoginUrl = rootLoginUrl;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
