package org.unitedinternet.cosmo.model;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulates user identification data which may be found in another system than the Calendar Server.
 * 
 * @see UserIdentitySupplier
 * @author corneliu dobrota
 *
 */
public class DefaultUserIdentity implements UserIdentity {

	private User user;
	
	public DefaultUserIdentity(User user){
		this.user = user;
	}

	@Override
	public Set<String> getEmails() {
		return user.getEmail() != null
				? Collections.unmodifiableSet(Collections.singleton(user.getEmail()))
				: Collections.emptySet();
	}

	public String getDisplayName() {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            String email = user.getEmail();
            String toReturn = null;
            if (firstName == null && lastName == null) {
                toReturn = email;
            } else if (firstName == null) {
                toReturn = lastName;
            } else if (lastName == null) {
                toReturn = firstName;
            } else {
                toReturn = firstName + " " + lastName;
            }
            return toReturn;
	}

	@Override
	public Set<UserIdentity> getGroups() {
		return user.getGroups().stream().map(
				DefaultGroupIdentity::new).collect(Collectors.toSet());
	}


}
