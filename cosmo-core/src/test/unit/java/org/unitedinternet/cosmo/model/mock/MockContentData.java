/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.mock;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.unitedinternet.cosmo.util.BufferedContent;



/**
 * Represents the data of a piece of Content. Data is stored
 * as a BufferedContent, either in memory (small content) or
 * on disk (large content).
 */
public class MockContentData {

    
    private BufferedContent content = null;
   
    /**
     * toString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }


    /**
     * Get an InputStream to the content data.  Repeated
     * calls to this method will return new instances
     * of InputStream.
     * @return InputStream
     */
    public InputStream getContentInputStream() {
        if (content == null) {
            return null;
        }
        
        return content.getInputStream();
    }
    
    /**
     * Set the content using an InputSteam.  Does not close the 
     * InputStream.
     * @param is content data
     * @throws IOException - if something is wrong this exception is thrown.
     */
    public void setContentInputStream(InputStream is) throws IOException {
        content = new BufferedContent(is);
    }
    
    /**
     * @return the size of the data read, or -1 for no data present
     */
    public long getSize() {
        if (content != null) {
            return content.getLength();
        }
        else {
            return -1;
        }
    }

}
