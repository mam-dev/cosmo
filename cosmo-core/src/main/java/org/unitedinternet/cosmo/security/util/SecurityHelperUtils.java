package org.unitedinternet.cosmo.security.util;

import org.unitedinternet.cosmo.dav.acl.AcePrincipal;
import org.unitedinternet.cosmo.dav.acl.AcePrincipalType;
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

    /**
     *
     * @param who
     * @param what
     * @param privilege
     * @return
     */
    public static boolean canAccess(User who, Item what, DavPrivilege privilege) {
        /**
         * This code represents proctected ACEs present in DavItemResourceBase.makeAcl
         */
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
        /**
         * End code representing protected ACEs
         */

        /* TODO read ACLs from item itself! */

        return false;
    }

    public static boolean matchPrincipal (UserBase user, Item what,  AcePrincipal principal) {
        if (principal.getType().equals(AcePrincipalType.ALL)) {
            return true;
        }
        if (principal.getType().equals(AcePrincipalType.AUTHENTICATED)) {
            return true;
        }
        if (principal.getType().equals(AcePrincipalType.SELF)) {

        }
    }

}
