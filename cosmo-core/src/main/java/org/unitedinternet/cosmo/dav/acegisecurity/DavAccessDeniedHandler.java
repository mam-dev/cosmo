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
package org.unitedinternet.cosmo.dav.acegisecurity;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;

/**
 * <p>
 * Handles <code>AccessDeniedException</code> by sending a 403 response enclosing an XML body describing the error.
 * </p>
 */
public class DavAccessDeniedHandler implements AccessDeniedHandler {

    public void handle(ServletRequest request, ServletResponse response, AccessDeniedException exception)
            throws IOException, ServletException {

        if (!(response instanceof HttpServletResponse)) {
            throw new IllegalStateException("Expected response of type: [" + HttpServletResponse.class.getName()
                    + "], received :[" + response.getClass().getName() + "]");
        }

        StandardDavResponse sdr = new StandardDavResponse((HttpServletResponse) response);
        NeedsPrivilegesException toSend = null;
        if (exception instanceof DavAccessDeniedException) {
            DavAccessDeniedException e = (DavAccessDeniedException) exception;
            toSend = new NeedsPrivilegesException(e.getHref(), e.getPrivilege());
        } else {
            toSend = new NeedsPrivilegesException(exception.getMessage());
        }
        sdr.sendDavError(toSend);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

    }
}
