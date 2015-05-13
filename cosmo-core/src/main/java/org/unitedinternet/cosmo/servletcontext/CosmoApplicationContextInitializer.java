package org.unitedinternet.cosmo.servletcontext;

import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 *  Initialize cosmo application.
 * 
 */
public class CosmoApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final String COSMO_ACTIVE_PROFILES = "cosmo.activeProfiles";


    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        Properties props = ServletContextUtil.
                extractApplicationProperties(applicationContext.getServletContext());

        applicationContext.getEnvironment().getPropertySources().addFirst(
                new PropertiesPropertySource("init", props));
        
        //add bean profiles
        if(props.containsKey(COSMO_ACTIVE_PROFILES)){
            String profiles = (String)props.get(COSMO_ACTIVE_PROFILES);
            applicationContext.getEnvironment().setActiveProfiles(profiles.split(","));
        }
    }

    
    
}
