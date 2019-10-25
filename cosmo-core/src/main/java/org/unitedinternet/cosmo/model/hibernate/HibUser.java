/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.model.hibernate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;

/**
 * Hibernate persistent User.
 */
@Table(name = "users", indexes = { @Index(name = "idx_activationid", columnList = "activationId") })
@DiscriminatorValue("user")
public class HibUser extends HibUserBase implements User {

    /**
     */
    private static final long serialVersionUID = -5401963358519490736L;



    /**
     */
    public static final int FIRSTNAME_LEN_MIN = 1;
    /**
     */
    public static final int FIRSTNAME_LEN_MAX = 128;
    /**
     */
    public static final int LASTNAME_LEN_MIN = 1;
    /**
     */
    public static final int LASTNAME_LEN_MAX = 128;
    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;



    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "firstname")
    @Length(min = FIRSTNAME_LEN_MIN, max = FIRSTNAME_LEN_MAX)
    private String firstName;

    @Column(name = "lastname")
    @Length(min = LASTNAME_LEN_MIN, max = LASTNAME_LEN_MAX)
    private String lastName;

    @Column(name = "email", nullable = true, unique = true)
    @Length(min = EMAIL_LEN_MIN, max = EMAIL_LEN_MAX)
    @Email
    private String email;

    private transient String oldEmail;

    @Column(name = "activationid", nullable = true, length = 255)
    @Length(min = 1, max = 255)
    private String activationId;

    @Column(name = "locked")
    private Boolean locked;

    @ManyToMany(mappedBy = "groups", targetEntity = HibGroup.class)
    private Set<Group> groups = new HashSet<>();

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        oldEmail = this.email;
        this.email = email;
    }

    @Override
    public String getOldEmail() {
        return oldEmail;
    }

    @Override
    public boolean isEmailChanged() {
        return oldEmail != null && !oldEmail.equals(email);
    }


    @Override
    public String getActivationId() {
        return activationId;
    }

    @Override
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    @Override
    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public void addGroup(Group group) {
        groups.add(group);
        group.getUsers().add(this);
    }

    @Override
    public void removeGroup(Group group) {
        groups.remove(group);
        group.getUsers().remove(this);
    }

    @Override
    public Boolean isLocked() {
        return null;
    }

    @Override
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isActivated() {
        return this.activationId == null;
    }

    public void activate() {
        this.activationId = null;
    }

    public HibUser() {
        super();
        locked = Boolean.FALSE;
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).append("username", getUsername()).append("password", "xxxxxx")
                .append("firstName", firstName).append("lastName", lastName).append("email", email)
                .append("admin", getAdmin()).append("activationId", activationId).append("locked", locked).append("uid", getUid())
                .toString();
    }

    @Override
    public void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException("UserName" + this.getUsername() + " UID" + this.getUid(),
                    "Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN || password.length() > PASSWORD_LEN_MAX) {

            throw new ModelValidationException("UserName" + this.getUsername() + " UID" + this.getUid(),
                    "Password must be " + PASSWORD_LEN_MIN + " to " + PASSWORD_LEN_MAX + " characters in length");
        }
    }

    @Override
    public String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = "user:" + username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(StandardCharsets.UTF_8));
    }
}
