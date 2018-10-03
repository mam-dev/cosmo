package org.unitedinternet.cosmo.security.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.unitedinternet.cosmo.acegisecurity.AuthenticationProviderDelegator;
import org.unitedinternet.cosmo.acegisecurity.AuthenticationProviderProxyFactory;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketAuthenticationProvider;
import org.unitedinternet.cosmo.acegisecurity.ui.CosmoAuthenticationEntryPoint;
import org.unitedinternet.cosmo.security.SuccessfulAuthenticationListener;

/**
 * TODO - Remove this.
 * @author daniel grigore
 *
 */
@Configuration
public class BasicAuthenticationFilterConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthenticationFilterConfiguration.class);
	
	@Autowired
	private DaoAuthenticationProvider daoAuthenticationProvider;
	
	@Autowired
	private AnonymousAuthenticationProvider anonymousAuthenticationProvider;
	
	@Autowired
	private TicketAuthenticationProvider ticketAuthenticationProvider;
	
	@Autowired
	private CosmoAuthenticationEntryPoint cosmoAuthenticationEntryPoint;
	
	@Autowired
	private AuthenticationProviderProxyFactory authenticationProviderProxyFactory;		
	
	@Bean
	public BasicAuthenticationFilter getBasicAuthenticationFilter() throws Exception {
		BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(getAuthenticationManager(), cosmoAuthenticationEntryPoint);
		return basicAuthenticationFilter;
	}
	
	
	
	@Bean
	public ProviderManager getAuthenticationManager() throws Exception {
		
		List<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(daoAuthenticationProvider);
		providers.add(anonymousAuthenticationProvider);
		providers.add(ticketAuthenticationProvider);
		providers.add(getAuthenticationProviderDelegator());
		
		return new ProviderManager(providers);
	}
	
	@Bean
	public AnonymousAuthenticationProvider getAnonymousAuthenticationProvider() {
		return new AnonymousAuthenticationProvider("badgerbadgerbadger");
	}
	
	@Bean
	public AuthenticationProviderDelegator getAuthenticationProviderDelegator() throws Exception {
		return new AuthenticationProviderDelegator(getProviders());
	}
	
	private Collection<? extends AuthenticationProvider> getProviders(){
	    Collection<? extends AuthenticationProvider> authenticationProviders = Collections.emptyList();
	    checkAuthenticationProviders(authenticationProviders);
	    LOGGER.info("Found [{}] authentication provider implementations", authenticationProviders.size());
	    
        Collection<? extends SuccessfulAuthenticationListener> successfulAuthenticationListeners = getSuccessfulAuthenticationListeners();
        LOGGER.info("Found [{}] successful authentication listener implementations", authenticationProviders.size());
        
        List<AuthenticationProvider> result = new ArrayList<>(1);
        
        for(AuthenticationProvider authenticationProvider : authenticationProviders){
            AuthenticationProvider authenticationProviderProxy = authenticationProviderProxyFactory.createProxyFor(authenticationProvider, successfulAuthenticationListeners);
            result.add(authenticationProviderProxy);
        }
        
        return result;
	}
	
	private static void checkAuthenticationProviders(Collection<? extends AuthenticationProvider> authenticationProviders){
	    if(authenticationProviders.isEmpty()){
//	        throw new SecurityInstantiationException("No authentication provider was found. Please ");
	    }
	}
	private Collection<? extends SuccessfulAuthenticationListener> getSuccessfulAuthenticationListeners() {
	    Collection<? extends SuccessfulAuthenticationListener> successfulAuthListeners = Collections.emptyList();
	    return successfulAuthListeners;
	}
}
