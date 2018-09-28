package org.unitedinternet.cosmo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalComponentFactoryChainConfiguration {

	@Autowired
	private SpringExternalComponentFactory springExternalComponentFactory;
	
	@Bean
	public ExternalComponentFactoryChain getExternalComponentFactoryChain() {
		ExternalComponentFactoryChain externalComponentFactoryChain = new ExternalComponentFactoryChain(springExternalComponentFactory, new DefaultExternalComponentFactory());
		return externalComponentFactoryChain;
	}
}
