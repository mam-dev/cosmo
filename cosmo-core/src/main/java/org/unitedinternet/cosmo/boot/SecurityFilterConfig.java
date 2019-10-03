package org.unitedinternet.cosmo.boot;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.ExtraTicketProcessingFilter;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketProcessingFilter;
import org.unitedinternet.cosmo.acegisecurity.ui.CosmoAuthenticationEntryPoint;
import org.unitedinternet.cosmo.dav.acegisecurity.DavAccessDecisionManager;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.unitedinternet.cosmo.filters.CosmoExceptionLoggerFilter;

/**
 * Configuration class that defines the DAV servlet as well as the list of filters.
 * 
 * @author daniel grigore
 *
 */
@Configuration
@SuppressWarnings("serial")
public class SecurityFilterConfig {

    public static final String PATH_DAV = "/dav/*";
    public static final String ROLES = "ROLES_WE_DONT_HAVE";

    /**
     * @see StandardRequestHandler component.
     */
    private static final String DAV_SERVLET_NAME = "davRequestHandler";

    @Autowired
    private ExtraTicketProcessingFilter extraTicketFilter;

    @Autowired
    private TicketProcessingFilter ticketFilter;

    @Autowired
    private CosmoAuthenticationEntryPoint authEntryPoint;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private DavAccessDecisionManager davDecisionManager;

    @Autowired
    private CosmoExceptionLoggerFilter cosmoExceptionFilter;

    @Autowired
    private HttpFirewall httpFirewall;

    @Bean
    public ServletRegistrationBean<?> davServlet() {
        HttpRequestHandlerServlet handler = new HttpRequestHandlerServlet() {
            @Override
            public String getServletName() {
                return DAV_SERVLET_NAME;
            }
        };
        ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(handler, PATH_DAV);
        bean.setName(handler.getServletName());
        bean.setOrder(0);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<?> openEntityManagerInViewFilter() {
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(new OpenEntityManagerInViewFilter());
        filterBean.addUrlPatterns(PATH_DAV);
        return filterBean;
    }

    // Security filter chain

    @Bean
    public FilterRegistrationBean<?> securityFilterChain() {
        FilterSecurityInterceptor securityFilter = new FilterSecurityInterceptor();
        securityFilter.setAuthenticationManager(this.authManager);
        securityFilter.setAccessDecisionManager(this.davDecisionManager);
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> metadata = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        metadata.put(AnyRequestMatcher.INSTANCE, SecurityConfig.createList(ROLES));
        securityFilter.setSecurityMetadataSource(new DefaultFilterInvocationSecurityMetadataSource(metadata));

        /*
         * Note that the order in which filters are defined is highly important.
         */
        SecurityFilterChain filterChain = new DefaultSecurityFilterChain(AnyRequestMatcher.INSTANCE,
                this.cosmoExceptionFilter, this.extraTicketFilter, this.ticketFilter,
                new BasicAuthenticationFilter(authManager, this.authEntryPoint), securityFilter);
        FilterChainProxy proxy = new FilterChainProxy(filterChain);
        proxy.setFirewall(this.httpFirewall);
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(proxy);
        filterBean.addUrlPatterns(PATH_DAV);
        return filterBean;
    }
}
