package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.UserBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "users")
@DiscriminatorColumn(name="usertype", discriminatorType = DiscriminatorType.STRING, length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class HibUserBase  extends HibAuditableObject implements UserBase {

        /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 64;

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

    @Column(name = "admin")
    private Boolean admin;

    private transient Boolean oldAdmin;


    @OneToMany(targetEntity = HibPreference.class, mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Preference> preferences = new HashSet<Preference>(0);

    @OneToMany(mappedBy = "owner", targetEntity = HibCollectionSubscription.class)
    private Set<CollectionSubscription> subscriptions = new HashSet<>();

    /**
     * Default constructor,
     */
    public HibUserBase() {
        admin = Boolean.FALSE;
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
    public boolean isOverlord() {
        return false;
    }

    @Override
    public Boolean getAdmin() {
        return oldAdmin;
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


        /**
     * Username determines equality
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || username == null) {
            return false;
        }
        if (!(obj instanceof UserBase)) {
            return false;
        }
        if (obj.getClass() != this.getClass())
            return false;

        return username.equals(((UserBase) obj).getUsername());
    }

    @Override
    public int hashCode() {
        if (username == null) {
            return super.hashCode();
        } else {
            return username.hashCode();
        }
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

}
