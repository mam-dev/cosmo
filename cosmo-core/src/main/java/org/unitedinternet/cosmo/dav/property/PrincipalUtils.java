package org.unitedinternet.cosmo.dav.property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.acl.NotRecognizedPrincipalException;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserBase;

public  class PrincipalUtils implements ExtendedDavConstants {
    private static final Log LOG = LogFactory.getLog(PrincipalUtils.class);
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

    public static boolean matchUser(User user, UserBase toMatch) {
        return user.equals(toMatch) || (
                toMatch instanceof Group && user.isMemberOf((Group) toMatch));

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

    public static DavUserPrincipal findUserPrincipalResource(String uri, DavResourceLocator currentLocator, DavResourceFactory resourceFactory) throws NotRecognizedPrincipalException {
        try {
            DavResourceLocator locator = currentLocator.getFactory().
                createResourceLocatorByUri(currentLocator.getContext(),
                                       uri);
            return (DavUserPrincipal) resourceFactory.resolve(locator);
        } catch (ClassCastException e) {
            throw new NotRecognizedPrincipalException("uri " + uri + " does not represent a principal");
        } catch (CosmoDavException e) {
            throw new NotRecognizedPrincipalException("uri " + uri + " does not represent a principal:" + e.getMessage());
        }
    }

    public static DavUserPrincipal createUserPrincipalResource(UserBase user, DavResourceLocator currentLocator, DavResourceFactory factory)  {
        try {
            DavResourceLocator locator = currentLocator.getFactory().createPrincipalLocator(currentLocator.getContext(), user);
            return (DavUserPrincipal) factory.createUserPrincipalResource(locator, user);
        } catch (CosmoDavException e) {
            LOG.error("Shouldn't throw this Exception there: " + e + "; User: " + user);
            throw new CosmoException();
        }
    }
}
