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
package org.unitedinternet.cosmo.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A filter used to implement various hacks required by broken clients.
 * 
 * Alternative solutions should be carefully considered before new code is added
 * here. Ideally bugs should be filed against the appropriate clients.
 * 
 * @author travis
 *
 */
public class ClientBugAccommodationFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(ClientBugAccommodationFilter.class);

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest &&
            ((HttpServletRequest)request).getHeader("User-Agent") != null &&
            ((HttpServletRequest)request).getHeader("User-Agent").contains("MSIE")&&
            response instanceof HttpServletResponse){
            final HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletResponse wrappedResponse = httpResponse;
            
            LOG.info("Modifying response to accommodate Internet Explorer.");
            wrappedResponse = new XMLContentTypeWrapper(wrappedResponse);
            wrappedResponse = new ReplaceTextWrapper(wrappedResponse, 
                    "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" ", "");
            chain.doFilter(request, wrappedResponse);
            wrappedResponse.getOutputStream().close();  
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * This wrapper solves two Internet Explorer compatibility problems:
     * 1) IE doesn't automatically parse XML unless the content type is 
     *    text/xml. This means XML parsing must be done manually by
     *    instantiating an ActiveX object which is unfortunately impossible
     *    in environments with certain security settings. This wrapper
     *    detects the +xml suffix (as introduced in RFC 3023) and replaces
     *    the content type with text/xml.
     * 2) IE fatally hangs when attempting to parse a Content-Type header
     *    containing the type=entry; parameter. This filter removes this
     *    parameter to avoid this crash.
     *    
     * @author travis
     *
     */
    static class XMLContentTypeWrapper extends HttpServletResponseWrapper {
        public XMLContentTypeWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setContentType(String type) {
            type = filterForIE(type);
            super.setContentType(type);
        }
        
        @Override
        public void setHeader(String name, String value) {
            if (name.equals("Content-Type")){
                value = filterForIE(value);
            }
            //Including unvalidated data in an HTTP response header can enable 
    		//cache-poisoning, cross-site scripting, cross-user defacement, page hijacking, 
    		//cookie manipulation or open redirect.
            value = StringEscapeUtils.escapeJavaScript(value);
            value = StringEscapeUtils.escapeHtml(value);
            super.setHeader(name, value);
        }
        
        // Internet Explorer doesn't do XML parsing for content types other than text/xml.
        private String filterForIE(String value){
            return value.replaceFirst(".*\\+xml; ?(?:type=entry;)?", "text/xml;");
        }
    }

    /**
     * This wrapper replaces all instances of <code>findString</code> with
     * <code>replaceString</code>. In this filter we use this capability to
     * remove the default XML namespace declaration because Internet Expolorer
     * refuses to parse XML containing this declaration.
     * 
     * This code was written using the following article as a reference:
     * 
     * http://www.ibm.com/developerworks/java/library/j-tomcat/
     * 
     * @author travis
     *
     */
    static class ReplaceTextWrapper extends HttpServletResponseWrapper{
        ReplaceTextStream stream;
        PrintWriter printWriter;
        public ReplaceTextWrapper(HttpServletResponse response, 
                String findString,
                String replaceString) throws IOException{
            super(response);
            stream = new ReplaceTextStream(response.getOutputStream(), 
                    findString, replaceString);
            printWriter = new PrintWriter(stream);

        }
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return stream;
        }
        @Override
        public PrintWriter getWriter() throws IOException {
            return printWriter;
        }
    }

    static class ReplaceTextStream extends ServletOutputStream {
        private String findString;
        private String replaceString;
        private OutputStream originalStream;
        private ByteArrayOutputStream tempStream;
        private boolean closed;
        
        public ReplaceTextStream(OutputStream originalStream,
                String findString, String replaceString) {
            this.originalStream = originalStream;
            tempStream = new ByteArrayOutputStream();
            this.findString = findString;
            this.replaceString = replaceString;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                processStream();
                originalStream.close();
                closed = true;
            } 
        }

        @Override
        public void flush() throws IOException {
            if (tempStream.size() != 0 && ! closed) {
                processStream();
                tempStream = new ByteArrayOutputStream();
            }
        }

        @Override
        public void write(int i) throws IOException {
            tempStream.write(i);
        }

        public void processStream() throws IOException{
            originalStream.write(replaceContent(tempStream.toByteArray()));
        }

        private byte[] replaceContent(byte[] bs) throws UnsupportedEncodingException {
            return new String(bs, "UTF-8").replaceAll(findString, replaceString).getBytes("UTF-8");
        }

        @Override
        public boolean isReady() {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new RuntimeException("Not yet implemented");
            
        }
    }
    
    public void init(FilterConfig arg0) throws ServletException {

    }

}
