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

/**
 * An exception that indicates that the icalendar uid chosen for an NoteItem
 * already in use by another item.
 */
@SuppressWarnings("serial")
public class IcalUidInUseException extends RuntimeException {
    
    public String testUid = null;
    public String existingUid = null;
    
    /** */
    public IcalUidInUseException(String message) {
        super(message);
    }
    
    /** */
    public IcalUidInUseException(String message, String testUid,
            String existingUid) {
        super(message);
        this.testUid = testUid;
        this.existingUid = existingUid;
    }

    /** */
    public IcalUidInUseException(String message,
                             Throwable cause) {
        super(message, cause);
    }

    /**
     * The uid of the item tested
     */
    public String getTestUid() {
        return testUid;
    }

    /**
     * The uid of the existing item with the duplicate icalUid.
     */
    public String getExistingUid() {
        return existingUid;
    }
}
