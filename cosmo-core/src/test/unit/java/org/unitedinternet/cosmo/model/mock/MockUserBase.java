package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.UserBase;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class MockUserBase extends MockAuditableObject implements UserBase {



    public static final Pattern USERNAME_PATTERN = Pattern
            .compile("^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$");

        /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 32;
    /**
     */


    private static final String USERNAME_OVERLORD = "root";
    private String uid;

    private String username;

    private transient String oldUsername;

    private Boolean admin;

    private transient Boolean oldAdmin;

    private Set<Preference> preferences = new HashSet<Preference>(0);

    private Set<CollectionSubscription> subscriptions = new HashSet<>();



    public MockUserBase() {
        admin = Boolean.FALSE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getUid()
     */
    /**
     * Gets uid.
     *
     * @return The string.
     */
    @Override
    public final String getUid() {
        return uid;
    }

        /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setUid(java.lang.String)
     */
    /**
     * Sets uid.
     *
     * @param uid
     *            The id.
     */
    @Override
    public final void setUid(final String uid) {
        this.uid = uid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getUsername()
     */
    /**
     * Gets username.
     *
     * @return The username.
     */
    public final String getUsername() {
        return username;
    }

        /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setUsername(java.lang.String)
     */
    /**
     * Sets username.
     *
     * @param username
     *            The username.
     */
    @Override
    public final void setUsername(final String username) {
        oldUsername = this.username;
        this.username = username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getOldUsername()
     */
    /**
     * Gets old username.
     *
     * @return The old username.
     */
    @Override
    public final String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isUsernameChanged()
     */
    /**
     * Verify if the username is changed.
     *
     * @return The boolean if the username is changed.
     */
    @Override
    public final boolean isUsernameChanged() {
        return oldUsername != null && !oldUsername.equals(username);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isOverlord()
     */
    /**
     * Is overlord.
     *
     * @return The boolean for isOverlord.
     */
    public final boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }


        /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getAdmin()
     */
    /**
     * Gets admin.
     *
     * @return admin.
     */
    @Override
    public final Boolean getAdmin() {
        return admin;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getOldAdmin()
     */
    /**
     * Gets old admin.
     *
     * @return The old admin.
     */
    @Override
    public final Boolean getOldAdmin() {
        return oldAdmin;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isAdminChanged()
     */
    /**
     * Verify if admin is changed.
     *
     * @return If admin is changed or not.
     */
    @Override
    public final boolean isAdminChanged() {
        return oldAdmin != null && !oldAdmin.equals(admin);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setAdmin(java.lang.Boolean)
     */
    /**
     * Sets admin.
     *
     * @param admin
     *            The admin.
     */
    @Override
    public final void setAdmin(final Boolean admin) {
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

        /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateUsername()
     */
    /**
     * Validate username.
     */
    public final void validateUsername() {
        if (username == null) {
            throw new ModelValidationException(this, "Username not specified");
        }
        if (username.length() < USERNAME_LEN_MIN || username.length() > USERNAME_LEN_MAX) {
            throw new ModelValidationException(this,
                    "Username must be " + USERNAME_LEN_MIN + " to " + USERNAME_LEN_MAX + " characters in length");
        }
        Matcher m = USERNAME_PATTERN.matcher(username);
        if (!m.matches()) {
            throw new ModelValidationException(this, "Username contains illegal " + "characters");
        }
    }

        /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getPreferences()
     */
    /**
     * Gets preferences.
     *
     * @return preferences.
     */
    @Override
    public final Set<Preference> getPreferences() {
        return preferences;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.unitedinternet.cosmo.model.copy.InterfaceUser#addPreference(org.unitedinternet.cosmo.model.copy.Preference)
     */
    /**
     * Adds preference.
     *
     * @param preference
     *            The preference.
     */
    @Override
    public final void addPreference(final Preference preference) {
        preference.setUser(this);
        preferences.add(preference);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getPreference(java.lang.String)
     */
    /**
     * Gets preference.
     *
     * @param key
     *            The key.
     * @return The preference.
     */
    @Override
    public final Preference getPreference(final String key) {
        for (Preference pref : preferences) {
            if (pref.getKey().equals(key)) {
                return pref;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#removePreference(java.lang.String)
     */
    /**
     * Removes preference.
     *
     * @param key
     *            The key.
     */
    @Override
    public final void removePreference(final String key) {
        removePreference(getPreference(key));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#removePreference(org.unitedinternet.cosmo.model.copy.
     * Preference)
     */
    /**
     * Removes preference.
     *
     * @param preference
     *            The preference.
     */
    @Override
    public final void removePreference(final Preference preference) {
        if (preference != null) {
            preferences.remove(preference);
        }
    }

    @Override
    public Set<CollectionSubscription> getSubscriptions() {
        return this.subscriptions;
    }

}
