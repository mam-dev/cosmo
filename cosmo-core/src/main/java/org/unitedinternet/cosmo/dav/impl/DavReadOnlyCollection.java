package org.unitedinternet.cosmo.dav.impl;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;

/**
 * This abstract class sets the default ACLs for collections not backed by any persistent items, i.e.
 * collections of user principals, group principals and other virtual collections.
 *
 * The access rights are set to as everyone could read it but no one can modify it.
 */
public abstract class DavReadOnlyCollection extends DavResourceBase implements DavCollection {

    private DavAcl acl;
    public DavReadOnlyCollection(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
        acl = makeAcl();
    }
    protected DavAcl getAcl() {
        return acl;
    }


    private DavAcl makeAcl() {
        DavAcl acl = new DavAcl();

        DavAce unauthenticated = new DavAce.UnauthenticatedAce();
        unauthenticated.setDenied(true);
        unauthenticated.getPrivileges().add(DavPrivilege.ALL);
        unauthenticated.setProtected(true);
        acl.getAces().add(unauthenticated);

        DavAce allAllow = new DavAce.AllAce();
        allAllow.getPrivileges().add(DavPrivilege.READ);
        allAllow.getPrivileges().add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
        allAllow.setProtected(true);
        acl.getAces().add(allAllow);

        DavAce allDeny = new DavAce.AllAce();
        allDeny.setDenied(true);
        allDeny.getPrivileges().add(DavPrivilege.ALL);
        allDeny.setProtected(true);
        acl.getAces().add(allDeny);

        return acl;
    }



}
