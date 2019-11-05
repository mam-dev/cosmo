package org.unitedinternet.cosmo.model;

import org.unitedinternet.cosmo.security.Permission;

import java.util.Set;

/**
 * Represents an unprotected access control entry supported by Cosmo
 */
public interface Ace {

    /**
     *
     * @return Associated item
     */
    public Item getItem();

    public enum Type {
        USER, //DAV:href with a partucular user
        AUTHENTICATED, // All authenticated users, DAV:authenticated
        // Other ACE types are not supported, as DAV:property DAV:owner can do it all (protected)
        // and other DAV:property types aint supported
    }


    public Type getType();

    public UserBase getUser(); // May be null if getType != USER

    public Set<Permission> getPermissions();

}
