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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet filter to log basic information about HTTP requests.
 * 
 * Format string syntax:
 * 
 * <table cellspacing="2" cellpadding="4"border="0">
 * <tr>
 * <th bgcolor="#CCCCCC" class="twikiFirstCol"><a rel="nofollow"
 * href="http://wiki.osafoundation.org/bin/view/Journal/HttpLoggingFilterDoc?sortcol=0;table=1;up=0#sorted_table"
 * title="Sort by this column"><font color="#000000">Request parameter</font></a></th>
 * <th bgcolor="#CCCCCC"><a rel="nofollow"
 * href="http://wiki.osafoundation.org/bin/view/Journal/HttpLoggingFilterDoc?sortcol=1;table=1;up=0#sorted_table"
 * title="Sort by this column"><font color="#000000">Formatting String</font></a></th>
 * <th bgcolor="#CCCCCC"><a rel="nofollow"
 * href="http://wiki.osafoundation.org/bin/view/Journal/HttpLoggingFilterDoc?sortcol=2;table=1;up=0#sorted_table"
 * title="Sort by this column"><font color="#000000">Request Object Access Method</font></a></th>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Method</td>
 * <td bgcolor="#EEEEEE" align="left">%M</td>
 * <td bgcolor="#EEEEEE" align="left">getMethod()</td>
 * </tr>
 * 
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Scheme</td>
 * <td bgcolor="#EEEEEE" align="left">%C</td>
 * <td bgcolor="#EEEEEE" align="left">getScheme()</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Server</td>
 * <td bgcolor="#EEEEEE" align="left">%S</td>
 * <td bgcolor="#EEEEEE" align="left">getServerName()</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Port</td>
 * <td bgcolor="#EEEEEE" align="left">%P</td>
 * <td bgcolor="#EEEEEE" align="left">getServerPort()</td>
 * </tr>
 * 
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Request URI</td>
 * <td bgcolor="#EEEEEE" align="left">%U</td>
 * <td bgcolor="#EEEEEE" align="left">getRequestURI()</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Content Length</td>
 * <td bgcolor="#EEEEEE" align="left">%L</td>
 * <td bgcolor="#EEEEEE" align="left">getContentLength()</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Query String</td>
 * <td bgcolor="#EEEEEE" align="left">%Q</td>
 * <td bgcolor="#EEEEEE" align="left">getQueryString()</td>
 * </tr>
 * 
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Session Id</td>
 * <td bgcolor="#EEEEEE" align="left">%I</td>
 * <td bgcolor="#EEEEEE" align="left">getSession().getId()</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">Auth Principal</td>
 * <td bgcolor="#EEEEEE" align="left">%A</td>
 * <td bgcolor="#EEEEEE" align="left">calculated</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE" align="left">%</td>
 * <td bgcolor="#EEEEEE" align="left">%%</td>
 * <td bgcolor="#EEEEEE" align="left">NA</td>
 * </tr>
 * 
 * </table>
 * 
 * @author travis
 * 
 */
public class HttpLoggingFilter implements Filter {
    private static final Log LOG = LogFactory.getLog("http-operations");

    private String format = "%M %U %Q %C %I";
    private CosmoSecurityManager securityManager;

    private static final String BEAN_SECURITY_MANAGER =
        "securityManager";
    private static final String BEAN_HTTP_LOGGING_FORMAT =
        "httpLoggingFormat";

    public void destroy() {
        // Nothing to destroy
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        if (LOG.isInfoEnabled() && request instanceof HttpServletRequest){
            LOG.info(this.formatRequest((HttpServletRequest) request, this.format));
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

        String format = (String)
            wac.getBean(BEAN_HTTP_LOGGING_FORMAT, String.class);

        if (format != null){
            try {
                this.format = (String) format;
            } catch (ClassCastException e){
                throw new ServletException("Could not initialize HttpLoggingFilter: " +
                        "httpLoggingFormat is not a string.");
            }
        }
    }

    private String formatRequest(HttpServletRequest request, String format) {

        StringBuilder sb = new StringBuilder();
        char[] formatArray = format.toCharArray();
        for (int i = 0; i < formatArray.length; i++) {
            if (formatArray[i] != '%') {
                sb.append(formatArray[i]);

            } else {
                i++;

                switch (formatArray[i]) {

                case 'M':
                    sb.append(request.getMethod());
                    break;
                case 'C':
                    sb.append(request.getScheme());
                    break;
                case 'S':
                    sb.append(request.getServerName());
                    break;
                case 'P':
                    sb.append(request.getServerPort());
                    break;
                case 'U':
                    sb.append(request.getRequestURI());
                    break;
                case 'L':
                    sb.append(request.getContentLength());
                    break;
                case 'Q':
                    sb.append(request.getQueryString());
                    break;
                case 'I':
                    HttpSession s = request.getSession(false);
                    sb.append(s==null ? "No session": s.getId());
                    break;
                case 'A':
                    if (securityManager == null){
                        sb.append("\"No security manager\"");
                        break;
                    }
                    try {
                        CosmoSecurityContext securityContext =
                            securityManager.getSecurityContext();
                        Ticket ticket = securityContext.getTicket();

                        User user = securityContext.getUser();
                        if (ticket != null) {
                            sb.append(ticket);
                        } else if (user != null) {
                            sb.append(user.getUsername());
                        } else {
                            sb.append("\"No auth token\"");
                        }

                    } catch (CosmoSecurityException e){
                        sb.append("\"No security context\"");
                    }

                    break;
                case '%':
                    sb.append('%');
                    break;
                default:
                    sb.append('%' + formatArray[i]);
                break;
                }
            }
        }
        return new String(sb);

    }

}
