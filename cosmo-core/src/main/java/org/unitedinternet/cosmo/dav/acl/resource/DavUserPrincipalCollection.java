package org.unitedinternet.cosmo.dav.acl.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.model.UserIterator;

public class DavUserPrincipalCollection extends DavAbstractPrincipalCollection {

    //private static final Log LOG = LogFactory.getLog(DavUserPrincipalCollection.class);

    public DavUserPrincipalCollection(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
    }

    @Override
    public String getDisplayName() {
        return "User Principals";
    }

    @Override
    public DavResourceIterator getMembers() {

        UserIterator it = getResourceFactory().getUserService().users();
        return new DavResourceIterator() {
            @Override
            public DavResource nextResource()  {
                    return PrincipalUtils.createUserPrincipalResource(it.next(), getResourceLocator(), getResourceFactory());
            }

            @Override
            public int size() {
                return 0; // Implement it if we'll use it.
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public DavResource next() {
                return nextResource();
            }
        };
    }

}
