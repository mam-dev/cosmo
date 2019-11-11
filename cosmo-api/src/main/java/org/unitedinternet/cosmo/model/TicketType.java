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

import org.unitedinternet.cosmo.security.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the security classification of a ticket. A ticket is
 * typed according to its privileges. There are three predefined
 * ticket types: read-only, read-write and free-busy.
 */
public class TicketType {



    public static final String ID_READ_ONLY = "read-only";
    public static final TicketType READ_ONLY = new TicketType(ID_READ_ONLY, new Permission[] {Permission.READ});
    public static final String ID_READ_WRITE = "read-write";
    public static final TicketType READ_WRITE = new TicketType(
            ID_READ_WRITE,
            new Permission[] { Permission.READ, Permission.WRITE, Permission.FREEBUSY });
    public static final String ID_FREE_BUSY = "free-busy";
    public static final TicketType FREE_BUSY = new TicketType(ID_FREE_BUSY,
            new Permission[] { Permission.FREEBUSY });

    private String id;
    private Set<Permission> permissions;

    public TicketType(String id) {
        this.id = id;
        this.permissions = new HashSet<>();
    }

    public TicketType(String id, Permission[] permissions) {
        this(id);
        this.permissions.addAll(Arrays.asList(permissions));
    }

    public String getId() {
        return id;
    }

    public Set<Permission> getPermissions() {
        return permissions;
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
