package org.unitedinternet.cosmo.security.util;



import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.HrefProperty;

import org.unitedinternet.cosmo.dav.StandardResourceLocatorFactory;

import org.unitedinternet.cosmo.dav.acl.AcePrincipal;
import org.unitedinternet.cosmo.dav.acl.AcePrincipalType;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.security.Permission;

public class SecurityHelperUtils {

    private enum Decision {
        GRANT, DENY, ABSTAIN
    }
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
     * @param perm permission
     * @return
     */

    public static boolean canAccess(User who, Item what, Permission perm) {
        /**
         * This code represents proctected ACEs present in DavItemResourceBase.makeAcl
         */

        if (canAccessPrincipal(who, what.getOwner())) {
            return true;
        }
        for (CollectionItem parent : what.getParents()) {
            if (canAccessPrincipal(who, parent.getOwner())) {
                return true;
            }
        }
        /**
         * End code representing protected ACEs
         */

        Decision decision = canAccessUnprotected(who, what, perm);
        return decision == Decision.GRANT;
    }

    /**
     * Decides whether a user can access an item. The decision is as follows:
     *
     * If permission is granted on parent collection, it is granted if not denied in child collection.
     *
     *
     * @param what
     * @param perm
     * @return
     */
    public static Decision canAccessUnprotected(User who, Item what, Permission perm) {
        for (Ace ace : what.getAces()) {
            if (ace.getType().equals(Ace.Type.AUTHENTICATED) ||
                    (ace.getType().equals(Ace.Type.USER) && (ace.getUser().equals(who) ||
                            (ace.getUser() instanceof Group && who.isMemberOf((Group) ace.getUser()))))) {
                if (ace.getPermissions().contains(perm))
                    return ace.isDeny() ? Decision.DENY : Decision.GRANT;
            }
        }
        // Check all parents as well
        for (CollectionItem parent : what.getAllParents()) {
            Decision decision = canAccessUnprotected(who, parent, perm);
                if (decision != Decision.ABSTAIN)
                    return decision;
            }

        return Decision.ABSTAIN;
    }

}
