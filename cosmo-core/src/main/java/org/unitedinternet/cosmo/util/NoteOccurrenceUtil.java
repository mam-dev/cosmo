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

import java.lang.reflect.Proxy;

import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;

import net.fortuna.ical4j.model.Date;

/**
 * Contains static NoteOccurrence support methods.
 */
public class NoteOccurrenceUtil {
    private static final Class<?>[] NOTE_OCCURRENCE_CLASS = new Class[] { NoteOccurrence.class };

    /**
     * Generate a NoteOccurrence for a given recurrence date and
     * master NoteItem.
     * @param recurrenceId
     * @param masterNote
     * @return NoteOccurrence instance
     */
    public static NoteOccurrence createNoteOccurrence(Date recurrenceId,
            NoteItem masterNote) {
        NoteOccurrenceInvocationHandler handler = new NoteOccurrenceInvocationHandler(
                recurrenceId, masterNote);

        return (NoteOccurrence) Proxy.newProxyInstance(masterNote.getClass()
                .getClassLoader(), NOTE_OCCURRENCE_CLASS, handler);
    }
}
