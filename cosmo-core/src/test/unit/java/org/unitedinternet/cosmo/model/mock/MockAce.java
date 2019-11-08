package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.Ace;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.UserBase;
import org.unitedinternet.cosmo.security.Permission;

import java.util.HashSet;
import java.util.Set;

public class MockAce implements Ace {

    private Item item;
    private Ace.Type type;

    private int order;

    private UserBase user;

    private Set<Permission> permissions = new HashSet<>();

    private boolean isDeny = false;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void setUser(UserBase user) {
        this.user = user;
        if (user != null) {
            this.type = Type.USER;
        }
    }


    @Override
    public UserBase getUser() {
        return user;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isDeny() {
        return isDeny;
    }

    @Override
    public void setIsDeny(boolean isDeny) {
        this.isDeny = isDeny;
    }


}
