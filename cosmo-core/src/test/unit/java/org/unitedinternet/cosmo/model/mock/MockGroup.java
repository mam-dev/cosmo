package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MockGroup  extends MockUserBase implements Group {

    private Set<User> users = new HashSet<>();

    private String displayName;

    @Override
    public Set<User> getUsers() {
        return users;
    }

    @Override
    public void addUser(User user) {
        users.add(user);
        user.getGroups().add(this);
    }

    @Override
    public void removeUser(User user) {
        users.remove(user);
        user.getGroups().remove(this);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Calculates entity tag.
     *
     * @return The entity tag
     */

    @Override
    public final String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = "group:" + username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isMemberOf(Group group) {
        return false;
    }
}
