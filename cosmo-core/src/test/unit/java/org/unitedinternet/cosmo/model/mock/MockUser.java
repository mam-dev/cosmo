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

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;

/**
 */
public class MockUser extends MockUserBase implements User {


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


    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private transient String oldEmail;

    private String activationId;

    private Boolean locked;

    private Set<Group> groups = new HashSet<>();
    /**
     * Constructor.
     */
    public MockUser() {
        super();
        locked = Boolean.FALSE;
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
     * ToString. {@inheritDoc}
     * 
     * @return The string.
     */
    public final String toString() {
        return new ToStringBuilder(this).append("username", getUsername()).append("password", "xxxxxx")
                .append("firstName", firstName).append("lastName", lastName).append("email", email)
                .append("admin", getAdmin()).append("activationId", activationId).append("locked", locked).toString();
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceUser#validateRawPassword()
     */
    /**
     * Validate raw password.
     */
    public final void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException(this, "Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN || password.length() > PASSWORD_LEN_MAX) {
            throw new ModelValidationException(this,
                    "Password must be " + PASSWORD_LEN_MIN + " to " + PASSWORD_LEN_MAX + " characters in length");
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


    /**
     * Calculates entity tag.
     * 
     * @return The entity tag
     */
    public final String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = "user:" + username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isMemberOf(Group group) {
        return group.getUsers().contains(this);
    }
}
