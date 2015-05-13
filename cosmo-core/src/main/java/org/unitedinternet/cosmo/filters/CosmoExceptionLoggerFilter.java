package org.unitedinternet.cosmo.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
/**
 * This class avoid to throw full stack trace to client. 
 * The exception is logged in the server log.
 * @author ccoman
 * TODO should this class extend something else that ExceptionTranslationFilter? 
 */
public class CosmoExceptionLoggerFilter extends ExceptionTranslationFilter {
    
    public CosmoExceptionLoggerFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationEntryPoint);
    }

    protected static final Log LOGGER = LogFactory.getLog(CosmoExceptionLoggerFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilter(req, res, chain);
        } catch (Exception e) {
            if(res instanceof HttpServletResponse){
                HttpServletResponse response = (HttpServletResponse) res;
                
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An expected error occured!");
            }
            LOGGER.error(e.getMessage(), e);
        }
    }
}
