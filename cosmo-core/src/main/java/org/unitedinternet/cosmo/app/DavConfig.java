package org.unitedinternet.cosmo.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.Jsr250SecurityConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.ExtraTicketProcessingFilter;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketProcessingFilter;
import org.unitedinternet.cosmo.acegisecurity.ui.CosmoAuthenticationEntryPoint;
import org.unitedinternet.cosmo.dav.acegisecurity.DavAccessDecisionManager;
import org.unitedinternet.cosmo.ext.ContentSourceProcessor;
import org.unitedinternet.cosmo.ext.ProxyFactory;
import org.unitedinternet.cosmo.filters.CosmoExceptionLoggerFilter;
import org.unitedinternet.cosmo.filters.UsernameRequestIntegrationFilter;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

import net.fortuna.ical4j.model.Calendar;

/**
 * TODO - Move this to web app submodule or to better packages.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class DavConfig {

    @Bean
    public ProxyFactory proxy() {
        return new ProxyFactory() {
            @Override
            public HttpHost getProxy(URL url) {
                return null;
            }
        };
    }

    @Bean
    public ContentSourceProcessor processors() {
        return new ContentSourceProcessor() {

            @Override
            public void postProcess(Calendar calendar) {
                // DO nothing
            }
        };
    }

    private static final String PATH_DAV = "/dav/*";

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
    private CosmoSecurityManager cosmoSecurityManager;

    @Autowired
    private CosmoExceptionLoggerFilter cosmoExceptionFilter;

    @Bean
    @SuppressWarnings("serial")
    public ServletRegistrationBean<?> davServlet() {
        return new ServletRegistrationBean<>(new HttpRequestHandlerServlet() {
            @Override
            public String getServletName() {
                return "davRequestHandler";
            }
        }, PATH_DAV);
    }

    @Bean
    public FilterRegistrationBean<?> openEntityManagerInViewFilter() {
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(new OpenEntityManagerInViewFilter());
        filterBean.addUrlPatterns(PATH_DAV);
        return filterBean;
    }

    // Security filters

    @Bean
    public FilterRegistrationBean<?> basicAuthFilter() {
        BasicAuthenticationFilter filter = new BasicAuthenticationFilter(this.authManager, this.authEntryPoint);
        FilterRegistrationBean<?> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns(PATH_DAV);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<?> ticketFilter() {
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(ticketFilter);
        filterBean.addUrlPatterns(PATH_DAV);
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<?> extraTicketFilter() {
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(extraTicketFilter);
        filterBean.addUrlPatterns(PATH_DAV);
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<?> cosmoExceptionFilter() {
        FilterRegistrationBean<?> bean = new FilterRegistrationBean<>(this.cosmoExceptionFilter);
        bean.addUrlPatterns(PATH_DAV);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<?> securityFilter() {
        FilterSecurityInterceptor filter = new FilterSecurityInterceptor();
        filter.setAuthenticationManager(this.authManager);
        filter.setAccessDecisionManager(this.davDecisionManager);

        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> metadata = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        Collection<ConfigAttribute> configAttributes = new ArrayList<>();
        configAttributes.add(Jsr250SecurityConfig.PERMIT_ALL_ATTRIBUTE);
        metadata.put(AnyRequestMatcher.INSTANCE, configAttributes);
        filter.setSecurityMetadataSource(new DefaultFilterInvocationSecurityMetadataSource(metadata));
        FilterRegistrationBean<?> filterBean = new FilterRegistrationBean<>(filter);
        filterBean.addUrlPatterns(PATH_DAV);
        // filterBean.setOrder(-1);
        return filterBean;
    }

    @Bean
    public FilterRegistrationBean<?> usernameIntegrationFilter() {
        UsernameRequestIntegrationFilter filter = new UsernameRequestIntegrationFilter(this.cosmoSecurityManager);
        FilterRegistrationBean<?> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns(PATH_DAV);
        return bean;
    }

}
