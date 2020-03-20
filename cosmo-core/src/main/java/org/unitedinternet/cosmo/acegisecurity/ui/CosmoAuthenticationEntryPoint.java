/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.acegisecurity.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.CosmoConstants;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketException;
import org.unitedinternet.cosmo.acegisecurity.providers.ticket.TicketedItemNotFoundException;

/**
 * Implements an <code>AuthenticationEntryPoint</code> that is
 * cognizant of Cosmo's various authentication providers
 */
@Component
public class CosmoAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    /**
     * <p>
     * Returns the appropriate servlet response based on the
     * authentication provider used to service the request and the
     * specific authentication failure.
     * </p>
     * <p>
     * When a ticket was provided, if the requested item could not
     * be found, returns <code>404</code>, otherwise returns
     * <code>401</code> and sets the <code>WWW-Authenticate</code>
     * header to <code>Ticket</code>/<li>
     * </p>
     * <p>
     * For all other requests, returns <code>401</code> and sets the
     * <code>WWW-Authenticate</code> header to
     * <code>Basic realm="Chandler Server"</code>.
     * </p>
     * @param request The HttpServletRequest.
     * @param response The HttpServletResponse.
     * @param authException The authentication exception.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws ServletException - if something is wrong this exception is thrown.
     */
    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // requests with ticket credentials
        if (authException instanceof TicketException) {

            if (authException instanceof TicketedItemNotFoundException) {
                httpResponse.setStatus(404);
                httpResponse.setContentLength(0);
            } else {
                httpResponse.addHeader("WWW-Authenticate", "Ticket");
                httpResponse.setStatus(401);
                httpResponse.setContentLength(0);
            }
        } else {
            // all other requests get basic
            httpResponse.addHeader("WWW-Authenticate", "Basic realm=\""
                    + CosmoConstants.PRODUCT_NAME + "\"");
            httpResponse.setStatus(401);
            httpResponse.setContentLength(0);
        }

    }
}
