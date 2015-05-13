package com.unitedinternet.calendar.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.unitedinternet.cosmo.metadata.CalendarSecurity;
@CalendarSecurity
public class DummyAuthenticationProvider implements AuthenticationProvider{
    
    public DummyAuthenticationProvider() {
        System.out.println("===========================================");
        System.out.println("Inside DummyAuthenticationProvider constructor");
    }
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	    String userName = authentication.getName();
		return new UsernamePasswordAuthenticationToken(userName, "somePassword");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
