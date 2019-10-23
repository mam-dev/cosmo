package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("group")
public class HibGroup extends HibUserBase implements Group {


    public static final int DISPLAYNAME_LEN_MIN = 1;

    public static final int DISPLAYNAME_LEN_MAX = 128;
    @ManyToMany(targetEntity = HibUser.class)
    @JoinTable(name="usergroupmap", joinColumns = {
            @JoinColumn(name ="fk_group") },
            inverseJoinColumns = {@JoinColumn(name ="fk_user")})
    private Set<User> users = new HashSet<>();

    @Column(name = "displayname")
    @Length(min = DISPLAYNAME_LEN_MIN, max=DISPLAYNAME_LEN_MAX)
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
    public String displayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    @Override
    public String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = "group:" + username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(StandardCharsets.UTF_8));
    }
}
