package org.unitedinternet.cosmo.acegisecurity.providers.ticket;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.unitedinternet.cosmo.server.ServerUtils;

import jakarta.servlet.http.HttpServletRequest;

public class TicketAuthenticationConverter implements AuthenticationConverter {
    
    private static final String SLASH = "/"; 
    
    @Override
    public @Nullable Authentication convert(HttpServletRequest httpRequest) {

        Set<String> keys = ServerUtils.findTicketKeys(httpRequest);

        if (!keys.isEmpty()) {
            String path = httpRequest.getPathInfo();
            if (path == null || path.isEmpty()) {
                path = SLASH;
            }
            if (!path.equals(SLASH) && path.endsWith(SLASH)) {
                path = path.substring(0, path.length() - 1);
            }            
            return new TicketAuthenticationToken(path, keys);
        }
        return null;
    }
}