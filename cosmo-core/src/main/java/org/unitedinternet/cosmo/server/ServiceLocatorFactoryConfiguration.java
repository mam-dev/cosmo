package org.unitedinternet.cosmo.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

@Configuration
public class ServiceLocatorFactoryConfiguration {

	@Autowired
	private CosmoSecurityManager cosmoSecurityManager;
	
	@Bean
	public ServiceLocatorFactory getServiceLocatorFactory() {
		ServiceLocatorFactory serviceLocatorFactory = new ServiceLocatorFactory();
		
		serviceLocatorFactory.setCmpPrefix("/cmp"); //TODO - add these to yaml config
		serviceLocatorFactory.setDavPrefix("/dav");
		serviceLocatorFactory.setSecurityManager(cosmoSecurityManager);
		
		return serviceLocatorFactory;
	}
}
