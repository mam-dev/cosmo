/*
 * Copyright 2006-2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A filter that puts Cosmo's authentication information into a request
 * attribute. This is primarily to make this information available
 * to the Tomcat AccessLogValve.
 * 
 * @author travis
 *
 */
public class UsernameRequestIntegrationFilter implements Filter {


    private static final String USERNAME_ATTRIBUTE_KEY = "COSMO_PRINCIPAL";
    private CosmoSecurityManager securityManager;
    private static final String BEAN_SECURITY_MANAGER = "securityManager";


    public void destroy() {
        // TODO Auto-generated method stub
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {
            String principal = securityManager.getSecurityContext().getName();
            if (securityManager.getSecurityContext().getTicket() != null) {
                principal = "ticket:" + principal;
            }
            request.setAttribute(USERNAME_ATTRIBUTE_KEY, principal);
        } catch (CosmoSecurityException e){
            request.setAttribute(USERNAME_ATTRIBUTE_KEY, "-");
        }
        
        chain.doFilter(request, response);
    }

    public void init(FilterConfig config) throws ServletException {
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext(
                    config.getServletContext()
            );

        this.securityManager = (CosmoSecurityManager)
            wac.getBean(BEAN_SECURITY_MANAGER,
                        CosmoSecurityManager.class);

        if (this.securityManager == null){
            throw new ServletException("Could not initialize HttpLoggingFilter: " +
            "Could not find security manager.");
        }
    }


}
