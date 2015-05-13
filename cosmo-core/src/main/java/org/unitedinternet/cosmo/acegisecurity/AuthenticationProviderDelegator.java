package org.unitedinternet.cosmo.acegisecurity;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class AuthenticationProviderDelegator implements AuthenticationProvider{
	
	private Collection<? extends AuthenticationProvider> delegates;
	
	public AuthenticationProviderDelegator(Collection<? extends AuthenticationProvider> delegates){
		this.delegates = delegates;
	}
	@Override	
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication result = null;
		
		for(AuthenticationProvider delegate : delegates){
			
			if(delegate.supports(authentication.getClass()) && (result = delegate.authenticate(authentication)) != null){
				break;
			}
		}
		
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		for(AuthenticationProvider delegate : delegates){
			if(delegate.supports(authentication)){
				return true;
			}
		}
		return false;
	}
}