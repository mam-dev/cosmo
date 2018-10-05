package org.unitedinternet.cosmo.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketAuthenticationProvider;

/**
 * TODO - Move this to web app submodule or to better packages.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class DavAuthConfig {

    @Autowired
    private TicketAuthenticationProvider ticketAuthProvider;

    @Bean
    public AuthenticationManager authManager() {
        List<AuthenticationProvider> providersList = new ArrayList<>();
        providersList.add(this.ticketAuthProvider);
        ProviderManager authManager = new ProviderManager(providersList);
        return authManager;
    }
}
