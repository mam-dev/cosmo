/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model;

import java.util.Set;

/**
 * Represents a user in the cosmo server.
 */
public interface User extends AuditableObject{

    /**
     */
    public static final String USERNAME_OVERLORD = "root";
    
    // Sort Strings
    /**
     * A String indicating the results should be sorted by Last Name then First Name
     */
    public static final String NAME_SORT_STRING = "Name";
    /**
     * A String indicating the results should be sorted by Username
     */
    public static final String USERNAME_SORT_STRING = "Username";
    /**
     * A String indicating the results should be sorted by Administrator
     */
    public static final String ADMIN_SORT_STRING = "Administrator";
    /**
     * A String indicating the results should be sorted by Email
     */
    public static final String EMAIL_SORT_STRING = "Email";
    /**
     * A String indicating the results should be sorted by Date Created
     */
    public static final String CREATED_SORT_STRING = "Created";
    /**
     * A String indicating the results should be sorted by Date last Modified
     */
    public static final String LAST_MODIFIED_SORT_STRING = "Last Modified";
    /**
     * A String indicating the results should be sorted by Activated status
     */
    public static final String ACTIVATED_SORT_STRING = "Activated";
    /**
     * A String indicating the results should be sorted by Locked status
     */
    public static final String LOCKED_SORT_STRING = "Locked";
    

    /**
     * The Default Sort Type
     */
    public static final String DEFAULT_SORT_STRING = NAME_SORT_STRING;
    
    public static final String NAME_URL_STRING = "name";
    public static final String USERNAME_URL_STRING = "username";
    public static final String ADMIN_URL_STRING = "admin";
    public static final String EMAIL_URL_STRING = "email";
    public static final String CREATED_URL_STRING = "created";
    public static final String LAST_MODIFIED_URL_STRING = "modified";
    public static final String ACTIVATED_URL_STRING = "activated";
    public static final String LOCKED_URL_STRING = "locked";
    
    /*
     * I'm not sure about putting this enum here, but it seems weird in other
     * places too. Since sort information is already here, in the *_SORT_STRING
     * constants, I think this is appropriate.
     */
    public enum SortType {
        NAME (NAME_URL_STRING, NAME_SORT_STRING),
        USERNAME (USERNAME_URL_STRING, USERNAME_SORT_STRING),
        ADMIN (ADMIN_URL_STRING, ADMIN_SORT_STRING),
        EMAIL (EMAIL_URL_STRING, EMAIL_SORT_STRING),
        CREATED (CREATED_URL_STRING, CREATED_SORT_STRING),
        LAST_MODIFIED (LAST_MODIFIED_URL_STRING, LAST_MODIFIED_SORT_STRING),
        ACTIVATED (ACTIVATED_URL_STRING, ACTIVATED_SORT_STRING),
        LOCKED (LOCKED_URL_STRING, LOCKED_SORT_STRING);

        private final String urlString;
        private final String titleString;

        SortType(String urlString, String titleString){
            this.urlString = urlString;
            this.titleString = titleString;
        }

        public String getTitleString() {
            return titleString;
        }

        public String getUrlString() {
            return urlString;
        }

        public static SortType getByUrlString(String string) {
            if (string.equals(NAME_URL_STRING)){
                return NAME;
            } else if (string.equals(USERNAME_URL_STRING)){
                return USERNAME;
            } else if (string.equals(ADMIN_URL_STRING)){
                return ADMIN;
            } else if (string.equals(EMAIL_URL_STRING)){
                return EMAIL;
            } else if (string.equals(CREATED_URL_STRING)){
                return CREATED;
            } else if (string.equals(LAST_MODIFIED_URL_STRING)){
                return LAST_MODIFIED;
            } else if (string.equals(ACTIVATED_URL_STRING)){
                return ACTIVATED;
            } else if (string.equals(LOCKED_URL_STRING)){
                return LOCKED;
            } else {
                return null;
            }
        }
    }
    
    /**
     */
    public String getUid();

    /**
     * @param uid
     */
    public void setUid(String uid);

    /**
     */
    public String getUsername();

    /**
     */
    public void setUsername(String username);

    /**
     */
    public String getOldUsername();

    /**
     */
    public boolean isUsernameChanged();

    /**
     */
    public String getPassword();

    /**
     */
    public void setPassword(String password);

    /**
     */
    public String getFirstName();

    /**
     */
    public void setFirstName(String firstName);

    /**
     */
    public String getLastName();

    /**
     */
    public void setLastName(String lastName);

    /**
     */
    public String getEmail();

    /**
     */
    public void setEmail(String email);

    /**
     */
    public String getOldEmail();

    /**
     */
    public boolean isEmailChanged();

    /**
     */
    public Boolean getAdmin();

    public Boolean getOldAdmin();

    /**
     */
    public boolean isAdminChanged();

    /**
     */
    public void setAdmin(Boolean admin);

    /**
     */
    public String getActivationId();

    /**
     */
    public void setActivationId(String activationId);

    /**
     */
    public boolean isOverlord();

    /**
     */
    public boolean isActivated();

    /**
     *
     *
     */
    public void activate();

    public Boolean isLocked();

    public void setLocked(Boolean locked);

    public Set<Preference> getPreferences();

    public void addPreference(Preference preference);

    public Preference getPreference(String key);

    public void removePreference(String key);

    public void removePreference(Preference preference);    

    public String calculateEntityTag();
    
    public Set<CollectionSubscription> getSubscriptions();

}