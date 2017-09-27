package com.unitedinternet.calendar.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.unitedinternet.cosmo.metadata.CalendarSecurity;

@CalendarSecurity
public class DummyAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyAuthenticationProvider.class);

    @Override
    public UsernamePasswordAuthenticationToken authenticate(Authentication authentication)
            throws AuthenticationException {
        String userName = authentication.getName();
        LOGGER.info("===Authenticating user [{}]===", userName);
        return new UsernamePasswordAuthenticationToken(userName, authentication.getCredentials(),
                authentication.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
