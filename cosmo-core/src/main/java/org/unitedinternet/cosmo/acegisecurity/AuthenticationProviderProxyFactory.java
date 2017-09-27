/*
 * AuthenticationProviderProxyFactory.java Apr 22, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.acegisecurity;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.SuccessfulAuthenticationListener;
import org.unitedinternet.cosmo.service.UserService;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class AuthenticationProviderProxyFactory {

    private UserService userService;
    private ContentDao contentDao;

    public AuthenticationProviderProxyFactory(UserService userService, ContentDao contentDao) {
        this.userService = userService;
        this.contentDao = contentDao;
    }

    public <T extends AuthenticationProvider, L extends SuccessfulAuthenticationListener> AuthenticationProvider createProxyFor(
            final T delegate, final Collection<L> successfulAuthenticationListeners) {

        AuthenticationProvider proxy = null;

        if (delegate.supports(UsernamePasswordAuthenticationToken.class)) {
            proxy = new AuthenticationProviderProxyTemplate<T, L>(delegate, successfulAuthenticationListeners) {

                @Override
                protected Authentication processInitialAuthenticationResult(Authentication authentication) {
                    User user = userService.getUser(authentication.getPrincipal().toString());
                    UserDetails userDetails = new CosmoUserDetails(user);
                    UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(userDetails,
                            authentication.getCredentials(), authentication.getAuthorities());

                    return result;
                }
            };
        } else if (delegate.supports(PreAuthenticatedAuthenticationToken.class)) {
            proxy = new AuthenticationProviderProxyTemplate<T, L>(delegate, successfulAuthenticationListeners);
        } else if (delegate.supports(TicketAuthenticationToken.class)) {
            proxy = new AuthenticationProviderProxyTemplate<T, L>(delegate, successfulAuthenticationListeners) {
                @Override
                protected Authentication processInitialAuthenticationResult(Authentication authentication) {
                    if (authentication instanceof TicketAuthenticationToken) {
                        TicketAuthenticationToken ticketAuthenticationToken = (TicketAuthenticationToken) authentication;
                        Ticket ticket = null;

                        for (String key : ticketAuthenticationToken.getKeys()) {
                            ticket = contentDao.findTicket(key);
                            if (ticket != null) {
                                ticketAuthenticationToken.setTicket(ticket);
                                break;
                            }
                        }
                    }
                    return authentication;
                }
            };
        } else {
            throw new IllegalStateException("Unknown authentication token type for authentication provider ["
                    + delegate.getClass().getName() + "]");
        }

        return proxy;
    }
}