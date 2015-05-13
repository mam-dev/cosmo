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

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the security classification of a ticket. A ticket is
 * typed according to its privileges. There are three predefined
 * ticket types: read-only, read-write and free-busy.
 */
public class TicketType {

    /** */
    public static final String PRIVILEGE_READ = "read";
    /** */
    public static final String PRIVILEGE_WRITE = "write";
    /** */
    public static final String PRIVILEGE_FREEBUSY = "freebusy";

    public static final String ID_READ_ONLY = "read-only";
    public static final TicketType READ_ONLY = new TicketType(ID_READ_ONLY,
            new String[] { PRIVILEGE_READ, PRIVILEGE_FREEBUSY });
    public static final String ID_READ_WRITE = "read-write";
    public static final TicketType READ_WRITE = new TicketType(
            ID_READ_WRITE,
            new String[] { PRIVILEGE_READ, PRIVILEGE_WRITE, PRIVILEGE_FREEBUSY });
    public static final String ID_FREE_BUSY = "free-busy";
    public static final TicketType FREE_BUSY = new TicketType(ID_FREE_BUSY,
            new String[] { PRIVILEGE_FREEBUSY });

    private String id;
    private Set<String> privileges;

    public TicketType(String id) {
        this.id = id;
        this.privileges = new HashSet<String>();
    }

    public TicketType(String id, String[] privileges) {
        this(id);
        for (String p : privileges) {
            this.privileges.add(p);
        }
    }

    public String getId() {
        return id;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }

    public String toString() {
        return id;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TicketType)) {
            return false;
        }
        return id.equals(((TicketType) o).id);
    }
    
    public int hashCode() {
        return 1;
    }

    public static TicketType createInstance(String id) {
        if (id.equals(ID_READ_ONLY)) {
            return READ_ONLY;
        }
        if (id.equals(ID_READ_WRITE)) {
            return READ_WRITE;
        }
        if (id.equals(ID_FREE_BUSY)) {
            return FREE_BUSY;
        }
        return new TicketType(id);
    }

    public static boolean isKnownType(String id) {
        return (id.equals(ID_READ_ONLY) || id.equals(ID_READ_WRITE) || id
                .equals(ID_FREE_BUSY));
    }

}
