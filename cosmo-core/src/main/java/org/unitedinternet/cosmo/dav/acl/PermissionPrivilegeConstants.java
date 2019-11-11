package org.unitedinternet.cosmo.dav.acl;

import org.unitedinternet.cosmo.security.Permission;

import java.util.HashMap;
import java.util.Map;

public class PermissionPrivilegeConstants {
    public static Map<Permission, DavPrivilege> PERMISSION_TO_PRIVILEGE = new HashMap<>();
    public static Map<DavPrivilege, Permission> PRIVILEGE_TO_PERMISSION = new HashMap<>();
    static {
        PERMISSION_TO_PRIVILEGE.put(Permission.READ, DavPrivilege.READ);
        PERMISSION_TO_PRIVILEGE.put(Permission.WRITE, DavPrivilege.WRITE);
        PERMISSION_TO_PRIVILEGE.put(Permission.FREEBUSY, DavPrivilege.READ_FREE_BUSY);
        for (Permission key :PERMISSION_TO_PRIVILEGE.keySet()) { // Reverse map
            PRIVILEGE_TO_PERMISSION.put(PERMISSION_TO_PRIVILEGE.get(key), key);
        }
    }
}
