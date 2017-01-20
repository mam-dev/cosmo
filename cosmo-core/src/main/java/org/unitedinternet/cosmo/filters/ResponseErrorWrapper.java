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

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.util.BufferedServletOutputStream;

/**
 * HttpServletResponseWrapper that catches INTERNAL_SERVER_ERROR
 * responses (status code 500) and allows the error to be flushed
 * or cleared.  This allows an error response to be voided, which
 * is useful for servlet filters that may want to retry the request
 * before returning an error to the client.
 */
public class ResponseErrorWrapper extends HttpServletResponseWrapper {
    
    private String errorMsg = null;
    private BufferedServletOutputStream bufferedOutput = null;
    private boolean hasError = false;
    
    public ResponseErrorWrapper(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public void sendError(int code, String msg) throws IOException {
        // Trap 500's
        if(code==SC_INTERNAL_SERVER_ERROR) {
            hasError = true;
            errorMsg = msg;
        } else {
            super.sendError(code, msg);
        }
    }

    @Override
    public void sendError(int code) throws IOException {
        // Trap 500's
        if(code==SC_INTERNAL_SERVER_ERROR) {
            hasError = true;
        } else {
            super.sendError(code);
        }
    }
    
    /**
     * If a 500 error was trapped, then flush it.  This can involve invoking
     * sendError() or setStatus() along with writing any data that
     * was buffered.
     * @returns true if an error was flushed, otherwise false
     * @throws IOException
     */
    public boolean flushError() throws IOException {
        if(hasError) {
            if(bufferedOutput!=null && !bufferedOutput.isEmpty()) {
                super.setStatus(SC_INTERNAL_SERVER_ERROR);
                IOUtils.copy(bufferedOutput.getBufferInputStream(), super.getOutputStream()); 
            }
            else if (errorMsg!=null) {
                super.sendError(SC_INTERNAL_SERVER_ERROR, errorMsg);
            }
            else {
                super.sendError(SC_INTERNAL_SERVER_ERROR);
            }
            
            clearError();
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear error, voiding any 500 response sent.
     */
    public void clearError() {
        bufferedOutput = null;
        errorMsg = null;
        hasError = false;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        
        // If an error was trapped, then return our custom outputstream
        // so that we can buffer any data sent and be able to send
        // it later on.
        if(hasError) {
            if(bufferedOutput==null) {
                bufferedOutput = new BufferedServletOutputStream();
            }
            
            return bufferedOutput;
        }
        
        return super.getOutputStream();
    }

    @Override
    public void setStatus(int sc, String sm) {
        // trap 500 server errors
        if(sc==SC_INTERNAL_SERVER_ERROR) {
            hasError = true;
            errorMsg = sm;
        } else {
            super.setStatus(sc, sm);
        }
    }

    @Override
    public void setStatus(int sc) {
        // trap 500 server errors
        if(sc== SC_INTERNAL_SERVER_ERROR) {
            hasError = true;
        } else {
            super.setStatus(sc);
        }
    }
    
    /**
     * @return buffered outputstream, only has a value if
     *         an error has been trapped and getOutputStream()
     *         was called, meaning the request handler was
     *         trying to send error data.
     */
    public BufferedServletOutputStream getBufferedOutputStream() {
        return bufferedOutput;
    }
}
