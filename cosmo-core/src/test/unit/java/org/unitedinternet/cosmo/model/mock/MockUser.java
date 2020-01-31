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
package org.unitedinternet.cosmo.model.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.User;

/**
 */
public class MockUser extends MockAuditableObject implements User {

    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 32;
    /**
     */
    public static final Pattern USERNAME_PATTERN = Pattern
            .compile("^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$");

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

    private String uid;

    private String username;

    private transient String oldUsername;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private transient String oldEmail;

    private String activationId;

    private Boolean admin;

    private transient Boolean oldAdmin;

    private Boolean locked;

    private Set<Preference> preferences = new HashSet<Preference>(0);
    
    private Set<CollectionSubscription> subscriptions = new HashSet<>();

    /**
     * Constructor.
     */
    public MockUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
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
    public final boolean isUsernameChanged() {
        return oldUsername != null && !oldUsername.equals(username);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getPassword()
     */
    /**
     * Gets password.
     * 
     * @return The password.
     */
    public final String getPassword() {
        return password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setPassword(java.lang.String)
     */
    /**
     * Sets password.
     * 
     * @param password
     *            The password.
     */
    public final void setPassword(final String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getFirstName()
     */
    /**
     * Gets first name.
     * 
     * @return The first name.
     */
    public final String getFirstName() {
        return firstName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setFirstName(java.lang.String)
     */
    /**
     * Sets first name.
     * 
     * @param firstName
     *            The first name.
     */
    public final void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getLastName()
     */
    /**
     * Gets last name.
     * 
     * @return The last name.
     */
    public final String getLastName() {
        return lastName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setLastName(java.lang.String)
     */
    /**
     * Sets last name.
     * 
     * @param lastName
     *            The last name.
     */
    public final void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getEmail()
     */
    /**
     * Gets email.
     * 
     * @return The email.
     */
    public final String getEmail() {
        return email;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setEmail(java.lang.String)
     */
    /**
     * Sets email.
     * 
     * @param email
     *            The email.
     */
    public final void setEmail(final String email) {
        oldEmail = this.email;
        this.email = email;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getOldEmail()
     */
    /**
     * Gets old email.
     * 
     * @return The email.
     */
    public final String getOldEmail() {
        return oldEmail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isEmailChanged()
     */
    /**
     * Is email changed.
     * 
     * @return If email is changed.
     */
    public final boolean isEmailChanged() {
        return oldEmail != null && !oldEmail.equals(email);
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
    public final void setAdmin(final Boolean admin) {
        oldAdmin = this.admin;
        this.admin = admin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#getActivationId()
     */
    /**
     * Gets activation id.
     * 
     * @return The activation id.
     */
    public final String getActivationId() {
        return activationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setActivationId(java.lang.String)
     */
    /**
     * Sets activation id.
     * 
     * @param activationId
     *            The activation id.
     */
    public final void setActivationId(final String activationId) {
        this.activationId = activationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isOverlord()
     */
    /**
     * Is overload.
     * 
     * @return The boolean for is overload.
     */
    public final boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isActivated()
     */
    /**
     * Verify if it is activated.
     * 
     * @return The boolean for is activated.
     */
    public final boolean isActivated() {
        return this.activationId == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#activate()
     */
    /**
     * Activate.
     */
    public final void activate() {
        this.activationId = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#isLocked()
     */
    /**
     * Is locked.
     * 
     * @return The boolean for it is locked or not.
     */
    public final Boolean isLocked() {
        return locked;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#setLocked(java.lang.Boolean)
     */
    /**
     * Sets locked.
     * 
     * @param locked
     *            The locked.
     */
    public final void setLocked(final Boolean locked) {
        this.locked = locked;
    }

    /**
     * Username determines equality
     * 
     * @param obj
     *            The object.
     * @return The equals boolean.
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null || username == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }

        return username.equals(((User) obj).getUsername());
    }

    /**
     * Hashcode. {@inheritDoc}
     * 
     * @return The hashCode.
     */
    @Override
    public final int hashCode() {
        if (username == null) {
            return super.hashCode();
        } else {
            return username.hashCode();
        }
    }

    /**
     * ToString. {@inheritDoc}
     * 
     * @return The string.
     */
    public final String toString() {
        return new ToStringBuilder(this).append("username", username).append("password", "xxxxxx")
                .append("firstName", firstName).append("lastName", lastName).append("email", email)
                .append("admin", admin).append("activationId", activationId).append("locked", locked).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validate()
     */
    /**
     * Validate.
     */
    public final void validate() {
        validateUsername();
        validateFirstName();
        validateLastName();
        validateEmail();
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateFirstName()
     */
    /**
     * Validate first name.
     */
    public final void validateFirstName() {
        if (firstName == null) {
            throw new ModelValidationException(this, "First name is null");
        }
        if (firstName.length() < FIRSTNAME_LEN_MIN || firstName.length() > FIRSTNAME_LEN_MAX) {
            throw new ModelValidationException(this,
                    "First name must be " + FIRSTNAME_LEN_MIN + " to " + FIRSTNAME_LEN_MAX + " characters in length");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateLastName()
     */
    /**
     * Validate last name.
     */
    public final void validateLastName() {
        if (lastName == null) {
            throw new ModelValidationException(this, "Last name is null");
        }
        if (lastName.length() < LASTNAME_LEN_MIN || lastName.length() > LASTNAME_LEN_MAX) {
            throw new ModelValidationException(this,
                    "Last name must be " + LASTNAME_LEN_MIN + " to " + LASTNAME_LEN_MAX + " characters in length");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateEmail()
     */
    /**
     * Validate email.
     */
    public final void validateEmail() {
        if (email == null) {
            throw new ModelValidationException(this, "Email is null");
        }
        if (email.length() < EMAIL_LEN_MIN || email.length() > EMAIL_LEN_MAX) {
            throw new ModelValidationException(this,
                    "Email must be " + EMAIL_LEN_MIN + " to " + EMAIL_LEN_MAX + " characters in length");
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
    public final void removePreference(final Preference preference) {
        if (preference != null) {
            preferences.remove(preference);
        }
    }
    
    @Override
    public Set<CollectionSubscription> getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * Calculates entity tag.
     * 
     * @return The entity tag
     */
    public final String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
