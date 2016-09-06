package org.unitedinternet.cosmo.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates user identification data which may be found in another system than the Calendar Server.
 * 
 * @see UserIdentitySupplier
 * @author corneliu dobrota
 *
 */
public class UserIdentity {
	
	private final Set<String> emails;
	private final String firstName;
	private final String lastName;
	
	private UserIdentity(Set<String> emails, String firstName, String lastName){
		this.emails = emails == null ? Collections.<String>emptySet() : new HashSet<String>(emails);
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Set<String> getEmails() {
		return Collections.unmodifiableSet(emails);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public static UserIdentity of(Set<String> emails, String firstName, String lastName){
		return new UserIdentity(emails, firstName, lastName);
	}
}