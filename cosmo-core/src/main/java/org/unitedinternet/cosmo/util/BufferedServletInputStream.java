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
package org.unitedinternet.cosmo.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;


/**
 * ServletInputStream implementation that relies on an
 * in-memory buffer or file (for larger requests) to stream content.
 * Useful when needing to consume the servlet input stream multiple
 * times.
 */
public class BufferedServletInputStream extends ServletInputStream {

    private InputStream is = null;
    private BufferedContent content = null;
    
    public BufferedServletInputStream(InputStream is) throws IOException {
       createBuffer(is);
    }
    
    /**
     * @param is InputStream to buffer
     * @param maxMemoryBuffer Maximum size of stream to buffer into memory.
     *                        InputStreams that exceed this will be buffered
     *                        into a temp file.
     * @throws IOException
     */
    public BufferedServletInputStream(InputStream is, int maxMemoryBuffer) throws IOException {
        createBuffer(is, maxMemoryBuffer);
     }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        is.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }
    
    /**
     * Reset the input stream, allowing it to be re-processed.
     * @throws IOException
     */
    public synchronized void resetToBeginning() throws IOException {
        is.close();
        is = content.getInputStream();
    }
    
    /**
     * @return length of buffered content
     */
    public long getLength() {
        return content.getLength();
    }
    
    private void createBuffer(InputStream is) throws IOException {
        content = new BufferedContent(is);
        this.is = content.getInputStream();
    }
    
    private void createBuffer(InputStream is, int maxMemoryBuffer) throws IOException {
        content = new BufferedContent(is, maxMemoryBuffer);
        this.is = content.getInputStream();
    }

    @Override
    public boolean isFinished() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean isReady() {
        throw new RuntimeException("Not yet implemented");
    }

   @Override
    public void setReadListener(ReadListener readListener) {
        throw new RuntimeException("Not yet implemented");
    }
    
}
