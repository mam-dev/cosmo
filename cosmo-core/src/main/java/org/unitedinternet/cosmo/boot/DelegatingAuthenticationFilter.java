package org.unitedinternet.cosmo.boot;

import java.io.IOException;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Derived AuthenticationFilter which replaces unusable defaults with more sensible ones
 */
@SuppressWarnings("unused")
public final class DelegatingAuthenticationFilter extends AuthenticationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingAuthenticationFilter.class);

    public DelegatingAuthenticationFilter(AuthenticationManager authenticationManager,
            AuthenticationConverter authenticationConverter) {
        this(authenticationManager, authenticationConverter, AnyRequestMatcher.INSTANCE);
    }

    public DelegatingAuthenticationFilter(AuthenticationManager authenticationManager,
            AuthenticationConverter authenticationConverter, RequestMatcher requestMatcher) {
        super(authenticationManager, authenticationConverter);
        // 403 instead of 401, as in Webflux
        setFailureHandler(DelegatingAuthenticationFilter::handleAuthenticationFailuresAsForbidden);
        // prevent redirect which should only be done for sessions
        setSuccessHandler((request, response, auth) -> {
        });
        setRequestMatcher(requestMatcher);
    }

    @Override
    @NonNull
    protected String getAlreadyFilteredAttributeName() {
        return getAuthenticationConverter().getClass().getName() + "_and_"
                + getAuthenticationManagerResolver().getClass().getName();
    }

    /**
     * @return RequestMatcher which matches whenever not yet authenticated. This is especially useful when a mocked
     *         authentication should override any functional one.
     */    
    public static RequestMatcher needsAuthenticationRequestMatcher() {
        return request -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated).isEmpty();
    }

    private static void handleAuthenticationFailuresAsForbidden(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException exception) throws IOException {
        LOG.debug("Handling AuthenticationException as 403 Forbidden", exception);
        new Http403ForbiddenEntryPoint().commence(request, response, exception);
    }
}
