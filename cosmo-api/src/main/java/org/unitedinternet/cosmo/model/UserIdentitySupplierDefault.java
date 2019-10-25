package org.unitedinternet.cosmo.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Default implementation that assumes user has only one email address.
 * 
 * @author daniel grigore
 *
 */
@Component
public class UserIdentitySupplierDefault implements UserIdentitySupplier {

    public UserIdentitySupplierDefault() {

    }

    @Override
    public UserIdentity forUser(User user) {
        return new DefaultUserIdentity(user);
    }

    @Override
    public UserIdentity forGroup(Group group) {
        return new DefaultGroupIdentity(group);
    }


}
