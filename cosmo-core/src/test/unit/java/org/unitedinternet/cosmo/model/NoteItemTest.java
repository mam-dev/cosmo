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
package org.unitedinternet.cosmo.model;

import java.util.Date;


import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;

/**
 * Test NoteItem
 */
public class NoteItemTest {
   
    /**
     * Tests note item etag.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNoteItemEtag() throws Exception {
        MockNoteItem master = new MockNoteItem();
        master.setUid("1");
        master.setModifiedDate(new Date());
        
        MockNoteItem mod = new MockNoteItem();
        mod.setUid("2");
        mod.setModifiedDate(new Date());
        mod.setModifies(master);
        
        String etag1 = master.calculateEntityTag();
        
        master.addModification(mod);
        
        String etag2 = master.calculateEntityTag();
        
        // etag should have changed when modification was added
        Assert.assertFalse(etag1.equals(etag2));
        
        mod.setModifiedDate(new Date(mod.getModifiedDate().getTime()+1));
        
        etag1 = etag2;
        etag2 = master.calculateEntityTag();
        
        // etag should have changed when modification was changed
        Assert.assertFalse(etag1.equals(etag2));
        
        // etag shouldn't change between calls
        Assert.assertTrue(etag2.equals(master.calculateEntityTag()));
        
        master.removeAllModifications();
        
        etag1 = etag2;
        etag2 = master.calculateEntityTag();
        
        // etag should have changed when modification was removed
        Assert.assertFalse(etag1.equals(etag2));
    }
}
