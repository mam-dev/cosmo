package org.unitedinternet.cosmo.model;

import org.unitedinternet.cosmo.CosmoException;

/**
 * Defines a contract for supplying identification data for a user.
 * 
 * Since there may be cases when a user data hold in a separate system may change
 * and those changes must reflect into user properties like <code>displayname</code>
 * or <code>calendar-user-address-set</code> there is the need to provide fresh,
 * non-stale user identity.  
 * Implementations are responsible for providing actual identity data for a specified user.
 * They are looked-up by the component container from a Spring context of simply instantiated if 
 * they are annotated with <code>@Supplier</code>
 * 
 * @author corneliu dobrota
 *
 */


public interface UserIdentitySupplier {
	UserIdentity forUser(User user);
	UserIdentity forGroup (Group group);

	default UserIdentity forUserOrGroup (UserBase user) {
		if (user instanceof  User)
		{
			return forUser((User)user);
		} else if (user instanceof Group) {
			return forGroup((Group)user);
		} else {
			throw new CosmoException();
		}
	}


}
