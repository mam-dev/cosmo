package org.unitedinternet.cosmo.boot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

/**
 * 
 * @author daniel grigore
 * 
 */
@Configuration
public class AuthenticationManagerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationManagerConfig.class);

    @Autowired
    private List<AuthenticationProvider> providers;

    @Bean
    public AuthenticationManager authManager() {
        LOG.info("\n\nBuilding AuthenticationManager with providers: \n\t{}\n\n", providers);
        ProviderManager authManager = new ProviderManager(providers);
        return authManager;
    }
}
