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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.unitedinternet.cosmo.util.BufferedServletInputStream;

/**
 * HttpServletRequestWrapper that allows a BufferedServletInputStream to be
 * used as part of a given HttpServletRequest.  This delegates all methods
 * to a given HttpServletRequest except for "getInputStream", which
 * returns the instance of BufferedServletInputStream.
 */
public class BufferedRequestWrapper extends HttpServletRequestWrapper {
    
    private BufferedServletInputStream inputStream = null;
    private boolean retryRequest = false;
    
    public BufferedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        inputStream = new BufferedServletInputStream(request.getInputStream());
    }
    
    /**
     * @param request servlet request
     * @param maxMemoryBuffer Maximum size of request that will be 
     *                        buffered in memory.  Larger requests
     *                        will be buffered to disk.
     * @throws IOException
     */
    public BufferedRequestWrapper(HttpServletRequest request,
            int maxMemoryBuffer) throws IOException {
        super(request);
        inputStream = new BufferedServletInputStream(request.getInputStream(),
                maxMemoryBuffer);
    }

    /**
     * When called, next call to getInputStream() will reset the buffered
     * inputstream, allowing the request to be re-processed
     */
    public void retryRequest() {
        retryRequest = true;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if(retryRequest==true) {
            retryRequest = false;
            inputStream.resetToBeginning();
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader()
        throws IOException {
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        InputStreamReader in = 
            new InputStreamReader(getInputStream(), encoding);
        return new BufferedReader(in);
    }
}
