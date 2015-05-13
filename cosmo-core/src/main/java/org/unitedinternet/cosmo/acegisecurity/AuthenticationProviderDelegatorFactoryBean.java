package org.unitedinternet.cosmo.acegisecurity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.CalendarSecurity;
import org.unitedinternet.cosmo.security.SuccessfulAuthenticationListener;
/**
 * 
 * @author corneliu dobrota
 *
 */
public class AuthenticationProviderDelegatorFactoryBean implements FactoryBean<AuthenticationProviderDelegator>{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderDelegatorFactoryBean.class);
    
    private AuthenticationProviderProxyFactory authenticationProviderProxyFactory;
    private ExternalComponentInstanceProvider externalComponentInstanceProvider;
    
    public AuthenticationProviderDelegatorFactoryBean(AuthenticationProviderProxyFactory authenticationProviderProxyFactory,
                                                        ExternalComponentInstanceProvider externalComponentInstanceProvider){
        this.authenticationProviderProxyFactory = authenticationProviderProxyFactory;
        this.externalComponentInstanceProvider = externalComponentInstanceProvider; 
    }
    
	@Override
	public AuthenticationProviderDelegator getObject() throws Exception {
		return new AuthenticationProviderDelegator(getProviders());
	}

	@Override
	public Class<AuthenticationProviderDelegator> getObjectType() {
		return AuthenticationProviderDelegator.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
	private Collection<? extends AuthenticationProvider> getProviders(){
	    Collection<? extends AuthenticationProvider> authenticationProviders = externalComponentInstanceProvider.getImplInstancesAnnotatedWith(CalendarSecurity.class, AuthenticationProvider.class);
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
	    Collection<? extends SuccessfulAuthenticationListener> successfulAuthListeners = externalComponentInstanceProvider.getImplInstancesAnnotatedWith(CalendarSecurity.class, SuccessfulAuthenticationListener.class);
	    return successfulAuthListeners;
	}
}