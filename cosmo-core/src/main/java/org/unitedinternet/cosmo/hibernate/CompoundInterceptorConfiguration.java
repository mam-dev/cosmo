package org.unitedinternet.cosmo.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.model.hibernate.AuditableObjectInterceptor;
import org.unitedinternet.cosmo.model.hibernate.EventStampInterceptor;

@Configuration
public class CompoundInterceptorConfiguration {

	@Autowired
	private AuditableObjectInterceptor auditableObjectInterceptor;
	
	@Autowired
	private EventStampInterceptor eventStampInterceptor;
	
	@Bean
	public CompoundInterceptor getCompoundInterceptor() {
		
		CompoundInterceptor compoundInterceptor = new CompoundInterceptor();
		List<Interceptor> interceptors = new ArrayList<>();
		interceptors.add(auditableObjectInterceptor);
		interceptors.add(eventStampInterceptor);
		compoundInterceptor.setInterceptors(interceptors);
		
		return compoundInterceptor;
	}
	
}
