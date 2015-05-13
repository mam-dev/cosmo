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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.unitedinternet.cosmo.model.ModificationUid;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.NoteOccurrence;
import org.unitedinternet.cosmo.model.hibernate.ModificationUidImpl;

import net.fortuna.ical4j.model.Date;


/**
 * <p>
 * InvocationHandler for supporting NoteOccurrences.
 * A NoteOccurrence wraps a master NoteItem, adding an
 * occurrence date.  The uuid of a NoteOccurrence is a
 * combination of the  master note's uuid and the 
 * occurrence date.  All other properties of a 
 * note occurrence should be inherited from the master
 * note.
 * </p>
 *
 * <p>
 * By using an InvocationHandler, a NoteOccurrence can
 * be implemented by using a dynamic proxy that overrides
 * certain methods and delgates the rest to a master
 * NoteItem.
 * </p>
 */
public class NoteOccurrenceInvocationHandler implements InvocationHandler {
    
    private Date occurrenceDate = null;
    private NoteItem masterNote = null;
    private ModificationUid modUid = null;
    private static final Set<NoteItem> EMPTY_MODS = Collections
            .unmodifiableSet(new HashSet<NoteItem>(0));
    
    public NoteOccurrenceInvocationHandler(Date occurrenceDate,
                                           NoteItem masterNote) {
        // uid is the same as a modification's uid
        this.modUid = new ModificationUidImpl(masterNote, occurrenceDate);
        this.occurrenceDate = occurrenceDate;
        this.masterNote = masterNote;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        String name = method.getName();
        
        // occurrences are read-only
        if(name.startsWith("set")) {
            throw new UnsupportedOperationException("unsupported op: " + name);
        }
        
        // override "getUid" to return the modUid
        if(name.equals("getUid")) {
            return modUid.toString();
        }
        // support getMasterNote()
        else if(name.equals("getMasterNote")) {
            return masterNote;
        }
        // support getModificationUuid()
        else if(name.equals("getModificationUid")) {
            return modUid;
        }
        // support getOccurrenceDate()
        else if(name.equals("getOccurrenceDate")) {
            return occurrenceDate;
        }
        // no modifications
        else if(name.equals("getModifications")) {
            return EMPTY_MODS;
        }
        // equals() and hashCode() are based on modUid, so override
        else if(name.equals("equals")) {
            return equalsOverride(args[0]);
        }
        else if(name.equals("hashCode")) {
            return hashCodeOverride();
        }
        // otherwise delegate to masterNote
        else {
            return method.invoke(masterNote, args);
        }
    }

    
    public boolean equalsOverride(Object obj) {
        if(obj==null) {
            return false;
        }
        if( ! (obj instanceof NoteOccurrence)) {
            return false;
        }
        
        NoteOccurrence no = (NoteOccurrence) obj;
        
        return no.getUid().equals(modUid.toString());
    }

    public int hashCodeOverride() {
        return modUid.toString().hashCode();
    }
    
}
