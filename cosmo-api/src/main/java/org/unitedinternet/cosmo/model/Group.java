package org.unitedinternet.cosmo.model;

import java.util.Set;

public interface Group extends UserBase  {
    public Set<User> getUsers();

    public void addUser(User user);

    public void removeUser (User user);

    public String getDisplayName();

    public void setDisplayName(String displayName);



}
