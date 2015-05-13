/*
 * Copyright 2008 Open Source Applications Foundation
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
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * ServletOutputStream implementation that buffers all data written to
 * the outputstream.  This allows the data to be captured without
 * being sent to the client.
 */
public class BufferedServletOutputStream extends ServletOutputStream {

    // buffer to memory for now
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
    }
    
    /**
     * @return inputstream to buffered data
     */
    public InputStream getBufferInputStream() {
        return new ByteArrayInputStream(buffer.toByteArray());
    }
    
    /**
     * @return true if buffered data is empty, otherwise false
     */
    public boolean isEmpty() {
        return buffer.size()==0;
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
