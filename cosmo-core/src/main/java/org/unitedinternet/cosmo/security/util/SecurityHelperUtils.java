package org.unitedinternet.cosmo.security.util;

import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.model.*;

public class SecurityHelperUtils {
    public static boolean canAccessPrincipal(User who, UserBase what) {
        if (who.getAdmin()) {
            return true;
        }


        if (what instanceof Group) {
            if (who.isMemberOf((Group) what)) {
                return true;
            }
        } else {
            if (who.equals(what)) {
                return true;
            }
        }

        return false;
    }

    public static boolean canAccess(User who, Item what, DavPrivilege privilege) {
        if (canAccessPrincipal(who, what.getOwner())) {
            return true;
        }
        if (privilege.equals(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET))
            return true;
        for (CollectionItem parent : what.getParents()) {
            if (canAccessPrincipal(who, parent.getOwner())) {
                return true;
            }
        }
        /* TODO read ACLs from item itself! */

        return false;
    }

}
