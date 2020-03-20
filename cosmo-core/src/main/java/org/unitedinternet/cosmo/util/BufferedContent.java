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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffers content into memory or a file depending on the size
 * of the content.  Smaller sizes will be buffered into memory
 * and larger sizes will be written to a temporary file.  The size
 * buffered into memory is configurable.
 */
public class BufferedContent {
   
    private static final Logger LOG = LoggerFactory.getLogger(BufferedContent.class);
    
    // default to 256K memory buffer
    public static final int DEFAULT_MEM_BUFFER_SIZE = 1024*256;
    
    private int maxMemoryBuffer = DEFAULT_MEM_BUFFER_SIZE;
    private static final int BUFFER_SIZE = 4096;
    
    private File file = null;
    private byte[] buffer = null;
    
    
    /**
     * @param is InputStream to buffer.  The data will be buffered into memory
     *        if it fits under the default MAX_MEMORY_BUFFER size, otherwise
     *        it will be buffered to a temporary file.
     * @throws IOException
     */
    public BufferedContent(InputStream is) throws IOException {
        createBuffer(is);
    }
    
    /**
     * @param is InputStream to buffer
     * @param maxMemoryBuffer Maximum size of stream to buffer into memory.
     *                        InputStreams that exceed this will be buffered
     *                        into a temp file.
     * @throws IOException
     */
    public BufferedContent(InputStream is, int maxMemoryBuffer) throws IOException {
        this.maxMemoryBuffer = maxMemoryBuffer;
        createBuffer(is);
    }
    
    /**
     * @return Inputstream to content.  Each call returns a new InputStream
     *         instance.
     */
    public InputStream getInputStream() {
        if(file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("unable to open temporary file");
            }
        }
        else {
            return new ByteArrayInputStream(buffer);
        }
    }
    
    /**
     * @return length of buffered content
     */
    public long getLength() {
        if(file != null) {
            return file.length();
        }
        else {
            return buffer.length;
        }
    }
    
    private void createBuffer(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int read = is.read(buf);
        while(read > 0) {
            bos.write(buf, 0, read);
            // If the request size is bigger than maxMemoryBuffer, then
            // buffer to file instead so we don't run out of memory
            if(bos.size()>maxMemoryBuffer) {
                createFileBuffer(bos.toByteArray(), is);
                return;
            }
            read = is.read(buf);
        }
        buffer = bos.toByteArray();
    }
    
    private void createFileBuffer(byte[] start, InputStream is) throws IOException{
        file = File.createTempFile("cosmo", "tmp"+ System.currentTimeMillis());
        FileOutputStream fos = new FileOutputStream(file);
        ByteArrayInputStream bis = new ByteArrayInputStream(start);
        IOUtils.copy(bis, fos);
        IOUtils.copy(is, fos);
        fos.close();
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            if(file!=null) {
                file.delete();
            }
        } catch(Exception e) {
            LOG.error("error deleting temp file: {}", file.getAbsolutePath(), e);
        }
    }
}