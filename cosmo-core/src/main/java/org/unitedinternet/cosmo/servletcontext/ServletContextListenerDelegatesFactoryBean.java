package org.unitedinternet.cosmo.servletcontext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.Delegate;
import org.unitedinternet.cosmo.servlet.ServletContextListenerDelegate;



/**
 * 
 * @author corneliu dobrota
 *
 */
@Configuration
public class ServletContextListenerDelegatesFactoryBean {
	
	private final ExternalComponentInstanceProvider instanceProvider;
	
	@Autowired
	public ServletContextListenerDelegatesFactoryBean(ExternalComponentInstanceProvider instanceProvider){
		this.instanceProvider = instanceProvider;
	}
	
	@Bean
	public List<? extends ServletContextListenerDelegate> getObject() throws Exception {
		Set<? extends ServletContextListenerDelegate> delegates = 
							instanceProvider.getImplInstancesAnnotatedWith(Delegate.class, 
																			ServletContextListenerDelegate.class);
		
		List<ServletContextListenerDelegate> delegatesList = new ArrayList<ServletContextListenerDelegate>(delegates);
		Collections.sort(delegatesList, new Comparator<ServletContextListenerDelegate>(){

			@Override
			public int compare(ServletContextListenerDelegate o1, ServletContextListenerDelegate o2) {
				
				int order1 = o1.getClass().getAnnotation(Delegate.class).order();
				int order2 = o2.getClass().getAnnotation(Delegate.class).order();
				
				return Integer.compare(order1, order2);
			}});
		
		return delegatesList;
	}	
}