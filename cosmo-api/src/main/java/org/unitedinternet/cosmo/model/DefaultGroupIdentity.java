package org.unitedinternet.cosmo.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultGroupIdentity implements UserIdentity {
    private final Group group;

    @Override
    public Set<String> getEmails() { // We return all emails. Should we designate admins and return them only
        Set<String> toReturn = new HashSet<>();
        for (User u: group.getUsers()) {
            toReturn.add(u.getEmail());
        }
        return toReturn;
    }

    public String getDisplayName() {
        return group.getDisplayName();
    }

    @Override
    public Set<UserIdentity> getGroups() {
        return Collections.emptySet(); // TODO Implement groups inside groups.
    }

    public DefaultGroupIdentity(Group group) {
        this.group = group;
    }
}



