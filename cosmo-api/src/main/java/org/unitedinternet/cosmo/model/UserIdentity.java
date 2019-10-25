package org.unitedinternet.cosmo.model;

import java.util.Set;

public interface UserIdentity {

    Set<String> getEmails();

    String getDisplayName();

    Set<UserIdentity> getGroups();
}
