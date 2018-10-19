package org.unitedinternet.cosmo.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

@Configuration
public class ServiceLocatorFactoryConfiguration {

    private static final String PREFIX_CMP = "/cmp";
    private static final String PREFIX_DAV = "/dav";

    @Autowired
    private CosmoSecurityManager cosmoSecurityManager;

    @Bean
    public ServiceLocatorFactory getServiceLocatorFactory() {
        ServiceLocatorFactory serviceLocatorFactory = new ServiceLocatorFactory();
        serviceLocatorFactory.setCmpPrefix(PREFIX_CMP);
        serviceLocatorFactory.setDavPrefix(PREFIX_DAV);
        serviceLocatorFactory.setSecurityManager(cosmoSecurityManager);

        return serviceLocatorFactory;
    }
}
