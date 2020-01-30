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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.User;

/**
 * Hibernate persistent User.
 */
@Entity
@Table(name = "users", indexes = { @Index(name = "idx_activationid", columnList = "activationId") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibUser extends HibAuditableObject implements User {

    /**
     */
    private static final long serialVersionUID = -5401963358519490736L;

    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 64;

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

    @Column(name = "uid", nullable = false, unique = true, length = 255)
    @NotNull
    @Length(min = 1, max = 255)
    private String uid;

    @Column(name = "username", nullable = false)
    @NotNull    
    @Length(min = USERNAME_LEN_MIN, max = USERNAME_LEN_MAX)
    @NaturalId
    /*
     * Per bug 11599: Usernames must be between 3 and 64 characters; may contain any Unicode character in the following
     * range of unicode code points: [#x20-#xD7FF] | [#xE000-#xFFFD] EXCEPT #x7F or #x3A. Oh and don't allow ';' or '/'
     * because there are problems with encoding them in urls (tomcat doesn't support it)
     */
    @javax.validation.constraints.Pattern(regexp = "^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$")    
    private String username;

    private transient String oldUsername;

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

    @Column(name = "admin")
    private Boolean admin;

    private transient Boolean oldAdmin;

    @Column(name = "locked")
    private Boolean locked;

    @OneToMany(targetEntity = HibPreference.class, mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Preference> preferences = new HashSet<Preference>(0);

    @OneToMany(mappedBy = "owner", targetEntity = HibCollectionSubscription.class)
    private Set<CollectionSubscription> subscriptions = new HashSet<>();

    /**
     * Default constructor,
     */
    public HibUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        oldUsername = this.username;
        this.username = username;
    }

    @Override
    public String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
    }

    @Override
    public boolean isUsernameChanged() {
        return oldUsername != null && !oldUsername.equals(username);
    }

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
    public Boolean getAdmin() {
        return admin;
    }

    @Override
    public Boolean getOldAdmin() {
        return oldAdmin;
    }

    @Override
    public boolean isAdminChanged() {
        return oldAdmin != null && !oldAdmin.equals(admin);
    }

    @Override
    public void setAdmin(Boolean admin) {
        oldAdmin = this.admin;
        this.admin = admin;
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
    public boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }

    @Override
    public boolean isActivated() {
        return this.activationId == null;
    }

    @Override
    public void activate() {
        this.activationId = null;
    }

    @Override
    public Boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * Username determines equality
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || username == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }

        return username.equals(((User) obj).getUsername());
    }

    @Override
    public int hashCode() {
        if (username == null) {
            return super.hashCode();
        } else {
            return username.hashCode();
        }
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).append("username", username).append("password", "xxxxxx")
                .append("firstName", firstName).append("lastName", lastName).append("email", email)
                .append("admin", admin).append("activationId", activationId).append("locked", locked).append("uid", uid)
                .toString();
    }

    @Override
    public Set<Preference> getPreferences() {
        return preferences;
    }

    @Override
    public void addPreference(Preference preference) {
        preference.setUser(this);
        preferences.add(preference);
    }

    @Override
    public Preference getPreference(String key) {
        for (Preference pref : preferences) {
            if (pref.getKey().equals(key)) {
                return pref;
            }
        }
        return null;
    }

    @Override
    public void removePreference(String key) {
        removePreference(getPreference(key));
    }

    @Override
    public void removePreference(Preference preference) {
        if (preference != null) {
            preferences.remove(preference);
        }
    }

    @Override
    public Set<CollectionSubscription> getSubscriptions() {
        return Collections.unmodifiableSet(this.subscriptions);
    }

    public String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }
}
