package org.unitedinternet.cosmo.dav.acl.resource;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.model.GroupIterator;

public class DavGroupPrincipalCollection extends DavAbstractPrincipalCollection {
    public DavGroupPrincipalCollection(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
    }

    @Override
    public String getDisplayName() {
        return "Group Principals";
    }

    @Override
    public DavResourceIterator getMembers() {
        GroupIterator it = getResourceFactory().getUserService().groups();
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
