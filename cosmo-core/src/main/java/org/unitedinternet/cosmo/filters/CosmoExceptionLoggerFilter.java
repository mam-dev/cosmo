package org.unitedinternet.cosmo.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Component;
/**
 * This class avoid to throw full stack trace to client. 
 * The exception is logged in the server log.
 * @author ccoman
 * TODO should this class extend something else that ExceptionTranslationFilter? 
 */
@Component
public class CosmoExceptionLoggerFilter extends ExceptionTranslationFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmoExceptionLoggerFilter.class);
    
    public CosmoExceptionLoggerFilter(AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationEntryPoint);
    }

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
