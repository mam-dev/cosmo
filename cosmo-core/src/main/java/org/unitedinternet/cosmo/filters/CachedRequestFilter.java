package org.unitedinternet.cosmo.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.unitedinternet.cosmo.util.CachedRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class CachedRequestFilter extends GenericFilterBean {
   private static final Log LOG = LogFactory.getLog(CachedRequestFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (LOG.isTraceEnabled()) {
            LOG.trace("Caching request: " + request.getMethod() + " " + request.getPathInfo());
        }
        filterChain.doFilter(new CachedRequest((request)), servletResponse);
    }
}
