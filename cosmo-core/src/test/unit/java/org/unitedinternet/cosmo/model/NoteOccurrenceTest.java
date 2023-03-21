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

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.util.NoteOccurrenceUtil;

/**
 * Test NoteOccurrenceItem
 */
public class NoteOccurrenceTest {
   
    private EntityFactory factory = new MockEntityFactory();
    
    /**
     * Tests generate note occurrence.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGenerateNoteOccurrence() throws Exception {
        
        MockNoteItem note = (MockNoteItem) factory.createNote();
        note.setUid("1");
        note.setCreationDate(new Date());
        note.setModifiedDate(new Date());
        note.setDisplayName("dn");
        note.setBody("body");
        note.addStamp(factory.createEventStamp(note));
        
        NoteOccurrence no = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070101"), note);
        NoteOccurrence no2 = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070102"), note);
        
        
        assertEquals("1:20070101", no.getUid());
        assertEquals(note, no.getMasterNote());
        assertNotNull(no.getOccurrenceDate());
        
        assertEquals(note.getCreationDate(), no.getCreationDate());
        assertEquals("dn", no.getDisplayName());
        assertEquals("body", no.getBody());
        
        assertEquals(1, no.getStamps().size());
        
        assertFalse(no.equals(no2));
        assertTrue(no.hashCode() != no2.hashCode());
        
        try {
            no.setUid("blah");
            fail("able to perform unsupported op");
        } catch (UnsupportedOperationException e) {
            
        }
    }    
    
}
