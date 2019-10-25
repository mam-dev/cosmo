package org.unitedinternet.cosmo.dav.property;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserBase;

public  class PrincipalUtils implements ExtendedDavConstants {
    /**
     * Returns full path to the user OR group URL. Used by properties DAV:owner and DAV:principal-url
     * @param locator
     * @param user
     * @return
     */
    public static String href(DavResourceLocator locator,
                              UserBase user) {
        if (user instanceof User) {
            return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(),
                    user.getUsername());
        } else if (user instanceof Group) {
            return TEMPLATE_GROUP.bindAbsolute(locator.getBaseHref(),
                    user.getUsername());
        } else {
            throw new CosmoException();
        }
    }

    /**
     * Returns relative  path of user/group
     * @param user
     * @return
     */
    public static String relativePath(UserBase user) {
        if (user instanceof User) {
            return TEMPLATE_USER.bind(user.getUsername());
        } else if (user instanceof Group) {
            return TEMPLATE_GROUP.bind(user.getUsername());
        } else {
            throw new CosmoException();
        }
    }
}
