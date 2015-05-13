/*
 * AuthenticationProviderProxyTemplate.java Apr 23, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.acegisecurity;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.unitedinternet.cosmo.security.SuccessfulAuthenticationListener;
/**
 * 
 * @author corneliu dobrota
 *
 */
public class AuthenticationProviderProxyTemplate<A extends AuthenticationProvider, L extends SuccessfulAuthenticationListener> implements AuthenticationProvider{
    
    private final A delegate;
    private final Collection<L> successfulAuthenticationListeners;
    
    public AuthenticationProviderProxyTemplate(final A delegate,
            final Collection<L> successfulAuthenticationListeners){
        this.delegate = delegate;
        this.successfulAuthenticationListeners = successfulAuthenticationListeners;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication initialAuth = delegate.authenticate(authentication);
        
        if(initialAuth != null && !delegate.supports(authentication.getClass())){
            return null;
        }
        
        for(SuccessfulAuthenticationListener l : successfulAuthenticationListeners){
            l.onSuccessfulAuthentication(initialAuth);
        }
        
        return processInitialAuthenticationResult(initialAuth);
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
    
    protected  Authentication processInitialAuthenticationResult(Authentication authentication){
        return authentication;
    }
}
