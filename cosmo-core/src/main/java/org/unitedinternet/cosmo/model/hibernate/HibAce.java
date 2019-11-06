package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.Ace;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.UserBase;
import org.unitedinternet.cosmo.security.Permission;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="aces")
public class HibAce extends BaseModelObject implements Ace {
    @ManyToOne(targetEntity = HibUserBase.class, fetch = FetchType.LAZY)
    @JoinColumn(name="userid")
    private UserBase user;


    @ManyToOne(targetEntity=HibItem.class, fetch=FetchType.LAZY)
    @JoinColumn(name="itemid")
    private Item item;

    @ElementCollection
    @JoinTable(
            name="ace_permission",
            joinColumns = @JoinColumn(name="aceid") // ACE is referenced by id as aceid in ace_permission
    )
    @Column(name="permission", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();


    @Enumerated(EnumType.STRING)
    private Ace.Type type;

    @Column(name="isDeny")
    private boolean denied;

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public void setItem(Item item) {
        this.item = item;
        item.getAces().add(this);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
        switch (type) {
            case USER:
                break;
            default:
                setUser(null);
        }
    }

    @Override
    public void setUser(UserBase user) {
        this.user = user;
        if (user != null)
            this.type = Type.USER;
    }

    @Override
    public UserBase getUser() {
        return this.user;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isDeny() {
        return denied;
    }

    @Override
    public void setIsDeny(boolean isDeny) {
        denied = isDeny;
    }
}
