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
package org.unitedinternet.cosmo.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.unitedinternet.cosmo.util.BufferedContent;

/**
 * Test BufferedContent
 */
public class BufferedContentTest {
   
    /**
     * Tests buffered content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBufferedContent() throws Exception {
        Random random = new Random();
        
        // 100K test
        byte[] bytes = new byte[1024*100];
        random.nextBytes(bytes);
        
        BufferedContent content = new BufferedContent(new ByteArrayInputStream(bytes));
        
        assertTrue(content.getLength()==(1024*100));
        
        // verify streams are the same
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(bytes), content.getInputStream()));
        // verify we can re-consume the same stream
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(bytes), content.getInputStream()));
        
        // should fit into memory
        assertTrue(content.getInputStream() instanceof ByteArrayInputStream);
        
        // should be buffered into file
        content = new BufferedContent(new ByteArrayInputStream(bytes), 1024*50);
        assertTrue(content.getLength()==(1024*100));
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(bytes), content.getInputStream()));
        assertTrue(IOUtils.contentEquals(new ByteArrayInputStream(bytes), content.getInputStream()));
        
        // should be in a file
        assertTrue(content.getInputStream() instanceof FileInputStream);
    }
}
